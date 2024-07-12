package de.janschuri.lunaticStorages.storage;

import de.janschuri.lunaticStorages.database.tables.ChestsTable;
import de.janschuri.lunaticStorages.utils.Logger;
import de.janschuri.lunaticStorages.utils.Utils;
import de.janschuri.lunaticlib.platform.bukkit.external.LogBlock;
import de.janschuri.lunaticlib.platform.bukkit.util.EventUtils;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Storage {

    private static final Map<Integer, Storage> storages = new HashMap<>();

    private Map<ItemStack, Integer> storageMap = new HashMap<>();
    private final Map<ItemStack, Map<Integer, Boolean>> storageItems = new HashMap<>();
    private final List<Integer> emptyChests = new ArrayList<>();
    private final int panelId;
    private final byte[] storageItem;

    private Storage (int panelId, byte[] storageItem) {
        this.storageItem = storageItem;
        int[] chests = getStorageChests(storageItem);
        this.panelId = panelId;
        loadStorage(chests);
    }

    public static Storage getStorage(int panelId, byte[] storageItem) {

        if (storages.containsKey(panelId)) {
            Storage storage = storages.get(panelId);

            if (storage.storageItem == storageItem) {
                return storage;
            } else {
                return new Storage(panelId, storageItem);
            }

        } else {
            return new Storage(panelId, storageItem);
        }
    }

    public int getPanelId() {
        return panelId;
    }

    public List<Map.Entry<ItemStack, Integer>> getStorageList(String locale, int sorter, Boolean desc, String search, int page) {
        List<Map.Entry<ItemStack, Integer>> storageList;

        Comparator<Map.Entry<ItemStack, Integer>> comparator;

        if (sorter == 0) {
            comparator = Comparator.comparing(entry -> Utils.getMCLanguage(entry.getKey(), locale));
        } else {
            comparator = Map.Entry.comparingByValue();
        }

        if (desc) {
            comparator = comparator.reversed();
        }

        if (search == null) {
            search = "";
        }

        String finalSearch = search;
        Predicate<Map.Entry<ItemStack, Integer>> filter = entry -> {
            if (finalSearch.isEmpty()) {
                return true;
            }
            String language = Utils.getMCLanguage(entry.getKey(), locale);
            return language.toLowerCase().contains(finalSearch.toLowerCase());
        };




        storageList = storageMap.entrySet().stream()
                .filter(filter)
                .sorted(comparator)
                .collect(Collectors.toList());

        int pageSize = 36;
        int startIndex = page * pageSize;
        int endIndex = startIndex + pageSize;

        if (endIndex > storageList.size()) {
            endIndex = storageList.size();
        }

        return storageList.subList(startIndex, endIndex);
    }
    public int getPages() {
        return storageMap.size() / 36;
    }
    public int getTotalAmount() {
        int sum = 0;
        for (int value : storageMap.values()) {
            sum += value;
        }
        // Formatting the sum into a string with comma as thousands separator
        return sum;
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

                            if (itemsChests.containsKey(id)) {
                                if (item.getAmount() != item.getMaxStackSize()) {
                                    itemsChests.put(id, false);
                                }
                            } else {
                                if (item.getAmount() != item.getMaxStackSize()) {
                                    itemsChests.put(id, false);
                                } else {
                                    itemsChests.put(id, true);
                                }
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
//    public static Inventory addMaptoInventory(Inventory inventory, List<Map.Entry<ItemStack, Integer>> list, int id, int page) {
//        int pageSize = 36;
//        int startIndex = page * pageSize;
//        int endIndex = Math.min((page + 1) * pageSize, list.size());
//
//        for (int i = startIndex; i < endIndex; i++) {
//            Map.Entry<ItemStack, Integer> entry = list.get(i);
//            ItemStack itemStack = entry.getKey();
//            int amount = entry.getValue();
//
//            ItemStack singleStack = itemStack.clone();
//            singleStack.setAmount(1);
//
//            byte[] itemSerialized = ItemStackUtils.serializeItemStack(itemStack);
//            ItemMeta meta = singleStack.getItemMeta();
//            if (meta == null) {
//                Bukkit.getLogger().warning("ItemMeta is null" + itemStack.getType());
//                continue;
//            }
//
//            meta.getPersistentDataContainer().set(Key.STORAGE_CONTENT, PersistentDataType.BYTE_ARRAY, itemSerialized);
//            meta.getPersistentDataContainer().set(Key.PANEL_BLOCK, PersistentDataType.INTEGER, id);
//
//            List<String> lore = meta.getLore();
//            if (lore == null) {
//                lore = new ArrayList<>();
//            }
//            lore.add("Amount: " + amount);
//            meta.setLore(lore);
//
//            singleStack.setItemMeta(meta);
//            inventory.addItem(singleStack);
//        }
//
//        return inventory;
//    }
    public void loadStorage (int[] chests){
        for (int id : chests) {
            if (ChestsTable.isChestInDatabase(id)) {

                int[] coords = ChestsTable.getChestCoords(id);
                assert coords != null;
                String worldString = ChestsTable.getChestWorld(id);
                World world = Bukkit.getWorld(worldString);

                Block block = world.getBlockAt(coords[0], coords[1], coords[2]);
                Container container = (Container) block.getState();

                Inventory chestInv = container.getSnapshotInventory();
                storageMap = addInventoryToMap(storageMap, chestInv, id);

            }
        }
    }
    public ItemStack getItemsFromStorage(ItemStack item, Player player) {
        ItemStack searchedItem = item.clone();

        int[] nonFullstackContainers;

        if (this.storageItems.get(searchedItem) != null) {
            nonFullstackContainers = this.storageItems.get(searchedItem).entrySet().stream()
                    .filter(entry -> !entry.getValue())
                    .mapToInt(Map.Entry::getKey)
                    .toArray();
        } else {
            nonFullstackContainers = new int[0];
        }

        int stackSize = searchedItem.getMaxStackSize();
        int foundItems = 0;

        for (int id : nonFullstackContainers) {
            if (foundItems == stackSize) {
                break;
            }
            if (!ChestsTable.isChestInDatabase(id)) {
                continue;
            }

                int[] coords = ChestsTable.getChestCoords(id);
                assert coords != null;
            String worldString = ChestsTable.getChestWorld(id);
            World world = Bukkit.getWorld(worldString);
                Block block = world.getBlockAt(coords[0], coords[1], coords[2]);

            if (!EventUtils.isAllowedViewChest(player, block)) {
                continue;
            }

            Container container = (Container) block.getState();
            Inventory chestInv = container.getSnapshotInventory();

            if (!EventUtils.isAllowedTakeItem(player, chestInv)) {
                continue;
            }

            for (ItemStack i : chestInv.getContents()) {
                if (foundItems == stackSize) {
                    break;
                }
                if (i == null) {
                    continue;
                }
                if (!i.isSimilar(searchedItem)) {
                    continue;
                }

                int amount = i.getAmount();
                int amountNeeded = stackSize - foundItems;

                if (amountNeeded < amount) {
                    container.getSnapshotInventory().removeItem(i);
                    i.setAmount(i.getAmount() - amountNeeded);
                    container.getSnapshotInventory().addItem(i);
                    container.update();
                    foundItems = foundItems + amountNeeded;

                    ItemStack itemStack = i.clone();
                    itemStack.setAmount(amountNeeded);

                    LogBlock.logChestRemove(player, block, itemStack);

                } else if (amountNeeded == amount) {
                    container.getSnapshotInventory().removeItem(i);
                    container.update();
                    foundItems = foundItems + amount;

                    ItemStack itemStack = i.clone();
                    itemStack.setAmount(amountNeeded);

                    LogBlock.logChestRemove(player, block, itemStack);

                } else {
                    container.getSnapshotInventory().removeItem(i);
                    container.update();
                    foundItems = foundItems + amount;

                    ItemStack itemStack = i.clone();
                    itemStack.setAmount(amount);

                    LogBlock.logChestRemove(player, block, itemStack);

                }
            }
            updateContainer(id, chestInv, searchedItem);
        }

        if (foundItems != stackSize) {

            int[] fullstackContainers;

            if (this.storageItems.get(searchedItem) != null) {
                fullstackContainers = this.storageItems.get(searchedItem).entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .mapToInt(Map.Entry::getKey)
                        .toArray();
            } else {
                fullstackContainers = new int[0];
            }

            for (int id : fullstackContainers) {
                if (foundItems == stackSize) {
                    break;
                }
                if (!ChestsTable.isChestInDatabase(id)) {
                    continue;
                }

                int[] coords = ChestsTable.getChestCoords(id);
                assert coords != null;
                String worldString = ChestsTable.getChestWorld(id);
                World world = Bukkit.getWorld(worldString);
                Block block = world.getBlockAt(coords[0], coords[1], coords[2]);

                if (!EventUtils.isAllowedViewChest(player, block)) {
                    continue;
                }

                Container container = (Container) block.getState();
                Inventory chestInv = container.getSnapshotInventory();

                if (!EventUtils.isAllowedTakeItem(player, chestInv)) {
                    continue;
                }

                for (ItemStack i : chestInv.getContents()) {
                    if (foundItems == stackSize) {
                        break;
                    }
                    if (i == null) {
                        continue;
                    }
                    if (!i.isSimilar(searchedItem)) {
                        continue;
                    }

                    int amount = i.getAmount();
                    int amountNeeded = stackSize - foundItems;

                    if (amountNeeded < amount) {
                        container.getSnapshotInventory().removeItem(i);
                        i.setAmount(i.getAmount() - amountNeeded);
                        container.getSnapshotInventory().addItem(i);
                        container.update();
                        foundItems = foundItems + amountNeeded;

                        ItemStack itemStack = i.clone();
                        itemStack.setAmount(amountNeeded);

                        LogBlock.logChestRemove(player, block, itemStack);

                    } else if (amountNeeded == amount) {
                        container.getSnapshotInventory().removeItem(i);
                        container.update();
                        foundItems = foundItems + amount;

                        ItemStack itemStack = i.clone();
                        itemStack.setAmount(amountNeeded);

                        LogBlock.logChestRemove(player, block, itemStack);

                    } else {
                        container.getSnapshotInventory().removeItem(i);
                        container.update();
                        foundItems = foundItems + amount;

                        ItemStack itemStack = i.clone();
                        itemStack.setAmount(amount);

                        LogBlock.logChestRemove(player, block, itemStack);
                    }
                }
                updateContainer(id, chestInv, searchedItem);
            }
        }

        updateStorageMap(searchedItem, -(foundItems));

        searchedItem.setAmount(foundItems);
        storages.put(panelId, this);

        return searchedItem;
    }
    public ItemStack insertItemsIntoStorage(ItemStack item, Player player) {

        ItemStack remainingItems = item.clone();
        ItemStack itemKey = remainingItems.clone();
        itemKey.setAmount(1);

        int[] containers;
        if (this.storageItems.get(itemKey) != null) {
            containers = this.storageItems.get(itemKey).entrySet().stream()
                    .filter(entry -> !entry.getValue())
                    .mapToInt(Map.Entry::getKey)
                    .toArray();
        } else {
            containers = new int[0];
        }

        for (int id : containers) {
            if (remainingItems.getAmount() == 0 || remainingItems.getType() == Material.AIR) {
                break;
            }
            if (!ChestsTable.isChestInDatabase(id)) {
                continue;
            }

            int[] coords = ChestsTable.getChestCoords(id);
            assert coords != null;
            String worldString = ChestsTable.getChestWorld(id);
            World world = Bukkit.getWorld(worldString);
            Block block = world.getBlockAt(coords[0], coords[1], coords[2]);

            if (!EventUtils.isAllowedViewChest(player, block)) {
                continue;
            }

            Container container = (Container) block.getState();
            Inventory chestInv = container.getSnapshotInventory();

            if (!EventUtils.isAllowedPutItem(player, chestInv)) {
                continue;
            }

            int oldAmount = remainingItems.getAmount();

            remainingItems = chestInv.addItem(remainingItems).get(0);
            container.update();
            if (remainingItems == null) {
                remainingItems = new ItemStack(Material.AIR);
            }

            int newAmount = oldAmount - remainingItems.getAmount();
            ItemStack itemStack = remainingItems.clone();
            itemStack.setAmount(newAmount);

            LogBlock.logChestInsert(player, block, itemStack);

            updateContainer(id, chestInv, itemKey);
        }

        if (remainingItems.getAmount() != 0 && !emptyChests.isEmpty()) {

            int[] emptyChests = this.emptyChests.stream().mapToInt(Integer::intValue).toArray();

            for (int id : emptyChests) {
                if (remainingItems.getAmount() == 0 || remainingItems.getType() == Material.AIR) {
                    break;
                }
                if (!ChestsTable.isChestInDatabase(id)) {
                    continue;
                }

                int[] coords = ChestsTable.getChestCoords(id);
                assert coords != null;
                String worldString = ChestsTable.getChestWorld(id);
                World world = Bukkit.getWorld(worldString);
                Block block = world.getBlockAt(coords[0], coords[1], coords[2]);

                if (!EventUtils.isAllowedViewChest(player, block)) {
                    continue;
                }

                Container container = (Container) block.getState();
                Inventory chestInv = container.getSnapshotInventory();

                if (!EventUtils.isAllowedPutItem(player, chestInv)) {
                    continue;
                }

                int oldAmount = remainingItems.getAmount();

                remainingItems = chestInv.addItem(remainingItems).get(0);
                container.update();
                if (remainingItems == null) {
                    remainingItems = new ItemStack(Material.AIR);
                }

                int newAmount = oldAmount - remainingItems.getAmount();
                ItemStack itemStack = remainingItems.clone();
                itemStack.setAmount(newAmount);

                LogBlock.logChestInsert(player, block, itemStack);

                updateContainer(id, chestInv, itemKey);
            }
        }

        int amount = item.getAmount() - remainingItems.getAmount();

        updateStorageMap(item, amount);



        storages.put(panelId, this);
        return remainingItems;
    }
    public void updateContainer(int id, Inventory containerInv, ItemStack itemKey) {
        Map<Integer, Boolean> itemsChests = new HashMap<>();

        if (this.storageItems.get(itemKey) != null) {
            itemsChests = this.storageItems.get(itemKey);
        }

        if (containerInv.containsAtLeast(itemKey, 1)) {

            itemsChests.put(id, true);

            for (ItemStack item : containerInv.getContents()) {
                if (item != null && item.isSimilar(itemKey) && item.getAmount() != item.getMaxStackSize()){
                    itemsChests.put(id, false);
                    break;
                }
            }

        } else {
            itemsChests.remove(id);
        }

        this.storageItems.put(itemKey, itemsChests);

        if (containerInv.firstEmpty() == -1) {
            emptyChests.remove(Integer.valueOf(id));
        } else {
            if (!emptyChests.contains(id)) {
                emptyChests.add(id);
            }
        }
    }

    public static int[] getStorageChests(byte[] serializedStorageItem) {
        if (serializedStorageItem != null) {
            ItemStack storageItem = ItemStackUtils.deserializeItemStack(serializedStorageItem);
            ItemMeta meta = storageItem.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            return container.get(Key.STORAGE, PersistentDataType.INTEGER_ARRAY);
        }

        return new int[0];
    }

}
