package de.janschuri.lunaticstorage.listener;

import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();
        String coords = Utils.getCoordsAsString(block);
        String worldName = block.getWorld().getName();

//        if (PanelsTable.isPanelInDatabase(worldName, coords)) {
//            if (player.isSneaking()) {
//                PanelsTable.removePanel(worldName, coords);
//            } else {
//                event.setCancelled(true);
//            }
//            return;
//        }
//
//        if (ChestsTable.isChestInDatabase(worldName, coords)) {
//            if (player.isSneaking()) {
//               ChestsTable.removeChest(worldName, coords);
//            } else {
//                event.setCancelled(true);
//            }
//            return;
//        }
    }
}

