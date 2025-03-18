package de.janschuri.lunaticstorage.listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockBreakListener implements Listener {

    private static final Map<Event, List<Item>> dropEvents = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if ((Utils.isPanel(block) || Utils.isStorageContainer(block)) && LunaticStorage.isDebug()) {
            if (!player.isSneaking()) {
                event.setCancelled(true);
            }

            if (Utils.isStorageContainer(block)) {
                StorageContainer storageContainer = StorageContainer.getStorageContainer(block);

                Map<ItemStack, Integer> difference = Utils.itemStackArrayToMap(storageContainer.getInventory().getContents(), true);

                storageContainer.clearWhitelist();
                storageContainer.clearBlacklist();
                storageContainer.updateStorages(difference);
                storageContainer.unload();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDropMonitor(BlockDropItemEvent event) {
        if (dropEvents.containsKey(event)) {

            List<Item> oldItems = dropEvents.get(event);
            List<Item> newItems = event.getItems();

            for (Item item : oldItems) {
                if (!newItems.contains(item)) {
                    item.remove();
                }
            }

            dropEvents.remove(event);
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockDrop(BlockDropItemEvent event) {

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

            dataContainer.remove(Key.STORAGE_ITEM);
            dataContainer.remove(Key.PANEL_BLOCK);
            dataContainer.remove(Key.PANEL_RANGE);
            dataContainer.remove(Key.RANGE_ITEM);

            event.getItems().addAll(newItems);

            dropEvents.put(event, newItems);
        }

        if (Utils.isStorageContainer(block)) {
            PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());

            dataContainer.remove(Key.STORAGE_CONTAINER);
            dataContainer.remove(Key.WHITELIST);
            dataContainer.remove(Key.BLACKLIST);
            dataContainer.remove(Key.WHITELIST_ENABLED);
            dataContainer.remove(Key.BLACKLIST_ENABLED);
        }
    }
}

