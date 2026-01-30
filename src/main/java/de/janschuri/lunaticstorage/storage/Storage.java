package de.janschuri.lunaticstorage.storage;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticlib.platform.paper.utils.EventUtils;
import de.janschuri.lunaticlib.platform.paper.utils.ItemStackUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.external.LogBlock;
import de.janschuri.lunaticstorage.gui.StorageGUI;
import de.janschuri.lunaticstorage.utils.Utils;
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
    private static final Map<Block, Map<ItemStack, ArrayList<StorageContainer>>> preferredContainersMap = new HashMap<>();
    private static final Map<Block, Map<Material, ArrayList<StorageContainer>>> preferredContainersMapByMaterial = new HashMap<>();
    private static final Map<Block, ArrayList<Block>> emptyContainersMap = new HashMap<>();
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
        preferredContainersMap.computeIfAbsent(this.block, k -> new HashMap<>());
        preferredContainersMapByMaterial.computeIfAbsent(this.block, k -> new HashMap<>());
    }

    public static Storage getStorage(Block block) {

        Storage storage = new Storage(block);

        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        if (dataContainer.has(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
            ItemStack newStorageItem = ItemStackUtils.deserializeItemStack(dataContainer.get(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY));

            boolean update = false;

            if (storage.getStorageItem() == null) {
                if (newStorageItem != null) {
                    update = true;
                }
            } else {
                if (newStorageItem == null) {
                    update = true;
                } else {
                    if (!storage.getStorageItem().isSimilar(newStorageItem)) {
                        update = true;
                    }
                }
            }

            if (dataContainer.has(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                ItemStack newRangeItem = ItemStackUtils.deserializeItemStack(dataContainer.get(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY));

                if (storage.getRangeItem() == null) {
                    if (newRangeItem != null) {
                        update = true;
                    }
                } else {
                    if (newRangeItem == null) {
                        update = true;
                    } else {
                        if (!storage.getRangeItem().isSimilar(newRangeItem)) {
                            update = true;
                        }
                    }
                }

                if (update) {
                    rangeItems.put(block, newRangeItem);
                }
            }

            if (update) {
                storageItems.put(block, newStorageItem);
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
        preferredContainersMap.remove(block);
        preferredContainersMapByMaterial.remove(block);
    }

    private void setStorageContainerAmounts(int totalContainers) {
        storageContainerAmounts.put(block, totalContainers);
    }

    private void setStorageLoadedContainerAmounts(int loadedContainers) {
        storageLoadedContainerAmounts.put(block, loadedContainers);
    }

    private Map<ItemStack, ArrayList<StorageContainer>> getPreferredContainers() {
        return preferredContainersMap.get(block);
    }

    private Map<Material, ArrayList<StorageContainer>> getPreferredContainersByMaterial() {
        return preferredContainersMapByMaterial.get(block);
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

    public int getLoadedContainersAmount() {
        return storageLoadedContainerAmounts.get(block);
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
        boolean update = false;

        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        if (item == null) {
            if (dataContainer.has(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                update = true;
            }
            dataContainer.remove(Key.RANGE_ITEM);
        } else {
            if (dataContainer.has(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                ItemStack oldItem = ItemStackUtils.deserializeItemStack(dataContainer.get(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY));
                if (!oldItem.isSimilar(item)) {
                    update = true;
                }
            } else {
                update = true;
            }
        }

        saveRangeItem(item);

        if (update) {
            loadStorage();
        }
        StorageGUI.updateStorageGUIs(block);
    }

    private void saveRangeItem(ItemStack item) {
        rangeItems.put(block, item);

        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        if (item == null) {
            dataContainer.remove(Key.RANGE_ITEM);
        } else {
            dataContainer.set(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY, ItemStackUtils.serializeItemStack(item));
        }
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
        Collection<StorageContainer> chests = Utils.getStorageChests(getStorageItem());
        long itemRange = Utils.getRangeFromItem(getRangeItem());
        long panelRange = Utils.getRangeFromBlock(block);
        long range = Math.max(itemRange, panelRange);
        getStorageMap().clear();
        getItemsContainers().clear();
        getEmptyContainers().clear();
        getPreferredContainers().clear();
        getPreferredContainersByMaterial().clear();

        int loadedContainers = 0;
        int totalContainers = 0;

        List<StorageContainer> invalidContainers = new ArrayList<>();

        for (StorageContainer container : chests) {
            if (!container.isValid()) {
                invalidContainers.add(container);
                continue;
            }
            totalContainers++;

            if (Utils.isInRange(block.getLocation(), container.getBlock().getLocation(), range) || range == -1) {
                loadedContainers++;
                container.addStorageId(block);
                Inventory chestInv = container.getInventory();

                addContainerWhitelist(container);

                addInventoryToMap(chestInv, container);
            } else {
                container.removeStorageId(container.getBlock());
            }
        }

        removeContainerFromStorageItem(invalidContainers.toArray(new StorageContainer[0]));
        setStorageLoadedContainerAmounts(loadedContainers);
        setStorageContainerAmounts(totalContainers);
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

        for (StorageContainer container : containers) {
            Utils.removeContainerFromStorageItem(container, getStorageItem());
        }

        saveStorageItem(getStorageItem());
    }

    public ItemStack getItemsFromStorage(ItemStack item, Player player) {
        return getItemsFromStorage(item, player, item.getMaxStackSize());
    }

    public ItemStack getItemsFromStorage(ItemStack item, Player player, int stackSize) {
        ItemStack searchedItem = item.clone();

        List<StorageContainer> nonFullstackContainers = new ArrayList<>();

        if (getItemsContainers().get(searchedItem) != null) {
            nonFullstackContainers = getItemsContainers().get(searchedItem).entrySet().stream()
                    .filter(entry -> !entry.getValue())
                    .map(entry -> StorageContainer.getStorageContainer(entry.getKey()))
                    .toList();
        }

        int foundItems = 0;

        List<StorageContainer> invalidContainers = new ArrayList<>();

        foundItems = getFoundItems(player, searchedItem, stackSize, foundItems, invalidContainers, nonFullstackContainers);

        if (foundItems != stackSize) {

            List<StorageContainer> fullstackContainers = new ArrayList<>();

            if (getItemsContainers().get(searchedItem) != null) {
                fullstackContainers = getItemsContainers().get(searchedItem).entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .map(entry -> StorageContainer.getStorageContainer(entry.getKey()))
                        .toList();
            }

            foundItems = getFoundItems(player, searchedItem, stackSize, foundItems, invalidContainers, fullstackContainers);
        }

        removeContainerFromStorageItem(invalidContainers.toArray(new StorageContainer[0]));
        updateStorageMap(searchedItem, -(foundItems));

        searchedItem.setAmount(foundItems);

        return searchedItem;
    }

    private int getFoundItems(Player player, ItemStack searchedItem, int stackSize, int foundItems, List<StorageContainer> invalidContainers, List<StorageContainer> fullstackContainers) {
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

            if (chestInv == null) {
                continue;
            }

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
        return foundItems;
    }

    public ItemStack insertItemsIntoStorage(ItemStack item, Player player) {

        ItemStack remainingItems = item.clone();
        ItemStack itemKey = remainingItems.clone();
        itemKey.setAmount(1);

        List<StorageContainer> invalidContainers = new ArrayList<>();

        List<StorageContainer> preferredContainers = new ArrayList<>();
        if (getPreferredContainers().get(itemKey) != null) {
            preferredContainers = getPreferredContainers().get(itemKey)
                    .stream()
                    .filter(storageContainer -> storageContainer.isOnWhitelist(item))
                    .filter(StorageContainer::isValid)
                    .toList();

            getPreferredContainers().put(itemKey, new ArrayList<>(preferredContainers));
        }


        remainingItems = insertAndGetOverflow(player, remainingItems, itemKey, preferredContainers, invalidContainers);

        if (remainingItems.getAmount() != 0) {
            List<StorageContainer> preferredContainersByMaterial = new ArrayList<>();
            if (getPreferredContainersByMaterial().get(itemKey.getType()) != null) {
                preferredContainersByMaterial = getPreferredContainersByMaterial().get(itemKey.getType())
                                .stream()
                                .filter(storageContainer -> storageContainer.isOnWhitelist(itemKey.getType()))
                                .filter(StorageContainer::isValid)
                                .toList();

                getPreferredContainersByMaterial().put(itemKey.getType(), new ArrayList<>(preferredContainersByMaterial));
            }

            remainingItems = insertAndGetOverflow(player, remainingItems, itemKey, preferredContainersByMaterial, invalidContainers);
        }

        if (remainingItems.getAmount() != 0) {
            List<StorageContainer> containersWithItem = new ArrayList<>();
            if (getItemsContainers().get(itemKey) != null) {
                containersWithItem = getItemsContainers().get(itemKey).entrySet().stream()
                        .filter(entry -> !entry.getValue())
                        .map(entry -> StorageContainer.getStorageContainer(entry.getKey()))
                        .toList();
            }

            remainingItems = insertAndGetOverflow(player, remainingItems, itemKey, containersWithItem, invalidContainers);
        }

        if (remainingItems.getAmount() != 0 && !getEmptyContainers().isEmpty()) {
            List<StorageContainer> emptyChests = getEmptyContainers().stream()
                    .map(StorageContainer::getStorageContainer)
                    .toList();

            remainingItems = insertAndGetOverflow(player, remainingItems, itemKey, emptyChests, invalidContainers);
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

    private ItemStack insertAndGetOverflow(Player player, ItemStack remainingItems, ItemStack itemKey, List<StorageContainer> containers, List<StorageContainer> invalidContainers) {
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

        addContainerWhitelist(container);
    }

    private void addContainerWhitelist(StorageContainer container) {
        Map<ItemStack, Boolean> whitelist = container.getWhitelist();

        if (whitelist != null) {
            for (Map.Entry<ItemStack, Boolean> entry : whitelist.entrySet()) {
                if (entry.getValue()) {
                    Map<ItemStack, ArrayList<StorageContainer>> preferredContainers = getPreferredContainers();
                    ItemStack itemKey = entry.getKey().clone();
                    itemKey.setAmount(1);
                    ArrayList<StorageContainer> containers = preferredContainers.computeIfAbsent(itemKey, k -> new ArrayList<>());
                    if (!containers.contains(container)) {
                        containers.add(container);
                    }
                    getPreferredContainers().put(entry.getKey(), containers);
                } else {
                    Map<Material, ArrayList<StorageContainer>> preferredContainers = getPreferredContainersByMaterial();
                    List<StorageContainer> containers = preferredContainers.computeIfAbsent(entry.getKey().getType(), k -> new ArrayList<>());
                    if (!containers.contains(container)) {
                        containers.add(container);
                    }
                    getPreferredContainersByMaterial().put(entry.getKey().getType(), new ArrayList<>(containers));
                }
            }
        }
    }

    public void addPreferredContainer(StorageContainer container, ItemStack item) {
        Map<ItemStack, ArrayList<StorageContainer>> preferredContainers = getPreferredContainers();
        ArrayList<StorageContainer> containers = preferredContainers.computeIfAbsent(item, k -> new ArrayList<>());
        if (!containers.contains(container)) {
            containers.add(container);
        }
        getPreferredContainers().put(item, containers);
    }

    public void addPreferredContainer(StorageContainer container, Material item) {
        Map<Material, ArrayList<StorageContainer>> preferredContainers = getPreferredContainersByMaterial();
        List<StorageContainer> containers = preferredContainers.computeIfAbsent(item, k -> new ArrayList<>());
        if (!containers.contains(container)) {
            containers.add(container);
        }
        getPreferredContainersByMaterial().put(item, new ArrayList<>(containers));
    }

    public ItemStack insertStorageItem(ItemStack item, boolean swapItems) {

        if (!Utils.isStorageItem(item) && !item.getType().equals(Material.AIR)) {
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
