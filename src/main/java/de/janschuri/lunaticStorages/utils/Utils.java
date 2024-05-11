package de.janschuri.lunaticStorages.utils;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticlib.utils.ItemStackUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.JSONObject;

public class Utils extends de.janschuri.lunaticlib.utils.Utils {


    public static String getCoordsAsString(Block block) {

        String world = block.getWorld().getName();

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        return x + "," + y + "," + z;
    }

    public static int[] parseCoords(String coords) {
        String[] coordStrings = coords.split(",");

        int[] coordsInt = new int[coordStrings.length];
        for (int i = 0; i < coordStrings.length; i++) {
            coordsInt[i] = Integer.parseInt(coordStrings[i]);
        }

        return coordsInt;
    }

    public static boolean containsChestsID(int[] array, int target) {
        for (int num : array) {
            if (num == target) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAllowed(Player player, Location location) {
        return true;
//        Bukkit.getLogger().info(String.valueOf(worldguardEnabled));
//        if (worldguardEnabled) {
//            WorldGuardWrapper wgWrapper = WorldGuardWrapper.getInstance();
//            Optional<IWrappedFlag<WrappedState>> flag = wgWrapper.getFlag("chest-access", WrappedState.class);
//            if (!flag.isPresent()) Bukkit.getLogger().info("WorldGuard flag 'chest-access' is not present!");
//            WrappedState state = flag.map(f -> wgWrapper.queryFlag(player, location, f).orElse(WrappedState.DENY)).orElse(WrappedState.DENY);
//            return state == WrappedState.ALLOW;
//        } else {
//            return true;
//        }

    }

    public static String getMCLanguage(ItemStack itemStack, String locale) {
        String nameKey = ItemStackUtils.getKey(itemStack);
        JSONObject language = LunaticStorage.getLanguagesMap().get(locale + ".json");

        if (itemStack.getItemMeta().hasDisplayName()) {
            return itemStack.getItemMeta().getDisplayName();
        } else {
            if (language != null) {
                String name = language.getString(nameKey);
                return name;
            } else {
                return itemStack.getType().toString();
            }
        }
    }
}
