package de.janschuri.lunaticstorage.commands.storage;

import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.commands.Subcommand;
import de.janschuri.lunaticlib.CommandMessageKey;
import de.janschuri.lunaticlib.LunaticCommand;
import de.janschuri.lunaticlib.Sender;

public class StorageReload extends Subcommand {

    private final CommandMessageKey reloadedMK = new CommandMessageKey(this, "reloaded");

    @Override
    public LunaticCommand getParentCommand() {
        return new Storage();
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
            sender.sendMessage(getMessage(NO_PERMISSION_MK));
            return true;
        }


            LunaticStorage.loadConfig();
            sender.sendMessage(getMessage(reloadedMK));

        return true;
    }
}
