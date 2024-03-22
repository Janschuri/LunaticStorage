package de.janschuri.lunaticStorages;

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

public class ChestClickListener implements Listener {


    private final Main plugin;

    public ChestClickListener(Main plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand != null && itemInHand.getType() != Material.AIR && itemInHand.getItemMeta().getPersistentDataContainer().has(Main.keyStorage)) {
            if (clickedBlock != null) {
                if (clickedBlock.getType() == Material.CHEST) {
                    event.setCancelled(true);

                    String coords = Main.getCoordsAsString(event.getClickedBlock());

                    if (!Main.getDatabase().isChestInDatabase(coords)) {
                        Main.getDatabase().saveChestData(coords);
                    }


                    int chestID = Main.getDatabase().getChestID(coords);

                    ItemMeta storageMeta = itemInHand.getItemMeta();

                    PersistentDataContainer dataContainer = storageMeta.getPersistentDataContainer();

                    int[] chests = dataContainer.get(plugin.keyStorage, PersistentDataType.INTEGER_ARRAY);

                    if (Main.containsChestsID(chests, chestID)) {
                        player.sendMessage("Diamond is already marked with the chest's location!");
                    } else {
                        int[] newChests = new int[chests.length + 1];
                        System.arraycopy(chests, 0, newChests, 0, chests.length);
                        newChests[chests.length] = chestID;

                        storageMeta.getPersistentDataContainer().set(plugin.keyStorage, PersistentDataType.INTEGER_ARRAY, newChests);
                        itemInHand.setItemMeta(storageMeta);

                        player.sendMessage("Diamond has been marked with the chest's location.");
                    }
                }
            }
        }
    }
}