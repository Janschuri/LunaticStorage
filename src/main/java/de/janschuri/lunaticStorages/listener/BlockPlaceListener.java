package de.janschuri.lunaticStorages.listener;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.storage.Key;
import de.janschuri.lunaticStorages.config.PluginConfig;
import de.janschuri.lunaticStorages.database.tables.PanelsTable;
import de.janschuri.lunaticStorages.utils.Utils;
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


                String coords = Utils.getCoordsAsString(block);
                String worldName = block.getWorld().getName();

                PanelsTable.savePanelsData(worldName, coords, null);
            }
        }
    }
}
