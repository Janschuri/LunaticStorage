package de.janschuri.lunaticStorages;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Storage {

    private Map<ItemStack, Integer> storageMap = new HashMap<>();
    private Map<ItemStack, Map<Integer, Boolean>> storageItems = new HashMap<>();
    private List<Integer> emptyChests = new ArrayList<>();
    private World world;

    public Storage (int[] chests, World world) {
        this.world = world;
        loadStorage(chests);
    }

    public List<Map.Entry<ItemStack, Integer>> getStorageList(String locale, int sorter, Boolean desc) {
        List<Map.Entry<ItemStack, Integer>> storageList = new ArrayList<>(storageMap.entrySet());

        Comparator<Map.Entry<ItemStack, Integer>> comparator = null;

        if (sorter == 1) {
            comparator = Comparator.comparing(entry -> Main.getLanguage(entry.getKey(), locale));
        } else {
            comparator = Comparator.comparing(Map.Entry::getValue);
        }

        if (desc) {
            comparator = comparator.reversed();
        }

//        String filterString = "log";
//
//        Predicate<Map.Entry<ItemStack, Integer>> filter = entry -> {
//            String language = Main.getLanguage(entry.getKey(), locale);
//            return language.toLowerCase().contains(filterString.toLowerCase());
//        };




        storageList = storageMap.entrySet().stream()
//                .filter(filter)
                .sorted(comparator)
                .collect(Collectors.toList());

        return storageList;
    }

    public int getPages() {
        return (int) Math.ceil((double) storageMap.size() / 36);
    }

    public void updateStorageMap(ItemStack item, int difference) {
        ItemStack clone = item.clone();
        clone.setAmount(1);

        int oldAmount = 0;
        if (this.storageMap.containsKey(clone)) {
            oldAmount = this.storageMap.get(clone);
        }

        if(oldAmount+difference == 0) {
            storageMap.remove(clone);
        } else {
            this.storageMap.put(clone, oldAmount + difference);
        }
    }

    public Map<ItemStack, Integer> addInventoryToMap(Map<ItemStack, Integer> storageMap, Inventory inventory, int id) {

        boolean empty = false;
        for (ItemStack item : inventory.getContents()) {
            if (item != null) {
                ItemStack clone = item.clone();
                clone.setAmount(1);

                boolean found = false;
                for (Map.Entry<ItemStack, Integer> entry : storageMap.entrySet()) {
                    ItemStack existingItem = entry.getKey();
                    if (existingItem.isSimilar(clone)) {
                        int amount = entry.getValue();
                        storageMap.put(existingItem, amount + item.getAmount());
                            Map<Integer, Boolean> itemsChests = this.storageItems.get(existingItem);
                            if (item.getAmount() == item.getMaxStackSize()) {
                                itemsChests.put(id, true);
                            } else {
                                itemsChests.put(id, false);
                            }
                            this.storageItems.put(existingItem, itemsChests);


                        found = true;
                        break;
                    }
                }

                if (!found) {
                    storageMap.put(clone, item.getAmount());

                    Map<Integer, Boolean> itemsChests = new HashMap<>();
                    if(item.getAmount() == item.getMaxStackSize()) {
                        itemsChests.put(id, true);
                    } else {
                        itemsChests.put(id, false);
                    }
                    this.storageItems.put(clone, itemsChests);
                }


            } else {
                empty = true;
            }
        }

        if (empty) {
            this.emptyChests.add(id);
        }
        return storageMap;
    }

    public static Inventory addMaptoInventory(Inventory inventory, List<Map.Entry<ItemStack, Integer>> list, int id, int page) {
        int pageSize = 36;
        int startIndex = page * pageSize;
        int endIndex = Math.min((page + 1) * pageSize, list.size());

        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<ItemStack, Integer> entry = list.get(i);
            ItemStack itemStack = entry.getKey();
            int amount = entry.getValue();

            ItemStack singleStack = itemStack.clone();
            singleStack.setAmount(1);

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


    public void loadStorage (int[] chests){
        for (int id : chests) {
            if (Main.getDatabase().isChestInDatabase(id)) {

                String coords = Main.getDatabase().getChestCoords(id);
                int coordsArray[] = Main.parseCoords(coords);

                int x = coordsArray[0];
                int y = coordsArray[1];
                int z = coordsArray[2];

                Block block = world.getBlockAt(x, y, z);
                Chest chest = (Chest) block.getState();

                Inventory chestInv = chest.getSnapshotInventory();


                storageMap = addInventoryToMap(storageMap, chestInv, id);

            }

        }
    }

    public ItemStack getItemsFromStorage(ItemStack item, Player player) {
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();

        byte[] serializedItem = meta.getPersistentDataContainer().get(Main.keyStorageContent, PersistentDataType.BYTE_ARRAY);
        ItemStack searchedItem = Main.deserializeItemStack(serializedItem);

        int[] chests = storageItems.getOrDefault(searchedItem, Collections.emptyMap()).entrySet().stream()
                .mapToInt(Map.Entry::getKey)
                .toArray();



        int stackSize = searchedItem.getMaxStackSize();
        int foundItems = 0;

        for (int id : chests) {

            if (Main.getDatabase().isChestInDatabase(id)) {
                String uuid = Main.getDatabase().getChestCoords(id);
                int coords[] = Main.parseCoords(uuid);

                int x = coords[0];
                int y = coords[1];
                int z = coords[2];

                Block block = world.getBlockAt(x, y, z);
                Chest chest = (Chest) block.getState();
                Location location = chest.getLocation();

                if (Main.isAllowed(player, location)) {

                    Inventory chestInv = chest.getSnapshotInventory();

                    for (ItemStack i : chestInv.getContents()) {
                        if (i != null) {
                            if (i.isSimilar(searchedItem)) {
                                int amount = i.getAmount();
                                int amountNeeded = stackSize - foundItems;
                                if (amountNeeded < amount) {
                                    chest.getSnapshotInventory().removeItem(i);
                                    i.setAmount(i.getAmount() - amountNeeded);
                                    chest.getSnapshotInventory().addItem(i);
                                    chest.update();
                                    foundItems = foundItems + amountNeeded;

                                    Map<Integer, Boolean> itemsChests = this.storageItems.get(searchedItem);
                                    itemsChests.put(id, false);
                                    this.storageItems.put(searchedItem, itemsChests);
                                } else if (amountNeeded == amount) {
                                    chest.getSnapshotInventory().removeItem(i);
                                    chest.update();
                                    foundItems = foundItems + amount;

                                    Map<Integer, Boolean> itemsChests = this.storageItems.get(searchedItem);
                                    itemsChests.remove(id);
                                    this.storageItems.put(searchedItem, itemsChests);
                                } else {
                                    chest.getSnapshotInventory().removeItem(i);
                                    chest.update();
                                    foundItems = foundItems + amount;

                                    Map<Integer, Boolean> itemsChests = this.storageItems.get(searchedItem);
                                    itemsChests.remove(id);
                                    this.storageItems.put(searchedItem, itemsChests);
                                }

                                if (foundItems == stackSize) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        searchedItem.setAmount(foundItems);

        return searchedItem;
    }

    public ItemStack insertItemsIntoStorage(ItemStack item, Player player) {

        ItemStack remainingItems = item.clone();
        ItemStack itemKey = remainingItems.clone();
        itemKey.setAmount(1);

        Set<Integer> allIntegersSet = new HashSet<>();
        allIntegersSet.addAll(emptyChests);

        for (Map<Integer, Boolean> innerMap : storageItems.values()) {
            for (Map.Entry<Integer, Boolean> entry : innerMap.entrySet()) {
                if (!entry.getValue()) { // Check if the value is false
                    allIntegersSet.add(entry.getKey());
                }
            }
        }
        int[] chests = allIntegersSet.stream().mapToInt(Integer::intValue).toArray();

        for (int id : chests) {
            if (Main.getDatabase().isChestInDatabase(id)) {
                String uuid = Main.getDatabase().getChestCoords(id);
                int coords[] = Main.parseCoords(uuid);

                int x = coords[0];
                int y = coords[1];
                int z = coords[2];

                Block block = world.getBlockAt(x, y, z);
                Chest chest = (Chest) block.getState();
                Location location = chest.getLocation();

                if (Main.isAllowed(player, location)) {
                    Inventory chestInv = chest.getSnapshotInventory();
                    Map<Integer, Boolean> itemsChests = new HashMap<>();
                    if (this.storageItems.get(itemKey) != null) {
                        itemsChests = this.storageItems.get(itemKey);
                    }

                    if (chestInv.addItem(remainingItems).isEmpty()) {
                        chest.update();
                        remainingItems.setAmount(0);


                        boolean foundNonFullStack = false;
                        boolean isEmptyChest = false;

                        for (ItemStack stack : chestInv.getContents()) {
                            if (stack != null && stack.isSimilar(item)) {
                                if (stack.getAmount() < stack.getMaxStackSize()) {
                                    foundNonFullStack = true; // Found a non-full stack
                                }
                            } else if (stack == null || stack.isEmpty()) {
                                isEmptyChest = true;
                            }
                        }

                        if (foundNonFullStack) {
                            itemsChests.put(id, false);
                        } else {
                            itemsChests.put(id, true);
                        }

                        if (!isEmptyChest) {
                            if (emptyChests.contains(id)) {
                                emptyChests.remove(emptyChests.indexOf(id));
                            }
                        }


                        break;
                    } else {
                        chest.update();
                        remainingItems = chestInv.addItem(remainingItems).get(0);
                        if (emptyChests.contains(id)) {
                            emptyChests.remove(emptyChests.indexOf(id));
                        }
                        itemsChests.put(id, true);
                    }

                    this.storageItems.put(itemKey, itemsChests);
                }
            }
        }


        return remainingItems;
    }
}
