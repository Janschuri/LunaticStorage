package de.janschuri.lunaticStorages.commands.subcommands.storage;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.commands.subcommands.Subcommand;
import de.janschuri.lunaticStorages.config.LanguageConfig;
import de.janschuri.lunaticlib.Sender;

public class ReloadSubcommand extends Subcommand {

    private static final String MAIN_COMMAND = "storage";
    private static final String NAME = "reload";
    private static final String PERMISSION = "lunaticstorages.admin.reload";


    protected ReloadSubcommand() {
        super(MAIN_COMMAND, NAME, PERMISSION);
    }

    @Override
    public boolean execute(Sender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(LanguageConfig.getLanguageConfig().getPrefix() + LanguageConfig.getLanguageConfig().getMessage("no_permission"));
        } else {
            LunaticStorage.loadConfig();
            sender.sendMessage(LanguageConfig.getLanguageConfig().getPrefix() + LanguageConfig.getLanguageConfig().getMessage("reload"));
        }
        return true;
    }
}
