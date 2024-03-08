package de.janschuri.lunaticStorages;

import de.janschuri.lunaticStorages.database.Database;
import de.janschuri.lunaticStorages.database.MySQL;
import de.janschuri.lunaticStorages.database.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

    public Material storageItem;
    public Material panelBlock;
    public int defaultLimit;

    NamespacedKey keyPanelID = new NamespacedKey(this, "panelID");
    NamespacedKey keyPane = new NamespacedKey(this, "gui_pane");
    NamespacedKey keyStoragePane = new NamespacedKey(this, "gui_storage_pane");
    NamespacedKey keyStorageContent = new NamespacedKey(this, "gui_storage_content");
    NamespacedKey keyLimit = new NamespacedKey(this, "limit");

    NamespacedKey keyPanelBlock = new NamespacedKey(this, "panel_block");
    NamespacedKey keyStorage = new NamespacedKey(this, "invs");



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
        getServer().getPluginManager().registerEvents(new PlaceBlockListener(this), this);
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

        storageItem = Material.matchMaterial(getConfig().getString("storage_item", "DIAMOND"));
        panelBlock = Material.matchMaterial(getConfig().getString("panel_block", "LODESTONE"));
        defaultLimit = getConfig().getInt("default_limit", 10);

    }

    public static Database getDatabase() {
        return Main.db;

    }

    public static String getCoordsAsString(Block block) {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        return x + "," + y + "," + z;
    }

    public static int[] parseCoords(String coords) {
        // Splitting the uniqueId string at ","
        String[] coordStrings = coords.split(",");

        // Parsing the string elements into integers
        int[] coordsInt = new int[coordStrings.length];
        for (int i = 0; i < coordStrings.length; i++) {
            coordsInt[i] = Integer.parseInt(coordStrings[i]);
        }

        return coordsInt;
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

    public Inventory addMaptoInventory(Inventory inventory, List<Map.Entry<ItemStack, Integer>> list, int id) {

        // Add items to the sum inventory (sorted by amount)
        for (Map.Entry<ItemStack, Integer> entry : list) {

            ItemStack itemStack = entry.getKey();
            int amount = entry.getValue();

            ItemStack singleStack = itemStack.clone();
            singleStack.setAmount(1); // Set amount to 1

            byte[] itemSerialized = serializeItemStack(itemStack);

            ItemMeta meta = singleStack.getItemMeta();
            meta.getPersistentDataContainer().set(this.keyStorageContent, PersistentDataType.BYTE_ARRAY, itemSerialized);
            meta.getPersistentDataContainer().set(this.keyPanelID, PersistentDataType.INTEGER, id);
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

            String coords = Main.getDatabase().getChestCoords(id);
            int coordsArray[] = Main.parseCoords(coords);

            int x = coordsArray[0];
            int y = coordsArray[1];
            int z = coordsArray[2];

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
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();

        byte[] serializedItem = meta.getPersistentDataContainer().get(this.keyStorageContent, PersistentDataType.BYTE_ARRAY);
        ItemStack searchedItem = deserializeItemStack(serializedItem);
        int stackSize = searchedItem.getMaxStackSize();
        int foundItems = 0;

        for (int id : chests) {
            String uuid = Main.getDatabase().getChestCoords(id);
            int coords[] = Main.parseCoords(uuid);

            int x = coords[0];
            int y = coords[1];
            int z = coords[2];

            Block block = world.getBlockAt(x, y, z);
            Chest chest = (Chest) block.getState();

            Inventory chestInv = chest.getInventory();

            for (ItemStack i : chestInv.getContents()) {
                if (i != null) {
                    if (i.isSimilar(searchedItem)) {
                        int amount = i.getAmount();
                        int amountNeeded = stackSize-foundItems;
                        if (amountNeeded < amount) {
                            chest.getSnapshotInventory().removeItem(i);
                            i.setAmount(i.getAmount()-amountNeeded);
                            chest.getSnapshotInventory().addItem(i);
                            chest.update();
                            foundItems = foundItems+amountNeeded;
                        } else if (amountNeeded == amount) {
                            chest.getSnapshotInventory().removeItem(i);
                            chest.update();
                            foundItems = foundItems+amount;
                        } else {
                            chest.getSnapshotInventory().removeItem(i);
                            chest.update();
                            foundItems = foundItems+amount;
                        }

                        if (foundItems == stackSize) {
                            break;
                        }
                    }
                }
            }
        }

        searchedItem.setAmount(foundItems);

        return searchedItem;
    }
}
