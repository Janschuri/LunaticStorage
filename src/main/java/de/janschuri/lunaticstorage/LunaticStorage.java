package de.janschuri.lunaticstorage;

import de.janschuri.lunaticlib.MessageKey;
import de.janschuri.lunaticlib.platform.bukkit.external.Metrics;
import de.janschuri.lunaticstorage.commands.storage.Storage;
import de.janschuri.lunaticstorage.config.DatabaseConfig;
import de.janschuri.lunaticstorage.config.LanguageConfig;
import de.janschuri.lunaticstorage.config.PluginConfig;
import de.janschuri.lunaticstorage.listener.*;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticlib.platform.bukkit.PlatformImpl;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public final class LunaticStorage extends JavaPlugin {

    private static Map<String, JSONObject> languagesMap = new HashMap<>();
    public static boolean debug;
    private static Path dataDirectory;
    private static Map<Integer, de.janschuri.lunaticstorage.storage.Storage> storages = new HashMap<>();
    private static LunaticStorage instance;
    private static DatabaseConfig databaseConfig;
    private static LanguageConfig languageConfig;
    private static PluginConfig pluginConfig;
    private GlowingBlocks glowingBlocks;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        dataDirectory = getDataFolder().toPath();
        glowingBlocks = new GlowingBlocks(this);

        loadConfig();

        int pluginId = 21912;
        Metrics metrics = new Metrics(this, pluginId);

        new PlatformImpl().registerCommand(this, new Storage());

        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new ChestClickListener(), this);
        getServer().getPluginManager().registerEvents(new PanelClickListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new ContainerEditListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private static void disable() {
        Logger.errorLog("Disabling LunaticStorage...");
        Bukkit.getServer().getPluginManager().disablePlugin(getInstance());
    }

    public static Path getDataDirectory() {
        return dataDirectory;
    }

    public static LunaticStorage getInstance() {
        return instance;
    }

    public static boolean loadConfig() {

        pluginConfig = new PluginConfig(dataDirectory);
        pluginConfig.load();
        languageConfig = new LanguageConfig(dataDirectory, pluginConfig.getLanguageKey());
        languageConfig.load();

        File mclangDE = new File(getInstance().getDataFolder().getAbsolutePath() + "/mclang/de_de.json");


        if (!mclangDE.exists()) {
            mclangDE.getParentFile().mkdirs();
            getInstance().saveResource("mclang/de_de.json", false);
        }

        try {
            File directory = new File(getInstance().getDataFolder().getAbsolutePath() + "/mclang");
            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    String jsonString = new String(Files.readAllBytes(file.toPath()));
                    JSONObject jsonObject = new JSONObject(jsonString);
                    languagesMap.put(fileName, jsonObject);
                }
            } else {
                System.out.println("No files found in the directory.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static Map<String, JSONObject> getLanguagesMap() {
        return languagesMap;
    }

    public static de.janschuri.lunaticstorage.storage.Storage getStorage(int id) {
        return storages.get(id);
    }

    public static boolean storageExists(int id) {
        return storages.containsKey(id);
    }

    public static void addStorage(int id, de.janschuri.lunaticstorage.storage.Storage storage) {
        storages.put(id, storage);
    }
    public static void removeStorage(int id) {
        storages.remove(id);
    }

    public static PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public static LanguageConfig getLanguageConfig() {
        return languageConfig;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static GlowingBlocks getGlowingBlocks() {
        return getInstance().glowingBlocks;
    }

    public static Component getMessage(MessageKey key) {
        return LunaticStorage.getLanguageConfig().getMessage(key);
    }

    public static String getMessageAsLegacyString(MessageKey key) {
        return LunaticStorage.getLanguageConfig().getMessageAsLegacyString(key);
    }
}
