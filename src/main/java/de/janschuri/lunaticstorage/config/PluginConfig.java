package de.janschuri.lunaticstorage.config;

import de.janschuri.lunaticlib.config.HasMessageKeys;
import de.janschuri.lunaticlib.config.LunaticConfig;
import de.janschuri.lunaticlib.config.LunaticMessageKey;
import de.janschuri.lunaticlib.config.MessageKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.nio.file.Path;
import java.util.List;

import static de.janschuri.lunaticstorage.LunaticStorage.getMessage;

public class PluginConfig extends LunaticConfig {

    private static final String CONFIG_FILE = "config.yml";
    private String languageKey;

    public PluginConfig(Path dataDirectory) {
        super(dataDirectory, CONFIG_FILE);
    }

    public void load() {
        super.load("config.yml");
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

    public void setShutdown(boolean shutdown) {
        setBoolean("shutdown", shutdown);
    }

    public boolean isShutdown() {
        return getBoolean("shutdown", false);
    }

    public boolean isDebug() {
        return getBoolean("debug", false);
    }

    public int getDefaultRangeItem() {
        return getInt("default_range_item", 5);
    }

    public int getDefaultRangePanel() {
        return getInt("default_range", 5);
    }

    public List<String> getLocales() {
        return getStringList("locales");
    }
}
