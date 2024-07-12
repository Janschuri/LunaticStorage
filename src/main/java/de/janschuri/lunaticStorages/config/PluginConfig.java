package de.janschuri.lunaticStorages.config;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticlib.common.config.LunaticConfigImpl;
import org.bukkit.Material;

import java.nio.file.Path;

public class PluginConfig extends LunaticConfigImpl {

    private static final String CONFIG_FILE = "config.yml";
    private String languageKey;
    private String storageItem;
    private String storagePanelBlock;


    public PluginConfig(Path dataDirectory) {
        super(dataDirectory, CONFIG_FILE, "config.yml");
    }

    public void load() {
        super.load();
        languageKey = getString("language", "EN");
        storageItem = getString("storage_item", "DIAMOND");
        storagePanelBlock = getString("panel_block", "LODESTONE");
        LunaticStorage.debug = getBoolean("debug", false);
    }

    public String getLanguageKey() {
        return languageKey;
    }

    public Material getStorageItem() {
        return Material.getMaterial(storageItem);
    }

    public Material getStoragePanelBlock() {
        return Material.getMaterial(storagePanelBlock);
    }

}
