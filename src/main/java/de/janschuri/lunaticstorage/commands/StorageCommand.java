package de.janschuri.lunaticstorage.commands;

import de.janschuri.lunaticlib.Sender;
import de.janschuri.lunaticlib.common.command.LunaticCommand;
import de.janschuri.lunaticlib.common.config.LunaticLanguageConfig;
import de.janschuri.lunaticlib.common.config.LunaticMessageKey;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.config.LanguageConfig;
import de.janschuri.lunaticlib.MessageKey;
import net.kyori.adventure.text.Component;

public abstract class StorageCommand extends LunaticCommand {

    protected static final MessageKey WRONG_USAGE_MK = new LunaticMessageKey("wrong_usage");
    protected static final MessageKey NO_PERMISSION_MK = new LunaticMessageKey("no_permission");
    protected static final MessageKey NO_CONSOLE_COMMAND_MK = new LunaticMessageKey("no_console_command");
    protected static final MessageKey NO_NUMBER_MK = new LunaticMessageKey("no_number");
    protected static final MessageKey YES_MK = new LunaticMessageKey("yes");
    protected static final MessageKey NO_MK = new LunaticMessageKey("no");

    @Override
    public LunaticLanguageConfig getLanguageConfig() {
        return LunaticStorage.getLanguageConfig();
    }

    @Override
    public Component noPermissionMessage(Sender sender, String[] strings) {
        return getMessage(NO_PERMISSION_MK);
    }

    @Override
    public Component wrongUsageMessage(Sender sender, String[] strings) {
        return getMessage(WRONG_USAGE_MK);
    }
}
