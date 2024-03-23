package de.janschuri.lunaticStorages.listener;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.StoragePanelGUI;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PanelClickListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        String coords = "";
        if (block != null) {
            coords = LunaticStorage.getCoordsAsString(block);
        }

        if (block != null && LunaticStorage.getDatabase().isPanelInDatabase(coords)) {
            event.setCancelled(true);
            int id = LunaticStorage.getDatabase().getPanelsID(coords);
            StoragePanelGUI.openGUI(player, id);
        }
    }
}
