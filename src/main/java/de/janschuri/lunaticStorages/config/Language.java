package de.janschuri.lunaticStorages.config;

import java.nio.file.Path;
import java.util.Map;

public class Language extends de.janschuri.lunaticlib.config.Language {

    private static Language instance;

    public Language(Path dataDirectory, String[] commands) {
        super(dataDirectory, commands, PluginConfig.getLanguageKey());
        instance = this;
        load();
    }

    public void load() {
        super.load();
    }

    public static Language getLanguage() {
        return instance;
    }
}
