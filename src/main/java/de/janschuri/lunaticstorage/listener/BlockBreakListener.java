package de.janschuri.lunaticstorage.listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.Key;
import de.janschuri.lunaticstorage.storage.Storage;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class BlockBreakListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (Utils.isPanel(block) || LunaticStorage.isDebug()) {
            Logger.debugLog("Panel block broken");
            if (!player.isSneaking()) {
                Logger.debugLog("Player is not sneaking");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockDrop(BlockDropItemEvent event) {
        List<Item> items = event.getItems();

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

            if (addBlockItem) {
                    Item item = (Item) player.getWorld().spawnEntity(block.getLocation(), EntityType.DROPPED_ITEM);
                    item.setItemStack(blockItem);
                    items.add(item);
            }

            if (dataContainer.has(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                byte[] bytes = dataContainer.get(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY);
                ItemStack itemStack = ItemStackUtils.deserializeItemStack(bytes);
                    Item item = (Item) player.getWorld().spawnEntity(block.getLocation(), EntityType.DROPPED_ITEM);
                    item.setItemStack(itemStack);
                    items.add(item);
            }

            if (dataContainer.has(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                byte[] bytes = dataContainer.get(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY);
                ItemStack itemStack = ItemStackUtils.deserializeItemStack(bytes);
                    Item item = (Item) player.getWorld().spawnEntity(block.getLocation(), EntityType.DROPPED_ITEM);
                    item.setItemStack(itemStack);
                    items.add(item);
            }

            dataContainer.remove(Key.STORAGE_ITEM);
            dataContainer.remove(Key.PANEL_BLOCK);
            dataContainer.remove(Key.PANEL_RANGE);
            dataContainer.remove(Key.RANGE_ITEM);
        }

        if (Utils.isContainer(block)) {
            PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());

            dataContainer.remove(Key.STORAGE_CONTAINER);
            dataContainer.remove(Key.WHITELIST);
            dataContainer.remove(Key.BLACKLIST);
            dataContainer.remove(Key.WHITELIST_ENABLED);
            dataContainer.remove(Key.BLACKLIST_ENABLED);

            StorageContainer storageContainer = StorageContainer.getStorageContainer(block);
            storageContainer.unload();
        }
    }
}

