package net.professoradam.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.*;
import de.janschuri.lunaticlib.common.command.HasParentCommand;
import de.janschuri.lunaticlib.common.config.LunaticCommandMessageKey;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.GUIManager;
import net.professoradam.lunaticstorage.commands.StorageCommand;
import net.professoradam.lunaticstorage.gui.ContainerListGUI;
import net.professoradam.lunaticstorage.utils.Logger;
import net.professoradam.lunaticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class StorageCheck extends StorageCommand implements HasParentCommand {

    private static final StorageCheck INSTANCE = new StorageCheck();

    private static final MessageKey RANGE_ITEM_MK = new LunaticCommandMessageKey(INSTANCE, "range_item")
            .defaultMessage("en", "The range of this rangeitem is: %range%")
            .defaultMessage("de", "Die Reichweite dieses Rangeitems ist: %range%");
    private static final MessageKey PANEL_MK = new LunaticCommandMessageKey(INSTANCE, "panel")
            .defaultMessage("en", "The range of this panel is: %range%")
            .defaultMessage("de", "Die Reichweite dieses Panels ist: %range%");
    private static final MessageKey WRONG_ITEM_MK = new LunaticCommandMessageKey(INSTANCE, "wrong_item")
            .defaultMessage("en", "You have to hold a storageitem, rangeitem or a panel in your hand.")
            .defaultMessage("de", "Du musst ein Storageitem, Rangeitem oder ein Panel in der Hand halten.");
    private static final CommandMessageKey HELP_MK = new LunaticCommandMessageKey(INSTANCE, "help")
            .defaultMessage("en", INSTANCE.getDefaultHelpMessage("Check the information of an item."))
            .defaultMessage("de", INSTANCE.getDefaultHelpMessage("Überprüfe die Informationen eines Items."));

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
                    getMessage(RANGE_ITEM_MK,
                            placeholder("%range%", Utils.getRangeFromItem(item) + ""))
            );
            return true;
        }

        if (Utils.isPanelBlockItem(item)) {
            playerSender.sendMessage(
                    getMessage(PANEL_MK,
                            placeholder("%range%", Utils.getRangeFromPanelBlockItem(item) + ""))
            );
            return true;
        }

        playerSender.sendMessage(getMessage(WRONG_ITEM_MK));

        return true;
    }

    @Override
    public Map<CommandMessageKey, String> getHelpMessages() {
        return Map.of(
                HELP_MK, getPermission()
        );
    }
}
