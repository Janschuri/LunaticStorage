package de.janschuri.lunaticstorage.listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticlib.platform.bukkit.util.BukkitUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.Key;
import de.janschuri.lunaticstorage.utils.Utils;
import de.janschuri.lunaticlib.MessageKey;
import de.janschuri.lunaticlib.platform.bukkit.external.AdventureAPI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
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

public class ChestClickListener implements Listener {

    private static final MessageKey containerAlreadyMarked = new MessageKey("container_already_marked");
    private static final MessageKey containerMarked = new MessageKey("container_marked");

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR || !itemInHand.getItemMeta().getPersistentDataContainer().has(Key.STORAGE, PersistentDataType.BOOLEAN)) {
            return;
        }

        if (clickedBlock == null) {
            return;
        }

        if (Utils.isContainerBlock(clickedBlock.getType()) && event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);

            Block block = event.getClickedBlock();
            UUID worldUUID = block.getWorld().getUID();
            long chestID = BukkitUtils.serializeCoords(block.getLocation());

            ItemMeta storageMeta = itemInHand.getItemMeta();

            PersistentDataContainer dataContainer = storageMeta.getPersistentDataContainer();

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

            if (!dataContainer.has(worldKey, PersistentDataType.LONG_ARRAY)) {
                long[] worldChests = new long[1];
                worldChests[0] = chestID;
                dataContainer.set(worldKey, PersistentDataType.LONG_ARRAY, worldChests);
                itemInHand.setItemMeta(storageMeta);
            } else {
                List<Long> chests = Utils.getListFromArray(dataContainer.get(worldKey, PersistentDataType.LONG_ARRAY));

                if (chests.contains(chestID)) {
                    AdventureAPI.sendMessage(player, LunaticStorage.getLanguageConfig().getMessage(containerAlreadyMarked));
                    return;
                } else {
                    chests.add(chestID);
                    dataContainer.set(worldKey, PersistentDataType.LONG_ARRAY, Utils.getArrayFromList(chests));
                    itemInHand.setItemMeta(storageMeta);
                    AdventureAPI.sendMessage(player, LunaticStorage.getLanguageConfig().getMessage(containerMarked));
                }
            }

            PersistentDataContainer blockDataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
            blockDataContainer.set(Key.STORAGE_CONTAINER, PersistentDataType.BOOLEAN, true);
        }
    }
}