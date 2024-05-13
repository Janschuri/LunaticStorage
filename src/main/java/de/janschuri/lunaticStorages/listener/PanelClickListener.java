package de.janschuri.lunaticStorages.listener;

import de.janschuri.lunaticStorages.storage.StoragePanelGUI;
import de.janschuri.lunaticStorages.database.tables.PanelsTable;
import de.janschuri.lunaticStorages.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PanelClickListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getClickedBlock() == null) {
            return;
        }

        if (!event.getAction().isRightClick()) {
            return;
        }

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        String coords = Utils.getCoordsAsString(block);
        String worldName = block.getWorld().getName();

        if (PanelsTable.isPanelInDatabase(worldName, coords)) {
            event.setCancelled(true);
            int id = PanelsTable.getPanelsID(worldName, coords);
            StoragePanelGUI.openGUI(player, id);
        }
    }
}
