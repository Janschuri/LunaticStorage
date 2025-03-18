package de.janschuri.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.Command;
import de.janschuri.lunaticlib.CommandMessageKey;
import de.janschuri.lunaticlib.PlayerSender;
import de.janschuri.lunaticlib.Sender;
import de.janschuri.lunaticlib.common.command.HasParentCommand;
import de.janschuri.lunaticlib.common.config.LunaticCommandMessageKey;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.GUIManager;
import de.janschuri.lunaticstorage.commands.StorageCommand;
import de.janschuri.lunaticstorage.gui.ContainerGUI;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Map;

public class StorageContainer extends StorageCommand implements HasParentCommand {

    private final CommandMessageKey notContainerMK = new LunaticCommandMessageKey(this, "not_container");

    @Override
    public Command getParentCommand() {
        return new Storage();
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.storage.container";
    }

    @Override
    public String getName() {
        return "container";
    }

    @Override
    public boolean execute(Sender sender, String[] args) {
        if (!(sender instanceof PlayerSender)) {
            sender.sendMessage(getMessage(NO_CONSOLE_COMMAND_MK));
            return true;
        }

        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(getMessage(NO_PERMISSION_MK));
            return true;
        }

        Player player = Bukkit.getPlayer(((PlayerSender) sender).getUniqueId());
        Block block = player.getTargetBlockExact(5);

        if (block == null) {
            sender.sendMessage(getMessage(notContainerMK));
            return true;
        }

        if (!Utils.isStorageContainer(block)) {
            sender.sendMessage(getMessage(notContainerMK));
            return true;
        }


        de.janschuri.lunaticstorage.storage.StorageContainer container = de.janschuri.lunaticstorage.storage.StorageContainer.getStorageContainer(block);

        GUIManager.openGUI(ContainerGUI.getContainerGUI(player, container), player);
        return true;
    }

    @Override
    public Map<CommandMessageKey, String> getHelpMessages() {
        return Map.of();
    }
}
