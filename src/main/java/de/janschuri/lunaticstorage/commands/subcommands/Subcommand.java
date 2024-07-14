package de.janschuri.lunaticstorage.commands.subcommands;

import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.config.LanguageConfig;
import de.janschuri.lunaticlib.MessageKey;
import de.janschuri.lunaticlib.common.command.AbstractLunaticCommand;

public abstract class Subcommand extends AbstractLunaticCommand {

    protected static final MessageKey WRONG_USAGE = new MessageKey("wrong_usage");
    protected static final MessageKey NO_PERMISSION = new MessageKey("no_permission");
    protected static final MessageKey NO_CONSOLE_COMMAND = new MessageKey("no_console_command");

    @Override
    public LanguageConfig getLanguageConfig() {
        return LunaticStorage.getLanguageConfig();
    }
}
