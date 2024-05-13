package de.janschuri.lunaticStorages.commands.subcommands.storage;

import de.janschuri.lunaticStorages.storage.Key;
import de.janschuri.lunaticStorages.commands.subcommands.Subcommand;
import de.janschuri.lunaticStorages.config.PluginConfig;
import de.janschuri.lunaticlib.senders.AbstractPlayerSender;
import de.janschuri.lunaticlib.senders.AbstractSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PanelSubcommand extends Subcommand {

    private static final String MAIN_COMMAND = "storage";
    private static final String NAME = "panel";
    private static final String PERMISSION = "lunaticstorages.admin.panel";

    protected PanelSubcommand() {
        super(MAIN_COMMAND, NAME, PERMISSION);
    }

    @Override
    public boolean execute(AbstractSender sender, String[] args) {
        if (!(sender instanceof AbstractPlayerSender)) {
            sender.sendMessage(language.getPrefix() + language.getMessage("no_console_command"));
        } else if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(language.getPrefix() + language.getMessage("no_permission"));
        } else {
            AbstractPlayerSender player = (AbstractPlayerSender) sender;

            ItemStack item = new ItemStack(PluginConfig.getStoragePanelBlock());

            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(Key.PANEL_BLOCK, PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);

            Player p = Bukkit.getPlayer(player.getUniqueId());
            p.getInventory().addItem(item);
            return true;
        }
        return true;
    }
}
