package de.janschuri.lunaticstorage.listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.Key;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static de.janschuri.lunaticstorage.config.PluginConfig.getShutdownMessage;

public class BlockPlaceListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
            ItemStack item = event.getItemInHand();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            Player player = event.getPlayer();

            if (Utils.isPanelBlockItem(item)) {
                if (LunaticStorage.getPluginConfig().isShutdown()) {
                    event.setCancelled(true);
                    player.sendMessage(getShutdownMessage());
                    return;
                }

                Block block = event.getBlockPlaced();
                PersistentDataContainer blockDataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
                blockDataContainer.set(Key.PANEL_BLOCK, PersistentDataType.INTEGER, 1);
                blockDataContainer.set(Key.PANEL_RANGE, PersistentDataType.LONG, dataContainer.get(Key.PANEL_RANGE, PersistentDataType.LONG));
            }
    }
}
