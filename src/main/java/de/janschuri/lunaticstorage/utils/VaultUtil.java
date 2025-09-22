package de.janschuri.lunaticstorage.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultUtil {

    private static Economy economy;

    static {
        try {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Vault not found or economy provider not available.");
        }
    }

    public static boolean hasEconomy() {
        return economy != null;
    }

    public static boolean withdraw(String playerName, double amount) {
        if (economy != null) {
            return economy.withdrawPlayer(playerName, amount).transactionSuccess();
        }
        return false;
    }

    public static boolean deposit(String playerName, double amount) {
        if (economy != null) {
            return economy.depositPlayer(playerName, amount).transactionSuccess();
        }
        return false;
    }
}