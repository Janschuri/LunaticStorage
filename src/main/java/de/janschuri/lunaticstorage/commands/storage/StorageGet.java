package de.janschuri.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.LunaticCommand;
import de.janschuri.lunaticlib.PlayerSender;
import de.janschuri.lunaticlib.Sender;
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

public class StorageGet extends Subcommand {

    @Override
    public LunaticCommand getParentCommand() {
        return new Storage();
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.admin.get";
    }

    @Override
    public String getName() {
        return "get";
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

        ItemStack item;

        if (type.equalsIgnoreCase("storageitem")) {
            item = createStorageItem();
        } else if (type.equalsIgnoreCase("rangeitem")) {
            if (range == -1) {
                range = LunaticStorage.getPluginConfig().getDefaultRangeItem();
            }
            item = createRangeItem(range);
        } else if (type.equalsIgnoreCase("panel")) {
            if (range == -1) {
                range = LunaticStorage.getPluginConfig().getDefaultRangePanel();
            }
            item = createPanelItem(range);
        } else {
            sender.sendMessage(getMessage(WRONG_USAGE_MK));
            return true;
        }

            PlayerSender player = (PlayerSender) sender;


            Player p = Bukkit.getPlayer(player.getUniqueId());
            p.getInventory().addItem(item);
            return true;
    }

    private ItemStack createStorageItem() {
        ItemStack item = new ItemStack(LunaticStorage.getPluginConfig().getStorageItem());

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Key.STORAGE, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createRangeItem(long range) {
        ItemStack item = new ItemStack(LunaticStorage.getPluginConfig().getRangeItem());

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Key.RANGE, PersistentDataType.LONG, range);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createPanelItem(long range) {
        ItemStack item = new ItemStack(LunaticStorage.getPluginConfig().getStoragePanelBlock());

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Key.PANEL_BLOCK, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(Key.PANEL_RANGE, PersistentDataType.LONG, range);
        item.setItemMeta(meta);

        return item;
    }


    @Override
    public List<Map<String, String>> getParams() {
        Map<String, String> panelMap = Map.of(
                "panel", getPermission()+".panel",
                "rangeitem", getPermission()+".rangeitem",
                "storageitem", getPermission()+".storageitem"
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