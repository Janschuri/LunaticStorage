package de.janschuri.lunaticstorage.listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.Key;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockDropItemEvent event) {
        List<Item> items = event.getItems();

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (Utils.isPanel(block)) {
            PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
            ItemStack blockItem = items.get(0).getItemStack();
            ItemMeta blockItemMeta = blockItem.getItemMeta();
            PersistentDataContainer blockItemDataContainer = blockItemMeta.getPersistentDataContainer();
            blockItemDataContainer.set(Key.PANEL_BLOCK, PersistentDataType.BOOLEAN, true);
            blockItem.setItemMeta(blockItemMeta);

            if (dataContainer.has(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                byte[] bytes = dataContainer.get(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY);
                ItemStack itemStack = ItemStackUtils.deserializeItemStack(bytes);
                Item item = (Item) player.getWorld().spawnEntity(block.getLocation(), EntityType.DROPPED_ITEM);
                item.setItemStack(itemStack);
                items.add(item);
            }

            dataContainer.remove(Key.STORAGE_ITEM);
            dataContainer.remove(Key.PANEL_BLOCK);
        }
    }
}
