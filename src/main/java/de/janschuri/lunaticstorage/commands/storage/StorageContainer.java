package de.janschuri.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.commands.Command;
import de.janschuri.lunaticlib.commands.HasParentCommand;
import de.janschuri.lunaticlib.config.CommandMessageKey;
import de.janschuri.lunaticlib.config.LunaticCommandMessageKey;
import de.janschuri.lunaticlib.platform.paper.inventorygui.handler.GUIManager;
import de.janschuri.lunaticlib.sender.PlayerSender;
import de.janschuri.lunaticlib.sender.Sender;
import de.janschuri.lunaticstorage.commands.StorageCommand;
import de.janschuri.lunaticstorage.gui.ContainerGUI;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Map;

public class StorageContainer extends StorageCommand implements HasParentCommand {

    private static final StorageContainer INSTANCE = new StorageContainer();
    private static final CommandMessageKey NOT_CONTAINER_MK = new LunaticCommandMessageKey(INSTANCE, "not_container")
            .defaultMessage("en", "The block you are looking at is not a container.")
            .defaultMessage("de", "Der Block den du anschaust ist kein Container.");
    private static final CommandMessageKey HELP_MK = new LunaticCommandMessageKey(INSTANCE, "help")
            .defaultMessage("en", INSTANCE.getDefaultHelpMessage("Open the container GUI."))
            .defaultMessage("de", INSTANCE.getDefaultHelpMessage("Öffne die Container GUI."));


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
            sender.sendMessage(getMessage(NOT_CONTAINER_MK));
            return true;
        }

        if (!Utils.isStorageContainer(block)) {
            sender.sendMessage(getMessage(NOT_CONTAINER_MK));
            return true;
        }


        de.janschuri.lunaticstorage.storage.StorageContainer container = de.janschuri.lunaticstorage.storage.StorageContainer.getStorageContainer(block);

        GUIManager.openGUI(ContainerGUI.getContainerGUI(player, container), player);
        return true;
    }

    @Override
    public Map<CommandMessageKey, String> getHelpMessages() {
        return Map.of(
                HELP_MK, getPermission()
        );
    }
}
