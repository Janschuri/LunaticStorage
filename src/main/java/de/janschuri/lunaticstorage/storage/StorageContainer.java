package de.janschuri.lunaticstorage.storage;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticlib.platform.bukkit.util.BlockUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StorageContainer {

    private static final Map<Block, StorageContainer> loadedContainers = new HashMap<>();

    private final Block block;
    private final List<Block> storageIds;
    private ItemStack[] contents;

    private StorageContainer(Block block) {
        this.block = block;
        if (block.getState() instanceof Container) {
            this.contents = ((Container) block.getState()).getInventory().getContents();
        }

        if (loadedContainers.containsKey(block)) {
            this.storageIds = loadedContainers.get(block).storageIds;
        } else {
            this.storageIds = new ArrayList<>();
            loadedContainers.put(block, this);
        }
    }

    public static StorageContainer getStorageContainer(Block block) {
        if (loadedContainers.containsKey(block)) {
            return loadedContainers.get(block);
        } else {
            return new StorageContainer(block);
        }
    }

    public static StorageContainer getStorageContainer(UUID worldUUID, long coords) {
        Block block = BlockUtils.deserializeCoords(coords, worldUUID).getBlock();
        return getStorageContainer(block);
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
        if (!storageIds.contains(id)) {
            storageIds.add(id);
        }
        loadedContainers.put(block, this);
    }

    public void removeStorageId(long id) {
        storageIds.remove(id);
        loadedContainers.put(block, this);
    }

    public void updateStorages(Map<ItemStack, Integer> difference) {
        for (Block block : storageIds) {
            Storage storage = Storage.getStorage(block);
            storage.updateStorage(difference);
        }
    }

    public void updateContents() {
        if (block.getState() instanceof Container) {
            ItemStack[] newContents = ((Container) block.getState()).getInventory().getContents();

            Map<ItemStack, Integer> difference = getDifference(contents, newContents);

            contents = newContents;
            updateStorages(difference);
        }
    }

    public static boolean isLoaded(Block block) {
        return loadedContainers.containsKey(block);
    }

    public Map<ItemStack, Integer> getDifference(ItemStack[] oldItems, ItemStack[] newItems) {
        Map<ItemStack, Integer> difference = new HashMap<>();

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
}
