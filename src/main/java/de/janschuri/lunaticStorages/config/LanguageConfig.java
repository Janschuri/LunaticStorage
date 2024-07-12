package de.janschuri.lunaticStorages.config;

import de.janschuri.lunaticlib.common.config.LunaticLanguageConfigImpl;

import java.nio.file.Path;

public class LanguageConfig extends LunaticLanguageConfigImpl {
    public LanguageConfig(Path dataDirectory, String languageKey) {
        super(dataDirectory, languageKey);
    }
}
