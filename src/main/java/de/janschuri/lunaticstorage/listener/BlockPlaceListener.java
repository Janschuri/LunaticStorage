package de.janschuri.lunaticstorage.listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.Key;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockPlaceListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        if (event.getBlockPlaced().getType().equals(LunaticStorage.getPluginConfig().getStoragePanelBlock())) {
            ItemStack item = event.getItemInHand();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

            if (dataContainer.has(Key.PANEL_BLOCK, PersistentDataType.BOOLEAN)) {
                Block block = event.getBlockPlaced();
                PersistentDataContainer blockDataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
                blockDataContainer.set(Key.PANEL_BLOCK, PersistentDataType.BOOLEAN, true);
            }
        }
    }
}
