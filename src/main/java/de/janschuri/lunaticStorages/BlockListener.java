package de.janschuri.lunaticStorages;

import org.bukkit.Bukkit;
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

    private final Main plugin;

    public BlockListener(Main plugin) {
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();
        String coords = Main.getCoordsAsString(block);

        if (Main.getDatabase().isPanelInDatabase(coords)) {
            if (player.isSneaking()) {
                Main.getDatabase().removePanel(coords);
            } else {
                event.setCancelled(true);
            }
        }

        if (Main.getDatabase().isChestInDatabase(coords)) {
            if (player.isSneaking()) {
                Main.getDatabase().removeChest(coords);
            } else {
                event.setCancelled(true);
            }
        }
    }
}

