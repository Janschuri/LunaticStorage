package net.professoradam.lunaticstorage;

import de.janschuri.lunaticlib.MessageKey;
import de.janschuri.lunaticlib.Placeholder;
import de.janschuri.lunaticlib.platform.bukkit.external.Metrics;
import net.professoradam.lunaticstorage.commands.storage.Storage;
import net.professoradam.lunaticstorage.config.LanguageConfig;
import net.professoradam.lunaticstorage.config.PluginConfig;
import net.professoradam.lunaticstorage.listener.*;
import net.professoradam.lunaticstorage.utils.Logger;
import de.janschuri.lunaticlib.platform.bukkit.PlatformImpl;
import fr.skytasul.glowingentities.GlowingBlocks;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public final class LunaticStorage extends JavaPlugin {

    private static Map<String, JSONObject> languagesMap = new HashMap<>();
    private static boolean debug;
    private static Path dataDirectory;
    private static LunaticStorage instance;
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

        int pluginId = 24545;
        Metrics metrics = new Metrics(this, pluginId);

        new PlatformImpl().registerCommand(this, new Storage());

        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new ChestClickListener(), this);
        getServer().getPluginManager().registerEvents(new PanelClickListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryMoveItemListener(), this);

        getServer().getPluginManager().registerEvents(new ContainerEditListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryChangeListener(), this);
    }

    @Override
    public void onDisable() {

    }

    public static LunaticStorage getInstance() {
        return instance;
    }

    public static boolean loadConfig() {

        pluginConfig = new PluginConfig(dataDirectory);
        pluginConfig.load();

        debug = pluginConfig.isDebug();

        if (pluginConfig.isDebug()) {
            Logger.infoLog("Debug mode enabled.");
        }

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

    public static Component getMessage(MessageKey key, Placeholder... placeholders) {
        return LunaticStorage.getLanguageConfig().getMessage(key, placeholders);
    }

    public static String getMessageAsLegacyString(MessageKey key) {
        return LunaticStorage.getLanguageConfig().getMessageAsLegacyString(key);
    }
}
