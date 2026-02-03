package de.janschuri.lunaticstorage.listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticlib.config.LunaticMessageKey;
import de.janschuri.lunaticlib.config.MessageKey;
import de.janschuri.lunaticlib.platform.paper.utils.EventUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.Key;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.janschuri.lunaticstorage.config.PluginConfig.getShutdownMessage;

public class ChestClickListener implements Listener {

    private static final MessageKey containerAlreadyMarked = new LunaticMessageKey("container_already_marked");
    private static final MessageKey containerMarked = new LunaticMessageKey("container_marked");

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (EventUtils.isFakeEvent(event)) {
            return;
        }


        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null) {
            return;
        }

        try {
            LunaticStorage.getGlowingBlocks().unsetGlowing(clickedBlock, player);
        } catch (Exception e) {
            Logger.error("Error while unsetting glowing block");
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR || !itemInHand.getItemMeta().getPersistentDataContainer().has(Key.STORAGE, PersistentDataType.INTEGER)) {
            return;
        }

        if (LunaticStorage.getPluginConfig().isShutdown()) {
            event.setCancelled(true);
            player.sendMessage(getShutdownMessage());
            return;
        }


        if (clickedBlock.getState() instanceof Container container && event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            int success = StorageContainer.addContainersToStorageItem(itemInHand, player.getWorld(), container);

            if (success == 0) {
                player.sendMessage(LunaticStorage.getLanguageConfig().getMessage(containerAlreadyMarked));
            } else {
                player.sendMessage(LunaticStorage.getLanguageConfig().getMessage(containerMarked));
            }
        }
    }
}