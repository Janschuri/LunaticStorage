package de.janschuri.lunaticStorages.commands.subcommands.storage;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.commands.subcommands.Subcommand;
import de.janschuri.lunaticStorages.config.Language;
import de.janschuri.lunaticlib.senders.AbstractPlayerSender;
import de.janschuri.lunaticlib.senders.AbstractSender;

public class ReloadSubcommand extends Subcommand {

    private static final String MAIN_COMMAND = "storage";
    private static final String NAME = "reload";
    private static final String PERMISSION = "lunaticstorages.admin.reload";


    protected ReloadSubcommand() {
        super(MAIN_COMMAND, NAME, PERMISSION);
    }

    @Override
    public boolean execute(AbstractSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(language.getPrefix() + language.getMessage("no_permission"));
        } else {
            LunaticStorage.loadConfig();
            sender.sendMessage(Language.getLanguage().getPrefix() + Language.getLanguage().getMessage("reload"));
        }
        return true;
    }
}
