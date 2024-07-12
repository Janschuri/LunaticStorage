package de.janschuri.lunaticStorages.commands.subcommands.storage;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.config.LanguageConfig;
import de.janschuri.lunaticStorages.storage.Key;
import de.janschuri.lunaticStorages.commands.subcommands.Subcommand;
import de.janschuri.lunaticStorages.config.PluginConfig;
import de.janschuri.lunaticlib.LunaticCommand;
import de.janschuri.lunaticlib.PlayerSender;
import de.janschuri.lunaticlib.Sender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.units.qual.N;

public class PanelSubcommand extends Subcommand {

    @Override
    public LunaticCommand getParentCommand() {
        return new StorageSubcommand();
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
            sender.sendMessage(getMessage(NO_CONSOLE_COMMAND));
            return true;
        }

        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(getMessage(NO_PERMISSION));
            return true;
        }


            PlayerSender player = (PlayerSender) sender;

            ItemStack item = new ItemStack(LunaticStorage.getPluginConfig().getStoragePanelBlock());

            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(Key.PANEL_BLOCK, PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);

            Player p = Bukkit.getPlayer(player.getUniqueId());
            p.getInventory().addItem(item);
            return true;
    }
}
