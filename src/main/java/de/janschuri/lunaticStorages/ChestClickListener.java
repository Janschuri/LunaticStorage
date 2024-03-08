package de.janschuri.lunaticStorages;

import org.bukkit.Location;
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

public class ChestClickListener implements Listener {


    private final Main plugin;

    public ChestClickListener(Main plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        // Check if the player is holding a diamond in their main hand
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand != null && itemInHand.getType() == Material.DIAMOND) {
            // Check if the player clicked on a block (e.g., chest)
            if (clickedBlock != null) {
                // Check if the clicked block is a chest
                if (clickedBlock.getType() == Material.CHEST) {
                    event.setCancelled(true);
                    // Player clicked with a diamond in their main hand on a chest
                    player.sendMessage("You clicked on a chest with a diamond in your main hand!");

                    String coords = Main.getCoordsAsString(event.getClickedBlock());

                    if(!Main.getDatabase().isChestInDatabase(coords)) {
                        Main.getDatabase().saveChestData(coords);
                    }


                        int chestID = Main.getDatabase().getChestID(coords);

                        // Modify the NBT data of the diamond item
                        ItemMeta diamondMeta = itemInHand.getItemMeta();

                        PersistentDataContainer dataContainer = diamondMeta.getPersistentDataContainer();

                        if (dataContainer.has(plugin.keyStorage, PersistentDataType.INTEGER_ARRAY)) {
                            int[] chests = dataContainer.get(plugin.keyStorage, PersistentDataType.INTEGER_ARRAY);
                            int[] newChests = new int[chests.length + 1];

                            // Copy elements from the original array to the new array
                            System.arraycopy(chests, 0, newChests, 0, chests.length);

                            // Add the new element at the end of the new array
                            newChests[chests.length] = chestID;

                            diamondMeta.getPersistentDataContainer().set(plugin.keyStorage, PersistentDataType.INTEGER_ARRAY, newChests);
                            itemInHand.setItemMeta(diamondMeta);
                        } else {
                            int[] chests = {chestID};
                            diamondMeta.getPersistentDataContainer().set(plugin.keyStorage, PersistentDataType.INTEGER_ARRAY, chests);
                            itemInHand.setItemMeta(diamondMeta);
                        }

                        player.sendMessage("Diamond has been marked with the chest's location.");
                    }


            }
        }
    }
}