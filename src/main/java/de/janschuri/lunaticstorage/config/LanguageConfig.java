package de.janschuri.lunaticstorage.config;

import de.janschuri.lunaticlib.config.LunaticLanguageConfig;
import de.janschuri.lunaticstorage.LunaticStorage;

import java.nio.file.Path;

public class LanguageConfig extends LunaticLanguageConfig {

    public LanguageConfig(Path dataDirectory, String languageKey) {
        super(dataDirectory, languageKey);
    }

    @Override
    protected String getPackage() {
        return LunaticStorage.class.getPackageName();
    }
}
