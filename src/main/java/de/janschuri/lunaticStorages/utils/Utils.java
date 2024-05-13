package de.janschuri.lunaticStorages.utils;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticlib.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.json.JSONObject;

import java.util.Objects;

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

    public static boolean isAllowedViewChest(Player player, Block chest) {
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, new ItemStack(Material.AIR), chest, BlockFace.UP);
        Bukkit.getPluginManager().callEvent(event);
        boolean allowed = !event.isCancelled();
        event.setCancelled(true);
        return allowed;
    }

    public static boolean isAllowedTakeItem(Player player, Inventory inventory) {
        InventoryView oldView = player.getOpenInventory();
        ItemStack cursor = oldView.getCursor();
        oldView.setCursor(new ItemStack(Material.AIR));
        InventoryView view = player.openInventory(inventory);
        player.openInventory(oldView);
        player.setItemOnCursor(cursor);

        InventoryClickEvent event = new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER, 0, ClickType.LEFT, InventoryAction.PICKUP_ALL);
        Bukkit.getPluginManager().callEvent(event);
        boolean allowed = !event.isCancelled();
        event.setCancelled(true);
        return allowed;
    }

    public static boolean isAllowedPutItem(Player player, Inventory inventory) {
        InventoryView oldView = player.getOpenInventory();
        ItemStack cursor = oldView.getCursor();
        oldView.setCursor(new ItemStack(Material.AIR));
        InventoryView view = player.openInventory(inventory);
        player.openInventory(oldView);
        player.setItemOnCursor(cursor);

        InventoryClickEvent event = new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER, 0, ClickType.RIGHT, InventoryAction.PLACE_ALL);
        Bukkit.getPluginManager().callEvent(event);
        boolean allowed = !event.isCancelled();
        event.setCancelled(true);
        return allowed;
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
