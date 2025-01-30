package de.janschuri.lunaticstorage.listener;

import de.janschuri.lunaticlib.platform.bukkit.util.EventUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.Storage;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {

        InventoryHolder destination = event.getDestination().getHolder();
        InventoryHolder source = event.getSource().getHolder();

        if (destination instanceof Container container) {
            Block block = container.getBlock();

            if (Utils.isPanel(block)) {
                ItemStack item = event.getItem().clone();

                if (item.getAmount() > 1) {
                    return;
                }


                Storage storage = Storage.getStorage(block);
                ItemStack returnItem = storage.insertItemsIntoStorage(item, null);

                if (returnItem.getAmount() > 0) {
                    event.setCancelled(true);
                } else {
                    event.setItem(new ItemStack(Material.AIR, 0));
                }

                return;
            }
        }

        handleContainerEdit(destination);
        handleContainerEdit(source);
    }

    private void handleContainerEdit(InventoryHolder holder) {

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
