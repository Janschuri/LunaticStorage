package de.janschuri.lunaticstorage.listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.gui.StorageGUI;
import de.janschuri.lunaticstorage.storage.Key;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.GUIManager;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PanelClickListener implements Listener {

    private static final Set<Player> timeoutPlayers = new HashSet<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (isTimeOut(event.getPlayer())) {
            return;
        }

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (Utils.isPanel(block)) {
            event.setCancelled(true);
            GUIManager.openGUI(new StorageGUI(player, block), player);
        }
    }

    private boolean isTimeOut(Player player) {
        boolean isTimeOut = timeoutPlayers.contains(player);

        timeoutPlayers.add(player);

        Utils.scheduleTask(() -> {
            timeoutPlayers.remove(player);
        }, 50, TimeUnit.MILLISECONDS);

        return isTimeOut;
    }
}
