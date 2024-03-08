package de.janschuri.lunaticStorages;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PlaceBlockListener implements Listener {

    private final Main plugin;

    public PlaceBlockListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        if (event.getBlockPlaced().getType().equals(plugin.panelBlock)) {
            ItemStack item = event.getItemInHand();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

            if (dataContainer.has(plugin.keyPanelBlock, PersistentDataType.BOOLEAN)) {
                event.getPlayer().sendMessage("Test");
                Block block = event.getBlockPlaced();


                String coords = Main.getCoordsAsString(block);

                Main.getDatabase().savePanelsData(coords, null);
            }
        }
    }
}

