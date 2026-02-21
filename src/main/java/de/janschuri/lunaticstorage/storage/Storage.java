package de.janschuri.lunaticstorage.storage;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticlib.platform.paper.utils.EventUtils;
import de.janschuri.lunaticlib.platform.paper.utils.ItemStackUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.external.LogBlock;
import de.janschuri.lunaticstorage.gui.StorageGUI;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static de.janschuri.lunaticstorage.LunaticStorage.sendDebugMessage;

public class Storage {

    private static final Map<Block, BukkitTask> storageLoadTasks = new HashMap<>();
    private static final Map<Block, Boolean> canLoadStorageMap = new HashMap<>();
    private static final Map<Block, Map<ItemStack, Integer>> storageMaps = new HashMap<>();
    private static final Map<Block, Map<ItemStack, Map<Block, Boolean>>> itemsContainersMap = new HashMap<>();
    private static final Map<Block, Map<ItemStack, ArrayList<StorageContainer>>> preferredContainersMap = new HashMap<>();
    private static final Map<Block, Map<Material, ArrayList<StorageContainer>>> preferredContainersMapByMaterial = new HashMap<>();
    private static final Map<Block, ArrayList<Block>> emptyContainersMap = new HashMap<>();
    private static final Map<Block, ItemStack> storageItems = new HashMap<>();
    private static final Map<Block, ItemStack> rangeItems = new HashMap<>();
    private static final Map<Block, Integer> storageLoadedContainerAmounts = new HashMap<>();
    private static final Map<Block, Integer> storageContainerAmounts = new HashMap<>();

    private static final Map<String, ItemStack> deserializedItemStackCache = new HashMap<>();

    private final Block block;

    private Storage (Block block) {
        this.block = block;
    }

    private static ItemStack deserializeItemStack(byte[] data) {
        String hash = Arrays.toString(data);
        if (deserializedItemStackCache.containsKey(hash)) {
            return deserializedItemStackCache.get(hash).clone();
        } else {
            ItemStack itemStack = ItemStackUtils.deserializeItemStack(data);
            deserializedItemStackCache.put(hash, itemStack);
            return itemStack;
        }
    }

    public static Storage getStorage(Block block) {
        Storage storage = new Storage(block);

        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());

