package de.janschuri.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.*;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.commands.Subcommand;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StorageCheck extends Subcommand {

    private final MessageKey rangeItemMK = new CommandMessageKey(this, "range_item")
            .defaultMessage("The range of this rangeitem is: %range%");
    private final MessageKey panelMK = new CommandMessageKey(this, "panel")
            .defaultMessage("The range of this panel is: %range%");
    private final MessageKey wrongItemMK = new CommandMessageKey(this, "wrong_item")
            .defaultMessage("You have to hold a storageitem, rangeitem or a panel in your hand.");

    @Override
    public LunaticCommand getParentCommand() {
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
            return true;
        }

        if (Utils.isRangeItem(item)) {
            playerSender.sendMessage(
                    getMessage(rangeItemMK)
                            .replaceText(getTextReplacementConfig("%range%", Utils.getRangeFromItem(item) + ""))
            );
            return true;
        }

        if (Utils.isPanelBlockItem(item)) {
            playerSender.sendMessage(
                    getMessage(panelMK)
                            .replaceText(getTextReplacementConfig("%range%", Utils.getRangeFromPanelBlockItem(item) + ""))
            );
            return true;
        }

        playerSender.sendMessage(getMessage(wrongItemMK));

        return true;
    }
}
