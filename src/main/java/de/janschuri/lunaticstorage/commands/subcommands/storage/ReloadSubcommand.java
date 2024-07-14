package de.janschuri.lunaticstorage.commands.subcommands.storage;

import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.commands.subcommands.Subcommand;
import de.janschuri.lunaticlib.CommandMessageKey;
import de.janschuri.lunaticlib.LunaticCommand;
import de.janschuri.lunaticlib.Sender;

public class ReloadSubcommand extends Subcommand {

    private final CommandMessageKey reloadedMK = new CommandMessageKey(this, "reloaded");

    @Override
    public LunaticCommand getParentCommand() {
        return new StorageSubcommand();
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.admin.reload";
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public boolean execute(Sender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(getMessage(NO_PERMISSION));
            return true;
        }


            LunaticStorage.loadConfig();
            sender.sendMessage(getMessage(reloadedMK));

        return true;
    }
}
