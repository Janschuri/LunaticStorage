package de.janschuri.lunaticStorages.listener;

import de.janschuri.lunaticStorages.Keys;
import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.config.PluginConfig;
import de.janschuri.lunaticStorages.database.tables.ChestsTable;
import de.janschuri.lunaticStorages.database.tables.PanelsTable;
import de.janschuri.lunaticStorages.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockListener implements Listener {

    private final LunaticStorage plugin;

    public BlockListener(LunaticStorage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        if (event.getBlockPlaced().getType().equals(PluginConfig.getStoragePanelBlock())) {
            ItemStack item = event.getItemInHand();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

            if (dataContainer.has(Keys.PANEL_BLOCK, PersistentDataType.BOOLEAN)) {
                event.getPlayer().sendMessage("Test");
                Block block = event.getBlockPlaced();


                String coords = Utils.getCoordsAsString(block);
                String worldName = block.getWorld().getName();

                PanelsTable.savePanelsData(worldName, coords, null);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();
        String coords = Utils.getCoordsAsString(block);
        String worldName = block.getWorld().getName();

        if (PanelsTable.isPanelInDatabase(worldName, coords)) {
            if (player.isSneaking()) {
                PanelsTable.removePanel(worldName, coords);
            } else {
                event.setCancelled(true);
            }
        }

        if (ChestsTable.isChestInDatabase(worldName, coords)) {
            if (player.isSneaking()) {
               ChestsTable.removeChest(worldName, coords);
            } else {
                event.setCancelled(true);
            }
        }
    }
}

