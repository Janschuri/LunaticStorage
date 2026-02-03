package de.janschuri.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.commands.Command;
import de.janschuri.lunaticlib.commands.HasParentCommand;
import de.janschuri.lunaticlib.config.CommandMessageKey;
import de.janschuri.lunaticlib.config.LunaticCommandMessageKey;
import de.janschuri.lunaticlib.sender.PlayerSender;
import de.janschuri.lunaticlib.sender.Sender;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.commands.StorageCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static de.janschuri.lunaticstorage.config.PluginConfig.getShutdownMessage;

public class StorageRandom extends StorageCommand implements HasParentCommand {

    private static final StorageRandom INSTANCE = new StorageRandom();
    private static final CommandMessageKey HELP_MK = new LunaticCommandMessageKey(INSTANCE, "help")
            .defaultMessage("en", INSTANCE.getDefaultHelpMessage("Get random items for tests."))
            .defaultMessage("de", INSTANCE.getDefaultHelpMessage("Erhalte zufällige Items für Tests."));

    @Override
    public Command getParentCommand() {
        return new Storage();
    }

    @Override
    public String getName() {
        return "random";
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.admin.random";
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

        if (LunaticStorage.getPluginConfig().isShutdown()) {
            sender.sendMessage(getShutdownMessage());
            return true;
        }

            PlayerSender player = (PlayerSender) sender;

            Player p = Bukkit.getPlayer(player.getUniqueId());

            while (p.getInventory().firstEmpty() != -1) {
                List<String> materialNames = Arrays.stream(Material.values())
                        .filter(Material::isItem)
                        .map(Material::name)
                        .toList();

                int max = materialNames.size() - 1;

                int randomNum = ThreadLocalRandom.current().nextInt(0, max);
                int randomAmount = ThreadLocalRandom.current().nextInt(1, 65);

                Material randomMaterial = Material.matchMaterial(materialNames.get(randomNum));

                ItemStack randomItem = new ItemStack(randomMaterial);

                if (randomAmount <= randomItem.getMaxStackSize()) {
                    randomItem.setAmount(randomAmount);
                } else {
                    randomItem.setAmount(randomItem.getMaxStackSize());
                }

                p.getInventory().addItem(randomItem);
            }

        return true;
    }

    @Override
    public Map<CommandMessageKey, String> getHelpMessages() {
        return Map.of(
                HELP_MK, getPermission()
        );
    }
}
