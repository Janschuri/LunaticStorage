package de.janschuri.lunaticstorage.commands.storage;

import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.Key;
import de.janschuri.lunaticstorage.commands.Subcommand;
import de.janschuri.lunaticlib.LunaticCommand;
import de.janschuri.lunaticlib.PlayerSender;
import de.janschuri.lunaticlib.Sender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class StoragePanel extends Subcommand {

    @Override
    public LunaticCommand getParentCommand() {
        return new Storage();
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.admin.panel";
    }

    @Override
    public String getName() {
        return "panel";
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

        long range;

        if (args.length > 0) {
            try {
                range = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(getMessage(NO_NUMBER_MK));
                return true;
            }
        } else {
            range = LunaticStorage.getPluginConfig().getDefaultRangePanel();
        }

            PlayerSender player = (PlayerSender) sender;

            ItemStack item = new ItemStack(LunaticStorage.getPluginConfig().getStoragePanelBlock());

            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(Key.PANEL_BLOCK, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(Key.PANEL_RANGE, PersistentDataType.LONG, range);
            item.setItemMeta(meta);

            Player p = Bukkit.getPlayer(player.getUniqueId());
            p.getInventory().addItem(item);
            return true;
    }
}
