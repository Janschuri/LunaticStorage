package de.janschuri.lunaticstorage.storage;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticlib.platform.paper.utils.EventUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.gui.StorageGUI;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class StorageContainer {
    private static final Map<Block, List<Block>> containerStorageIds = new HashMap<>();

    private final Block block;

    private StorageContainer(Block block) {
        this.block = block;

        containerStorageIds.computeIfAbsent(block, k -> new ArrayList<>());
    }

    public static StorageContainer getStorageContainer(Block block) {
        return new StorageContainer(block);
    }

    public static StorageContainer getStorageContainer(UUID worldUUID, String coords) {
        Block block = Utils.deserializeCoords(coords, worldUUID).getBlock();

        if (block == null) {
            return null;
        }

        return getStorageContainer(block);
    }

    public List<Block> getStorageIds() {
        return containerStorageIds.get(block);
    }


    public Inventory getInventory() {
        if (block.getState() instanceof Chest) {
            return ((Chest) block.getState()).getBlockInventory();
        }

        if (block.getState() instanceof Container) {
            return ((Container) block.getState()).getInventory();
        } else {
            return null;
        }
    }

    public Block getBlock() {
        return block;
    }

    public void update() {
        block.getState().update();
    }

    public void addStorageId(Block id) {
        if (!getStorageIds().contains(id)) {
            getStorageIds().add(id);
        }
    }

    public void removeStorageId(Block block) {
        getStorageIds().remove(block);
        if (getStorageIds().isEmpty()) {
            unload();
        }
    }

    public void unload() {
        containerStorageIds.remove(block);
    }

    public void updateStorages(Map<ItemStack, Integer> difference) {

        for (Block block : getStorageIds()) {
            Storage storage = Storage.getStorage(block);
            storage.updateStorage(difference);
            storage.updateContainer(this, difference.keySet().toArray(new ItemStack[0]));
            StorageGUI.updateStorageGUIs(block);
        }
    }

    public static boolean isLoaded(Block block) {
        return containerStorageIds.containsKey(block);
    }

    public Map<ItemStack, Integer> getDifference(Map<ItemStack, Integer> oldItems, Map<ItemStack, Integer>  newItems) {
        Map<ItemStack, Integer> difference = new HashMap<>();

        if (oldItems == null) {
            oldItems = new HashMap<>();
        }

        Map<ItemStack, Integer> oldItemsCopy = new HashMap<>(oldItems);
        Map<ItemStack, Integer> newItemsCopy = new HashMap<>(newItems);

        for (ItemStack oldItem : oldItems.keySet()) {
            for (ItemStack newItem : newItems.keySet()) {
                if (oldItem.isSimilar(newItem)) {

                    if (oldItems.get(oldItem) != newItems.get(newItem)) {
                        difference.put(newItem.clone(), newItems.get(newItem) - oldItems.get(oldItem));
                    }

                    oldItemsCopy.remove(oldItem);
                    newItemsCopy.remove(newItem);
                }
            }
        }

        for (ItemStack oldItem : oldItemsCopy.keySet()) {
            difference.put(oldItem.clone(), -oldItems.get(oldItem));
        }

        for (ItemStack newItem : newItemsCopy.keySet()) {
            difference.put(newItem.clone(), newItems.get(newItem));
        }

        return difference;
    }

    public boolean isValid() {
        boolean isValid = true;
        if (!(block.getState() instanceof Container)) {
            isValid = false;
        }

        if (isValid && !(Utils.isStorageContainer(block))) {
            isValid = false;
        }

        if (!isValid) {
            unload();
        }

        return isValid;
    }

    public boolean isAllowedPutItem(Player player, ItemStack item) {
        if (!EventUtils.isAllowedInteract(player, block)) {
            return false;
        }

        if (!EventUtils.isAllowedPutItem(player, getInventory())) {
            return false;
        }

        if (isWhitelistEnabled() && !isWhitelisted(item)) {
            return false;
        }

        if (isBlacklistEnabled() && isBlacklisted(item)) {
            return false;
        }

        return true;
    }

    public boolean isAllowedTakeItem(Player player) {
        if (!EventUtils.isAllowedInteract(player, block)) {
            return false;
        }

        if (!EventUtils.isAllowedTakeItem(player, getInventory())) {
            return false;
        }

        return true;
    }

    public void setWhitelistEnabled(boolean enabled) {
        setWhitelistEnabled(enabled, true);
    }

    public void setWhitelistEnabled(boolean enabled, boolean updateDoubleChest) {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        dataContainer.set(Key.WHITELIST_ENABLED, PersistentDataType.INTEGER, (enabled ? 1 : 0));

        if (isDoubleChest() && updateDoubleChest) {
            getOtherHalf().setWhitelistEnabled(enabled, false);
        }
    }

    public void setBlacklistEnabled(boolean enabled) {
        setBlacklistEnabled(enabled, true);
    }

    public void setBlacklistEnabled(boolean enabled, boolean updateDoubleChest) {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        dataContainer.set(Key.BLACKLIST_ENABLED, PersistentDataType.INTEGER,  (enabled ? 1 : 0));

        if (isDoubleChest() && updateDoubleChest) {
            getOtherHalf().setBlacklistEnabled(enabled, false);
        }
    }

    public boolean isWhitelistEnabled() {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());

        if (dataContainer.has(Key.WHITELIST_ENABLED, PersistentDataType.INTEGER)) {
            return dataContainer.get(Key.WHITELIST_ENABLED, PersistentDataType.INTEGER) == 1;
        } else {
            dataContainer.set(Key.WHITELIST_ENABLED, PersistentDataType.INTEGER, 0);
            return false;
        }
    }

    public boolean isBlacklistEnabled() {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());

        if (dataContainer.has(Key.BLACKLIST_ENABLED, PersistentDataType.INTEGER)) {
            return dataContainer.get(Key.BLACKLIST_ENABLED, PersistentDataType.INTEGER) == 1;
        } else {
            dataContainer.set(Key.BLACKLIST_ENABLED, PersistentDataType.INTEGER, 0);
            return false;
        }
    }

    public void setWhitelist(Map<ItemStack, Boolean> whitelist) {
        setWhitelist(whitelist, true);
    }

    public void setWhitelist(Map<ItemStack, Boolean> whitelist, boolean updateDoubleChest) {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        dataContainer.set(Key.WHITELIST, PersistentDataType.BYTE_ARRAY, Utils.serializeItemStackMap(whitelist));

        if (isDoubleChest() && updateDoubleChest) {
            getOtherHalf().setWhitelist(whitelist, false);
        }
    }

    public void setBlacklist(Map<ItemStack, Boolean> blacklist) {
        setBlacklist(blacklist, true);
    }

    public void setBlacklist(Map<ItemStack, Boolean> blacklist, boolean updateDoubleChest) {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        dataContainer.set(Key.BLACKLIST, PersistentDataType.BYTE_ARRAY, Utils.serializeItemStackMap(blacklist));

        if (isDoubleChest() && updateDoubleChest) {
            getOtherHalf().setBlacklist(blacklist, false);
        }
    }

    public void addToWhitelist(ItemStack item, boolean matchNBT) {
        Map<ItemStack, Boolean> whitelist = getWhitelist();
        ItemStack itemKey = item.clone();
        itemKey.setAmount(1);

        whitelist.put(itemKey, matchNBT);

        List<Block> storageIds = getStorageIds();
        if (matchNBT) {
            for (Block storageId : storageIds) {
                Storage storage = Storage.getStorage(storageId);
                if (storage != null) {
                    storage.addPreferredContainer(this, itemKey);
                }
            }
        } else {
            for (Block storageId : storageIds) {
                Storage storage = Storage.getStorage(storageId);
                if (storage != null) {
                    storage.addPreferredContainer(this, itemKey.getType());
                }
            }
        }

        setWhitelist(whitelist);
    }

    public void addToBlacklist(ItemStack item, boolean matchNBT) {
        Map<ItemStack, Boolean> blacklist = getBlacklist();

        if (blacklist.containsKey(item)) {
            return;
        }

        blacklist.put(item, matchNBT);
        setBlacklist(blacklist);
    }

    public void removeFromWhitelist(ItemStack item) {
        Map<ItemStack, Boolean> whitelist = getWhitelist();
        whitelist.remove(item);
        setWhitelist(whitelist);
    }

    public void removeFromBlacklist(ItemStack item) {
        Map<ItemStack, Boolean> blacklist = getBlacklist();
        blacklist.remove(item);
        setBlacklist(blacklist);
    }

    public void toggleWhitelist() {
        setWhitelistEnabled(!isWhitelistEnabled());
    }

    public void toggleBlacklist() {
        setBlacklistEnabled(!isBlacklistEnabled());
    }

    public void toggleWhitelistNBT(ItemStack item) {
        Map<ItemStack, Boolean> whitelist = getWhitelist();
        if (whitelist.containsKey(item)) {
            whitelist.put(item, !whitelist.get(item));
        } else {
            whitelist.put(item, true);
        }
        setWhitelist(whitelist);
    }

    public void toggleblacklistNBT(ItemStack item) {
        Map<ItemStack, Boolean> blacklist = getBlacklist();
        if (blacklist.containsKey(item)) {
            blacklist.put(item, !blacklist.get(item));
        } else {
            blacklist.put(item, true);
        }
        setBlacklist(blacklist);
    }

    public Map<ItemStack, Boolean> getWhitelist() {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());

        if (dataContainer.has(Key.WHITELIST, PersistentDataType.BYTE_ARRAY)) {
            return Utils.deserializeItemStackMap(dataContainer.get(Key.WHITELIST, PersistentDataType.BYTE_ARRAY));
        } else {
            Map<ItemStack, Boolean> whitelist = new HashMap<>();
            dataContainer.set(Key.WHITELIST, PersistentDataType.BYTE_ARRAY, Utils.serializeItemStackMap(whitelist));
            return whitelist;
        }
    }

    public Map<ItemStack, Boolean> getBlacklist() {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());

        if (dataContainer.has(Key.BLACKLIST, PersistentDataType.BYTE_ARRAY)) {
            return Utils.deserializeItemStackMap(dataContainer.get(Key.BLACKLIST, PersistentDataType.BYTE_ARRAY));
        } else {
            Map<ItemStack, Boolean> blacklist = new HashMap<>();
            dataContainer.set(Key.BLACKLIST, PersistentDataType.BYTE_ARRAY, Utils.serializeItemStackMap(blacklist));
            return blacklist;
        }
    }

    private boolean isWhitelisted(ItemStack item) {
        for (Map.Entry<ItemStack, Boolean> entry : getWhitelist().entrySet()) {
            if (entry.getValue()) {
                if (entry.getKey().isSimilar(item)) {
                    return true;
                }
            } else {
                if (entry.getKey().getType() == item.getType()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBlacklisted(ItemStack item) {
        for (Map.Entry<ItemStack, Boolean> entry : getBlacklist().entrySet()) {
            if (entry.getValue()) {
                if (entry.getKey().isSimilar(item)) {
                    return true;
                }
            } else {
                if (entry.getKey().getType() == item.getType()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isDoubleChest() {
        if (block.getState() instanceof Chest) {
            return Utils.isDoubleChest((Chest) block.getState());
        }
        return false;
    }

    public StorageContainer getOtherHalf() {
        if (isDoubleChest()) {
            Chest chest = (Chest) block.getState();
            return getStorageContainer(Utils.getOtherChestHalf(chest).getBlock());
        }
        return null;
    }

    public void addInvToWhitelist() {
        ItemStack[] contents = getInventory().getContents();
        for (ItemStack item : contents) {
            if (item != null) {
                addToWhitelist(item, true);
            }
        }

        if (isDoubleChest()) {
            ItemStack[] doubleChestContents = getOtherHalf().getInventory().getContents();
            for (ItemStack item : doubleChestContents) {
                if (item != null) {
                    addToWhitelist(item, true);
                }
            }
        }
    }

    public boolean isOnWhitelist(ItemStack item) {
        return getWhitelist().containsKey(item) && getWhitelist().get(item);
    }

    public boolean isOnWhitelist(Material item) {
        for (Map.Entry<ItemStack, Boolean> entry : getWhitelist().entrySet()) {
            if (entry.getKey().getType() == item) {
                return true;
            }
        }
        return false;
    }

    public void clearWhitelist() {
        setWhitelist(new HashMap<>());
    }

    public void clearBlacklist() {
        setBlacklist(new HashMap<>());
    }
    public int getWhiteListPages() {
        return (int) (getWhitelist().size() / 36.0);
    }

    public int getBlackListPages() {
        return (int) (getBlacklist().size() / 36.0);
    }

    public static boolean handleContainerRightClick(Player player, ItemStack itemInHand, Container container) {
        UUID worldUUID = container.getWorld().getUID();
        ItemMeta storageMeta = itemInHand.getItemMeta();
        PersistentDataContainer dataContainer = storageMeta.getPersistentDataContainer();

        // Check if the storage item already has the world in its list and add it if not
        if (!dataContainer.has(Key.STORAGE_ITEM_WORLDS, PersistentDataType.STRING)) {
            dataContainer.set(Key.STORAGE_ITEM_WORLDS, PersistentDataType.STRING, worldUUID.toString());
            itemInHand.setItemMeta(storageMeta);
        } else {
            String worldUUIDString = dataContainer.get(Key.STORAGE_ITEM_WORLDS, PersistentDataType.STRING);
            List<UUID> worlds = new ArrayList<>();
            if (worldUUIDString != null) {
                worlds = Utils.getUUIDListFromString(worldUUIDString);
            }
            if (!worlds.contains(worldUUID)) {
                worlds.add(worldUUID);
                dataContainer.set(Key.STORAGE_ITEM_WORLDS, PersistentDataType.STRING, Utils.getUUIDListAsString(worlds));
                itemInHand.setItemMeta(storageMeta);
            }
        }

        NamespacedKey worldKey = new NamespacedKey(LunaticStorage.getInstance(), worldUUID.toString());
        List<String> chests = new ArrayList<>();
        List<Block> containerBlocks = new ArrayList<>();

        if (container instanceof Chest) {
            Chest chest = (Chest) container;
            if (Utils.isDoubleChest(chest)) {
                DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();
                Chest leftChest = (Chest) doubleChest.getLeftSide();
                Chest rightChest = (Chest) doubleChest.getRightSide();
                String leftChestID = Utils.serializeCoords(leftChest.getLocation());
                String rightChestID = Utils.serializeCoords(rightChest.getLocation());
                chests.add(leftChestID);
                chests.add(rightChestID);
                containerBlocks.add(leftChest.getBlock());
                containerBlocks.add(rightChest.getBlock());
            } else {
                String chestID = Utils.serializeCoords(container.getLocation());
                chests.add(chestID);
                containerBlocks.add(container.getBlock());
            }
        } else {
            String chestID = Utils.serializeCoords(container.getLocation());
            chests.add(chestID);
            containerBlocks.add(container.getBlock());
        }

        if (addChestsToPersistentDataContainer(dataContainer, worldKey, chests)) {
            for (Block block : containerBlocks) {
                if (!Utils.isStorageContainer(block)) {
                    StorageContainer storageContainer = StorageContainer.getStorageContainer(block);
                    if (!storageContainer.getInventory().isEmpty()) {
                        storageContainer.addInvToWhitelist();
                        storageContainer.setWhitelistEnabled(true);
                    }
                }
                PersistentDataContainer blockDataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
                blockDataContainer.set(Key.STORAGE_CONTAINER, PersistentDataType.INTEGER, 1);
            }
            itemInHand.setItemMeta(storageMeta);
            return true;
        } else {
            return false;
        }
    }

    private static boolean addChestsToPersistentDataContainer(PersistentDataContainer dataContainer, NamespacedKey worldKey, List<String> chests) {
        if (!dataContainer.has(worldKey, PersistentDataType.BYTE_ARRAY)) {
            byte[] worldChests = Utils.getArrayFromList(chests);
            dataContainer.set(worldKey, PersistentDataType.BYTE_ARRAY, worldChests);
            return true;
        } else {
            List<String> oldChests = Utils.getListFromArray(dataContainer.get(worldKey, PersistentDataType.BYTE_ARRAY));

            boolean allAlreadyMarked = true;

            for (String chest : chests) {
                if (!oldChests.contains(chest)) {
                    oldChests.add(chest);
                    allAlreadyMarked = false;
                }
            }

            dataContainer.set(worldKey, PersistentDataType.BYTE_ARRAY, Utils.getArrayFromList(oldChests));

            return !allAlreadyMarked;
        }
    }
}
