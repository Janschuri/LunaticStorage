package de.janschuri.lunaticStorages;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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

    private final Main plugin;

    public StorageCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        } else {
            Player player = (Player) sender;

            if (args.length < 1) {

            } else if (args[0].equalsIgnoreCase("item")) {
                ItemStack item = new ItemStack(plugin.storageItem);
                int limit = plugin.defaultLimit;
                if (args.length > 1) {
                    try {
                        limit = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("nix zahl");
                    }
                }

                int[] invs = new int[] {};

                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(plugin.keyLimit, PersistentDataType.INTEGER, limit);
                meta.getPersistentDataContainer().set(Main.keyStorage, PersistentDataType.INTEGER_ARRAY, invs);
                item.setItemMeta(meta);

                player.getInventory().addItem(item);
            } else if (args[0].equalsIgnoreCase("panel")) {
                ItemStack item = new ItemStack(plugin.panelBlock);


                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(plugin.keyPanelBlock, PersistentDataType.BOOLEAN, true);
                item.setItemMeta(meta);
                player.getInventory().addItem(item);
            }
        }

        return true;
    }
}
