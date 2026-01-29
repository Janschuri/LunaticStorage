package de.janschuri.lunaticstorage.commands;

import de.janschuri.lunaticlib.commands.LunaticCommand;
import de.janschuri.lunaticlib.config.LunaticLanguageConfig;
import de.janschuri.lunaticlib.config.LunaticMessageKey;
import de.janschuri.lunaticlib.config.MessageKey;
import de.janschuri.lunaticlib.sender.Sender;
import de.janschuri.lunaticstorage.LunaticStorage;
import net.kyori.adventure.text.Component;

public abstract class StorageCommand extends LunaticCommand {

    protected static final MessageKey WRONG_USAGE_MK = new LunaticMessageKey("wrong_usage")
            .defaultMessage("en", "Wrong usage! Please check the command syntax.")
            .defaultMessage("de", "Falsche Verwendung! Bitte überprüfe die Befehlsyntax.");
    protected static final MessageKey NO_PERMISSION_MK = new LunaticMessageKey("no_permission")
            .defaultMessage("en", "You don't have permission to execute this command.")
            .defaultMessage("de", "Du hast keine Berechtigung, diesen Befehl auszuführen.");
    protected static final MessageKey NO_CONSOLE_COMMAND_MK = new LunaticMessageKey("no_console_command")
            .defaultMessage("en", "This command can only be executed by a player.")
            .defaultMessage("de", "Dieser Befehl kann nur von einem Spieler ausgeführt werden.");
    protected static final MessageKey NO_NUMBER_MK = new LunaticMessageKey("no_number")
            .defaultMessage("en", "Please enter a whole number.")
            .defaultMessage("de", "Bitte gib eine ganze Zahl ein.");
    protected static final MessageKey YES_MK = new LunaticMessageKey("yes")
            .defaultMessage("en", "Yes")
            .defaultMessage("de", "Ja");
    protected static final MessageKey NO_MK = new LunaticMessageKey("no")
            .defaultMessage("en", "No")
            .defaultMessage("de", "Nein");
    protected static final MessageKey TYPE_MK = new LunaticMessageKey("item_type")
            .defaultMessage("en", "type")
            .defaultMessage("de", "Typ");

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
