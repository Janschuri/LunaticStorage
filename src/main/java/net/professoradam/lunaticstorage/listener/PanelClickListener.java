package net.professoradam.lunaticstorage.listener;

import com.jeff_media.customblockdata.CustomBlockData;
import net.professoradam.lunaticstorage.LunaticStorage;
import net.professoradam.lunaticstorage.gui.StorageGUI;
import net.professoradam.lunaticstorage.storage.Key;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.GUIManager;
import net.professoradam.lunaticstorage.utils.Logger;
import net.professoradam.lunaticstorage.utils.Utils;
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
