package de.janschuri.lunaticStorages;

import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class blockListener implements Listener {

    private final Main plugin;

    public blockListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        if (event.getBlockPlaced().getType().equals(plugin.panelBlock)) {
            ItemStack item = event.getItemInHand();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

            if (dataContainer.has(plugin.keyPanelBlock, PersistentDataType.BOOLEAN)) {
                event.getPlayer().sendMessage("Test");
                Block block = event.getBlockPlaced();


                String coords = Main.getCoordsAsString(block);

                Main.getDatabase().savePanelsData(coords, null);
            }
        }
    }

    public void onBlockBreak(BlockBreakEvent event) {

        if (event.getBlock().getType().equals(plugin.panelBlock)) {
            event.setCancelled(true);
        }

        String coords = Main.getCoordsAsString(event.getBlock());

        if (Main.getDatabase().isChestInDatabase(coords)) {
            event.setCancelled(true);
        }
    }
}

