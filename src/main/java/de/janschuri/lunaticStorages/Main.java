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

    static String pluginNamespace = "lunaticstorage";
    static NamespacedKey keyPanelID = new NamespacedKey(pluginNamespace, "panel_id");
    static NamespacedKey keyPane = new NamespacedKey(pluginNamespace, "gui_pane");
    static NamespacedKey keyStoragePane = new NamespacedKey(pluginNamespace, "gui_storage_pane");
    static NamespacedKey keyStorageContent = new NamespacedKey(pluginNamespace, "gui_storage_content");
    static NamespacedKey keyLimit = new NamespacedKey(pluginNamespace, "limit");
    static NamespacedKey keyPanelBlock = new NamespacedKey(pluginNamespace, "panel_block");
    static NamespacedKey keyStorage = new NamespacedKey(pluginNamespace, "invs");



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
        getServer().getPluginManager().registerEvents(new blockListener(this), this);
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
    public static byte[] serializeItemStack(ItemStack item) {
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

    public static ItemStack deserializeItemStack(byte[] data) {
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

    public static boolean containsInt(int[] array, int target) {
        for (int num : array) {
            if (num == target) {
                return true; // Found the target integer in the array
            }
        }
        return false; // Target integer not found in the array
    }
}
