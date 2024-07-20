package de.janschuri.lunaticstorage.commands;

import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.config.LanguageConfig;
import de.janschuri.lunaticlib.MessageKey;
import de.janschuri.lunaticlib.common.command.AbstractLunaticCommand;

public abstract class Subcommand extends AbstractLunaticCommand {

    protected static final MessageKey WRONG_USAGE_MK = new MessageKey("wrong_usage");
    protected static final MessageKey NO_PERMISSION_MK = new MessageKey("no_permission");
    protected static final MessageKey NO_CONSOLE_COMMAND_MK = new MessageKey("no_console_command");
    protected static final MessageKey NO_NUMBER_MK = new MessageKey("no_number");

    @Override
    public LanguageConfig getLanguageConfig() {
        return LunaticStorage.getLanguageConfig();
    }
}
