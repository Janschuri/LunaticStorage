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
            openGUI(player, id);
        }
    }

    private void openGUI(Player player, int id) {
        Inventory gui = Bukkit.createInventory(null, 54, "Storage");
        World world = player.getWorld();

        gui = loadGui(gui, id, world);

        player.openInventory(gui);
    }
    
    private Inventory loadGui (Inventory gui, int panelID, World world) {
        int page;
        int pages;

        if (gui.getItem(49) != null && !gui.getItem(49).isEmpty()) {
            page = getPage(gui);
            Bukkit.getLogger().info("Seite oben: " + page);
        } else {
            page = 1;
        }

        gui.clear();

        for (int i = 0; i < 9; i++) {
            gui.setItem(i, createPane());
            gui.setItem(45 + i, createPane());
        }

        if (Main.getDatabase().getPanelsStorageItem(panelID) != null) {
            byte[] serializedItem = Main.getDatabase().getPanelsStorageItem(panelID);
            ItemStack item = plugin.deserializeItemStack(serializedItem);
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();

            int[] chests = container.get(Main.keyStorage, PersistentDataType.INTEGER_ARRAY);
            List<Map.Entry<ItemStack, Integer>> storage = Storage.getStorage(chests, world, page);
            pages = Storage.getStoragePages(chests, world);
            gui = Storage.addMaptoInventory(gui, storage, panelID);

            container.set(plugin.keyPanelID, PersistentDataType.INTEGER, panelID);

            item.setItemMeta(meta);
            gui.setItem(8, item);

            gui.setItem(49, createPagePane(panelID, page, pages));

            if (page < pages) {
                gui.setItem(50, createArrowRight(panelID));
            }
            if(page>1) {
                gui.setItem(48, createArrowLeft(panelID));
            }

        } else {
            gui.setItem(8, createStoragePane(panelID));
        }



        return gui;
    }

    private ItemStack createPane() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();

        meta.getPersistentDataContainer().set(Main.keyPane, PersistentDataType.BOOLEAN, true);

        pane.setItemMeta(meta);
        return pane;
    }

    private ItemStack createStoragePane(int id) {
        ItemStack pane = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();

        meta.getPersistentDataContainer().set(Main.keyStoragePane, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(Main.keyPanelID, PersistentDataType.INTEGER, id);

        pane.setItemMeta(meta);
        return pane;
    }

    private ItemStack createPagePane(int id, int page, int pages) {
        ItemStack pane = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();

        meta.getPersistentDataContainer().set(Main.keyPage, PersistentDataType.INTEGER, page);
        meta.getPersistentDataContainer().set(Main.keyPanelID, PersistentDataType.INTEGER, id);
        meta.setDisplayName("Seite: " + page + "/" + pages);

        pane.setItemMeta(meta);
        return pane;
    }

    private ItemStack createArrowLeft(int id) {
        ItemStack arrow = Main.getSkull("https://textures.minecraft.net/texture/f6dab7271f4ff04d5440219067a109b5c0c1d1e01ec602c0020476f7eb612180");
        ItemMeta meta = arrow.getItemMeta();

        meta.getPersistentDataContainer().set(Main.keyLeftArrow, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(Main.keyPanelID, PersistentDataType.INTEGER, id);
        meta.setDisplayName("<<<");

        arrow.setItemMeta(meta);
        return arrow;
    }

    private ItemStack createArrowRight(int id) {
        ItemStack arrow = Main.getSkull("https://textures.minecraft.net/texture/8aa187fede88de002cbd930575eb7ba48d3b1a06d961bdc535800750af764926");
        ItemMeta meta = arrow.getItemMeta();

        meta.getPersistentDataContainer().set(Main.keyRightArrow, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(Main.keyPanelID, PersistentDataType.INTEGER, id);
        meta.setDisplayName(">>>");

        arrow.setItemMeta(meta);
        return arrow;
    }

    private Inventory setPage(Inventory gui, int page) {
        ItemStack item = gui.getItem(49);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Main.keyPage, PersistentDataType.INTEGER, page);
        item.setItemMeta(meta);


        Bukkit.getLogger().info("Setter Seite: " + getPage(gui));
        return gui;
    }

    private int getPage(Inventory gui) {
        if (gui.getItem(49).getItemMeta().getPersistentDataContainer().get(Main.keyPage, PersistentDataType.INTEGER) == null) {
            return 1;
        } else {
            return gui.getItem(49).getItemMeta().getPersistentDataContainer().get(Main.keyPage, PersistentDataType.INTEGER);
        }
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
                int id = gui.getItem(8).getItemMeta().getPersistentDataContainer().get(Main.keyPanelID, PersistentDataType.INTEGER);
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

                        if (event.getClickedInventory() == playerInv && event.isShiftClick()) {
                            Bukkit.getLogger().info("test 2");
                            if (Main.getDatabase().getPanelsStorageItem(id) != null) {
                                if (processingClickEvent) return;
                                Bukkit.getLogger().info("test 3");
                                byte[] serializedItem = Main.getDatabase().getPanelsStorageItem(id);
                                ItemStack storageItem = Main.deserializeItemStack(serializedItem);
                                ItemMeta storageItemMeta = storageItem.getItemMeta();
                                PersistentDataContainer dataContainerStorageItem = storageItemMeta.getPersistentDataContainer();
                                int[] chests = dataContainerStorageItem.get(Main.keyStorage, PersistentDataType.INTEGER_ARRAY);

                                ItemStack newItem = Storage.insertItemsIntoStorage(chests, world, item);

                                event.getClickedInventory().setItem(event.getSlot(), newItem);

                                if (!newItem.isEmpty()) {
                                    processingClickEvent = true;
                                    Bukkit.getLogger().info("jajajaja");
                                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                        processingClickEvent = false;
                                    }, 20L);
                                }

                                gui = loadGui(gui, id, world);
                            }
                        } else {
                            if (processingClickEvent) return;
                            processingClickEvent = true;

                            if (!cursorItem.isEmpty()) {
                                if (dataContainer.has(Main.keyStoragePane)) {
                                    ItemStack itemCursor = event.getCursor();
                                    ItemMeta metaCursor = itemCursor.getItemMeta();

                                    PersistentDataContainer dataContainerCursor = metaCursor.getPersistentDataContainer();
                                    if (dataContainerCursor.has(Main.keyStorage)) {
                                        ItemStack cursorClone = itemCursor.clone();
                                        cursorClone.setAmount(1);
                                        byte[] storage = Main.serializeItemStack(cursorClone);
                                        Main.getDatabase().savePanelsData(id, storage);
                                        gui.setItem(8, cursorClone);
                                        event.getCursor().subtract(1);

                                        gui = loadGui(gui, id, world);
                                    }
                                } else if (dataContainer.has(Main.keyStorageContent) && Main.getDatabase().getPanelsStorageItem(id) != null) {
                                    byte[] serializedItem = Main.getDatabase().getPanelsStorageItem(id);
                                    ItemStack storageItem = Main.deserializeItemStack(serializedItem);
                                    ItemMeta storageItemMeta = storageItem.getItemMeta();
                                    PersistentDataContainer dataContainerStorageItem = storageItemMeta.getPersistentDataContainer();
                                    int[] chests = dataContainerStorageItem.get(Main.keyStorage, PersistentDataType.INTEGER_ARRAY);

                                    ItemStack newItem = Storage.insertItemsIntoStorage(chests, world, cursorItem);

                                    player.setItemOnCursor(newItem);

                                    gui = loadGui(gui, id, world);
                                }
                            } else if (dataContainer.has(Main.keyStorageContent)) {

                                byte[] serializedItem = Main.getDatabase().getPanelsStorageItem(id);
                                ItemStack storageItem = Main.deserializeItemStack(serializedItem);
                                ItemMeta storageItemMeta = storageItem.getItemMeta();
                                PersistentDataContainer dataContainerStorageItem = storageItemMeta.getPersistentDataContainer();
                                int[] chests = dataContainerStorageItem.get(Main.keyStorage, PersistentDataType.INTEGER_ARRAY);


                                ItemStack newItem = Storage.getItemsFromStorage(chests, world, item);

                                if (!newItem.isEmpty()) {
                                    player.setItemOnCursor(newItem);

                                    gui = loadGui(gui, id, world);
                                }
                            } else if (dataContainer.has(Main.keyStorage)) {
                                Main.getDatabase().savePanelsData(id, null);
                                player.setItemOnCursor(item);

                                gui = loadGui(gui, id, world);
                            } else if (dataContainer.has(Main.keyRightArrow)) {
                                int page = getPage(gui) + 1;
                                gui = setPage(gui, page);
                                Bukkit.getLogger().info("Seite: " + getPage(gui));
                                gui = loadGui(gui, id, world);
                            } else if (dataContainer.has(Main.keyLeftArrow)) {
                                int page = getPage(gui) - 1;
                                gui = setPage(gui, page);
                                Bukkit.getLogger().info("Seite: " + getPage(gui));
                                gui = loadGui(gui, id, world);
                            }

                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                processingClickEvent = false;
                            }, 5L);
                        }
                    }

            }
    }

}