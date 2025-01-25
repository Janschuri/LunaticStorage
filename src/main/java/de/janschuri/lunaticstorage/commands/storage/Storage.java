package de.janschuri.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.common.command.LunaticHelpCommand;
import de.janschuri.lunaticstorage.commands.Subcommand;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticlib.LunaticCommand;
import de.janschuri.lunaticlib.Sender;

import java.util.List;

public class Storage extends Subcommand {

    @Override
    public List<LunaticCommand> getSubcommands() {
        return List.of(
            new StorageRandom(),
            new StorageReload(),
            new StorageGet(),
            new StorageCreate(),
            new StorageContainerSettings(),
            new StorageCheck(),
            getHelpCommand()
        );
    }

    @Override
    public LunaticHelpCommand getHelpCommand() {
        return new LunaticHelpCommand(getLanguageConfig(), this);
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.storage";
    }

    @Override
    public String getName() {
        return "storage";
    }

    @Override
    public boolean execute(Sender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(getMessage(NO_PERMISSION_MK));
            return true;
        }
        if (args.length == 0) {
            getHelpCommand().execute(sender, args);
            return true;
        }

        final String subcommand = args[0];

        for (LunaticCommand sc : getSubcommands()) {
            if (checkIsSubcommand(sc, subcommand)) {
                String[] newArgs = new String[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, args.length - 1);
                return sc.execute(sender, newArgs);
            }
        }
        getHelpCommand().execute(sender, args);


        return true;
    }
}
