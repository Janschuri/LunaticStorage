package de.janschuri.lunaticstorage.listener;

import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class ContainerEditListener implements Listener {



    @EventHandler
    public void onPlayerInteract(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
//        Logger.debugLog("HOLDER: " + holder);

//        Logger.debugLog("Player interacted with inventory");

        if (event.getInventory().getHolder() instanceof Block block) {
//            Logger.debugLog("Block is a container");
            if (Utils.isStorageContainer(block)) {
                if (StorageContainer.isLoaded(block)) {
//                    Logger.debugLog("Updating storage container contents");
                    StorageContainer storageContainer = StorageContainer.getStorageContainer(block);
                    storageContainer.updateContents();
                }
            }
        }
    }

}
