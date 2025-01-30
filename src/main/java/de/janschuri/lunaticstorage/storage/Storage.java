package de.janschuri.lunaticstorage.storage;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.gui.StorageGUI;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import de.janschuri.lunaticlib.platform.bukkit.external.LogBlock;
import de.janschuri.lunaticlib.platform.bukkit.util.EventUtils;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class Storage {

    private static final Map<Block, Map<ItemStack, Integer>> storageMaps = new HashMap<>();
    private static final Map<Block, Map<ItemStack, Map<Block, Boolean>>> itemsContainersMap = new HashMap<>();
    private static final Map<Block, List<Block>> emptyContainersMap = new HashMap<>();
    private static final Map<Block, ItemStack> storageItems = new HashMap<>();
    private static final Map<Block, ItemStack> rangeItems = new HashMap<>();
    private static final Map<Block, Integer> storageLoadedContainerAmounts = new HashMap<>();
    private static final Map<Block, Integer> storageContainerAmounts = new HashMap<>();


    private final Block block;

    private Storage (Block block) {
        this.block = block;
        storageMaps.computeIfAbsent(this.block, k -> new HashMap<>());
        itemsContainersMap.computeIfAbsent(this.block, k -> new HashMap<>());
        emptyContainersMap.computeIfAbsent(this.block, k -> new ArrayList<>());
        storageItems.computeIfAbsent(this.block, k -> null);
        rangeItems.computeIfAbsent(this.block, k -> null);
        storageLoadedContainerAmounts.computeIfAbsent(this.block, k -> 0);
        storageContainerAmounts.computeIfAbsent(this.block, k -> 0);
    }

    public static Storage getStorage(Block block) {

        Storage storage = new Storage(block);

            PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
            if (dataContainer.has(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                ItemStack storageItem = ItemStackUtils.deserializeItemStack(dataContainer.get(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY));

                boolean update = false;

                if (storageItems.get(block) == null) {
                    if (storageItem != null) {
                        Logger.debugLog("Storage item added");
                        update = true;
                    }
                } else {
                    if (storageItem == null) {
                        Logger.debugLog("Storage item removed");
                        update = true;
                    } else {
                        if (!storageItems.get(block).isSimilar(storageItem)) {
                            Logger.debugLog("Storage item changed");
                            update = true;
                        }
                    }
                }

                if (dataContainer.has(Key.RANGE, PersistentDataType.BYTE_ARRAY)) {
                    ItemStack rangeItem = ItemStackUtils.deserializeItemStack(dataContainer.get(Key.RANGE, PersistentDataType.BYTE_ARRAY));
                    if (rangeItems.get(block) == null) {
                        if (rangeItem != null) {
                            Logger.debugLog("Range item added");
                            update = true;
                        }
                    } else {
                        if (rangeItem == null) {
                            Logger.debugLog("Range item removed");
                            update = true;
                        } else {
                            if (!rangeItems.get(block).isSimilar(rangeItem)) {
                                Logger.debugLog("Range item changed");
                                update = true;
                            }
                        }
                    }
                }

                if (update) {
                    storageItems.put(block, storageItem);
                    storage.loadStorage();
                }
            }

            return storage;
    }

    public static void removeStorage(Block block) {
        storageMaps.remove(block);
        itemsContainersMap.remove(block);
        emptyContainersMap.remove(block);
        storageItems.remove(block);
        rangeItems.remove(block);
        storageLoadedContainerAmounts.remove(block);
        storageContainerAmounts.remove(block);
    }

    public ItemStack getStorageItem() {
        return storageItems.get(block);
    }

    public ItemStack getRangeItem() {
        return rangeItems.get(block);
    }

    public int getContainerAmount() {
        return storageContainerAmounts.get(block);
    }

    private void setContainersAmount(int amount) {
        storageContainerAmounts.put(block, amount);
    }

    public int getLoadedContainersAmount() {
        return storageLoadedContainerAmounts.get(block);
    }

    private void setLoadedContainersAmount(int amount) {
        storageLoadedContainerAmounts.put(block, amount);
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
        boolean update = false;

        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        if (item == null) {
            if (dataContainer.has(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                update = true;
            }
        } else {
            if (dataContainer.has(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                ItemStack oldItem = ItemStackUtils.deserializeItemStack(dataContainer.get(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY));
                if (!oldItem.isSimilar(item)) {
                    update = true;
                }
            } else {
                update = true;
            }
        }

        saveStorageItem(item);

        if (update) {
            Logger.debugLog("Storage item changed, updating storage");
            loadStorage();
        }
        StorageGUI.updateStorageGUIs(block);
    }

    private void saveStorageItem(ItemStack item) {
        storageItems.put(block, item);

        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        if (item == null) {
            dataContainer.remove(Key.STORAGE_ITEM);
        } else {
            dataContainer.set(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY, ItemStackUtils.serializeItemStack(item));
        }
    }

    public void setRangeItem(ItemStack item) {
        rangeItems.put(block, item);
        boolean update = false;

        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        if (item == null) {
            if (dataContainer.has(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                Logger.debugLog("Range item removed");
                update = true;
            }
            dataContainer.remove(Key.RANGE_ITEM);
        } else {
            if (dataContainer.has(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                ItemStack oldItem = ItemStackUtils.deserializeItemStack(dataContainer.get(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY));
                if (!oldItem.isSimilar(item)) {
                    Logger.debugLog("Range item changed");
                    update = true;
                }
            } else {
                Logger.debugLog("Range item added");
                update = true;
            }
            dataContainer.set(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY, ItemStackUtils.serializeItemStack(item));
        }

        if (update) {
            Logger.debugLog("Range item changed, updating storage");
            loadStorage();
        }
        StorageGUI.updateStorageGUIs(block);
    }

    public List<Map.Entry<ItemStack, Integer>> getStorageList() {
        return new ArrayList<>(getStorageMap().entrySet());
    }

    public int getTotalAmount() {
        int sum = 0;
        for (int value : getStorageMap().values()) {
            sum += value;
        }
        return sum;
    }

    public long getRange() {
        long itemRange = Utils.getRangeFromItem(getRangeItem());
        long panelRange = Utils.getRangeFromBlock(block);
        return Math.max(itemRange, panelRange);
    }

    public void updateStorageMap(ItemStack item, int difference) {
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

    public boolean addInventoryToMap(Inventory inventory, StorageContainer container) {
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

        return true;
    }

    public void loadStorage () {
        Logger.debugLog("Loading storage");
        Collection<StorageContainer> chests = Utils.getStorageChests(getStorageItem());
        long itemRange = Utils.getRangeFromItem(getRangeItem());
        long panelRange = Utils.getRangeFromBlock(block);
        long range = Math.max(itemRange, panelRange);
        getStorageMap().clear();
        getItemsContainers().clear();
        getEmptyContainers().clear();

        int loadedContainers = 0;
        int totalContainers = 0;

        for (StorageContainer container : chests) {
            if (!container.isValid()) {
                removeContainerFromStorageItem(container);
                continue;
            }
            totalContainers++;

            if (Utils.isInRange(block.getLocation(), container.getBlock().getLocation(), range) || range == -1) {
                loadedContainers++;
                container.addStorageId(block);
                Inventory chestInv = container.getInventory();
                addInventoryToMap(chestInv, container);
            } else {
                Logger.debugLog("Unload storage container"
                        + " " + container.getBlock().getLocation().getBlockX()
                        + " " + container.getBlock().getLocation().getBlockY()
                        + " " + container.getBlock().getLocation().getBlockZ()
                );
                container.removeStorageId(container.getBlock());
            }
        }

        setLoadedContainersAmount(loadedContainers);
        setContainersAmount(totalContainers);
    }

    public void updateStorage(Map<ItemStack, Integer> difference) {
            for (Map.Entry<ItemStack, Integer> entry : difference.entrySet()) {
                updateStorageMap(entry.getKey(), entry.getValue());
            }
    }
    private void removeContainerFromStorageItem(StorageContainer... containers) {
        if (containers.length == 0) {
            return;
        }

        Logger.debugLog("Removing storage container from storage item");

        for (StorageContainer container : containers) {
            Utils.removeContainerFromStorageItem(container, getStorageItem());
        }

        saveStorageItem(getStorageItem());
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

        List<StorageContainer> invalidContainers = new ArrayList<>();

        for (StorageContainer container: nonFullstackContainers) {
            if (foundItems == stackSize) {
                break;
            }
            if (!container.isValid()) {
                invalidContainers.add(container);
                continue;
            }

            Block block = container.getBlock();

            Inventory chestInv = container.getInventory();

            if (chestInv == null) {
                continue;
            }

            if (!container.isAllowedTakeItem(player)) {
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
                    container.getInventory().removeItem(i);
                    i.setAmount(i.getAmount() - amountNeeded);
                    container.getInventory().addItem(i);
                    container.update();
                    foundItems = foundItems + amountNeeded;

                    ItemStack itemStack = i.clone();
                    itemStack.setAmount(amountNeeded);

                    LogBlock.logChestRemove(player, block, itemStack);

                } else if (amountNeeded == amount) {
                    container.getInventory().removeItem(i);
                    container.update();
                    foundItems = foundItems + amount;

                    ItemStack itemStack = i.clone();
                    itemStack.setAmount(amountNeeded);

                    LogBlock.logChestRemove(player, block, itemStack);

                } else {
                    container.getInventory().removeItem(i);
                    container.update();
                    foundItems = foundItems + amount;

                    ItemStack itemStack = i.clone();
                    itemStack.setAmount(amount);

                    LogBlock.logChestRemove(player, block, itemStack);

                }
            }
            updateContainer(container, searchedItem);
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
                if (!container.isValid()) {
                    invalidContainers.add(container);
                    continue;
                }

                Block block = container.getBlock();

                Inventory chestInv = container.getInventory();

                if (!container.isAllowedTakeItem(player)) {
                    continue;
                }

                for (ItemStack i : chestInv.getContents()) {
                    if (foundItems == stackSize) {
                        updateContainer(container, searchedItem);
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
                        container.getInventory().removeItem(i);
                        i.setAmount(i.getAmount() - amountNeeded);
                        container.getInventory().addItem(i);
                        container.update();
                        foundItems = foundItems + amountNeeded;

                        ItemStack itemStack = i.clone();
                        itemStack.setAmount(amountNeeded);

                        LogBlock.logChestRemove(player, block, itemStack);

                    } else if (amountNeeded == amount) {
                        container.getInventory().removeItem(i);
                        container.update();
                        foundItems = foundItems + amount;

                        ItemStack itemStack = i.clone();
                        itemStack.setAmount(amountNeeded);

                        LogBlock.logChestRemove(player, block, itemStack);

                    } else {
                        container.getInventory().removeItem(i);
                        container.update();
                        foundItems = foundItems + amount;

                        ItemStack itemStack = i.clone();
                        itemStack.setAmount(amount);

                        LogBlock.logChestRemove(player, block, itemStack);
                    }
                }
                updateContainer(container, searchedItem);
            }
        }

        removeContainerFromStorageItem(invalidContainers.toArray(new StorageContainer[0]));
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

        List<StorageContainer> invalidContainers = new ArrayList<>();

        for (StorageContainer container : containers) {
            if (remainingItems.getAmount() == 0 || remainingItems.getType() == Material.AIR) {
                break;
            }
            if (!container.isValid()) {
                invalidContainers.add(container);
                continue;
            }

            if (!container.isAllowedPutItem(player, remainingItems)) {
                continue;
            }

            Block block = container.getBlock();

            Inventory chestInv = container.getInventory();

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

            updateContainer(container, itemKey);
        }

        if (remainingItems.getAmount() != 0 && !getEmptyContainers().isEmpty()) {

            List<StorageContainer> emptyChests = getEmptyContainers().stream()
                    .map(StorageContainer::getStorageContainer)
                    .toList();

            for (StorageContainer container : emptyChests) {
                if (remainingItems.getAmount() == 0 || remainingItems.getType() == Material.AIR) {
                    break;
                }
                if (!container.isValid()) {
                    invalidContainers.add(container);
                    continue;
                }

                if (!container.isAllowedPutItem(player, remainingItems)) {
                    continue;
                }

                Block block = container.getBlock();

                Inventory chestInv = container.getInventory();

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

                updateContainer(container, itemKey);
            }
        }

        int amount = 0;

        if (remainingItems.getType() != Material.AIR) {
            amount = item.getAmount() - remainingItems.getAmount();
        } else {
            amount = item.getAmount();
        }

        removeContainerFromStorageItem(invalidContainers.toArray(new StorageContainer[0]));
        updateStorageMap(item, amount);

        return remainingItems;
    }
    public void updateContainer(StorageContainer container, ItemStack... itemKeys) {
        Map<Block, Boolean> itemsChests = new HashMap<>();
        Block block = container.getBlock();
        Inventory containerInv = container.getInventory();

        for (ItemStack itemKey : itemKeys) {
            if (getItemsContainers().get(itemKey) != null) {
                itemsChests = getItemsContainers().get(itemKey);
            }

            if (containerInv != null && containerInv.containsAtLeast(itemKey, 1)) {

                itemsChests.put(block, true);

                for (ItemStack item : containerInv.getContents()) {
                    if (item != null && item.isSimilar(itemKey) && item.getAmount() != item.getMaxStackSize()) {
                        itemsChests.put(block, false);
                        break;
                    }
                }

            } else {
                itemsChests.remove(block);
            }

            getItemsContainers().put(itemKey, itemsChests);
        }

        if (containerInv == null || containerInv.firstEmpty() == -1) {
            getEmptyContainers().remove(block);
        } else {
            if (!getEmptyContainers().contains(block)) {
                getEmptyContainers().add(block);
            }
        }
    }

    public ItemStack insertStorageItem(ItemStack item, boolean swapItems) {

        if (!Utils.isStorageItem(item) && !item.getType().equals(Material.AIR)) {
            Logger.debugLog("insertStorageItem: not storage item");
            return item;
        }

        ItemStack result;
        ItemStack storageItem;

        if (getStorageItem() == null) {
            storageItem = item.clone();
            result = new ItemStack(Material.AIR);
        } else {
            storageItem = getStorageItem().clone();

            if (storageItem.isSimilar(item) && item.getType() != Material.AIR) {
                if (storageItem.getMaxStackSize() > storageItem.getAmount()) {
                    int itemAmount = item.getAmount();
                    int storageItemAmount = storageItem.getAmount();
                    int totalAmount = itemAmount + storageItemAmount;
                    if (totalAmount <= storageItem.getMaxStackSize()) {
                        storageItem.setAmount(totalAmount);
                        result = new ItemStack(Material.AIR);
                    } else {
                        storageItem.setAmount(storageItem.getMaxStackSize());
                        int newItemAmount = totalAmount - storageItem.getMaxStackSize();
                        item.setAmount(newItemAmount);
                        result = item.clone();
                    }
                } else {
                    result = item;
                }
            } else {
                if (swapItems) {
                    result = storageItem.clone();
                    storageItem = item.clone();
                } else {
                    result = item.clone();
                }
            }
        }

        if (!storageItem.getType().isAir()) {
            setStorageItem(storageItem);
        } else {
            setStorageItem(null);
        }

        return result;
    }

    public ItemStack insertRangeItem(ItemStack item, boolean swapItems) {
        if (!Utils.isRangeItem(item) && !item.getType().equals(Material.AIR)) {
            Logger.debugLog("insertRangeItem: not range item");
            return item;
        }

        ItemStack result;
        ItemStack rangeItem;

        if (getRangeItem() == null) {
            rangeItem = item.clone();
            result = new ItemStack(Material.AIR);
        } else {
            rangeItem = getRangeItem().clone();

            if (rangeItem.isSimilar(item) && item.getType() != Material.AIR) {
                if (rangeItem.getMaxStackSize() > rangeItem.getAmount()) {
                    int itemAmount = item.getAmount();
                    int rangeItemAmount = rangeItem.getAmount();
                    int totalAmount = itemAmount + rangeItemAmount;
                    if (totalAmount <= rangeItem.getMaxStackSize()) {
                        rangeItem.setAmount(totalAmount);
                        result = new ItemStack(Material.AIR);
                    } else {
                        rangeItem.setAmount(rangeItem.getMaxStackSize());
                        int newItemAmount = totalAmount - rangeItem.getMaxStackSize();
                        item.setAmount(newItemAmount);
                        result = item.clone();
                    }
                } else {
                    result = item;
                }
            } else {
                if (swapItems) {
                    result = rangeItem.clone();
                    rangeItem = item.clone();
                } else {
                    result = item.clone();
                }
            }
        }

        if (rangeItem.getType() != Material.AIR) {
            setRangeItem(rangeItem);
        } else {
            setRangeItem(null);
        }

        return result;
    }
}
