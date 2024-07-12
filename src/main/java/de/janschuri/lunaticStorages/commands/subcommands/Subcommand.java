package de.janschuri.lunaticStorages.commands.subcommands;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.config.LanguageConfig;
import de.janschuri.lunaticlib.LunaticCommand;
import de.janschuri.lunaticlib.MessageKey;
import de.janschuri.lunaticlib.common.command.AbstractLunaticCommand;

import java.util.List;

public abstract class Subcommand extends AbstractLunaticCommand {

    protected static final MessageKey WRONG_USAGE = new MessageKey("wrong_usage");
    protected static final MessageKey NO_PERMISSION = new MessageKey("no_permission");
    protected static final MessageKey NO_CONSOLE_COMMAND = new MessageKey("no_console_command");

    @Override
    public LanguageConfig getLanguageConfig() {
        return LunaticStorage.getLanguageConfig();
    }
}
