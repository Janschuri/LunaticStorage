package de.janschuri.lunaticStorage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class StorageCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public StorageCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        Main pluginInstance = (Main) plugin;
        Map<UUID, List<Inventory>> chestsClicked = pluginInstance.chestsClicked;

        // Initialize an inventory to display the summed inventories
        Inventory sumInventory = player.getServer().createInventory(null, 9 * 3, "Sum of Inventories");

        // Initialize a map to store the summed inventory
        Map<ItemStack, Integer> summedInventory = new HashMap<>();

        // Iterate over the entries of the map
        for (Map.Entry<UUID, List<Inventory>> entry : chestsClicked.entrySet()) {
            List<Inventory> inventories = entry.getValue();

            // Iterate over the inventories
            for (Inventory inventory : inventories) {
                // Convert the inventory to a map
                Map<ItemStack, Integer> inventoryMap = InventoryUtils.inventoryToMap(inventory);

                // Sum the inventory map with the summedInventory map
                for (Map.Entry<ItemStack, Integer> itemEntry : inventoryMap.entrySet()) {
                    ItemStack itemStack = itemEntry.getKey();
                    int amount = itemEntry.getValue();

                    summedInventory.merge(itemStack, amount, Integer::sum);
                }
            }
        }

        // Sort the summed inventory map by amount
        List<Map.Entry<ItemStack, Integer>> sortedEntries = summedInventory.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());

        // Add items to the sum inventory (sorted by amount)
        for (Map.Entry<ItemStack, Integer> entry : sortedEntries) {
            ItemStack itemStack = entry.getKey();
            int amount = entry.getValue();

            ItemStack singleStack = itemStack.clone();
            singleStack.setAmount(1); // Set amount to 1

            ItemMeta meta = singleStack.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore == null) {
                    lore = new ArrayList<>();
                }
                lore.add("Amount: " + amount);
                meta.setLore(lore);
                singleStack.setItemMeta(meta);
            }

            sumInventory.addItem(singleStack);
        }

        // Open the sum inventory to the player
        player.openInventory(sumInventory);

        return true;
    }
}
