package de.janschuri.lunaticStorages;

import de.janschuri.lunaticStorages.database.Database;
import de.janschuri.lunaticStorages.database.MySQL;
import de.janschuri.lunaticStorages.database.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;


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
}
