package de.janschuri.lunaticstorage.listener;

import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class ContainerEditListener implements Listener {



    @EventHandler
    public void onPlayerInteract(InventoryOpenEvent event) {
        Location loc = event.getInventory().getLocation();

        if (loc == null) {
            return;
        }

        Block block = loc.getBlock();

        if (Utils.isContainer(block)) {
            if (StorageContainer.isLoaded(block)) {
                StorageContainer storageContainer = StorageContainer.getStorageContainer(block);
                storageContainer.updateContents();
            }
        }
    }

}
