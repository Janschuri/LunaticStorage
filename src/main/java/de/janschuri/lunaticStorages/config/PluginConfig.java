package de.janschuri.lunaticStorages.config;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticlib.common.config.AbstractConfig;
import org.bukkit.Material;

import java.nio.file.Path;

public class PluginConfig extends AbstractConfig {

    private static PluginConfig instance;
    private static final String CONFIG_FILE = "config.yml";
    private String languageKey;
    private String storageItem;
    private String storagePanelBlock;


    public PluginConfig(Path dataDirectory) {
        super(dataDirectory, CONFIG_FILE, "config.yml");
        instance = this;
        load();
    }

    public void load() {
        super.load();
        languageKey = getString("language", "EN");
        storageItem = getString("storage_item", "DIAMOND");
        storagePanelBlock = getString("panel_block", "LODESTONE");
        LunaticStorage.debug = getBoolean("debug", false);
    }

    public static String getLanguageKey() {
        return instance.languageKey;
    }

    public static PluginConfig getConfig() {
        return instance;
    }

    public static Material getStorageItem() {
        return Material.getMaterial(instance.storageItem);
    }

    public static Material getStoragePanelBlock() {
        return Material.getMaterial(instance.storagePanelBlock);
    }

}
