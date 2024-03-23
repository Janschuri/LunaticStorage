package de.janschuri.lunaticStorages;

import de.janschuri.lunaticStorages.nms.SignGUI;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class StorageCommand implements CommandExecutor {

    private final LunaticStorage plugin;

    public StorageCommand(LunaticStorage plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        } else {
            Player player = (Player) sender;

            if (args.length < 1) {

            } else if (args[0].equalsIgnoreCase("item")) {
                ItemStack item = new ItemStack(plugin.storageItem);

                int[] invs = new int[] {};

                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(LunaticStorage.keyStorage, PersistentDataType.INTEGER_ARRAY, invs);
                item.setItemMeta(meta);

                player.getInventory().addItem(item);
            } else if (args[0].equalsIgnoreCase("panel")) {
                ItemStack item = new ItemStack(plugin.panelBlock);


                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(plugin.keyPanelBlock, PersistentDataType.BOOLEAN, true);
                item.setItemMeta(meta);
                player.getInventory().addItem(item);
            } else if (args[0].equalsIgnoreCase("sign")) {
                SignGUI signGui = new SignGUI(plugin);
                signGui.sendSign(player, lines -> {
                    // The lines array will always have a length of 4, filling empty prompts with empty strings
                    String name = String.join(" ", lines).trim(); // Join lines to get a single message, then trim it
                    player.sendMessage("Your message was: " + name);
                });
            } else if (args[0].equalsIgnoreCase("random")) {

                while (player.getInventory().firstEmpty() != -1) {
                    List<String> materialNames = Arrays.stream(Material.values())
                            .filter(material -> new ItemStack(material).getType().isItem())
                            .map(Material::name)
                            .collect(Collectors.toList());

                    int max = materialNames.size() - 1;

                    int randomNum = ThreadLocalRandom.current().nextInt(0, max);
                    int randomAmount = ThreadLocalRandom.current().nextInt(1, 65);

                    sender.sendMessage("Liste:" + max);

                    Material randomMaterial = Material.matchMaterial(materialNames.get(randomNum));

                    ItemStack randomItem = new ItemStack(randomMaterial);

                    if (randomAmount <= randomItem.getMaxStackSize()) {
                        randomItem.setAmount(randomAmount);
                    } else {
                        randomItem.setAmount(randomItem.getMaxStackSize());
                    }

                    player.getInventory().addItem(randomItem);
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("lunaticstorage.admin")) {
                    sender.sendMessage(plugin.prefix + plugin.messages.get("no_permission"));
                } else {
                    plugin.loadConfig(plugin);
                    sender.sendMessage(plugin.prefix + plugin.messages.get("reload"));
                }
            }
        }

        return true;
    }
}
