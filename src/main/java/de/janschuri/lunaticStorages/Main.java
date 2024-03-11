package de.janschuri.lunaticStorages;

import de.janschuri.lunaticStorages.database.Database;
import de.janschuri.lunaticStorages.database.MySQL;
import de.janschuri.lunaticStorages.database.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;


public final class Main extends JavaPlugin {

    private static Database db;
    private static FileConfiguration config;

    public Material storageItem;
    public Material panelBlock;
    public int defaultLimit;
    public static Map<Integer, Storage> storages = new HashMap<>();

    static String pluginNamespace = "lunaticstorage";
    static NamespacedKey keyPanelID = new NamespacedKey(pluginNamespace, "panel_id");
    static NamespacedKey keyPane = new NamespacedKey(pluginNamespace, "gui_pane");
    static NamespacedKey keyStoragePane = new NamespacedKey(pluginNamespace, "gui_storage_pane");
    static NamespacedKey keyStorageContent = new NamespacedKey(pluginNamespace, "gui_storage_content");
    static NamespacedKey keyLimit = new NamespacedKey(pluginNamespace, "limit");
    static NamespacedKey keyPanelBlock = new NamespacedKey(pluginNamespace, "panel_block");
    static NamespacedKey keyStorage = new NamespacedKey(pluginNamespace, "invs");
    static NamespacedKey keyLeftArrow = new NamespacedKey(pluginNamespace, "left_arrow");
    static NamespacedKey keyRightArrow = new NamespacedKey(pluginNamespace, "right_arrow");
    static NamespacedKey keyPage = new NamespacedKey(pluginNamespace, "page");



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
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
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

    public static ItemStack getSkull(String url) {

        PlayerProfile pProfile = getProfile(url);
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwnerProfile(pProfile);
        head.setItemMeta(meta);

        return head;
    }

    private static PlayerProfile getProfile(String url) {
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();
        URL urlObject;
        try {
            urlObject = new URL(url);
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Invalid URL", exception);
        }
        textures.setSkin(urlObject);
        profile.setTextures(textures);
        return profile;
    }

    public static boolean isAllowed(Player player, Location location) {

            Bukkit.getLogger().info("blub");
            WorldGuardWrapper wgWrapper = WorldGuardWrapper.getInstance();
            Optional<IWrappedFlag<WrappedState>> flag = wgWrapper.getFlag("chest-access", WrappedState.class);
            if (!flag.isPresent()) Bukkit.getLogger().info("Nice rechte");
            WrappedState state = flag.map(f -> wgWrapper.queryFlag(player, location, f).orElse(WrappedState.DENY)).orElse(WrappedState.DENY);
            return state == WrappedState.ALLOW;

    }
}
