package de.janschuri.lunaticstorage.listener;

import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class ContainerEditListener implements Listener {



    @EventHandler
    public void onPlayerInteract(InventoryClickEvent event) {
        event.getInventory().getHolder();

        if (event.getInventory().getHolder() instanceof Block block) {
            if (Utils.isContainer(block)) {
                if (StorageContainer.isLoaded(block)) {
                    Logger.debugLog("Updating storage container contents");
                    StorageContainer storageContainer = StorageContainer.getStorageContainer(block);
                    storageContainer.updateContents();
                }
            }
        }
    }

}
