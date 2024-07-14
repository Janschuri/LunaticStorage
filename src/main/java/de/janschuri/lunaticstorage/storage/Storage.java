package de.janschuri.lunaticstorage.storage;

import de.janschuri.lunaticstorage.gui.StorageGUI;
import de.janschuri.lunaticstorage.utils.Utils;
import de.janschuri.lunaticlib.platform.bukkit.external.LogBlock;
import de.janschuri.lunaticlib.platform.bukkit.util.EventUtils;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
    private final Map<ItemStack, Map<Block, Boolean>> itemsContainers = new HashMap<>();
    private final List<Block> emptyContainers = new ArrayList<>();
    private final int panelId;
    private final byte[] storageItem;

    private Storage (int panelId, byte[] storageItem) {
        this.storageItem = storageItem;
        Collection<StorageContainer> chests = getStorageChests(storageItem);
        this.panelId = panelId;
        loadStorage(chests);
        storages.put(panelId, this);
    }

    public static Storage getStorage(int panelId, byte[] storageItem) {

        if (storages.containsKey(panelId)) {
            Storage storage = storages.get(panelId);

            if (storage.storageItem == storageItem) {
                return storage;
            }
        }

            return new Storage(panelId, storageItem);
    }

    public List<Map.Entry<ItemStack, Integer>> getStorageList(String locale, int sorter, Boolean desc, String search) {
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

        return storageList;
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

    public Map<ItemStack, Integer> addInventoryToMap(Map<ItemStack, Integer> storageMap, Inventory inventory, StorageContainer container) {
        boolean empty = false;
        Block block = container.getBlock();

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
                            Map<Block, Boolean> itemsChests = this.itemsContainers.get(existingItem);

                            if (itemsChests.containsKey(block)) {
                                if (item.getAmount() != item.getMaxStackSize()) {
                                    itemsChests.put(block, false);
                                }
                            } else {
                                if (item.getAmount() != item.getMaxStackSize()) {
                                    itemsChests.put(block, false);
                                } else {
                                    itemsChests.put(block, true);
                                }
                            }

                            this.itemsContainers.put(existingItem, itemsChests);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    storageMap.put(clone, item.getAmount());

                    Map<Block, Boolean> itemsChests = new HashMap<>();
                    if(item.getAmount() == item.getMaxStackSize()) {
                        itemsChests.put(block, true);
                    } else {
                        itemsChests.put(block, false);
                    }
                    this.itemsContainers.put(clone, itemsChests);
                }


            } else {
                empty = true;
            }
        }

        if (empty) {
            this.emptyContainers.add(block);
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
    public void loadStorage (Collection<StorageContainer> chests) {
        for (StorageContainer container : chests) {
                container.addStorageId(panelId);
                Inventory chestInv = container.getSnapshotInventory();
                storageMap = addInventoryToMap(storageMap, chestInv, container);
        }
    }

    public static void updateStorage(int id, Map<ItemStack, Integer> difference) {
        Storage storage = storages.get(id);
        if (storage != null) {
            for (Map.Entry<ItemStack, Integer> entry : difference.entrySet()) {
                storage.updateStorageMap(entry.getKey(), entry.getValue());
            }
        }
    }

    public ItemStack getItemsFromStorage(ItemStack item, Player player) {
        ItemStack searchedItem = item.clone();

        List<StorageContainer> nonFullstackContainers = new ArrayList<>();

        if (this.itemsContainers.get(searchedItem) != null) {
            nonFullstackContainers = this.itemsContainers.get(searchedItem).entrySet().stream()
                    .filter(entry -> !entry.getValue())
                    .map(entry -> StorageContainer.getStorageContainer(entry.getKey()))
                    .toList();
        }

        int stackSize = searchedItem.getMaxStackSize();
        int foundItems = 0;

        for (StorageContainer container: nonFullstackContainers) {
            if (foundItems == stackSize) {
                break;
            }
            if (!Utils.isContainer(container.getBlock())){
                continue;
            }

            Block block = container.getBlock();

            if (!EventUtils.isAllowedViewChest(player, block)) {
                continue;
            }

            Inventory chestInv = container.getSnapshotInventory();

            if (chestInv == null) {
                continue;
            }

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
            updateContainer(container, chestInv, searchedItem);
        }

        if (foundItems != stackSize) {

            List<StorageContainer> fullstackContainers = new ArrayList<>();

            if (this.itemsContainers.get(searchedItem) != null) {
                fullstackContainers = this.itemsContainers.get(searchedItem).entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .map(entry -> StorageContainer.getStorageContainer(entry.getKey()))
                        .toList();
            }

            for (StorageContainer container : fullstackContainers) {
                if (foundItems == stackSize) {
                    break;
                }
                if (!Utils.isContainer(container.getBlock())){
                    continue;
                }

                Block block = container.getBlock();

                if (!EventUtils.isAllowedViewChest(player, block)) {
                    continue;
                }

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
                updateContainer(container, chestInv, searchedItem);
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

        List<StorageContainer> containers = new ArrayList<>();
        if (this.itemsContainers.get(itemKey) != null) {
            containers = this.itemsContainers.get(itemKey).entrySet().stream()
                    .filter(entry -> !entry.getValue())
                    .map(entry -> StorageContainer.getStorageContainer(entry.getKey()))
                    .toList();
        }

        for (StorageContainer container : containers) {
            if (remainingItems.getAmount() == 0 || remainingItems.getType() == Material.AIR) {
                break;
            }
            if (!Utils.isContainer(container.getBlock())){
                continue;
            }

            Block block = container.getBlock();

            if (!EventUtils.isAllowedViewChest(player, block)) {
                continue;
            }

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

            updateContainer(container, chestInv, itemKey);
        }

        if (remainingItems.getAmount() != 0 && !emptyContainers.isEmpty()) {

            List<StorageContainer> emptyChests = this.emptyContainers.stream()
                    .map(StorageContainer::getStorageContainer)
                    .toList();

            for (StorageContainer container : emptyChests) {
                if (remainingItems.getAmount() == 0 || remainingItems.getType() == Material.AIR) {
                    break;
                }
                if (!Utils.isContainer(container.getBlock())){
                    continue;
                }

                Block block = container.getBlock();

                if (!EventUtils.isAllowedViewChest(player, block)) {
                    continue;
                }

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

                updateContainer(container, chestInv, itemKey);
            }
        }

        int amount = item.getAmount() - remainingItems.getAmount();

        updateStorageMap(item, amount);



        storages.put(panelId, this);
        return remainingItems;
    }
    public void updateContainer(StorageContainer container, Inventory containerInv, ItemStack itemKey) {
        Map<Block, Boolean> itemsChests = new HashMap<>();
        Block block = container.getBlock();

        if (this.itemsContainers.get(itemKey) != null) {
            itemsChests = this.itemsContainers.get(itemKey);
        }

        if (containerInv.containsAtLeast(itemKey, 1)) {

            itemsChests.put(block, true);

            for (ItemStack item : containerInv.getContents()) {
                if (item != null && item.isSimilar(itemKey) && item.getAmount() != item.getMaxStackSize()){
                    itemsChests.put(block, false);
                    break;
                }
            }

        } else {
            itemsChests.remove(block);
        }

        this.itemsContainers.put(itemKey, itemsChests);

        if (containerInv.firstEmpty() == -1) {
            emptyContainers.remove(block);
        } else {
            if (!emptyContainers.contains(block)) {
                emptyContainers.add(block);
            }
        }
    }

    public static Collection<StorageContainer> getStorageChests(byte[] serializedStorageItem) {
        Collection<StorageContainer> storageContainers = new ArrayList<>();

        if (serializedStorageItem != null) {
            ItemStack storageItem = ItemStackUtils.deserializeItemStack(serializedStorageItem);
            ItemMeta meta = storageItem.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();

            String worldsString = container.get(Key.STORAGE_ITEM_WORLDS, PersistentDataType.STRING);
            if (worldsString != null) {
                List<UUID> worlds = Utils.getUUIDListFromString(worldsString);

                for (UUID worldUUID : worlds) {
                    NamespacedKey worldKey = Key.getKey(worldUUID.toString());
                    long[] chests = container.get(worldKey, PersistentDataType.LONG_ARRAY);
                    if (chests != null) {
                        for (long chest : chests) {
                            storageContainers.add(StorageContainer.getStorageContainer(worldUUID, chest));
                        }
                    }
                }
            }
        }

        return storageContainers;
    }

}
