package de.janschuri.lunaticStorages;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class StoragePanelGUI implements Listener {


    private final Main plugin;
    private boolean processingClickEvent = false;

    public StoragePanelGUI(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        String coords = Main.getCoordsAsString(block);

        if (block != null && Main.getDatabase().isPanelInDatabase(coords)) {
            event.setCancelled(true);
            int id = Main.getDatabase().getPanelsID(coords);
            // Open the GUI
            openGUI(player, id);
        }
    }

    private void openGUI(Player player, int id) {
        Inventory gui = Bukkit.createInventory(null, 6 * 9, "My GUI");
        World world = player.getWorld();
        // Fill the first and last rows with grey glass panes to prevent item movement
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, createPane());
            gui.setItem(45 + i, createPane());
        }

        if (Main.getDatabase().getPanelsStorageItem(id) != null) {
            byte[] serializedItem = Main.getDatabase().getPanelsStorageItem(id);
            ItemStack storageItem = plugin.deserializeItemStack(serializedItem);
            ItemMeta storageItemMeta = storageItem.getItemMeta();
            storageItemMeta.getPersistentDataContainer().set(plugin.keyPanelID, PersistentDataType.INTEGER, id);
            storageItem.setItemMeta(storageItemMeta);
            gui.setItem(8, storageItem);
        } else {
            gui.setItem(8, createStoragePane(id));
        }

        if (Main.getDatabase().getPanelsStorageItem(id) != null) {
            byte[] serializedItem = Main.getDatabase().getPanelsStorageItem(id);
            ItemStack storageItem = plugin.deserializeItemStack(serializedItem);
            ItemMeta storageItemMeta = storageItem.getItemMeta();
            PersistentDataContainer dataContainer = storageItemMeta.getPersistentDataContainer();
            int[] chests = dataContainer.get(plugin.keyStorage, PersistentDataType.INTEGER_ARRAY);
            Bukkit.getLogger().info(Arrays.toString(chests));
            List<Map.Entry<ItemStack, Integer>> storage = plugin.getStorage(chests, world);
            gui = plugin.addMaptoInventory(gui, storage, id);
        }

        player.openInventory(gui);
    }

    private ItemStack createPane() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();

        meta.getPersistentDataContainer().set(plugin.keyPane, PersistentDataType.BOOLEAN, true);

        pane.setItemMeta(meta);
        return pane;
    }

    private ItemStack createStoragePane(int id) {
        ItemStack pane = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();

        meta.getPersistentDataContainer().set(plugin.keyStoragePane, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(plugin.keyPanelID, PersistentDataType.INTEGER, id);

        pane.setItemMeta(meta);
        return pane;
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Inventory gui = event.getInventory();
        Inventory playerInv = event.getWhoClicked().getInventory();

        if (gui.contains(createPane())) {
            event.setCancelled(true);
        }
        if (event.getInventory().equals(playerInv)) {
            event.setCancelled(false);
        }

    }


    @EventHandler
        public void onClick(InventoryClickEvent event) {
            Inventory gui = event.getView().getTopInventory();
            if (gui.contains(createPane())) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                World world = player.getWorld();
                ItemStack item = event.getCurrentItem();
                ItemStack cursorItem = event.getCursor();
                Inventory playerInv = event.getView().getBottomInventory();

                    if (event.getClickedInventory() == playerInv && !event.isShiftClick()) {
                        event.setCancelled(false);
                    } else if (item == null || item.isEmpty() || item.getType() == Material.AIR) {

                    } else {
                        ItemMeta meta = item.getItemMeta();
                        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
                        int id = dataContainer.get(plugin.keyPanelID, PersistentDataType.INTEGER);

                        if (!cursorItem.isEmpty()) {
                            if (dataContainer.has(plugin.keyStoragePane)) {
                                ItemStack itemCursor = event.getCursor();
                                ItemMeta metaCursor = itemCursor.getItemMeta();

                                PersistentDataContainer dataContainerCursor = metaCursor.getPersistentDataContainer();
                                if (dataContainerCursor.has(plugin.keyStorage)) {
                                    if (processingClickEvent) return;
                                    processingClickEvent = true;
                                    ItemStack cursorClone = itemCursor.clone();
                                    cursorClone.setAmount(1);
                                    byte[] storage = plugin.serializeItemStack(cursorClone);
                                    Main.getDatabase().savePanelsData(id, storage);
                                    gui.setItem(8, cursorClone);
                                    event.getCursor().subtract(1);

                                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                        processingClickEvent = false;
                                    }, 20L);
                                }
                            }
                        } else if (dataContainer.has(plugin.keyStorageContent)) {

                            byte[] serializedItem = Main.getDatabase().getPanelsStorageItem(id);
                            ItemStack storageItem = plugin.deserializeItemStack(serializedItem);
                            ItemMeta storageItemMeta = storageItem.getItemMeta();
                            PersistentDataContainer dataContainerStorageItem = storageItemMeta.getPersistentDataContainer();
                            int[] chests = dataContainerStorageItem.get(plugin.keyStorage, PersistentDataType.INTEGER_ARRAY);


                            ItemStack newItem = plugin.getItemsFromStorage(chests, world, item);

                            if (!newItem.isEmpty()) {
                                player.setItemOnCursor(newItem);

                                gui.clear();
                                for (int i = 0; i < 9; i++) {
                                    gui.setItem(i, createPane());
                                    gui.setItem(45 + i, createPane());
                                }

                                if (Main.getDatabase().getPanelsStorageItem(id) != null) {
                                    byte[] serializedItem1 = Main.getDatabase().getPanelsStorageItem(id);
                                    ItemStack storageItem1 = plugin.deserializeItemStack(serializedItem1);
                                    gui.setItem(8, storageItem1);
                                } else {
                                    gui.setItem(8, createStoragePane(id));
                                }

                                List<Map.Entry<ItemStack, Integer>> storage = plugin.getStorage(chests, world);

                                gui = plugin.addMaptoInventory(gui, storage, id);
                            }
                        } else if (dataContainer.has(plugin.keyStorage)) {
                            if (processingClickEvent) return;
                            processingClickEvent = true;
                            Main.getDatabase().savePanelsData(id, null);
                            gui.setItem(8, createStoragePane(id));
                            player.setItemOnCursor(item);

                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                processingClickEvent = false;
                            }, 20L);
                        }
                    }

            }
    }

}