package de.janschuri.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.commands.Command;
import de.janschuri.lunaticlib.commands.HasParentCommand;
import de.janschuri.lunaticlib.config.CommandMessageKey;
import de.janschuri.lunaticlib.config.LunaticCommandMessageKey;
import de.janschuri.lunaticlib.config.MessageKey;
import de.janschuri.lunaticlib.platform.paper.inventorygui.handler.GUIManager;
import de.janschuri.lunaticlib.sender.PlayerSender;
import de.janschuri.lunaticlib.sender.Sender;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.commands.StorageCommand;
import de.janschuri.lunaticstorage.gui.ContainerListGUI;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.janschuri.lunaticstorage.config.PluginConfig.getShutdownMessage;

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

        if (LunaticStorage.getPluginConfig().isShutdown()) {
            sender.sendMessage(getShutdownMessage());
            return true;
        }

        PlayerSender playerSender = (PlayerSender) sender;

        Player player = Bukkit.getPlayer(playerSender.getUniqueId());

        if (player == null) {
            Logger.error("Player is null!");
            return false;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (Utils.isStorageItem(item)) {
            Map<UUID, List<String>> containerMap = Utils.getStorageContainerCoordsMap(item);

            ContainerListGUI gui = ContainerListGUI.getContainerListGUI(containerMap)
                    .onContainerChange((event, updatedMap) -> {
                        ItemStack itemNew = player.getInventory().getItemInMainHand();
                        StorageContainer.setChestsToPersistentDataContainer(itemNew, updatedMap);
                        player.getInventory().setItemInMainHand(itemNew);
                    });

            GUIManager.openGUI(gui, player);

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
