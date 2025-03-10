package de.janschuri.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.*;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.commands.Subcommand;
import de.janschuri.lunaticstorage.storage.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public class StorageCreate extends Subcommand {

    private CommandMessageKey noItemInHandMk = new CommandMessageKey(this, "no_item_in_hand");
    private CommandMessageKey noBlockInHandMk = new CommandMessageKey(this, "no_block_in_hand");

    @Override
    public LunaticCommand getParentCommand() {
        return new Storage();
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.admin.create";
    }

    @Override
    public String getName() {
        return "create";
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

        if (args.length < 1) {
            sender.sendMessage(getMessage(WRONG_USAGE_MK));
            return true;
        }

        String type = args[0];
        long range = -1;

        if (args.length > 1) {
            try {
                range = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(getMessage(NO_NUMBER_MK));
                return true;
            }

            if (range < 0) {
                sender.sendMessage(getMessage(WRONG_USAGE_MK));
                return true;
            }
        }

        PlayerSender player = (PlayerSender) sender;
        Player p = Bukkit.getPlayer(player.getUniqueId());

        if (!player.hasItemInMainHand()) {
            sender.sendMessage(getMessage(noItemInHandMk));
            return true;
        }

        assert p != null;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (type.equalsIgnoreCase("storageitem")) {
            createStorageItem(item);
        } else if (type.equalsIgnoreCase("rangeitem")) {
            if (range == -1) {
                range = LunaticStorage.getPluginConfig().getDefaultRangeItem();
            }
            createRangeItem(item, range);
        } else if (type.equalsIgnoreCase("panel")) {
            if (range == -1) {
                range = LunaticStorage.getPluginConfig().getDefaultRangePanel();
            }

            if (!item.getType().isBlock()) {
                sender.sendMessage(getMessage(noBlockInHandMk));
                return true;
            }

            createPanelItem(item, range);
        } else {
            sender.sendMessage(getMessage(WRONG_USAGE_MK));
            return true;
        }


            return true;
    }

    private ItemStack createStorageItem(ItemStack item) {

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Key.STORAGE, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createRangeItem(ItemStack item, long range) {

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Key.RANGE, PersistentDataType.LONG, range);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createPanelItem(ItemStack item, long range) {

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Key.PANEL_BLOCK, PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(Key.PANEL_RANGE, PersistentDataType.LONG, range);
        item.setItemMeta(meta);

        return item;
    }


    @Override
    public List<Map<String, String>> getParams() {
        Map<String, String> panelMap = Map.of(
                "panel", getPermission(),
                "rangeitem", getPermission(),
                "storageitem", getPermission()
        );

        return List.of(panelMap);
    }

    @Override
    public List<Component> getParamsNames() {
        return List.of(
                Component.text("panel"),
                Component.text("rangeitem"),
                Component.text("storageitem")
        );
    }
}
