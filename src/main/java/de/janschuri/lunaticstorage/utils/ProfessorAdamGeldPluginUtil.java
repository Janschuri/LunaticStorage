package de.janschuri.lunaticstorage.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.professoradamgeldplugin.api.ProfessorEconomyAPI;

public class ProfessorAdamGeldPluginUtil {

    private static final ProfessorEconomyAPI economyAPI;
    private static final boolean economyEnabled;

    static {
        FileConfiguration config = Bukkit.getPluginManager().getPlugin("LunaticStorage").getConfig();
        economyEnabled = config.getBoolean("economy_enabled", true);

        economyAPI = (ProfessorEconomyAPI) Bukkit.getServer().getPluginManager().getPlugin("ProfessorAdamGeldPlugin");
        if (economyAPI == null && economyEnabled) {
            Bukkit.getLogger().warning("ProfessorAdamGeldPlugin not found. Economy features will be disabled.");
        }
    }

    public static boolean hasEconomy() {
        return economyEnabled && economyAPI != null;
    }

    public static boolean withdraw(Player player, double amount) {
        if (hasEconomy()) {
            try {
                validateAmount(amount);
                return economyAPI.withdraw(player.getUniqueId(), (int) amount);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid withdrawal amount: " + e.getMessage());
            } catch (Exception e) {
                Bukkit.getLogger().warning("Failed to withdraw money: " + e.getMessage());
            }
        }
        return false;
    }

    public static void deposit(Player player, double amount) {
        if (hasEconomy()) {
            try {
                validateAmount(amount);
                economyAPI.deposit(player.getUniqueId(), (int) amount);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid deposit amount: " + e.getMessage());
            } catch (Exception e) {
                Bukkit.getLogger().warning("Failed to deposit money: " + e.getMessage());
            }
        }
    }

    private static void validateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }
}