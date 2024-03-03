package de.janschuri.lunaticStorages;

import de.janschuri.lunaticStorages.database.Database;
import de.janschuri.lunaticStorages.database.MySQL;
import de.janschuri.lunaticStorages.database.SQLite;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


public final class Main extends JavaPlugin {

    private static Database db;
    private static FileConfiguration config;


    @Override
    public void onEnable() {

        saveDefaultConfig();

        loadConfig(this);

        if (config.getBoolean("Database.MySQL.enabled")) {
            db = new MySQL(this);
            if (db.getSQLConnection() == null) {
                Bukkit.getLogger().log(Level.SEVERE, "Error initializing MySQL database");
                Bukkit.getLogger().info("Falling back to SQLite due to initialization error");

                db = new SQLite(this);

            }
        }
        else {
            db = new SQLite(this);
        }

        db.load();

        getServer().getPluginManager().registerEvents(new StoragePanelGUI(this), this);


        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new ChestClickListener(this), this);
        getCommand("storage").setExecutor(new StorageCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void loadConfig(Plugin plugin) {

        File cfgfile = new File(plugin.getDataFolder().getAbsolutePath() + "/config.yml");
        config = YamlConfiguration.loadConfiguration(cfgfile);

    }

    public static Database getDatabase() {
        return Main.db;

    }

    public static String generateUniqueId(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    public static int[] parseUniqueId(String uniqueId) {
        // Splitting the uniqueId string at ","
        String[] coordStrings = uniqueId.split(",");

        // Parsing the string elements into integers
        int[] coords = new int[coordStrings.length];
        for (int i = 0; i < coordStrings.length; i++) {
            coords[i] = Integer.parseInt(coordStrings[i]);
        }

        return coords;
    }
    
    public ItemMeta setKey (ItemMeta meta, String keyString, byte[] serializedItem) {
        NamespacedKey key = new NamespacedKey(this, keyString);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE_ARRAY, serializedItem);
        return meta;
    }

    public ItemMeta setKey (ItemMeta meta, String keyString, boolean boo) {
        NamespacedKey key = new NamespacedKey(this, keyString);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, boo);
        return meta;
    }

    // Method to serialize an ItemStack
    public byte[] serializeItemStack(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            // Handle the IOException
            e.printStackTrace(); // Or handle it in another appropriate way
            return null; // Return null or throw a custom exception if necessary
        }
    }

    public ItemStack deserializeItemStack(byte[] data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (IOException | ClassNotFoundException e) {
            // Handle the IOException or ClassNotFoundException
            e.printStackTrace(); // Or handle it in another appropriate way
            return null; // Return null or throw a custom exception if necessary
        }
    }

    public Map<ItemStack, Integer> inventoryToMap(Inventory inventory) {
        Map<ItemStack, Integer> itemMap = new HashMap<>();

        for (ItemStack item : inventory.getContents()) {
            if (item != null) {
                // Clone the item to avoid modifying the original
                ItemStack clone = item.clone();
                clone.setAmount(1); // Set amount to 1 to treat each item stack as a unique key

                // Check if item already exists in the map with the same metadata
                boolean found = false;
                for (Map.Entry<ItemStack, Integer> entry : itemMap.entrySet()) {
                    ItemStack existingItem = entry.getKey();
                    if (existingItem.isSimilar(clone)) {
                        int amount = entry.getValue();
                        itemMap.put(existingItem, amount + item.getAmount());
                        found = true;
                        break;
                    }
                }

                // If item not found, add it to the map
                if (!found) {
                    itemMap.put(clone, item.getAmount());
                }
            }
        }

        return itemMap;
    }

    public Inventory addMaptoInventory(Inventory inventory, List<Map.Entry<ItemStack, Integer>> list) {

        // Add items to the sum inventory (sorted by amount)
        for (Map.Entry<ItemStack, Integer> entry : list) {

            ItemStack itemStack = entry.getKey();
            int amount = entry.getValue();

            ItemStack singleStack = itemStack.clone();
            singleStack.setAmount(1); // Set amount to 1

            byte[] itemSerialized = serializeItemStack(itemStack);

            ItemMeta meta = singleStack.getItemMeta();
            meta = this.setKey(meta, "guiStorageItem", itemSerialized);
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore == null) {
                    lore = new ArrayList<>();
                }
                lore.add("Amount: " + amount);
                meta.setLore(lore);

            }
            singleStack.setItemMeta(meta);
            inventory.addItem(singleStack);
        }

        return inventory;
    }

    public List<Map.Entry<ItemStack, Integer>> getStorage (int[] chests, World world){

        Map<ItemStack, Integer> summedInventory = new HashMap<>();
        List<Inventory> inventories = new ArrayList<>();
        for (int id : chests) {

            String uuid = Main.getDatabase().getUUID(id);
            int coords[] = Main.parseUniqueId(uuid);

            int x = coords[0];
            int y = coords[1];
            int z = coords[2];

            Block block = world.getBlockAt(x, y, z);
            Chest chest = (Chest) block.getState();

            Inventory chestInv = chest.getInventory();
            inventories.add(chestInv);

        }

        // Iterate over the inventories
        for (Inventory inventory : inventories) {
            // Convert the inventory to a map
            Map<ItemStack, Integer> inventoryMap = this.inventoryToMap(inventory);

            // Sum the inventory map with the summedInventory map
            for (Map.Entry<ItemStack, Integer> itemEntry : inventoryMap.entrySet()) {
                ItemStack itemStack = itemEntry.getKey();
                int amount = itemEntry.getValue();

                summedInventory.merge(itemStack, amount, Integer::sum);
            }
        }


        // Sort the summed inventory map by amount
        List<Map.Entry<ItemStack, Integer>> sortedEntries = summedInventory.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());

        return sortedEntries;
    }

    public ItemStack getItemsFromStorage(int[] chests, World world, ItemStack item) {
        Bukkit.getLogger().info("1");
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        NamespacedKey key = new NamespacedKey(this, "guiStorageItem");

        byte[] serializedItem = meta.getPersistentDataContainer().get(key, PersistentDataType.BYTE_ARRAY);
        ItemStack searchedItem = deserializeItemStack(serializedItem);

        boolean itemfound = false;
        Bukkit.getLogger().info("2");
        for (int id : chests) {
            Bukkit.getLogger().info("3");
            String uuid = Main.getDatabase().getUUID(id);
            int coords[] = Main.parseUniqueId(uuid);

            int x = coords[0];
            int y = coords[1];
            int z = coords[2];

            Block block = world.getBlockAt(x, y, z);
            Chest chest = (Chest) block.getState();

            Inventory chestInv = chest.getInventory();

            for (ItemStack i : chestInv.getContents()) {
                if (i != null) {

                    if (i.isSimilar(searchedItem)) {
                        // Found the item in the chest
                        itemfound = true;
                        chest.getSnapshotInventory().removeItem(i);
                        i.setAmount(i.getAmount()-1);
                        chest.getSnapshotInventory().addItem(i);
                        chest.update();
                    }
                }
            }
        }

        if (itemfound) {
            return searchedItem;
        } else {
            return new ItemStack(Material.DIRT);
        }

    }
}
