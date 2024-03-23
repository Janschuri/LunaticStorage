package de.janschuri.lunaticStorages;

import de.janschuri.lunaticStorages.database.Database;
import de.janschuri.lunaticStorages.database.MySQL;
import de.janschuri.lunaticStorages.database.SQLite;
import de.janschuri.lunaticStorages.listener.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;


public final class LunaticStorage extends JavaPlugin {

    private static Database db;
    private static FileConfiguration config;
    private static Map<String, JSONObject> languages = new HashMap<>();

    private static FileConfiguration lang;
    public String language;
    public String prefix;
    public Material storageItem;
    public Material panelBlock;
    public static boolean worldguard;
    public Map<String, String> messages = new HashMap<>();
    public static Map<Integer, Storage> storages = new HashMap<>();

    static String pluginNamespace = "lunaticstorage";
    public static NamespacedKey keyPanelID = new NamespacedKey(pluginNamespace, "panel_id");
    public static NamespacedKey keyPane = new NamespacedKey(pluginNamespace, "gui_pane");
    public static NamespacedKey keyStoragePane = new NamespacedKey(pluginNamespace, "gui_storage_pane");
    public static NamespacedKey keyStorageContent = new NamespacedKey(pluginNamespace, "gui_storage_content");
    public static NamespacedKey keyLimit = new NamespacedKey(pluginNamespace, "limit");
    public static NamespacedKey keyPanelBlock = new NamespacedKey(pluginNamespace, "panel_block");
    public static NamespacedKey keyStorage = new NamespacedKey(pluginNamespace, "invs");
    public static NamespacedKey keyLeftArrow = new NamespacedKey(pluginNamespace, "left_arrow");
    public static NamespacedKey keyRightArrow = new NamespacedKey(pluginNamespace, "right_arrow");
    public static NamespacedKey keyPage = new NamespacedKey(pluginNamespace, "page");
    public static NamespacedKey keySearch = new NamespacedKey(pluginNamespace, "search");
    public static NamespacedKey keyDesc = new NamespacedKey(pluginNamespace, "desc");
    public static NamespacedKey keySorter = new NamespacedKey(pluginNamespace, "sorter");




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

        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestClickListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new QuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PanelClickListener(), this);
        getCommand("storage").setExecutor(new StorageCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void loadConfig(LunaticStorage plugin) {

        File cfgfile = new File(plugin.getDataFolder().getAbsolutePath() + "/config.yml");
        config = YamlConfiguration.loadConfiguration(cfgfile);

        storageItem = Material.matchMaterial(getConfig().getString("storage_item", "DIAMOND"));
        panelBlock = Material.matchMaterial(getConfig().getString("panel_block", "LODESTONE"));
        worldguard = getConfig().getBoolean("worldguard", true);

        File langfileEN = new File(plugin.getDataFolder().getAbsolutePath() + "/langEN.yml");
        File langfileDE = new File(plugin.getDataFolder().getAbsolutePath() + "/langDE.yml");
        language = config.getString("language", "EN");

        if (!langfileEN.exists()) {
            langfileEN.getParentFile().mkdirs();
            plugin.saveResource("langEN.yml", false);
        }

        if (!langfileDE.exists()) {
            langfileDE.getParentFile().mkdirs();
            plugin.saveResource("langDE.yml", false);
        }


        if (language.equalsIgnoreCase("EN"))
        {
            lang = YamlConfiguration.loadConfiguration(langfileEN);
        }

        if (language.equalsIgnoreCase("DE"))
        {
            lang = YamlConfiguration.loadConfiguration(langfileDE);
        }

        prefix = ChatColor.translateAlternateColorCodes('&', lang.getString("prefix"));

        //messages
        ConfigurationSection messagesSection = lang.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                messages.put(key, ChatColor.translateAlternateColorCodes('&', messagesSection.getString(key, key)));
            }
        } else {
            getLogger().warning("Could not find 'messages' section in lang.yml");
        }


        File mclangDE = new File(plugin.getDataFolder().getAbsolutePath() + "/mclang/de_de.json");


        if (!mclangDE.exists()) {
            mclangDE.getParentFile().mkdirs();
            plugin.saveResource("mclang/de_de.json", false);
        }



        try {
            File directory = new File(plugin.getDataFolder().getAbsolutePath() + "/mclang");
            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    String jsonString = new String(Files.readAllBytes(file.toPath()));
                    JSONObject jsonObject = new JSONObject(jsonString);
                    languages.put(fileName, jsonObject);
                }
            } else {
                System.out.println("No files found in the directory.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Database getDatabase() {
        return LunaticStorage.db;

    }

    public static String getCoordsAsString(Block block) {
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

    public static byte[] serializeItemStack(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
            e.printStackTrace();
            return null;
        }
    }

    public static boolean containsChestsID(int[] array, int target) {
        for (int num : array) {
            if (num == target) {
                return true;
            }
        }
        return false;
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
            WorldGuardWrapper wgWrapper = WorldGuardWrapper.getInstance();
            Optional<IWrappedFlag<WrappedState>> flag = wgWrapper.getFlag("chest-access", WrappedState.class);
            if (!flag.isPresent()) Bukkit.getLogger().info("WorldGuard flag 'chest-access' is not present!");
            WrappedState state = flag.map(f -> wgWrapper.queryFlag(player, location, f).orElse(WrappedState.DENY)).orElse(WrappedState.DENY);
            return state == WrappedState.ALLOW;
    }

    public static String getLanguage(ItemStack itemStack, String locale) {
        String nameKey = getKey(itemStack);
        JSONObject language = languages.get(locale + ".json");

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

    public static String getKey(ItemStack itemStack){
        Material material = itemStack.getType();
        if(material.isBlock()){
            String id = material.getKey().getKey();
            return "block.minecraft."+id;
        } else if(material.isItem()){
            String id = material.getKey().getKey();
            return "item.minecraft."+id;
        }
        return "block.minecraft.dirt";
    }
}
