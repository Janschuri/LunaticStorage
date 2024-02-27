package de.janschuri.lunaticStorage;

import de.janschuri.lunaticStorage.database.Database;
import de.janschuri.lunaticStorage.database.MySQL;
import de.janschuri.lunaticStorage.database.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;


public final class Main extends JavaPlugin {

    private static Database db;
    private static FileConfiguration config;

    public Map<UUID, List<Inventory>> chestsClicked = new HashMap<>();

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

    public void putChests(UUID uuid, Inventory inventory) {
        chestsClicked.computeIfAbsent(uuid, k -> new ArrayList<>()).add(inventory);
    }

    public List<Inventory> getChests(UUID uuid) {
        return chestsClicked.getOrDefault(uuid, Collections.emptyList());

    }

    public static int generateUniqueId(int x, int y, int z) {
        int id = (x << 20) | (y << 10) | z;
        return id;
    }

    public static int[] parseUniqueId(short uniqueId) {
        int x = (uniqueId >> 20) & 0x3FF;
        int y = (uniqueId >> 10) & 0x3FF;
        int z = uniqueId & 0x3FF;
        return new int[]{x, y, z};
    }
}
