package de.janschuri.lunaticStorages.commands.subcommands.storage;

import de.janschuri.lunaticStorages.commands.subcommands.Subcommand;
import de.janschuri.lunaticlib.commands.AbstractSubcommand;
import de.janschuri.lunaticlib.senders.AbstractSender;

public class StorageSubcommand extends Subcommand {
    private static final String MAIN_COMMAND = "storage";
    private static final String PERMISSION = "lunaticstorages.admin";
    private static final String NAME = "storage";

    private final HelpCommand helpSubcommand = new HelpCommand();

    public StorageSubcommand() {
        super(MAIN_COMMAND, NAME, PERMISSION, new AbstractSubcommand[] {
                    new HelpCommand(),
                    new ItemSubcommand(),
                    new PanelSubcommand(),
                    new RandomSubcommand(),
                    new ReloadSubcommand(),
                }
        );
    }

    @Override
    public boolean execute(AbstractSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(language.getPrefix() + language.getMessage("no_permission"));
        } else {
            if (args.length == 0) {
                helpSubcommand.execute(sender, args);
            } else {
                final String subcommand = args[0];

                for (AbstractSubcommand sc : subcommands) {
                    if (language.checkIsSubcommand(NAME, sc.getName(), subcommand)) {
                        String[] newArgs = new String[args.length - 1];
                        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
                        return sc.execute(sender, newArgs);
                    }
                }
                sender.sendMessage(language.getPrefix() + language.getMessage("wrong_usage"));
            }
        }
        return true;
    }
}
