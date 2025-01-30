package de.janschuri.lunaticstorage.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class InventoryMoveItemListener implements Listener {

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        Inventory destination = event.getDestination();
        Inventory source = event.getSource();

        ItemStack itemStack = event.getItem().clone();
        int amount = itemStack.getAmount();
        List<Map.Entry<ItemStack, Integer>> destinationChanges = List.of(Map.entry(itemStack, amount));
        List<Map.Entry<ItemStack, Integer>> sourceChanges = List.of(Map.entry(itemStack, -amount));

        InventoryChangeEvent destinationEvent = new InventoryChangeEvent(destination, destinationChanges);
        InventoryChangeEvent sourceEvent = new InventoryChangeEvent(source, sourceChanges);
        Bukkit.getPluginManager().callEvent(destinationEvent);
        Bukkit.getPluginManager().callEvent(sourceEvent);
    }

}
