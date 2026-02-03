package de.janschuri.lunaticstorage.listener;

import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class InventoryChangeListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryChangeMonitor(InventoryChangeEvent event) {
        if (LunaticStorage.getPluginConfig().isShutdown()) {
            return;
        }

        if (event.isCancelled()) {
            event.getSourceEvent().setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryChange(InventoryChangeEvent event) {
        if (LunaticStorage.getPluginConfig().isShutdown()) {
            return;
        }

        Map<ItemStack, Integer> changes = event.getChanges();

        Inventory inventory = event.getInventory();

        if (inventory == null) {
            return;
        }

        if (inventory.getHolder() instanceof Container || inventory.getHolder() instanceof DoubleChest) {
            Block block = inventory.getLocation().getBlock();

            if (Utils.isStorageContainer(block)) {
                if (StorageContainer.isLoaded(block)) {
                    StorageContainer storageContainer = StorageContainer.getStorageContainer(block);
                    storageContainer.updateStorages(changes);
                }
            }
        }
    }
}
