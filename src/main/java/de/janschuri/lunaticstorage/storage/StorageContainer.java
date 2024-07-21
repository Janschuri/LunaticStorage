package de.janschuri.lunaticstorage.storage;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticlib.platform.bukkit.util.BukkitUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class StorageContainer {
    private static final Map<Block, ItemStack[]> containerContents = new HashMap<>();
    private static final Map<Block, List<Block>> containerStorageIds = new HashMap<>();

    private final Block block;

    private StorageContainer(Block block) {
        this.block = block;
        containerContents.computeIfAbsent(block, k -> null);
        containerStorageIds.computeIfAbsent(block, k -> new ArrayList<>());
    }

    public static StorageContainer getStorageContainer(Block block) {
        return new StorageContainer(block);
    }

    public static StorageContainer getStorageContainer(UUID worldUUID, long coords) {
        Block block = BukkitUtils.deserializeCoords(coords, worldUUID).getBlock();
        return getStorageContainer(block);
    }

    public void setContainerContent(ItemStack[] contents) {
        containerContents.put(block, contents);
    }

    public ItemStack[] getContainerContents() {
        return containerContents.get(block);
    }

    public List<Block> getStorageIds() {
        return containerStorageIds.get(block);
    }


    public Inventory getSnapshotInventory() {
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
        containerContents.remove(block);
        containerStorageIds.remove(block);
    }

    public void updateStorages(Map<ItemStack, Integer> difference) {
        for (Block block : getStorageIds()) {
            Storage storage = Storage.getStorage(block);
            storage.updateStorage(difference);
        }
    }

    public void updateContents() {
        if (block.getState() instanceof Container) {
            ItemStack[] newContents = ((Container) block.getState()).getInventory().getContents();

            Map<ItemStack, Integer> difference = getDifference(getContainerContents(), newContents);

            if (difference.isEmpty()) {
                return;
            }

            setContainerContent(newContents);
            updateStorages(difference);
        } else {
            if (getContainerContents() != null) {
                updateStorages(getDifference(getContainerContents(), new ItemStack[0]));
            }
            setContainerContent(null);
        }
    }

    public static boolean isLoaded(Block block) {
        return containerStorageIds.containsKey(block);
    }

    public Map<ItemStack, Integer> getDifference(ItemStack[] oldItems, ItemStack[] newItems) {
        Map<ItemStack, Integer> difference = new HashMap<>();

        if (oldItems == null) {
            oldItems = new ItemStack[0];
        }

        List<ItemStack> oldItemList = new ArrayList<>();
        for (ItemStack oldItem : oldItems) {
            if (oldItem != null) {
                oldItemList.add(oldItem);
            }
        }
        List<ItemStack> newItemList = new ArrayList<>();
        for (ItemStack newItem : newItems) {
            if (newItem != null) {
                newItemList.add(newItem);
            }
        }

        for (ItemStack oldItem : oldItemList) {
            if (newItemList.contains(oldItem)) {
                ItemStack newItem = newItemList.get(newItemList.indexOf(oldItem));
                int differenceCount = newItem.getAmount() - oldItem.getAmount();
                if (differenceCount != 0) {
                    difference.put(newItem, differenceCount);
                }
            } else {
                difference.put(oldItem, -oldItem.getAmount());
            }
            newItemList.remove(oldItem);
        }

        for (ItemStack newItem : newItemList) {
            difference.put(newItem, newItem.getAmount());
        }

        return difference;
    }

    public boolean isValid() {
        boolean isValid = true;
        if (!(block.getState() instanceof Container)) {
            isValid = false;
        }

        if (!(Utils.isContainer(block))) {
            isValid = false;
        }

        if (!isValid) {
            unload();
        }

        return isValid;
    }

    public boolean canPlaceItem(ItemStack item) {
        if (isWhitelistEnabled() && !isWhitelisted(item)) {
            return false;
        }

        if (isBlacklistEnabled() && isBlacklisted(item)) {
            return false;
        }

        return true;
    }

    public void setWhitelistEnabled(boolean enabled) {
        setWhitelistEnabled(enabled, true);
    }

    public void setWhitelistEnabled(boolean enabled, boolean updateDoubleChest) {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        dataContainer.set(Key.WHITELIST_ENABLED, PersistentDataType.BOOLEAN, enabled);

        if (isDoubleChest() && updateDoubleChest) {
            getOtherHalf().setWhitelistEnabled(enabled, false);
        }
    }

    public void setBlacklistEnabled(boolean enabled) {
        setBlacklistEnabled(enabled, true);
    }

    public void setBlacklistEnabled(boolean enabled, boolean updateDoubleChest) {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        dataContainer.set(Key.BLACKLIST_ENABLED, PersistentDataType.BOOLEAN, enabled);

        if (isDoubleChest() && updateDoubleChest) {
            getOtherHalf().setBlacklistEnabled(enabled, false);
        }
    }

    public boolean isWhitelistEnabled() {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());

        if (dataContainer.has(Key.WHITELIST_ENABLED, PersistentDataType.BOOLEAN)) {
            return dataContainer.get(Key.WHITELIST_ENABLED, PersistentDataType.BOOLEAN);
        } else {
            dataContainer.set(Key.WHITELIST_ENABLED, PersistentDataType.BOOLEAN, false);
            return false;
        }
    }

    public boolean isBlacklistEnabled() {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());

        if (dataContainer.has(Key.BLACKLIST_ENABLED, PersistentDataType.BOOLEAN)) {
            return dataContainer.get(Key.BLACKLIST_ENABLED, PersistentDataType.BOOLEAN);
        } else {
            dataContainer.set(Key.BLACKLIST_ENABLED, PersistentDataType.BOOLEAN, false);
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

        if (whitelist.containsKey(item)) {
            return;
        }

        whitelist.put(item, matchNBT);
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

    public void toggleWhitelist(ItemStack item) {
        Map<ItemStack, Boolean> whitelist = getWhitelist();
        if (whitelist.containsKey(item)) {
            whitelist.put(item, !whitelist.get(item));
        } else {
            whitelist.put(item, true);
        }
        setWhitelist(whitelist);
    }

    public void toggleBlacklist(ItemStack item) {
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
}
