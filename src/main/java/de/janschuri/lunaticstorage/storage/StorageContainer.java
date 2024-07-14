package de.janschuri.lunaticstorage.storage;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticlib.platform.bukkit.util.BlockUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class StorageContainer {

    private final Location location;
    private final Block block;

    public StorageContainer(Block block) {
        this.location = block.getLocation();
        this.block = block;
    }

    public StorageContainer(UUID worlduUUID, long coords) {
        this.location = BlockUtils.deserializeCoords(coords, worlduUUID);
        this.block = location.getBlock();
    }

    public Inventory getSnapshotInventory() {
        if (block.getState() instanceof Container) {
            return ((Container) block.getState()).getInventory();
        } else {
            return null;
        }
    }

    public boolean isStorageContainer() {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        return dataContainer.has(Key.STORAGE_CONTAINER, PersistentDataType.BOOLEAN);
    }

    public Block getBlock() {
        return block;
    }

    public void update() {
        block.getState().update();
    }
}
