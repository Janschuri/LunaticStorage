package de.janschuri.lunaticstorage.utils;

import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Utils extends de.janschuri.lunaticlib.common.utils.Utils {


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

    public static List<Long> getListFromArray(long[] array) {
        return Arrays.stream(array).boxed().collect(Collectors.toList());
    }

    public static long[] getArrayFromList(List<Long> list) {
        return list.stream().mapToLong(i -> i).toArray();
    }

    public static String getUUIDListAsString(List<UUID> uuids) {
        StringBuilder sb = new StringBuilder();
        for (UUID uuid : uuids) {
            sb.append(uuid.toString()).append(",");
        }
        return sb.toString();
    }

    public static List<UUID> getUUIDListFromString(String uuids) {
        String[] uuidStrings = uuids.split(",");
        return Arrays.stream(uuidStrings).map(UUID::fromString).collect(Collectors.toList());
    }

    public static String getMCLanguage(ItemStack itemStack, String locale) {
        String nameKey = ItemStackUtils.getKey(itemStack);
        JSONObject language = LunaticStorage.getLanguagesMap().get(locale + ".json");
        String name;

        if (itemStack.getItemMeta() != null && itemStack.getItemMeta().hasDisplayName()) {
            name = itemStack.getItemMeta().getDisplayName();
        } else {
            if (language != null) {
                name = language.getString(nameKey);
            } else {
                name = itemStack.getType().toString();
            }
        }
        return name.toLowerCase();
    }

    public static boolean isContainer(Material material) {
        return material == Material.CHEST
                || material == Material.TRAPPED_CHEST
                || material == Material.BARREL
                || material == Material.SHULKER_BOX
                || material == Material.BLACK_SHULKER_BOX
                || material == Material.BLUE_SHULKER_BOX
                || material == Material.BROWN_SHULKER_BOX
                || material == Material.CYAN_SHULKER_BOX
                || material == Material.GRAY_SHULKER_BOX
                || material == Material.GREEN_SHULKER_BOX
                || material == Material.LIGHT_BLUE_SHULKER_BOX
                || material == Material.LIGHT_GRAY_SHULKER_BOX
                || material == Material.LIME_SHULKER_BOX
                || material == Material.MAGENTA_SHULKER_BOX
                || material == Material.ORANGE_SHULKER_BOX
                || material == Material.PINK_SHULKER_BOX
                || material == Material.PURPLE_SHULKER_BOX
                || material == Material.RED_SHULKER_BOX
                || material == Material.WHITE_SHULKER_BOX
                || material == Material.YELLOW_SHULKER_BOX;
    }
}
