package de.janschuri.lunaticStorages.config;

import de.janschuri.lunaticlib.common.config.AbstractLanguageConfig;

import java.nio.file.Path;

public class LanguageConfig extends AbstractLanguageConfig {

    private static LanguageConfig instance;

    public LanguageConfig(Path dataDirectory, String[] commands) {
        super(dataDirectory, commands, PluginConfig.getLanguageKey());
        instance = this;
        load();
    }

    public void load() {
        super.load();
    }

    public static LanguageConfig getLanguageConfig() {
        return instance;
    }
}
