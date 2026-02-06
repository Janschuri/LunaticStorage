package de.janschuri.lunaticstorage.listener;

import de.janschuri.lunaticlib.platform.paper.inventorygui.handler.GUIManager;
import de.janschuri.lunaticlib.platform.paper.utils.EventUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.gui.StorageGUI;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static de.janschuri.lunaticstorage.config.LanguageConfig.getShutdownMessage;

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
            if (LunaticStorage.getPluginConfig().isShutdown()) {
                event.setCancelled(true);
                player.sendMessage(getShutdownMessage());
                return;
            }

            event.setCancelled(true);
            GUIManager.openGUI(StorageGUI.getStorageGUI(player, block), player);
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
