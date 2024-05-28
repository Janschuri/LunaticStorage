package de.janschuri.lunaticStorages.commands.subcommands.storage;

import de.janschuri.lunaticStorages.commands.subcommands.Subcommand;
import de.janschuri.lunaticStorages.config.LanguageConfig;
import de.janschuri.lunaticlib.Sender;
import de.janschuri.lunaticlib.common.commands.AbstractSubcommand;

public class StorageSubcommand extends Subcommand {
    private static final String MAIN_COMMAND = "storage";
    private static final String PERMISSION = "lunaticstorages.admin";
    private static final String NAME = "storage";


    public StorageSubcommand() {
        super(MAIN_COMMAND, NAME, PERMISSION, new Subcommand[] {
                    new ItemSubcommand(),
                    new PanelSubcommand(),
                    new RandomSubcommand(),
                    new ReloadSubcommand(),
                }
        );
    }

    @Override
    public boolean execute(Sender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(LanguageConfig.getLanguageConfig().getPrefix() + LanguageConfig.getLanguageConfig().getMessage("no_permission"));
        } else {
            if (args.length == 0) {

            } else {
                final String subcommand = args[0];

                for (AbstractSubcommand sc : subcommands) {
                    if (LanguageConfig.getLanguageConfig().checkIsSubcommand(NAME, sc.getName(), subcommand)) {
                        String[] newArgs = new String[args.length - 1];
                        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
                        return sc.execute(sender, newArgs);
                    }
                }
                sender.sendMessage(LanguageConfig.getLanguageConfig().getPrefix() + LanguageConfig.getLanguageConfig().getMessage("wrong_usage"));
            }
        }
        return true;
    }
}
