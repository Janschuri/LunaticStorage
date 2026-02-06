package de.janschuri.lunaticstorage.config;

import de.janschuri.lunaticlib.config.HasMessageKeys;
import de.janschuri.lunaticlib.config.LunaticLanguageConfig;
import de.janschuri.lunaticlib.config.LunaticMessageKey;
import de.janschuri.lunaticlib.config.MessageKey;
import de.janschuri.lunaticstorage.LunaticStorage;
import net.kyori.adventure.text.Component;

import java.nio.file.Path;

import static de.janschuri.lunaticstorage.LunaticStorage.getMessage;

public class LanguageConfig extends LunaticLanguageConfig implements HasMessageKeys {

    public LanguageConfig(Path dataDirectory, String languageKey) {
        super(dataDirectory, languageKey);
    }

    @Override
    protected String getPackage() {
        return LunaticStorage.class.getPackageName();
    }

    public static final MessageKey SHUTDOWN_MK = new LunaticMessageKey("plugin_shutdown")
            .defaultMessage("en", "The plugin is currently in shutdown mode. Please try again later.")
            .defaultMessage("de", "Das Plugin befindet sich derzeit im Shutdown-Modus. Bitte versuchen es später erneut.");


    public static Component getShutdownMessage() {
        return LunaticStorage.getMessage(SHUTDOWN_MK);
    }
}
