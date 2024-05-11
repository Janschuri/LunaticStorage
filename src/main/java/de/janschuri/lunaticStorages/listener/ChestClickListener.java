package de.janschuri.lunaticStorages.listener;

import de.janschuri.lunaticStorages.Keys;
import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.database.tables.ChestsTable;
import de.janschuri.lunaticStorages.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.units.qual.K;

public class ChestClickListener implements Listener {


    private final LunaticStorage plugin;

    public ChestClickListener(LunaticStorage plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand != null && itemInHand.getType() != Material.AIR && itemInHand.getItemMeta().getPersistentDataContainer().has(Keys.STORAGE, PersistentDataType.INTEGER_ARRAY)) {
            if (clickedBlock != null) {
                if (clickedBlock.getType() == Material.CHEST) {
                    event.setCancelled(true);

                    Block block = event.getClickedBlock();
                    String coords = Utils.getCoordsAsString(block);
                    String worldName = block.getWorld().getName();

                    if (!ChestsTable.isChestInDatabase(worldName, coords)) {
                        ChestsTable.saveChestData(worldName, coords);
                    }


                    int chestID = ChestsTable.getChestID(worldName, coords);

                    ItemMeta storageMeta = itemInHand.getItemMeta();

                    PersistentDataContainer dataContainer = storageMeta.getPersistentDataContainer();

                    int[] chests = dataContainer.get(Keys.STORAGE, PersistentDataType.INTEGER_ARRAY);

                    if (Utils.containsChestsID(chests, chestID)) {
                        player.sendMessage("Diamond is already marked with the chest's location!");
                    } else {
                        int[] newChests = new int[chests.length + 1];
                        System.arraycopy(chests, 0, newChests, 0, chests.length);
                        newChests[chests.length] = chestID;

                        storageMeta.getPersistentDataContainer().set(Keys.STORAGE, PersistentDataType.INTEGER_ARRAY, newChests);
                        itemInHand.setItemMeta(storageMeta);

                        player.sendMessage("Diamond has been marked with the chest's location.");
                    }
                }
            }
        }
    }
}