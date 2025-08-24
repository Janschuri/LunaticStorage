package net.professoradam.lunaticstorage.listener;

import com.jeff_media.customblockdata.CustomBlockData;
import net.professoradam.lunaticstorage.LunaticStorage;
import net.professoradam.lunaticstorage.storage.Key;
import net.professoradam.lunaticstorage.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockPlaceListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

            ItemStack item = event.getItemInHand();
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

            if (Utils.isPanelBlockItem(item)) {
                Block block = event.getBlockPlaced();
                PersistentDataContainer blockDataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
                blockDataContainer.set(Key.PANEL_BLOCK, PersistentDataType.INTEGER, 1);
                blockDataContainer.set(Key.PANEL_RANGE, PersistentDataType.LONG, dataContainer.get(Key.PANEL_RANGE, PersistentDataType.LONG));
            }
    }
}
