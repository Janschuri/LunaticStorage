package de.janschuri.lunaticstorage;

import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import de.janschuri.lunaticstorage.storage.Key;
import de.janschuri.lunaticstorage.storage.Storage;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.support.MockBukkitTestBase;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LunaticStoragePanelDropTest extends MockBukkitTestBase {

    @Test
    void dropsPanelLinkerAndRangeItemWhenSneakingPlayerBreaksPanel() {
        WorldMock world = server.addSimpleWorld("panel-drop-test");
        PlayerMock player = server.addPlayer("PanelTester");
        player.teleport(new Location(world, 0.0, 65.0, 0.0));
        player.setSneaking(true);

        Block supportBlock = world.getBlockAt(0, 64, 0);
        supportBlock.setType(Material.STONE);

        Block panelBlock = world.getBlockAt(0, 65, 0);
        Block chestBlock = world.getBlockAt(1, 65, 0);
        chestBlock.setType(Material.CHEST);

        ItemStack storageItem = createStorageItem(Material.DIAMOND);
        ItemStack rangeItem = createRangeItem(Material.AMETHYST_SHARD, 9L);
        ItemStack panelItem = createPanelItem(Material.valueOf(plugin.getConfig().getString("panel_block")), 9L);

        Container chest = (Container) chestBlock.getState();
        assertEquals(1, StorageContainer.addContainersToStorageItem(storageItem, world, chest));

        BlockState replacedState = panelBlock.getState();
        panelBlock.setType(panelItem.getType());

        BlockPlaceEvent placeEvent = new BlockPlaceEvent(
                panelBlock,
                replacedState,
                supportBlock,
                panelItem,
                player,
                true,
                EquipmentSlot.HAND
        );
        server.getPluginManager().callEvent(placeEvent);

        assertFalse(placeEvent.isCancelled());
        assertTrue(Utils.isPanel(panelBlock));
        assertEquals(9L, Utils.getRangeFromBlock(panelBlock));

        Storage storage = Storage.getStorage(panelBlock);
        storage.saveStorageItem(storageItem);
        storage.saveRangeItem(rangeItem);

        BlockState brokenState = panelBlock.getState();
        BlockBreakEvent breakEvent = player.simulateBlockBreak(panelBlock);

        assertNotNull(breakEvent);
        assertFalse(breakEvent.isCancelled());
        assertEquals(Material.AIR, panelBlock.getType());

        List<Item> droppedEntities = new ArrayList<>();
        BlockDropItemEvent dropEvent = new BlockDropItemEvent(panelBlock, brokenState, player, droppedEntities);
        server.getPluginManager().callEvent(dropEvent);

        assertEquals(3, dropEvent.getItems().size());

        ItemStack droppedPanel = getDroppedItem(dropEvent, Utils::isPanelBlockItem);
        ItemStack droppedLinker = getDroppedItem(dropEvent, Utils::isStorageItem);
        ItemStack droppedRangeItem = getDroppedItem(dropEvent, Utils::isRangeItem);

        assertEquals(panelItem.getType(), droppedPanel.getType());
        assertEquals(9L, Utils.getRangeFromPanelBlockItem(droppedPanel));

        Map<UUID, List<String>> linkedContainers = Utils.getStorageContainerCoordsMap(droppedLinker);
        assertEquals(List.of(Utils.serializeCoords(chestBlock.getLocation())), linkedContainers.get(world.getUID()));

        assertEquals(rangeItem.getType(), droppedRangeItem.getType());
        assertEquals(9L, Utils.getRangeFromItem(droppedRangeItem));

        assertFalse(Utils.isPanel(panelBlock));
    }

    private static ItemStack createStorageItem(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        meta.getPersistentDataContainer().set(Key.STORAGE, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createRangeItem(Material material, long range) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        meta.getPersistentDataContainer().set(Key.RANGE, PersistentDataType.LONG, range);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createPanelItem(Material material, long range) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        meta.getPersistentDataContainer().set(Key.PANEL_BLOCK, PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(Key.PANEL_RANGE, PersistentDataType.LONG, range);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getDroppedItem(BlockDropItemEvent event, Predicate<ItemStack> matcher) {
        return event.getItems().stream()
                .map(Item::getItemStack)
                .filter(matcher)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected matching dropped item."));
    }
}
