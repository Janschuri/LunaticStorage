package de.janschuri.lunaticstorage.config;

import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticlib.common.config.LunaticConfigImpl;
import org.bukkit.Material;

import java.nio.file.Path;

public class PluginConfig extends LunaticConfigImpl {

    private static final String CONFIG_FILE = "config.yml";
    private String languageKey;


    public PluginConfig(Path dataDirectory) {
        super(dataDirectory, CONFIG_FILE, "config.yml");
    }

    public void load() {
        super.load();
        LunaticStorage.debug = getBoolean("debug", false);
    }

    public String getLanguageKey() {
        return getString("language", "EN");
    }

    public Material getStorageItem() {
        String storageItem = getString("storage_item", "DIAMOND");
        return Material.getMaterial(storageItem);
    }

    public Material getStoragePanelBlock() {
        String panelBlock = getString("panel_block", "LODESTONE");
        return Material.getMaterial(panelBlock);
    }

    public Material getRangeItem() {
        String rangeItem = getString("range_item", "MAGMA_CREAM");
        return Material.getMaterial(rangeItem);
    }

    public int getDefaultRangeItem() {
        return getInt("default_range_item", 5);
    }

    public int getDefaultRange() {
        return getInt("default_range", 5);
    }
}
