package de.janschuri.lunaticstorage.utils;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import de.janschuri.lunaticstorage.storage.Key;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
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
        if (uuids == null || uuids.isEmpty()) {
            return null;
        }

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

    public static boolean isContainerBlock(Material material) {
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

    public static boolean isPanel(Block block) {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        return dataContainer.has(Key.PANEL_BLOCK, PersistentDataType.INTEGER);
    }

    public static boolean isContainer(Block block) {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
        return dataContainer.has(Key.STORAGE_CONTAINER, PersistentDataType.INTEGER);
    }

    public static boolean isStorageItem(ItemStack item) {
        if (item == null) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return false;
        }

        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

        return dataContainer.has(Key.STORAGE, PersistentDataType.INTEGER);
    }

    public static boolean isRangeItem(ItemStack item) {
        if (item == null) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return false;
        }

        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

        return dataContainer.has(Key.RANGE, PersistentDataType.INTEGER);
    }

    public static Collection<StorageContainer> getStorageChests(ItemStack storageItem) {
        Collection<StorageContainer> storageContainers = new ArrayList<>();

        if (storageItem != null) {
            ItemMeta meta = storageItem.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();

            String worldsString = container.get(Key.STORAGE_ITEM_WORLDS, PersistentDataType.STRING);
            if (worldsString != null) {
                List<UUID> worlds = Utils.getUUIDListFromString(worldsString);

                if (worlds == null) {
                    return storageContainers;
                }

                for (UUID worldUUID : worlds) {
                    NamespacedKey worldKey = Key.getKey(worldUUID.toString());
                    long[] chests = container.get(worldKey, PersistentDataType.LONG_ARRAY);
                    if (chests != null) {
                        for (long chest : chests) {
                            storageContainers.add(StorageContainer.getStorageContainer(worldUUID, chest));
                        }
                    }
                }
            }
        }

        return storageContainers;
    }

    public static long getRangeFromItem(ItemStack item) {
        if (item == null) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(Key.RANGE, PersistentDataType.LONG);
    }

    public static long getRangeFromBlock(Block block) {
        PersistentDataContainer dataContainer = new CustomBlockData(block, LunaticStorage.getInstance());

        if (!dataContainer.has(Key.PANEL_RANGE, PersistentDataType.LONG)) {
            return 0;
        }

        return dataContainer.get(Key.PANEL_RANGE, PersistentDataType.LONG);
    }

    public static byte[] serializeItemStackMap(Map<ItemStack, Boolean> itemStackMap) {
        JSONObject json = new JSONObject();
        for (Map.Entry<ItemStack, Boolean> entry : itemStackMap.entrySet()) {
            ItemStack itemStack = entry.getKey();
            boolean matchNBT = entry.getValue();

            JSONObject itemJson = new JSONObject();
            itemJson.put("item", ItemStackUtils.serializeItemStack(itemStack));
            itemJson.put("matchNBT", matchNBT);

            json.put(itemStack.getType().name(), itemJson);
        }
        return json.toString().getBytes();
    }

    public static Map<ItemStack, Boolean> deserializeItemStackMap(byte[] bytes) {
        Map<ItemStack, Boolean> itemStackMap = new HashMap<>();
        JSONObject json = new JSONObject(new String(bytes));
        for (String key : json.keySet()) {
            JSONObject itemJson = json.getJSONObject(key);
            String itemBytes = itemJson.get("item").toString();
            byte[] itemBytesArray = toByteArray(itemBytes);
            ItemStack itemStack = ItemStackUtils.deserializeItemStack(itemBytesArray);
            boolean matchNBT = itemJson.getBoolean("matchNBT");

            itemStackMap.put(itemStack, matchNBT);
        }
        return itemStackMap;
    }

    public static byte[] toByteArray(String strings) {
        strings = strings.substring(1, strings.length() - 1);
        String[] stringArray = strings.split(",");
        byte[] byteArray = new byte[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            byteArray[i] = Byte.parseByte(stringArray[i]);
        }
        return byteArray;
    }

    public static boolean isDoubleChest(Chest chest) {
        return chest.getInventory().getHolder() instanceof org.bukkit.block.DoubleChest;
    }

    public static Chest getOtherChestHalf(Chest chest) {
        if (isDoubleChest(chest)) {
            DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();
            if (doubleChest.getLeftSide().equals(chest)) {
                return (Chest) doubleChest.getRightSide();
            } else {
                return (Chest) doubleChest.getLeftSide();
            }
        }
        return null;
    }

    public static Map<ItemStack, Boolean> getSubMap(Map<ItemStack, Boolean> items, int startIndex, int endIndex) {
        Map<ItemStack, Boolean> subMap = new HashMap<>();
        int i = 0;
        for (Map.Entry<ItemStack, Boolean> entry : items.entrySet()) {
            if (i >= startIndex && i < endIndex) {
                subMap.put(entry.getKey(), entry.getValue());
            }
            i++;
        }
        return subMap;
    }
}
