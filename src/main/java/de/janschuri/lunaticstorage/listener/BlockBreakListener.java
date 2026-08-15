package de.janschuri.lunaticstorage.listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticlib.platform.paper.utils.ItemStackUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.gui.ContainerGUI;
import de.janschuri.lunaticstorage.gui.ContainerListGUI;
import de.janschuri.lunaticstorage.gui.StorageGUI;
import de.janschuri.lunaticstorage.storage.Key;
import de.janschuri.lunaticstorage.storage.Storage;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static de.janschuri.lunaticstorage.config.LanguageConfig.getShutdownMessage;

public class BlockBreakListener implements Listener {

    private static final long DROP_DIFF_TTL_TICKS = 20L * 60L;
    private static final Map<Event, List<Item>> dropEvents = new HashMap<>();
    private static final Map<Block, PendingDropDiff> dropDiffs = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();

        boolean isPanel = Utils.isPanel(block);
        boolean isStorageContainer = Utils.isStorageContainer(block);

        if (isPanel || isStorageContainer) {
            StorageGUI.closeAllAtBlock(block);
            ContainerListGUI.destroyAllAtLocation(block.getLocation());
            ContainerGUI.closeAllAtBlock(block);

            if (LunaticStorage.getPluginConfig().isShutdown()) {
                event.setCancelled(true);
                player.sendMessage(getShutdownMessage());
                return;
            }

            if (!player.isSneaking()) {
                event.setCancelled(true);
            } else if (isStorageContainer) {
                StorageContainer storageContainer = StorageContainer.getStorageContainer(block);
                Inventory inventory = storageContainer.getInventory();

                if (inventory == null) {
                    Logger.error("Inventory is null");
                    return;
                }

                Map<ItemStack, Integer> difference = Utils.itemStackArrayToMap(inventory.getContents(), true);
                cacheDropDiff(block, difference);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDrop(BlockDropItemEvent event) {
        Block block = event.getBlock();
        boolean isPanel = Utils.isPanel(block);
        boolean isStorageContainer = Utils.isStorageContainer(block);
        if (isPanel || isStorageContainer) {
            PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());

            dataContainer.remove(Key.STORAGE_ITEM);
            dataContainer.remove(Key.PANEL_BLOCK);
            dataContainer.remove(Key.PANEL_RANGE);
            dataContainer.remove(Key.RANGE_ITEM);

            dataContainer.remove(Key.STORAGE_CONTAINER);
            dataContainer.remove(Key.WHITELIST);
            dataContainer.remove(Key.BLACKLIST);
            dataContainer.remove(Key.WHITELIST_ENABLED);
            dataContainer.remove(Key.BLACKLIST_ENABLED);
        }

        if (dropEvents.containsKey(event)) {

            List<Item> oldItems = dropEvents.get(event);
            List<Item> newItems = event.getItems();

            for (Item item : oldItems) {
                if (!newItems.contains(item)) {
                    item.remove();
                }
            }

            dropEvents.remove(event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPanelBlockDrop(BlockDropItemEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (Utils.isPanel(block)) {

            Storage.removeStorage(block);

            PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
            ItemStack blockItem;
            boolean addBlockItem = false;

            if (event.getItems().isEmpty()) {
                addBlockItem = true;
                blockItem = new ItemStack(event.getBlockState().getType());
            } else {
                blockItem = event.getItems().get(0).getItemStack();
            }


            ItemMeta blockItemMeta = blockItem.getItemMeta();
            PersistentDataContainer blockItemDataContainer = blockItemMeta.getPersistentDataContainer();
            blockItemDataContainer.set(Key.PANEL_BLOCK, PersistentDataType.INTEGER, 1);
            blockItemDataContainer.set(Key.PANEL_RANGE, PersistentDataType.LONG, dataContainer.get(Key.PANEL_RANGE, PersistentDataType.LONG));
            blockItem.setItemMeta(blockItemMeta);


            List<Item> newItems = new ArrayList<>();

            if (addBlockItem) {
                    Item item = (Item) player.getWorld().spawnEntity(block.getLocation(), EntityType.DROPPED_ITEM);
                    item.setItemStack(blockItem);
                    newItems.add(item);
            }

            if (dataContainer.has(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                byte[] bytes = dataContainer.get(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY);
                ItemStack itemStack = ItemStackUtils.deserializeItemStack(bytes);
                    Item item = (Item) player.getWorld().spawnEntity(block.getLocation(), EntityType.DROPPED_ITEM);
                    item.setItemStack(itemStack);
                    newItems.add(item);
            }

            if (dataContainer.has(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                byte[] bytes = dataContainer.get(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY);
                ItemStack itemStack = ItemStackUtils.deserializeItemStack(bytes);
                    Item item = (Item) player.getWorld().spawnEntity(block.getLocation(), EntityType.DROPPED_ITEM);
                    item.setItemStack(itemStack);
                    newItems.add(item);
            }



            event.getItems().addAll(newItems);

            dropEvents.put(event, newItems);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onStorageContainerBlockDrop(BlockDropItemEvent event) {
        Block block = event.getBlock();
        PendingDropDiff pendingDropDiff = removeDropDiff(block);
        boolean isStorageContainer = Utils.isStorageContainer(block);
        if (isStorageContainer) {
            StorageContainer storageContainer = StorageContainer.getStorageContainer(block);

            Map<ItemStack, Integer> diff = pendingDropDiff != null ? pendingDropDiff.difference() : Collections.emptyMap();
            storageContainer.updateStorages(diff);
        }
    }

    public static void clearDropDiffs() {
        for (PendingDropDiff pendingDropDiff : dropDiffs.values()) {
            pendingDropDiff.cleanupTask().cancel();
        }

        dropDiffs.clear();
    }

    private static void cacheDropDiff(Block block, Map<ItemStack, Integer> difference) {
        removeDropDiff(block);
        PendingDropDiff[] pendingHolder = new PendingDropDiff[1];
        BukkitTask cleanupTask = LunaticStorage.getInstance().getServer().getScheduler().runTaskLater(LunaticStorage.getInstance(), () -> {
            PendingDropDiff currentDropDiff = dropDiffs.get(block);
            if (currentDropDiff == pendingHolder[0]) {
                dropDiffs.remove(block);
                long ageSeconds = (System.currentTimeMillis() - currentDropDiff.createdAt()) / 1000L;
                Logger.warn("Cleaned up stale drop diff for storage container at " + block.getLocation() + " after " + ageSeconds + " seconds.");
            }
        }, DROP_DIFF_TTL_TICKS);

        PendingDropDiff pendingDropDiff = new PendingDropDiff(difference, cleanupTask, System.currentTimeMillis());
        pendingHolder[0] = pendingDropDiff;
        dropDiffs.put(block, pendingDropDiff);
    }

    private static PendingDropDiff removeDropDiff(Block block) {
        PendingDropDiff pendingDropDiff = dropDiffs.remove(block);
        if (pendingDropDiff != null) {
            pendingDropDiff.cleanupTask().cancel();
        }

        return pendingDropDiff;
    }

    private record PendingDropDiff(Map<ItemStack, Integer> difference, BukkitTask cleanupTask, long createdAt) {
    }
}
