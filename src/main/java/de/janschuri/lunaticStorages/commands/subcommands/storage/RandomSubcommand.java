package de.janschuri.lunaticStorages.commands.subcommands.storage;

import de.janschuri.lunaticStorages.commands.subcommands.Subcommand;
import de.janschuri.lunaticStorages.config.LanguageConfig;
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

    protected RandomSubcommand() {
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

            Player p = Bukkit.getPlayer(player.getUniqueId());

            while (p.getInventory().firstEmpty() != -1) {
                List<String> materialNames = Arrays.stream(Material.values())
                        .filter(material -> new ItemStack(material).getType().isItem())
                        .map(Material::name)
                        .collect(Collectors.toList());

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
        }
        return true;
    }
}
