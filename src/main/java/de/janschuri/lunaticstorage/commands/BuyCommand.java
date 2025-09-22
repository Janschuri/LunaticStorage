package de.janschuri.lunaticstorage.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.janschuri.lunaticstorage.utils.ProfessorAdamGeldPluginUtil;

public class BuyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!ProfessorAdamGeldPluginUtil.hasEconomy()) {
            player.sendMessage(ChatColor.RED + "Command disabled: No economy plugin found.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /buy <item>");
            return true;
        }

        String item = args[0].toUpperCase();

        if (item.equals("STORAGE_PANEL")) {
            double cost = 100.0; // Example cost

            if (ProfessorAdamGeldPluginUtil.withdraw(player, cost)) {
                player.sendMessage(ChatColor.GREEN + "You have successfully purchased a STORAGE_PANEL for " + cost + " coins.");
                // Logic to give the player the STORAGE_PANEL item goes here
            } else {
                player.sendMessage(ChatColor.RED + "You do not have enough money to buy a STORAGE_PANEL.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Unknown item: " + item);
        }

        return true;
    }
}