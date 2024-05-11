package de.janschuri.lunaticStorages.commands.subcommands.storage;

import de.janschuri.lunaticStorages.config.Language;
import de.janschuri.lunaticlib.commands.AbstractHelpSubcommand;

public class HelpCommand extends AbstractHelpSubcommand {

    private static final String NAME = "help";
    private static final String PERMISSION = "lunaticstorages.admin.help";
    private static final String MAIN_COMMAND = "storage";

    public HelpCommand() {
        super(Language.getLanguage(), MAIN_COMMAND, NAME, PERMISSION, StorageSubcommand.class);
    }
}
