package de.janschuri.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.*;
import de.janschuri.lunaticlib.common.command.HasParentCommand;
import de.janschuri.lunaticlib.common.config.LunaticCommandMessageKey;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.GUIManager;
import de.janschuri.lunaticstorage.commands.StorageCommand;
import de.janschuri.lunaticstorage.gui.ContainerListGUI;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class StorageCheck extends StorageCommand implements HasParentCommand {

    private final MessageKey rangeItemMK = new LunaticCommandMessageKey(this, "range_item")
            .defaultMessage("en", "The range of this rangeitem is: %range%")
            .defaultMessage("de", "Die Reichweite dieses Rangeitems ist: %range%");
    private final MessageKey panelMK = new LunaticCommandMessageKey(this, "panel")
            .defaultMessage("en", "The range of this panel is: %range%")
            .defaultMessage("de", "Die Reichweite dieses Panels ist: %range%");
    private final MessageKey wrongItemMK = new LunaticCommandMessageKey(this, "wrong_item")
            .defaultMessage("en", "You have to hold a storageitem, rangeitem or a panel in your hand.")
            .defaultMessage("de", "Du musst ein Storageitem, Rangeitem oder ein Panel in der Hand halten.");

    @Override
    public Command getParentCommand() {
        return new Storage();
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.storage.check";
    }

    @Override
    public String getName() {
        return "check";
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

        if (Utils.isStorageItem(item)) {
            GUIManager.openGUI(new ContainerListGUI(item), player);

            return true;
        }

        if (Utils.isRangeItem(item)) {
            playerSender.sendMessage(
                    getMessage(rangeItemMK,
                            placeholder("%range%", Utils.getRangeFromItem(item) + ""))
            );
            return true;
        }

        if (Utils.isPanelBlockItem(item)) {
            playerSender.sendMessage(
                    getMessage(panelMK,
                            placeholder("%range%", Utils.getRangeFromPanelBlockItem(item) + ""))
            );
            return true;
        }

        playerSender.sendMessage(getMessage(wrongItemMK));

        return true;
    }

    @Override
    public Map<CommandMessageKey, String> getHelpMessages() {
        return Map.of();
    }
}
