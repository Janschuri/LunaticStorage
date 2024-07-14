package de.janschuri.lunaticstorage.commands.subcommands.storage;

import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.Key;
import de.janschuri.lunaticstorage.commands.subcommands.Subcommand;
import de.janschuri.lunaticlib.LunaticCommand;
import de.janschuri.lunaticlib.PlayerSender;
import de.janschuri.lunaticlib.Sender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemSubcommand extends Subcommand {

    @Override
    public LunaticCommand getParentCommand() {
        return new StorageSubcommand();
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.admin.item";
    }

    @Override
    public String getName() {
        return "item";
    }

    @Override
    public boolean execute(Sender sender, String[] args) {
        if (!(sender instanceof PlayerSender)) {
            sender.sendMessage(getMessage(NO_CONSOLE_COMMAND));
            return true;
        }

        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(getMessage(NO_PERMISSION));
            return true;
        }
            PlayerSender player = (PlayerSender) sender;

            ItemStack item = new ItemStack(LunaticStorage.getPluginConfig().getStorageItem());

            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(Key.STORAGE, PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);

            Player p = Bukkit.getPlayer(player.getUniqueId());
            p.getInventory().addItem(item);
            return true;
    }
}
