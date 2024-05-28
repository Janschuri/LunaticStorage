package de.janschuri.lunaticStorages.commands.subcommands.storage;

import de.janschuri.lunaticStorages.config.LanguageConfig;
import de.janschuri.lunaticStorages.storage.Key;
import de.janschuri.lunaticStorages.commands.subcommands.Subcommand;
import de.janschuri.lunaticStorages.config.PluginConfig;
import de.janschuri.lunaticlib.PlayerSender;
import de.janschuri.lunaticlib.Sender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemSubcommand extends Subcommand {

    private static final String MAIN_COMMAND = "storage";
    private static final String NAME = "item";
    private static final String PERMISSION = "lunaticstorages.admin.item";

    protected ItemSubcommand() {
        super(MAIN_COMMAND, NAME, PERMISSION);
    }

    @Override
    public boolean execute(Sender sender, String[] args) {
        if (!(sender instanceof PlayerSender)) {
            sender.sendMessage(LanguageConfig.getLanguageConfig().getPrefix() + LanguageConfig.getLanguageConfig().getMessage("no_console_command"));
        } else if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(LanguageConfig.getLanguageConfig().getPrefix() + LanguageConfig.getLanguageConfig().getMessage("no_permission"));
        } else {
            PlayerSender player = (PlayerSender) sender;

            ItemStack item = new ItemStack(PluginConfig.getStorageItem());

            int[] invs = new int[]{};

            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(Key.STORAGE, PersistentDataType.INTEGER_ARRAY, invs);
            item.setItemMeta(meta);

            Player p = Bukkit.getPlayer(player.getUniqueId());
            p.getInventory().addItem(item);
            return true;
        }
        return true;
    }
}
