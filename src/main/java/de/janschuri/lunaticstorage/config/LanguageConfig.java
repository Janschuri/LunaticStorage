package de.janschuri.lunaticstorage.config;

import de.janschuri.lunaticlib.MessageKey;
import de.janschuri.lunaticlib.common.config.LunaticLanguageConfigImpl;
import de.janschuri.lunaticstorage.LunaticStorage;
import net.kyori.adventure.text.Component;

import java.nio.file.Path;

public class LanguageConfig extends LunaticLanguageConfigImpl {

    public LanguageConfig(Path dataDirectory, String languageKey) {
        super(dataDirectory, languageKey);
    }
}