        if (!storageMaps.keySet().contains(block)) {
            storageMaps.computeIfAbsent(block, k -> new HashMap<>());
            itemsContainersMap.computeIfAbsent(block, k -> new HashMap<>());
            emptyContainersMap.computeIfAbsent(block, k -> new ArrayList<>());
            storageItems.computeIfAbsent(block, k -> null);
            rangeItems.computeIfAbsent(block, k -> null);
            storageLoadedContainerAmounts.computeIfAbsent(block, k -> 0);
            storageContainerAmounts.computeIfAbsent(block, k -> 0);
            preferredContainersMap.computeIfAbsent(block, k -> new HashMap<>());
            preferredContainersMapByMaterial.computeIfAbsent(block, k -> new HashMap<>());
            canLoadStorageMap.computeIfAbsent(block, k -> true);


            if (dataContainer.has(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                ItemStack storageItem = deserializeItemStack(dataContainer.get(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY));
                storage.storageItems.put(block, storageItem);
            } else {
                storage.storageItems.put(block, null);
            }

            if (dataContainer.has(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY)) {
                ItemStack rangeItem = deserializeItemStack(dataContainer.get(Key.RANGE_ITEM, PersistentDataType.BYTE_ARRAY));
                storage.rangeItems.put(block, rangeItem);
            } else {
                storage.rangeItems.put(block, null);
            }

            storage.loadStorage();
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

    private Boolean canLoadStorage(Block block) {
        return canLoadStorageMap.get(block);
    }

    private BukkitTask getLoadTask() {
        return storageLoadTasks.get(block);
    }

    private void setLoadTask(BukkitTask task) {
        storageLoadTasks.put(block, task);
    }

    private void setCanLoadStorage(Block block, Boolean canLoad) {
        canLoadStorageMap.put(block, canLoad);
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

    public ArrayList<StorageContainer> getPreferredContainersByMaterial(Material material) {
        return preferredContainersMapByMaterial.get(block).getOrDefault(material, new ArrayList<>());
    }

    public ArrayList<StorageContainer> getPreferredContainersForItem(ItemStack item) {
        return preferredContainersMap.get(block).getOrDefault(item, new ArrayList<>());
    }

    public List<Block> getEmptyContainers() {
        return emptyContainersMap.get(block);
    }

    public void saveStorageItem(ItemStack item) {
        storageItems.put(block, item);

        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        if (item == null) {
            dataContainer.remove(Key.STORAGE_ITEM);
        } else {
            dataContainer.set(Key.STORAGE_ITEM, PersistentDataType.BYTE_ARRAY, ItemStackUtils.serializeItemStack(item));
        }
    }

    public void saveRangeItem(ItemStack item) {
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
            int newAmount = (oldAmount + difference);

            if (newAmount < 0) {
                newAmount = 0;
            }

            getStorageMap().put(clone, newAmount);
        }

        StorageGUI.updateStorageGUIs(block);
    }

    public void updateStorageMap(Map<ItemStack, Integer> difference) {
        for (Map.Entry<ItemStack, Integer> entry : difference.entrySet()) {
            updateStorageMap(entry.getKey(), entry.getValue());
        }
    }

    public void cancelLoadStorage() {
        setCanLoadStorage(block, false);
        BukkitTask loadTask = getLoadTask();
        if (loadTask != null) {
            loadTask.cancel();
        }
    }

    public void loadStorage() {
        cancelLoadStorage();
        setCanLoadStorage(block, true);

        long start = System.currentTimeMillis();

        Collection<StorageContainer> chests = Utils.getStorageChests(getStorageItem());
        Iterator<StorageContainer> it = chests.iterator();

        long itemRange = Utils.getRangeFromItem(getRangeItem());
        long panelRange = Utils.getRangeFromBlock(block);
        long range = Math.max(itemRange, panelRange);

        getStorageMap().clear();
        getItemsContainers().clear();
        getEmptyContainers().clear();
        getPreferredContainers().clear();
        getPreferredContainersByMaterial().clear();

        List<StorageContainer> invalidContainers = new ArrayList<>();
        final int[] loaded = {0};
        final int[] total = {0};

        long maxTotalMillis = 5000;
        long maxMillisPerTick = 50;

        Bukkit.getScheduler().runTaskTimer(LunaticStorage.getInstance(), task -> {
            setLoadTask(task);

            if (System.currentTimeMillis() - start > maxTotalMillis || !canLoadStorage(block)) {
                Logger.error("Loading storage for block at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " timed out or was cancelled.");
                task.cancel();
                // optional: cleanup / final counters
                removeContainerFromStorageItem(invalidContainers.toArray(new StorageContainer[0]));
                setStorageLoadedContainerAmounts(loaded[0]);
                setStorageContainerAmounts(total[0]);
                return;
            }

            long tickStart = System.currentTimeMillis();

            while (it.hasNext() && (System.currentTimeMillis() - tickStart) < maxMillisPerTick) {
                StorageContainer container = it.next();

                if (!container.isValid()) {
                    invalidContainers.add(container);
                    continue;
                }

                total[0]++;

                if (Utils.isInRange(block.getLocation(), container.getBlock().getLocation(), range) || range == -1) {
                    loaded[0]++;
                    container.addStorageId(block);

                    updateStorageMap(container.getContainerMap());
                    updateContainer(container, container.getContainerMap().keySet().toArray(new ItemStack[0]));
                } else {
                    container.removeStorageId(container.getBlock());
                }
            }

            if (!it.hasNext()) {
                task.cancel();

                long timeTaken = System.currentTimeMillis() - start;
                Logger.info("Loaded storage for block at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " with " + loaded[0] + " / " + total[0] + " containers in " + timeTaken + " ms.");
                removeContainerFromStorageItem(invalidContainers.toArray(new StorageContainer[0]));
                setStorageLoadedContainerAmounts(loaded[0]);
                setStorageContainerAmounts(total[0]);
            }

            StorageGUI.updateStorageGUIs(block);

        }, 0L, 1L);
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

        sendDebugMessage(player, "Searching for item " + searchedItem.getType() + " in non-fullstack containers: " + nonFullstackContainers.size() + " containers found.");
        foundItems = getFoundItems(player, searchedItem, stackSize, foundItems, invalidContainers, nonFullstackContainers);

        if (foundItems != stackSize) {

            List<StorageContainer> fullstackContainers = new ArrayList<>();

            if (getItemsContainers().get(searchedItem) != null) {
                fullstackContainers = getItemsContainers().get(searchedItem).entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .map(entry -> StorageContainer.getStorageContainer(entry.getKey()))
                        .toList();
            }

            sendDebugMessage(player, "Searching for item " + searchedItem.getType() + " in fullstack containers: " + fullstackContainers.size() + " containers found.");
            foundItems = getFoundItems(player, searchedItem, stackSize, foundItems, invalidContainers, fullstackContainers);
        }

        removeContainerFromStorageItem(invalidContainers.toArray(new StorageContainer[0]));

        searchedItem.setAmount(foundItems);

        return searchedItem;
    }

    private int getFoundItems(Player player, ItemStack searchedItem, int stackSize, int foundItems, List<StorageContainer> invalidContainers, List<StorageContainer> containers) {
        if (containers.isEmpty()) {
            sendDebugMessage(player, "No containers with item " + searchedItem.getType() + " found. Stopping.");
            return foundItems;
        }

        for (StorageContainer container : containers) {
            sendDebugMessage(player, "Checking container at " + container.getBlock().getX() + ", " + container.getBlock().getY() + ", " + container.getBlock().getZ());
            if (foundItems == stackSize) {
                sendDebugMessage(player, "Found " + foundItems + " items, which is enough for the stack size of " + stackSize + ". Stopping search.");
                break;
            }
            if (!container.isValid()) {
                sendDebugMessage(player, "Container at " + container.getBlock().getX() + ", " + container.getBlock().getY() + ", " + container.getBlock().getZ() + " is not valid. Skipping.");
                invalidContainers.add(container);
                continue;
            }

            Block block = container.getBlock();

            Inventory chestInv = container.getInventory();

            if (chestInv == null) {
                sendDebugMessage(player, "Inventory is null. Skipping.");
                continue;
            }

            if (!container.isAllowedTakeItem(player)) {
                sendDebugMessage(player, "Player is not allowed to take items from container at " + container.getBlock().getX() + ", " + container.getBlock().getY() + ", " + container.getBlock().getZ() + ". Skipping.");
                continue;
            }

            for (ItemStack i : chestInv.getContents()) {
                if (foundItems == stackSize) {
                    sendDebugMessage(player, "Found " + foundItems + " items, which is enough for the stack size of " + stackSize + ". Stopping search.");
                    updateContainer(container, searchedItem);
                    break;
                }
                if (i == null) {
                    sendDebugMessage(player, "ItemStack is null. Skipping.");
                    continue;
                }
                if (!i.isSimilar(searchedItem)) {
                    sendDebugMessage(player, "ItemStack " + i.getType() + " is not similar to searched item " + searchedItem.getType() + ". Skipping.");
                    continue;
                }

                int amount = i.getAmount();
                int amountNeeded = stackSize - foundItems;

                if (amountNeeded < amount) {
                    sendDebugMessage(player, "ItemStack has " + amount + " items, but only " + amountNeeded + " are needed to reach the stack size of " + stackSize + ". Taking " + amountNeeded + " items.");
                    container.getInventory().removeItem(i);
                    i.setAmount(i.getAmount() - amountNeeded);
                    container.getInventory().addItem(i);
                    container.update();
                    foundItems = foundItems + amountNeeded;

                    ItemStack itemStack = i.clone();
                    itemStack.setAmount(amountNeeded);

                    container.updateContainerMap(searchedItem, -amount);
                    LogBlock.logChestRemove(player, block, itemStack);

                } else if (amountNeeded == amount) {
                    sendDebugMessage(player, "ItemStack has exactly " + amount + " items, which is exactly the amount needed to reach the stack size of " + stackSize + ". Taking all " + amountNeeded + " items.");
                    container.getInventory().removeItem(i);
                    container.update();
                    foundItems = foundItems + amount;

                    ItemStack itemStack = i.clone();
                    itemStack.setAmount(amountNeeded);

                    container.updateContainerMap(searchedItem, -amountNeeded);
                    LogBlock.logChestRemove(player, block, itemStack);

                } else {
                    sendDebugMessage(player, "ItemStack has " + amount + " items, which is less than the amount needed to reach the stack size of " + stackSize + ". Taking all " + amount + " items.");
                    container.getInventory().removeItem(i);
                    container.update();
                    foundItems = foundItems + amount;

                    ItemStack itemStack = i.clone();
                    itemStack.setAmount(amount);

                    container.updateContainerMap(searchedItem, -amount);
                    LogBlock.logChestRemove(player, block, itemStack);
                }
            }
            updateContainer(container, searchedItem);
        }
        sendDebugMessage(player, "Found " + foundItems + " items so far.");
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


        sendDebugMessage(player, "Inseting into preferred containers for item " + itemKey.getType() + ": " + preferredContainers.size() + " containers found.");
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

            sendDebugMessage(player, "Inseting into preferred containers for material " + itemKey.getType() + ": " + preferredContainersByMaterial.size() + " containers found.");
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

            sendDebugMessage(player, "Inseting into containers with item for item " + itemKey.getType() + ": " + containersWithItem.size() + " containers found.");
            remainingItems = insertAndGetOverflow(player, remainingItems, itemKey, containersWithItem, invalidContainers);
        }

        if (remainingItems.getAmount() != 0 && !getEmptyContainers().isEmpty()) {
            List<StorageContainer> emptyChests = getEmptyContainers().stream()
                    .map(StorageContainer::getStorageContainer)
                    .toList();

            sendDebugMessage(player, "Inseting into empty containers: " + emptyChests.size() + " containers found.");
            remainingItems = insertAndGetOverflow(player, remainingItems, itemKey, emptyChests, invalidContainers);
        }

        int amount = 0;

        if (remainingItems.getType() != Material.AIR) {
            amount = item.getAmount() - remainingItems.getAmount();
        } else {
            amount = item.getAmount();
        }

        removeContainerFromStorageItem(invalidContainers.toArray(new StorageContainer[0]));

        return remainingItems;
    }

    private ItemStack insertAndGetOverflow(Player player, ItemStack remainingItems, ItemStack itemKey, List<StorageContainer> containers, List<StorageContainer> invalidContainers) {
        if (containers.isEmpty()) {
            sendDebugMessage(player, "No containers to insert items into. Stopping.");
            return remainingItems;
        }

        for (StorageContainer container : containers) {
            sendDebugMessage(player, "Trying to insert items into container at " + container.getBlock().getX() + ", " + container.getBlock().getY() + ", " + container.getBlock().getZ());
            if (remainingItems.getAmount() == 0 || remainingItems.getType() == Material.AIR) {
                sendDebugMessage(player, "No remaining items to insert. Stopping.");
                break;
            }
            if (!container.isValid()) {
                sendDebugMessage(player, "Container at " + container.getBlock().getX() + ", " + container.getBlock().getY() + ", " + container.getBlock().getZ() + " is not valid. Skipping.");
                invalidContainers.add(container);
                continue;
            }

            if (!container.isAllowedPutItem(player, remainingItems)) {
                sendDebugMessage(player, "Player is not allowed to put items into container at " + container.getBlock().getX() + ", " + container.getBlock().getY() + ", " + container.getBlock().getZ() + ". Skipping.");
                continue;
            }

            Block block = container.getBlock();

            Inventory chestInv = container.getInventory();

            if (!EventUtils.isAllowedPutItem(player, chestInv)) {
                sendDebugMessage(player, "Player is not allowed to put items into inventory of container at " + container.getBlock().getX() + ", " + container.getBlock().getY() + ", " + container.getBlock().getZ() + ". Skipping.");
                continue;
            }

            ItemStack logItemStack = remainingItems.clone();
            int oldAmount = remainingItems.getAmount();

            remainingItems = chestInv.addItem(remainingItems).get(0);
            container.update();
            if (remainingItems == null) {
                sendDebugMessage(player, "No remaining items to insert. Stopping.");
                remainingItems = new ItemStack(Material.AIR);
            }

            int logAmount = oldAmount - remainingItems.getAmount();
            logItemStack.setAmount(logAmount);

            container.updateContainerMap(itemKey, logAmount);
            LogBlock.logChestInsert(player, block, logItemStack);

            updateContainer(container, itemKey);
        }
        sendDebugMessage(player, "Remaining items after trying to insert into containers: " + remainingItems.getAmount());
        return remainingItems;
    }

    public void updateContainer(StorageContainer container, ItemStack... itemKeys) {
        Map<Block, Boolean> itemsChests = new HashMap<>();
        Block block = container.getBlock();
        container.update();
        Inventory containerInv = container.getInventory();
        Location loc = container.getBlock().getLocation();

        sendDebugMessage(loc, "Updating container at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " for items: " + Arrays.toString(itemKeys));

        if (containerInv != null) {
            sendDebugMessage(loc, "Container inventory contents:" + Arrays.toString(containerInv.getContents()));
        } else {
            sendDebugMessage(loc, "Container inventory is null.");
        }


        for (ItemStack itemKeysSrc : itemKeys) {
            ItemStack itemKey = itemKeysSrc.clone();
            itemKey.setAmount(1);

            if (getItemsContainers().get(itemKey) != null) {
                itemsChests = getItemsContainers().get(itemKey);
            }

            if (containerInv != null && containerInv.containsAtLeast(itemKey, 1)) {
                sendDebugMessage(loc, "Container at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " contains item " + itemKey.getType() + " which is similar to item key. Marking container as fullstack for this item until proven otherwise.");

                itemsChests.put(block, true);

                for (ItemStack item : containerInv.getContents()) {
                    if (item != null && item.isSimilar(itemKey) && item.getAmount() != item.getMaxStackSize()) {
                        sendDebugMessage(loc, "Container at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " contains item " + item.getType() + " which is similar to item key " + itemKey.getType() + " but has amount " + item.getAmount() + " which is less than max stack size " + item.getMaxStackSize() + ". Marking container as non fullstack for this item.");
                        itemsChests.put(block, false);
                        break;
                    }
                }

            } else {
                sendDebugMessage(loc, "Container at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " does not contain item " + itemKey.getType() + " which is similar to item key. Removing container from item containers.");
                itemsChests.remove(block);
            }

            getItemsContainers().put(itemKey, itemsChests);


            Map<Block, Boolean> newItemsChests = getItemsContainers().get(itemKey);

            sendDebugMessage(loc, "After updating container for item key " + itemKey.getType() + ", items containers are: " + newItemsChests.entrySet().stream()
                    .map(entry -> entry.getKey().getX() + "," + entry.getKey().getY() + "," + entry.getKey().getZ() + "=" + entry.getValue())
                    .reduce("", (a, b) -> a + " " + b));
        }

        if (containerInv == null || containerInv.firstEmpty() == -1) {
            sendDebugMessage(loc, "Container at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " is not empty. Removing from empty containers list if it is in there.");
            getEmptyContainers().remove(block);
        } else {
            if (!getEmptyContainers().contains(block)) {
                sendDebugMessage(loc, "Container at " + block.getX() + ", " + block.getY() + ", " + block.getZ() + " is empty. Adding to empty containers list.");
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
            saveStorageItem(storageItem);
        } else {
            saveStorageItem(null);
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
            saveRangeItem(rangeItem);
        } else {
            saveRangeItem(null);
        }

        return result;
    }
}
