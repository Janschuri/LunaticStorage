package de.janschuri.lunaticstorage.listener;

import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class InventoryChangeListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryChangeMonitor(InventoryChangeEvent event) {
        if (event.isCancelled()) {
            event.getSourceEvent().setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryChange(InventoryChangeEvent event) {
        Map<ItemStack, Integer> changes = event.getChanges();

        Inventory inventory = event.getInventory();

        if (inventory == null) {
            return;
        }

        if (inventory.getHolder() instanceof Container container) {
            Block block = container.getBlock();

            if (Utils.isStorageContainer(block)) {
                if (StorageContainer.isLoaded(block)) {
                    StorageContainer storageContainer = StorageContainer.getStorageContainer(block);
                    storageContainer.updateStorages(changes);
                }
            }
        }
    }
}
