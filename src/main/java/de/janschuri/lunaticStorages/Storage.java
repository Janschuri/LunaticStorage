package de.janschuri.lunaticStorages;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class Storage {

    public static Map<ItemStack, Integer> inventoryToMap(Inventory inventory) {
        Map<ItemStack, Integer> itemMap = new HashMap<>();

        for (ItemStack item : inventory.getContents()) {
            if (item != null) {
                // Clone the item to avoid modifying the original
                ItemStack clone = item.clone();
                clone.setAmount(1); // Set amount to 1 to treat each item stack as a unique key

                // Check if item already exists in the map with the same metadata
                boolean found = false;
                for (Map.Entry<ItemStack, Integer> entry : itemMap.entrySet()) {
                    ItemStack existingItem = entry.getKey();
                    if (existingItem.isSimilar(clone)) {
                        int amount = entry.getValue();
                        itemMap.put(existingItem, amount + item.getAmount());
                        found = true;
                        break;
                    }
                }

                // If item not found, add it to the map
                if (!found) {
                    itemMap.put(clone, item.getAmount());
                }
            }
        }

        return itemMap;
    }

    public static Inventory addMaptoInventory(Inventory inventory, List<Map.Entry<ItemStack, Integer>> list, int id) {

        // Add items to the sum inventory (sorted by amount)
        for (Map.Entry<ItemStack, Integer> entry : list) {

            ItemStack itemStack = entry.getKey();
            int amount = entry.getValue();

            ItemStack singleStack = itemStack.clone();
            singleStack.setAmount(1); // Set amount to 1

            byte[] itemSerialized = Main.serializeItemStack(itemStack);

            ItemMeta meta = singleStack.getItemMeta();
            meta.getPersistentDataContainer().set(Main.keyStorageContent, PersistentDataType.BYTE_ARRAY, itemSerialized);
            meta.getPersistentDataContainer().set(Main.keyPanelID, PersistentDataType.INTEGER, id);
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore == null) {
                    lore = new ArrayList<>();
                }
                lore.add("Amount: " + amount);
                meta.setLore(lore);

            }
            singleStack.setItemMeta(meta);
            inventory.addItem(singleStack);
        }

        return inventory;
    }

    public static List<Map.Entry<ItemStack, Integer>> getStorage (int[] chests, World world){

        Map<ItemStack, Integer> summedInventory = new HashMap<>();
        List<Inventory> inventories = new ArrayList<>();
        for (int id : chests) {

            String coords = Main.getDatabase().getChestCoords(id);
            int coordsArray[] = Main.parseCoords(coords);

            int x = coordsArray[0];
            int y = coordsArray[1];
            int z = coordsArray[2];

            Block block = world.getBlockAt(x, y, z);
            Chest chest = (Chest) block.getState();

            Inventory chestInv = chest.getSnapshotInventory();
            inventories.add(chestInv);

        }

        // Iterate over the inventories
        for (Inventory inventory : inventories) {
            // Convert the inventory to a map
            Map<ItemStack, Integer> inventoryMap = inventoryToMap(inventory);

            // Sum the inventory map with the summedInventory map
            for (Map.Entry<ItemStack, Integer> itemEntry : inventoryMap.entrySet()) {
                ItemStack itemStack = itemEntry.getKey();
                int amount = itemEntry.getValue();

                summedInventory.merge(itemStack, amount, Integer::sum);
            }
        }

        // Sort the summed inventory map by amount
        List<Map.Entry<ItemStack, Integer>> sortedEntries = summedInventory.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());



        return sortedEntries;
    }

    public static ItemStack getItemsFromStorage(int[] chests, World world, ItemStack item) {
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();

        byte[] serializedItem = meta.getPersistentDataContainer().get(Main.keyStorageContent, PersistentDataType.BYTE_ARRAY);
        ItemStack searchedItem = Main.deserializeItemStack(serializedItem);
        int stackSize = searchedItem.getMaxStackSize();
        int foundItems = 0;

        for (int id : chests) {
            String uuid = Main.getDatabase().getChestCoords(id);
            int coords[] = Main.parseCoords(uuid);

            int x = coords[0];
            int y = coords[1];
            int z = coords[2];

            Block block = world.getBlockAt(x, y, z);
            Chest chest = (Chest) block.getState();

            Inventory chestInv = chest.getSnapshotInventory();

            for (ItemStack i : chestInv.getContents()) {
                if (i != null) {
                    if (i.isSimilar(searchedItem)) {
                        int amount = i.getAmount();
                        int amountNeeded = stackSize-foundItems;
                        if (amountNeeded < amount) {
                            chest.getSnapshotInventory().removeItem(i);
                            i.setAmount(i.getAmount()-amountNeeded);
                            chest.getSnapshotInventory().addItem(i);
                            chest.update();
                            foundItems = foundItems+amountNeeded;
                        } else if (amountNeeded == amount) {
                            chest.getSnapshotInventory().removeItem(i);
                            chest.update();
                            foundItems = foundItems+amount;
                        } else {
                            chest.getSnapshotInventory().removeItem(i);
                            chest.update();
                            foundItems = foundItems+amount;
                        }

                        if (foundItems == stackSize) {
                            break;
                        }
                    }
                }
            }
        }

        searchedItem.setAmount(foundItems);

        return searchedItem;
    }
}
