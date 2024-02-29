package de.janschuri.lunaticStorages;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class StorageCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public StorageCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Initialize an inventory to display the summed inventories
        Inventory inventory = Bukkit.getServer().createInventory(null, 9 * 6);


        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        ItemMeta diamondMeta = itemInHand.getItemMeta();

        World world = player.getWorld();

        NamespacedKey key = new NamespacedKey(plugin, "invs");

        PersistentDataContainer dataContainer = diamondMeta.getPersistentDataContainer();
        int[] chests = dataContainer.get(key, PersistentDataType.INTEGER_ARRAY);

        List<Map.Entry<ItemStack, Integer>> storage = InventoryUtils.getStorage(chests, world);

        inventory = InventoryUtils.addMaptoInventory(inventory, storage);


        // Open the sum inventory to the player
        player.openInventory(inventory);

        return true;
    }
}
