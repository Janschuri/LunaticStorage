package de.janschuri.lunaticstorage.listener;

import com.mysql.cj.log.Log;
import de.janschuri.lunaticlib.platform.bukkit.util.EventUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ContainerEditListener implements Listener {


    @EventHandler
    public void onPlayerInteract(InventoryClickEvent event) {

        if (EventUtils.isFakeEvent(event)) {
            Logger.debugLog("Ignoring fake event");
            return;
        }

        Inventory topInventory = event.getView().getTopInventory();
        InventoryHolder holder = topInventory.getHolder();

        handleContainerEdit(holder);
    }

    @EventHandler
    public void onHopperTransfer(InventoryMoveItemEvent event) {
        InventoryHolder destination = event.getDestination().getHolder();
        InventoryHolder source = event.getSource().getHolder();

        handleContainerEdit(destination);
        handleContainerEdit(source);
    }

    private void handleContainerEdit(InventoryHolder holder) {
//        if (true) {
//            return;
//        }

        if (holder instanceof Container container) {
            Block block = container.getBlock();

            if (Utils.isStorageContainer(block)) {
                if (StorageContainer.isLoaded(block)) {
                    StorageContainer storageContainer = StorageContainer.getStorageContainer(block);

                    Bukkit.getScheduler().runTaskLater(LunaticStorage.getInstance(), () -> {
                        storageContainer.updateContents();
                    }, 20);
                }
            }
        }
    }

}
