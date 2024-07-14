package de.janschuri.lunaticstorage.listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.gui.StorageGUI;
import de.janschuri.lunaticstorage.storage.Key;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.GUIManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PanelClickListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getClickedBlock() == null) {
            return;
        }

        if (!(event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());

        if (dataContainer.has(Key.PANEL_BLOCK, PersistentDataType.BOOLEAN)) {
            event.setCancelled(true);
            GUIManager.openGUI(StorageGUI.getStorageGUI(player, block), player);
        }
    }
}
