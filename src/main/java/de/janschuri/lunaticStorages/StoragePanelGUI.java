package de.janschuri.lunaticStorages;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class StoragePanelGUI implements Listener {


    private final Main plugin;

    public StoragePanelGUI(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block != null && block.getType() == Material.LODESTONE) {
            // Open the GUI
            openGUI(player);
            Bukkit.getLogger().info("klick");
        }
    }

    private void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 6 * 9, "My GUI");

        // Fill the first and last rows with grey glass panes to prevent item movement
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, createPane());
            gui.setItem(45 + i, createPane());
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        ItemMeta diamondMeta = itemInHand.getItemMeta();

        World world = player.getWorld();

        NamespacedKey key = new NamespacedKey(plugin, "invs");

        PersistentDataContainer dataContainer = diamondMeta.getPersistentDataContainer();
        int[] chests = dataContainer.get(key, PersistentDataType.INTEGER_ARRAY);

        List<Map.Entry<ItemStack, Integer>> storage = plugin.getStorage(chests, world);

        gui = plugin.addMaptoInventory(gui, storage);

        // Open GUI for the player
        player.openInventory(gui);
    }

    private ItemStack createPane() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();

        NamespacedKey key = new NamespacedKey(plugin, "guiPane");
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        pane.setItemMeta(meta);
        return pane;
    }

    @EventHandler
    public void onPlayerInventory(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        World world = player.getWorld();

        if (item != null) {

            ItemMeta meta = item.getItemMeta();

            NamespacedKey keyPane = new NamespacedKey(plugin, "guiPane");
            NamespacedKey keyStorageItem = new NamespacedKey(plugin, "guiStorageItem");

            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

            if(dataContainer.has(keyPane, PersistentDataType.BOOLEAN)) {
                event.setCancelled(true);
            }
            if(dataContainer.has(keyStorageItem, PersistentDataType.BYTE_ARRAY)) {
                event.setCancelled(true);
                ItemStack cursorItem = player.getItemOnCursor();
                if (cursorItem.isEmpty()) {
                    ItemStack itemInHand = player.getInventory().getItemInMainHand();

                    ItemMeta diamondMeta = itemInHand.getItemMeta();
                    NamespacedKey key = new NamespacedKey(plugin, "invs");
                    PersistentDataContainer dataContainerDiamond = diamondMeta.getPersistentDataContainer();
                    int[] chests = dataContainerDiamond.get(key, PersistentDataType.INTEGER_ARRAY);

                    ItemStack newItem = plugin.getItemsFromStorage(chests, world, item);

                    if (!newItem.isEmpty()) {
                        player.setItemOnCursor(newItem);
                    }
                }
            }
        }
    }
}