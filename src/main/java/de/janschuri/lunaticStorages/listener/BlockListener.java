package de.janschuri.lunaticStorages.listener;

import de.janschuri.lunaticStorages.LunaticStorage;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockListener implements Listener {

    private final LunaticStorage plugin;

    public BlockListener(LunaticStorage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        if (event.getBlockPlaced().getType().equals(plugin.panelBlock)) {
            ItemStack item = event.getItemInHand();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

            if (dataContainer.has(LunaticStorage.keyPanelBlock, PersistentDataType.BOOLEAN)) {
                event.getPlayer().sendMessage("Test");
                Block block = event.getBlockPlaced();


                String coords = LunaticStorage.getCoordsAsString(block);

                LunaticStorage.getDatabase().savePanelsData(coords, null);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();
        String coords = LunaticStorage.getCoordsAsString(block);

        if (LunaticStorage.getDatabase().isPanelInDatabase(coords)) {
            if (player.isSneaking()) {
                LunaticStorage.getDatabase().removePanel(coords);
            } else {
                event.setCancelled(true);
            }
        }

        if (LunaticStorage.getDatabase().isChestInDatabase(coords)) {
            if (player.isSneaking()) {
                LunaticStorage.getDatabase().removeChest(coords);
            } else {
                event.setCancelled(true);
            }
        }
    }
}

