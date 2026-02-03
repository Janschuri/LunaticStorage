package de.janschuri.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.commands.Command;
import de.janschuri.lunaticlib.commands.HasParams;
import de.janschuri.lunaticlib.commands.HasParentCommand;
import de.janschuri.lunaticlib.config.CommandMessageKey;
import de.janschuri.lunaticlib.config.LunaticCommandMessageKey;
import de.janschuri.lunaticlib.sender.Sender;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.commands.StorageCommand;
import de.janschuri.lunaticstorage.utils.Logger;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Map;

public class StorageShutdown extends StorageCommand implements HasParentCommand, HasParams {

    private static final StorageShutdown INSTANCE = new StorageShutdown();
    private static final CommandMessageKey HELP_MK = new LunaticCommandMessageKey(INSTANCE, "help")
            .defaultMessage("en", INSTANCE.getDefaultHelpMessage("Enable or disable the shutdown mode."))
            .defaultMessage("de", INSTANCE.getDefaultHelpMessage("Aktiviere oder deaktiviere den Shutdown-Modus."));

    private final CommandMessageKey shutdownEnabledMK = new LunaticCommandMessageKey(this, "shutdown_enabled")
            .defaultMessage("en", "Shutdown mode has been enabled.")
            .defaultMessage("de", "Der Shutdown-Modus wurde aktiviert.");

    private final CommandMessageKey shutdownDisabledMK = new LunaticCommandMessageKey(this, "shutdown_disabled")
            .defaultMessage("en", "Shutdown mode has been disabled.")
            .defaultMessage("de", "Der Shutdown-Modus wurde deaktiviert.");

    private final CommandMessageKey shutdownAlreadyEnabledMK = new LunaticCommandMessageKey(this, "shutdown_already_enabled")
            .defaultMessage("en", "Shutdown mode is already enabled.")
            .defaultMessage("de", "Der Shutdown-Modus ist bereits aktiviert.");

    private final CommandMessageKey shutdownAlreadyDisabledMK = new LunaticCommandMessageKey(this, "shutdown_already_disabled")
            .defaultMessage("en", "Shutdown mode is already disabled.")
            .defaultMessage("de", "Der Shutdown-Modus ist bereits deaktiviert.");

    @Override
    public Command getParentCommand() {
        return new Storage();
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.admin.shutdown";
    }

    @Override
    public String getName() {
        return "shutdown";
    }

    @Override
    public boolean execute(Sender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(getMessage(NO_PERMISSION_MK));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(getMessage(HELP_MK));
            return true;
        }

        String option = args[0].toLowerCase();
        switch (option) {
            case "enable" -> {
                if (!sender.hasPermission("lunaticstorage.admin.shutdown.enable")) {
                    sender.sendMessage(getMessage(NO_PERMISSION_MK));
                    return true;
                }

                if (LunaticStorage.getPluginConfig().isShutdown()) {
                    sender.sendMessage(getMessage(shutdownAlreadyEnabledMK));
                    return true;
                }

                LunaticStorage.getPluginConfig().setShutdown(true);
                sender.sendMessage(getMessage(shutdownEnabledMK));
                Logger.warn("Shutdown mode has been enabled. The server will no longer be able to access LunaticStorage features.");
            }
            case "disable" -> {
                if (!sender.hasPermission("lunaticstorage.admin.shutdown.disable")) {
                    sender.sendMessage(getMessage(NO_PERMISSION_MK));
                    return true;
                }

                if (!LunaticStorage.getPluginConfig().isShutdown()) {
                    sender.sendMessage(getMessage(shutdownAlreadyDisabledMK));
                    return true;
                }

                LunaticStorage.getPluginConfig().setShutdown(false);
                sender.sendMessage(getMessage(shutdownDisabledMK));
                Logger.warn("Shutdown mode has been disabled. The server can now access LunaticStorage features again.");
            }
            default -> sender.sendMessage(getMessage(HELP_MK));
        }

        return true;
    }

    @Override
    public Map<CommandMessageKey, String> getHelpMessages() {
        return Map.of(
                HELP_MK, getPermission()
        );
    }

    @Override
    public List<Component> getParamsNames() {
        return List.of(
                Component.text("enable/disable")
        );
    }

    @Override
    public List<Map<String, String>> getParams() {
        return List.of(
                Map.of(
                        "enable", "lunaticstorage.admin.shutdown.enable",
                        "disable", "lunaticstorage.admin.shutdown.disable"
                )
        );
    }
}
