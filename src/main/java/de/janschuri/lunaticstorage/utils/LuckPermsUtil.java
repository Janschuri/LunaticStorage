package de.janschuri.lunaticstorage.utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LuckPermsUtil {

    private static LuckPerms luckPerms;

    static {
        try {
            luckPerms = LuckPermsProvider.get();
        } catch (Exception e) {
            Bukkit.getLogger().warning("LuckPerms not found. Falling back to Bukkit permissions.");
        }
    }

    public static boolean hasPermission(Player player, String permission) {
        if (luckPerms != null) {
            return luckPerms.getUserManager().getUser(player.getUniqueId())
                    .getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        }
        return player.hasPermission(permission);
    }
}