package de.janschuri.lunaticstorage.listener;

import com.google.common.collect.ImmutableMap;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final ImmutableMap<ItemStack, Integer> changes;
    private final Inventory inventory;

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public InventoryChangeEvent(Inventory inventory, List<Map.Entry<ItemStack, Integer>> changes) {
        super(false);
        this.inventory = inventory;

        Map<ItemStack, Integer> newChanges = new HashMap<>();

        for (Map.Entry<ItemStack, Integer> entry : changes) {
            if (newChanges.containsKey(entry.getKey())) {
                newChanges.put(entry.getKey(), newChanges.get(entry.getKey()) + entry.getValue());
            } else {
                newChanges.put(entry.getKey(), entry.getValue());
            }
        }

        this.changes = ImmutableMap.copyOf(newChanges);
    }

    public ImmutableMap<ItemStack, Integer> getChanges() {
        return changes;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
