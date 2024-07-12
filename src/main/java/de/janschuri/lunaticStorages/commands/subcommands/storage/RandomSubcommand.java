package de.janschuri.lunaticStorages.commands.subcommands.storage;

import de.janschuri.lunaticStorages.commands.subcommands.Subcommand;
import de.janschuri.lunaticStorages.config.LanguageConfig;
import de.janschuri.lunaticlib.LunaticCommand;
import de.janschuri.lunaticlib.PlayerSender;
import de.janschuri.lunaticlib.Sender;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class RandomSubcommand extends Subcommand {

    private static final String MAIN_COMMAND = "storage";
    private static final String NAME = "random";
    private static final String PERMISSION = "lunaticstorages.admin.random";

    @Override
    public LunaticCommand getParentCommand() {
        return new StorageSubcommand();
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
            sender.sendMessage(getMessage(NO_CONSOLE_COMMAND));
            return true;
        }

        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(getMessage(NO_PERMISSION));
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
}
