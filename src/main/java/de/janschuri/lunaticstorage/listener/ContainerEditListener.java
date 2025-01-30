package de.janschuri.lunaticstorage.listener;

import de.janschuri.lunaticlib.platform.bukkit.util.EventUtils;
import de.janschuri.lunaticstorage.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ContainerEditListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (EventUtils.isFakeEvent(event)) {
            Logger.debugLog("Ignoring fake event");
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BlockState || holder instanceof DoubleChest) {

            List<Map.Entry<ItemStack, Integer>> changes = new ArrayList<>();

            switch (event.getAction()) {
                case PICKUP_ONE:
                case DROP_ONE_SLOT: {
                    if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {

                        ItemStack itemStack = event.getCurrentItem().clone();
                        int amount = -1;
                        changes.add(Map.entry(itemStack, amount));
                    }
                    break;
                }
                case PICKUP_HALF: {
                    if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {

                        ItemStack itemStack = event.getCurrentItem().clone();
                        int amount = -(event.getCurrentItem().getAmount() + 1) / 2;
                        changes.add(Map.entry(itemStack, amount));
                    }
                    break;
                }
                case PICKUP_SOME: {
                    if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                        ItemStack itemStack = event.getCursor().clone();
                        int amount = event.getCurrentItem().getAmount() - event.getCurrentItem().getMaxStackSize();
                        changes.add(Map.entry(itemStack, amount));
                    }
                    break;
                }
                case PICKUP_ALL:
                case DROP_ALL_SLOT: {
                    if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                        ItemStack itemStack = event.getCurrentItem().clone();
                        int amount = -event.getCurrentItem().getAmount();
                        changes.add(Map.entry(itemStack, amount));
                    }
                    break;
                }
                case PLACE_ONE: {
                    if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                        ItemStack itemStack = event.getCursor().clone();
                        int amount = 1;
                        changes.add(Map.entry(itemStack, amount));
                    }
                    break;
                }
                case PLACE_SOME: {
                    if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                        ItemStack itemStack = event.getCursor().clone();
                        int amount = event.getCurrentItem().getMaxStackSize() - event.getCurrentItem().getAmount();
                        changes.add(Map.entry(itemStack, amount));
                    }
                    break;
                }
                case PLACE_ALL: {
                    if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                        ItemStack itemStack = event.getCursor().clone();
                        int amount = event.getCursor().getAmount();
                        changes.add(Map.entry(itemStack, amount));
                    }
                    break;
                }
                case SWAP_WITH_CURSOR: {
                    if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                        ItemStack itemStack1 = event.getCursor().clone();
                        int amount = event.getCursor().getAmount();
                        changes.add(Map.entry(itemStack1, amount));

                        ItemStack itemStack2 = event.getCurrentItem().clone();
                        amount = -event.getCurrentItem().getAmount();
                        changes.add(Map.entry(itemStack2, amount));
                    }
                    break;
                }
                case MOVE_TO_OTHER_INVENTORY: {
                    boolean removed = event.getRawSlot() < event.getView().getTopInventory().getSize();
                    ItemStack itemStack = event.getCurrentItem().clone();
                    int amount = event.getCurrentItem().getAmount() * (removed ? -1 : 1);
                    changes.add(Map.entry(itemStack, amount));

                    break;
                }
                case COLLECT_TO_CURSOR: {
                    ItemStack cursor = event.getCursor();
                    if (cursor == null) {
                        return;
                    }
                    int toPickUp = cursor.getMaxStackSize() - cursor.getAmount();
                    int takenFromContainer = 0;
                    boolean takeFromFullStacks = false;
                    Inventory top = event.getView().getTopInventory();
                    Inventory bottom = event.getView().getBottomInventory();
                    while (toPickUp > 0) {
                        for (ItemStack stack : top.getStorageContents()) {
                            if (cursor.isSimilar(stack)) {
                                if (takeFromFullStacks == (stack.getAmount() == stack.getMaxStackSize())) {
                                    int take = Math.min(toPickUp, stack.getAmount());
                                    toPickUp -= take;
                                    takenFromContainer += take;
                                    if (toPickUp <= 0) {
                                        break;
                                    }
                                }
                            }
                        }
                        if (toPickUp <= 0) {
                            break;
                        }
                        for (ItemStack stack : bottom.getStorageContents()) {
                            if (cursor.isSimilar(stack)) {
                                if (takeFromFullStacks == (stack.getAmount() == stack.getMaxStackSize())) {
                                    int take = Math.min(toPickUp, stack.getAmount());
                                    toPickUp -= take;
                                    if (toPickUp <= 0) {
                                        break;
                                    }
                                }
                            }
                        }
                        if (takeFromFullStacks) {
                            break;
                        } else {
                            takeFromFullStacks = true;
                        }
                    }
                    if (takenFromContainer > 0) {
                        ItemStack itemStack = event.getCursor().clone();
                        int amount = -takenFromContainer;
                        changes.add(Map.entry(itemStack, amount));
                    }
                    break;
                }
                case HOTBAR_SWAP:
                case HOTBAR_MOVE_AND_READD: {
                    if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                        ItemStack otherSlot = (event.getClick() == ClickType.SWAP_OFFHAND) ? event.getWhoClicked().getInventory().getItemInOffHand() : event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                        if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                            ItemStack itemStack = event.getCurrentItem().clone();
                            int amount = -event.getCurrentItem().getAmount();
                            changes.add(Map.entry(itemStack, amount));
                        }
                        if (otherSlot != null && otherSlot.getType() != Material.AIR) {
                            ItemStack itemStack = otherSlot.clone();
                            int amount = otherSlot.getAmount();
                            changes.add(Map.entry(itemStack, amount));
                        }
                    }
                    break;
                }
            }

            Inventory inventory = event.getView().getTopInventory();

            InventoryChangeEvent inventoryChangeEvent = new InventoryChangeEvent(inventory, changes);
            Bukkit.getPluginManager().callEvent(inventoryChangeEvent);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event) {

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BlockState || holder instanceof DoubleChest) {

            List<Map.Entry<ItemStack, Integer>> changes = new ArrayList<>();

            Inventory container = event.getView().getTopInventory();
            int containerSize = container.getSize();
            for (Entry<Integer, ItemStack> e : event.getNewItems().entrySet()) {
                int slot = e.getKey();
                if (slot < containerSize) {
                    ItemStack old = container.getItem(slot);
                    int oldAmount = (old == null || old.getType() == Material.AIR) ? 0 : old.getAmount();

                    ItemStack itemStack = e.getValue().clone();
                    int amount = e.getValue().getAmount() - oldAmount;
                    changes.add(Map.entry(itemStack, amount));
                }
            }

            Inventory inventory = event.getView().getTopInventory();
            InventoryChangeEvent inventoryChangeEvent = new InventoryChangeEvent(inventory, changes);
            Bukkit.getPluginManager().callEvent(inventoryChangeEvent);
        }
    }
}
