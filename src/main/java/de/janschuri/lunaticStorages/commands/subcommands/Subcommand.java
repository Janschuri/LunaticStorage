package de.janschuri.lunaticStorages.commands.subcommands;

import de.janschuri.lunaticStorages.config.LanguageConfig;
import de.janschuri.lunaticlib.common.commands.AbstractSubcommand;

import java.util.List;

public abstract class Subcommand extends AbstractSubcommand {

    protected Subcommand(String mainCommand, String name, String permission) {
        super(LanguageConfig.getLanguageConfig(), mainCommand, name, permission);
    }

    protected Subcommand(String mainCommand, String name, String permission, List<String> params) {
        super(LanguageConfig.getLanguageConfig(), mainCommand, name, permission, params);
    }

    protected Subcommand(String mainCommand, String name, String permission, AbstractSubcommand[] subcommands) {
        super(LanguageConfig.getLanguageConfig(), mainCommand, name, permission, subcommands);
    }
}
