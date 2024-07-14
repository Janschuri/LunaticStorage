package de.janschuri.lunaticstorage.listener;

import de.janschuri.lunaticlib.platform.bukkit.inventorygui.GUIManager;
import de.janschuri.lunaticstorage.storage.Storage;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerEditListener implements Listener {

    List<Inventory> inventories = new ArrayList<>();
    Map<Inventory, ItemStack[]> oldItems = new HashMap<>();
    Map<Inventory, StorageContainer> storages = new HashMap<>();


    @EventHandler
    public void onPlayerInteract(InventoryOpenEvent event) {
        Block block = event.getInventory().getLocation().getBlock();

        if (Utils.isContainer(block)) {
            if (StorageContainer.isLoaded(block)) {
                StorageContainer storageContainer = StorageContainer.getStorageContainer(block);
                inventories.add(event.getInventory());
                storages.put(event.getInventory(), storageContainer);
                oldItems.put(event.getInventory(), event.getInventory().getContents());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (inventories.contains(event.getInventory())) {
            ItemStack[] newItems = event.getInventory().getContents();
            ItemStack[] oldItemsArray = oldItems.get(event.getInventory());

            Map<ItemStack, Integer> difference = getDifference(oldItemsArray, newItems);

            StorageContainer storageContainer = storages.get(event.getInventory());
            storageContainer.updateStorages(difference);


            inventories.remove(event.getInventory());
        }
    }

    public Map<ItemStack, Integer> getDifference(ItemStack[] oldItems, ItemStack[] newItems) {
        Map<ItemStack, Integer> difference = new HashMap<>();

        List<ItemStack> oldItemList = List.of(oldItems);
        List<ItemStack> newItemList = new ArrayList<>(List.of(newItems));

        for (ItemStack oldItem : oldItemList) {
            if (newItemList.contains(oldItem)) {
                ItemStack newItem = newItemList.get(newItemList.indexOf(oldItem));
                int differenceCount = newItem.getAmount() - oldItem.getAmount();
                if (differenceCount != 0) {
                    difference.put(newItem, differenceCount);
                }
            } else {
                difference.put(oldItem, -oldItem.getAmount());
            }
            newItemList.remove(oldItem);
        }

        for (ItemStack newItem : newItemList) {
            difference.put(newItem, newItem.getAmount());
        }

        return difference;
    }

}
