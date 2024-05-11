package de.janschuri.lunaticStorages.commands.subcommands;

import de.janschuri.lunaticStorages.config.Language;
import de.janschuri.lunaticlib.commands.AbstractSubcommand;

import java.util.List;

public abstract class Subcommand extends AbstractSubcommand {

    protected Subcommand(String mainCommand, String name, String permission) {
        super(Language.getLanguage(), mainCommand, name, permission);
    }

    protected Subcommand(String mainCommand, String name, String permission, List<String> params) {
        super(Language.getLanguage(), mainCommand, name, permission, params);
    }

    protected Subcommand(String mainCommand, String name, String permission, AbstractSubcommand[] subcommands) {
        super(Language.getLanguage(), mainCommand, name, permission, subcommands);
    }
}
