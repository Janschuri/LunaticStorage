package de.janschuri.lunaticstorage.storage;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticlib.platform.bukkit.util.BlockUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.gui.StorageGUI;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import de.janschuri.lunaticlib.platform.bukkit.external.LogBlock;
import de.janschuri.lunaticlib.platform.bukkit.util.EventUtils;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
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

    private static final List<Block> storages = new ArrayList<>();

    private static final Map<Block, Map<ItemStack, Integer>> storageMaps = new HashMap<>();
    private static final Map<Block, Map<ItemStack, Map<Block, Boolean>>> itemsContainersMap = new HashMap<>();
    private static final Map<Block, List<Block>> emptyContainersMap = new HashMap<>();
    private static final Map<Block, ItemStack> storageItems = new HashMap<>();

    private final Block block;

    private Storage (Block block) {
        this.block = block;
        storageMaps.computeIfAbsent(this.block, k -> new HashMap<>());
        itemsContainersMap.computeIfAbsent(this.block, k -> new HashMap<>());
        emptyContainersMap.computeIfAbsent(this.block, k -> new ArrayList<>());
        storageItems.computeIfAbsent(this.block, k -> null);
    }

    public static Storage getStorage(Block block) {

        Storage storage = new Storage(block);

            PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
            if (dataContainer.has(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                ItemStack storageItem = ItemStackUtils.deserializeItemStack(dataContainer.get(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY));

                boolean update = false;

                if (storageItems.get(block) == null) {
                    if (storageItem != null) {
                        update = true;
                    }
                } else {
                    if (!storageItems.get(block).isSimilar(storageItem)) {
                        update = true;
                    }
                }

                if (update) {
                    storageItems.put(block, storageItem);
                    storage.loadStorage();
                }
            }

            return storage;
    }

    public ItemStack getStorageItem() {
        return storageItems.get(block);
    }

    public Map<ItemStack, Integer> getStorageMap() {
        return storageMaps.get(block);
    }

    public Map<ItemStack, Map<Block, Boolean>> getItemsContainers() {
        return itemsContainersMap.get(block);
    }

    public List<Block> getEmptyContainers() {
        return emptyContainersMap.get(block);
    }

    public void setStorageItem(ItemStack item) {
        storageItems.put(block, item);

        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        if (item == null) {
            dataContainer.remove(Key.STORAGE_ITEM);
        } else {
            dataContainer.set(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY, ItemStackUtils.serializeItemStack(item));
        }
        loadStorage();
        StorageGUI.updateStorageGUIs(block);
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




        storageList = getStorageMap().entrySet().stream()
                .filter(filter)
                .sorted(comparator)
                .collect(Collectors.toList());

        return storageList;
    }

    public int getTotalAmount() {
        int sum = 0;
        for (int value : getStorageMap().values()) {
            sum += value;
        }
        // Formatting the sum into a string with comma as thousands separator
        return sum;
    }

    public void updateStorageMap(ItemStack item, int difference) {
        Logger.debugLog("Updating storage map for " + block + " with " + item + " and " + difference);
        ItemStack clone = item.clone();
        clone.setAmount(1);

        int oldAmount = 0;
        if (getStorageMap().containsKey(clone)) {
            oldAmount = getStorageMap().get(clone);
        }

        if(oldAmount+difference == 0) {
            getStorageMap().remove(clone);
        } else {
            getStorageMap().put(clone, oldAmount + difference);
        }

        StorageGUI.updateStorageGUIs(block);
    }

    public void addInventoryToMap(Inventory inventory, StorageContainer container) {
        boolean empty = false;
        Block block = container.getBlock();

        for (ItemStack item : inventory.getContents()) {
            if (item != null) {
                ItemStack clone = item.clone();
                clone.setAmount(1);

                boolean found = false;
                for (Map.Entry<ItemStack, Integer> entry : getStorageMap().entrySet()) {
                    ItemStack existingItem = entry.getKey();
                    if (existingItem.isSimilar(clone)) {
                        int amount = entry.getValue();
                        getStorageMap().put(existingItem, amount + item.getAmount());
                            Map<Block, Boolean> itemsChests = getItemsContainers().get(existingItem);

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

                            getItemsContainers().put(existingItem, itemsChests);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    getStorageMap().put(clone, item.getAmount());

                    Map<Block, Boolean> itemsChests = new HashMap<>();
                    if(item.getAmount() == item.getMaxStackSize()) {
                        itemsChests.put(block, true);
                    } else {
                        itemsChests.put(block, false);
                    }
                    getItemsContainers().put(clone, itemsChests);
                }


            } else {
                empty = true;
            }
        }

        if (empty) {
            getEmptyContainers().add(block);
        }

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
    public void loadStorage () {
        Logger.debugLog("Loading storage for " + block);
        Collection<StorageContainer> chests = getStorageChests(getStorageItem());
        getStorageMap().clear();
        getItemsContainers().clear();
        getEmptyContainers().clear();

        for (StorageContainer container : chests) {
                container.addStorageId(block);
                Inventory chestInv = container.getSnapshotInventory();
                addInventoryToMap(chestInv, container);
        }
    }

    public void updateStorage(Map<ItemStack, Integer> difference) {
            for (Map.Entry<ItemStack, Integer> entry : difference.entrySet()) {
                updateStorageMap(entry.getKey(), entry.getValue());
            }
    }

    public ItemStack getItemsFromStorage(ItemStack item, Player player) {
        ItemStack searchedItem = item.clone();

        List<StorageContainer> nonFullstackContainers = new ArrayList<>();

        if (getItemsContainers().get(searchedItem) != null) {
            nonFullstackContainers = getItemsContainers().get(searchedItem).entrySet().stream()
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

            if (getItemsContainers().get(searchedItem) != null) {
                fullstackContainers = getItemsContainers().get(searchedItem).entrySet().stream()
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
                        updateContainer(container, chestInv, searchedItem);
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

        return searchedItem;
    }
    public ItemStack insertItemsIntoStorage(ItemStack item, Player player) {

        ItemStack remainingItems = item.clone();
        ItemStack itemKey = remainingItems.clone();
        itemKey.setAmount(1);

        List<StorageContainer> containers = new ArrayList<>();
        if (getItemsContainers().get(itemKey) != null) {
            containers = getItemsContainers().get(itemKey).entrySet().stream()
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

        if (remainingItems.getAmount() != 0 && !getEmptyContainers().isEmpty()) {

            List<StorageContainer> emptyChests = getEmptyContainers().stream()
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

        return remainingItems;
    }
    public void updateContainer(StorageContainer container, Inventory containerInv, ItemStack itemKey) {
        Map<Block, Boolean> itemsChests = new HashMap<>();
        Block block = container.getBlock();

        if (getItemsContainers().get(itemKey) != null) {
            itemsChests = getItemsContainers().get(itemKey);
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

        getItemsContainers().put(itemKey, itemsChests);

        if (containerInv.firstEmpty() == -1) {
            getEmptyContainers().remove(block);
        } else {
            if (!getEmptyContainers().contains(block)) {
                getEmptyContainers().add(block);
            }
        }
    }

    public static Collection<StorageContainer> getStorageChests(ItemStack storageItem) {
        Collection<StorageContainer> storageContainers = new ArrayList<>();

        if (storageItem != null) {
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
