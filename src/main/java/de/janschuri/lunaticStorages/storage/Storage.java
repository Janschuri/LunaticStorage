package de.janschuri.lunaticStorages.storage;

import de.diddiz.LogBlock.Actor;
import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.database.tables.ChestsTable;
import de.janschuri.lunaticStorages.utils.Utils;
import de.janschuri.lunaticlib.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Storage {

    private Map<ItemStack, Integer> storageMap = new HashMap<>();
    private final Map<ItemStack, Map<Integer, Boolean>> storageItems = new HashMap<>();
    private final List<Integer> emptyChests = new ArrayList<>();
    private final World world;

    public Storage (int[] chests, World world) {
        this.world = world;
        loadStorage(chests);
    }

    public List<Map.Entry<ItemStack, Integer>> getStorageList(String locale, int sorter, Boolean desc, String search) {
        List<Map.Entry<ItemStack, Integer>> storageList;

        Comparator<Map.Entry<ItemStack, Integer>> comparator;

        if (sorter == 1) {
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

        return storageList;
    }
    public int getPages() {
        return (int) Math.ceil((double) storageMap.size() / 36);
    }
    public String getTotalAmount() {
        int sum = 0;
        for (int value : storageMap.values()) {
            sum += value;
        }
        // Formatting the sum into a string with comma as thousands separator
        return String.format("%,d", sum);
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
                            if (item.getAmount() != item.getMaxStackSize()) {
                                if ((item.getAmount() + amount) % item.getMaxStackSize() == 0) {
                                    itemsChests.put(id, true);
                                } else {
                                    itemsChests.put(id, false);
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

            byte[] itemSerialized = ItemStackUtils.serializeItemStack(itemStack);
            ItemMeta meta = singleStack.getItemMeta();
            if (meta == null) {
                Bukkit.getLogger().warning("ItemMeta is null" + itemStack.getType());
                continue;
            }

            meta.getPersistentDataContainer().set(Key.STORAGE_CONTENT, PersistentDataType.BYTE_ARRAY, itemSerialized);
            meta.getPersistentDataContainer().set(Key.PANEL_BLOCK, PersistentDataType.INTEGER, id);

            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add("Amount: " + amount);
            meta.setLore(lore);

            singleStack.setItemMeta(meta);
            inventory.addItem(singleStack);
        }

        return inventory;
    }
    public void loadStorage (int[] chests){
        for (int id : chests) {
            if (ChestsTable.isChestInDatabase(id)) {

                int[] coords = ChestsTable.getChestCoords(id);
                assert coords != null;

                Block block = world.getBlockAt(coords[0], coords[1], coords[2]);
                Container container = (Container) block.getState();

                Inventory chestInv = container.getSnapshotInventory();
                storageMap = addInventoryToMap(storageMap, chestInv, id);

            }

        }
    }
    public ItemStack getItemsFromStorage(ItemStack item, Player player) {
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();

        byte[] serializedItem = meta.getPersistentDataContainer().get(Key.STORAGE_CONTENT, PersistentDataType.BYTE_ARRAY);
        ItemStack searchedItem = ItemStackUtils.deserializeItemStack(serializedItem);

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
                Block block = world.getBlockAt(coords[0], coords[1], coords[2]);

            if (!Utils.isAllowedViewChest(player, block)) {
                continue;
            }

            Container container = (Container) block.getState();
            Inventory chestInv = container.getSnapshotInventory();

            if (!Utils.isAllowedTakeItem(player, chestInv)) {
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
                    LunaticStorage.getConsumer().queueChestAccess(Actor.actorFromEntity(player), block.getState(), itemStack, true);
                } else if (amountNeeded == amount) {
                    container.getSnapshotInventory().removeItem(i);
                    container.update();
                    foundItems = foundItems + amount;

                    ItemStack itemStack = i.clone();
                    itemStack.setAmount(amountNeeded);
                    LunaticStorage.getConsumer().queueChestAccess(Actor.actorFromEntity(player), block.getState(), itemStack, true);
                } else {
                    container.getSnapshotInventory().removeItem(i);
                    container.update();
                    foundItems = foundItems + amount;

                    ItemStack itemStack = i.clone();
                    itemStack.setAmount(amount);
                    LunaticStorage.getConsumer().queueChestAccess(Actor.actorFromEntity(player), block.getState(), itemStack, true);
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
                Block block = world.getBlockAt(coords[0], coords[1], coords[2]);

                if (!Utils.isAllowedViewChest(player, block)) {
                    continue;
                }

                Container container = (Container) block.getState();
                Inventory chestInv = container.getSnapshotInventory();

                if (!Utils.isAllowedTakeItem(player, chestInv)) {
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
                        LunaticStorage.getConsumer().queueChestAccess(Actor.actorFromEntity(player), block.getState(), itemStack, true);
                    } else if (amountNeeded == amount) {
                        container.getSnapshotInventory().removeItem(i);
                        container.update();
                        foundItems = foundItems + amount;

                        ItemStack itemStack = i.clone();
                        itemStack.setAmount(amountNeeded);
                        LunaticStorage.getConsumer().queueChestAccess(Actor.actorFromEntity(player), block.getState(), itemStack, true);
                    } else {
                        container.getSnapshotInventory().removeItem(i);
                        container.update();
                        foundItems = foundItems + amount;

                        ItemStack itemStack = i.clone();
                        itemStack.setAmount(amount);
                        LunaticStorage.getConsumer().queueChestAccess(Actor.actorFromEntity(player), block.getState(), itemStack, true);
                    }
                }
                updateContainer(id, chestInv, searchedItem);
            }
        }

        updateStorageMap(searchedItem, foundItems*-1);
        searchedItem.setAmount(foundItems);

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
            if (remainingItems.isEmpty() || remainingItems.getAmount() == 0 || remainingItems.getType() == Material.AIR) {
                break;
            }
            if (!ChestsTable.isChestInDatabase(id)) {
                continue;
            }

            int[] coords = ChestsTable.getChestCoords(id);
            assert coords != null;
            Block block = world.getBlockAt(coords[0], coords[1], coords[2]);

            if (!Utils.isAllowedViewChest(player, block)) {
                continue;
            }

            Container container = (Container) block.getState();
            Inventory chestInv = container.getSnapshotInventory();

            if (!Utils.isAllowedPutItem(player, chestInv)) {
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

            LunaticStorage.getConsumer().queueChestAccess(Actor.actorFromEntity(player), block.getState(), itemStack, false);
            updateContainer(id, chestInv, itemKey);
        }

        if (remainingItems.getAmount() != 0 && !emptyChests.isEmpty()) {

            int[] emptyChests = this.emptyChests.stream().mapToInt(Integer::intValue).toArray();

            for (int id : emptyChests) {
                if (remainingItems.isEmpty() || remainingItems.getAmount() == 0 || remainingItems.getType() == Material.AIR) {
                    break;
                }
                if (!ChestsTable.isChestInDatabase(id)) {
                    continue;
                }

                int[] coords = ChestsTable.getChestCoords(id);
                assert coords != null;
                Block block = world.getBlockAt(coords[0], coords[1], coords[2]);

                if (!Utils.isAllowedViewChest(player, block)) {
                    continue;
                }

                Container container = (Container) block.getState();
                Inventory chestInv = container.getSnapshotInventory();

                if (!Utils.isAllowedPutItem(player, chestInv)) {
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

                LunaticStorage.getConsumer().queueChestAccess(Actor.actorFromEntity(player), container, itemStack, false);
                updateContainer(id, chestInv, itemKey);
            }
        }

        int amount = item.getAmount() - remainingItems.getAmount();
        updateStorageMap(item, amount+1);

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
        }
    }

}
