package de.janschuri.lunaticstorage.commands.storage;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticlib.CommandMessageKey;
import de.janschuri.lunaticlib.LunaticCommand;
import de.janschuri.lunaticlib.PlayerSender;
import de.janschuri.lunaticlib.Sender;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.GUIManager;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.commands.Subcommand;
import de.janschuri.lunaticstorage.gui.ContainerGUI;
import de.janschuri.lunaticstorage.storage.Key;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public class StorageContainerSettings extends Subcommand {

    private final CommandMessageKey notContainerMK = new CommandMessageKey(this, "not_container");

    @Override
    public LunaticCommand getParentCommand() {
        return new Storage();
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.storage.container";
    }

    @Override
    public String getName() {
        return "container";
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

        Player player = Bukkit.getPlayer(((PlayerSender) sender).getUniqueId());
        Block block = player.getTargetBlockExact(5);

        if (!Utils.isContainer(block)) {
            sender.sendMessage(getMessage(notContainerMK));
            return true;
        }


        StorageContainer container = StorageContainer.getStorageContainer(block);

        GUIManager.openGUI(ContainerGUI.getContainerGUI(player, container), player);
        return true;
    }
}
