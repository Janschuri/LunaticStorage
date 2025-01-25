package de.janschuri.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.*;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.GUIManager;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.commands.Subcommand;
import de.janschuri.lunaticstorage.config.LanguageConfig;
import de.janschuri.lunaticstorage.gui.ContainerListGUI;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class StorageRemove extends Subcommand {

    private final MessageKey removedMK = new CommandMessageKey(this, "removed")
            .defaultMessage("§aSuccessfully removed the container from storageitem in %world% at %x% %y% %z%");
    private final MessageKey wrongItemMK = new CommandMessageKey(this, "wrong_item")
            .defaultMessage("§cYou need to hold a storageitem in your hand!");
    private final MessageKey noWorldMK = new CommandMessageKey(this, "no_world")
            .defaultMessage("§cPlease specify a world uuid!");
    private final MessageKey worldNotFoundMK = new CommandMessageKey(this, "world_not_found")
            .defaultMessage("§cWorld not with uuid %uuid% not found!");
    private final MessageKey noContainerMK = new CommandMessageKey(this, "no_container")
            .defaultMessage("§cPlease specify a container!");
    private final MessageKey confirmMK = new CommandMessageKey(this, "confirm")
            .defaultMessage("§cAre you sure you want to remove the container from the storageitem?");

    @Override
    public LunaticCommand getParentCommand() {
        return new Storage();
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.storage.use";
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public boolean execute(Sender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(getMessage(NO_PERMISSION_MK));
            return true;
        }

        if (!(sender instanceof PlayerSender)) {
            sender.sendMessage(getMessage(NO_CONSOLE_COMMAND_MK));
            return true;
        }

        PlayerSender playerSender = (PlayerSender) sender;

        Player player = Bukkit.getPlayer(playerSender.getUniqueId());

        if (player == null) {
            Logger.errorLog("Player is null!");
            return false;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!Utils.isStorageItem(item)) {
            playerSender.sendMessage(getMessage(wrongItemMK));
            return true;
        }

        if (args.length < 1) {
            playerSender.sendMessage(getMessage(noWorldMK));
            return true;
        }

        String uuidString = args[0];

        if (!Utils.isUUID(uuidString)) {
            playerSender.sendMessage(getMessage(noWorldMK));
            return true;
        }

        UUID worldUUID = UUID.fromString(uuidString);

        World world = Bukkit.getWorld(worldUUID);

        if (world == null) {
            playerSender.sendMessage(getMessage(worldNotFoundMK).replaceText(getTextReplacementConfig("%uuid%", worldUUID.toString())));
            return true;
        }

        if (args.length < 2) {
            playerSender.sendMessage(getMessage(noContainerMK));
            return true;
        }

        String containerCoords = args[1];

        Location location = Utils.deserializeCoords(containerCoords, worldUUID);

        if (location == null) {
            playerSender.sendMessage(getMessage(noContainerMK));
            return true;
        }

        if (!Utils.isContainer(location.getBlock())) {
            playerSender.sendMessage(getMessage(noContainerMK));
            return true;
        }

        StorageContainer container = StorageContainer.getStorageContainer(location.getBlock());

        Utils.removeContainerFromStorageItem(container, item);
        playerSender.sendMessage(getMessage(removedMK)
                .replaceText(getTextReplacementConfig("%world%", world.getName()))
                .replaceText(getTextReplacementConfig("%x%", String.valueOf(location.getBlockX())))
                .replaceText(getTextReplacementConfig("%y%", String.valueOf(location.getBlockY())))
                .replaceText(getTextReplacementConfig("%z%", String.valueOf(location.getBlockZ()))));

        return true;
    }
}
