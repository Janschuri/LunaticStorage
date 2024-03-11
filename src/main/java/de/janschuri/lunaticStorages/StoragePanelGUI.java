package de.janschuri.lunaticStorages;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
        String coords = "";
        if (block != null) {
            coords = Main.getCoordsAsString(block);
        }

        if (block != null && Main.getDatabase().isPanelInDatabase(coords)) {
            event.setCancelled(true);
            int id = Main.getDatabase().getPanelsID(coords);
            openGUI(player, id);
        }
    }

    private void openGUI(Player player, int id) {
        Inventory gui = Bukkit.createInventory(null, 54, "Storage");
        World world = player.getWorld();

        String locale = player.getLocale().toLowerCase();
        gui = loadGui(gui, id, world, locale);

        player.openInventory(gui);
    }
    
    private Inventory loadGui (Inventory gui, int panelID, World world, String locale) {
        int page;
        int pages;
        boolean desc;
        int sorter;

        if (gui.getItem(49) != null && !gui.getItem(49).isEmpty()) {
            page = getPage(gui);
        } else {
            page = 1;
        }

        if (gui.getItem(6) != null && !gui.getItem(6).isEmpty()) {
            desc = getDesc(gui);
        } else {
            desc = true;
        }

        if (gui.getItem(5) != null && !gui.getItem(5).isEmpty()) {
            sorter = getSorter(gui);
        } else {
            sorter = 0;
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
            Storage storage;

            if (Main.storages.containsKey(panelID)) {
                storage = Main.storages.get(panelID);
            } else {
                storage = new Storage(chests, world);
                Main.storages.put(panelID, storage);
            }

            pages = storage.getPages();
            gui = Storage.addMaptoInventory(gui, storage.getStorageList(locale, sorter, desc), panelID, page-1);

            container.set(plugin.keyPanelID, PersistentDataType.INTEGER, panelID);

            item.setItemMeta(meta);
            gui.setItem(8, item);

            gui.setItem(49, createPagePane(panelID, page, pages));
            gui.setItem(6, createSortArrow(panelID, desc));
            gui.setItem(5, createSorter(panelID, sorter));

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

    private ItemStack createSortArrow(int id, boolean desc) {


        if(desc) {
            ItemStack arrow = Main.getSkull("https://textures.minecraft.net/texture/a3852bf616f31ed67c37de4b0baa2c5f8d8fca82e72dbcafcba66956a81c4");
            ItemMeta meta = arrow.getItemMeta();
            meta.getPersistentDataContainer().set(Main.keyDesc, PersistentDataType.BOOLEAN, desc);
            meta.setDisplayName("Descended");
            arrow.setItemMeta(meta);
            return arrow;
        } else {
            ItemStack arrow = Main.getSkull("https://textures.minecraft.net/texture/b221da4418bd3bfb42eb64d2ab429c61decb8f4bf7d4cfb77a162be3dcb0b927");
            ItemMeta meta = arrow.getItemMeta();
            meta.getPersistentDataContainer().set(Main.keyDesc, PersistentDataType.BOOLEAN, desc);
            meta.setDisplayName("Ascended");
            arrow.setItemMeta(meta);
            return arrow;
        }
    }

    private ItemStack createSorter(int id, int sorter) {
        if (sorter == 1) {
            ItemStack sorterItem = Main.getSkull("https://textures.minecraft.net/texture/bc35e72022e2249c9a13e5ed8a4583717a626026773f5416440d573a938c93");
            ItemMeta meta = sorterItem.getItemMeta();
            meta.getPersistentDataContainer().set(Main.keySorter, PersistentDataType.INTEGER, sorter);
            meta.setDisplayName("by name");
            sorterItem.setItemMeta(meta);
            return sorterItem;
        } else {
            ItemStack sorterItem = Main.getSkull("https://textures.minecraft.net/texture/5a990d613ba553ddc5501e0436baabc17ce22eb4dc656d01e777519f8c9af23a");
            ItemMeta meta = sorterItem.getItemMeta();
            meta.getPersistentDataContainer().set(Main.keySorter, PersistentDataType.INTEGER, sorter);
            meta.setDisplayName("by amount");
            sorterItem.setItemMeta(meta);
            return sorterItem;
        }
    }

    private Inventory setSorter(Inventory gui, int sorter) {
        ItemStack item = gui.getItem(5);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Main.keySorter, PersistentDataType.INTEGER, sorter);
        item.setItemMeta(meta);
        return gui;
    }

    private int getSorter(Inventory gui) {
        if (gui.getItem(5).getItemMeta().getPersistentDataContainer().get(Main.keySorter, PersistentDataType.INTEGER) == null) {
            return 0;
        } else {
            return gui.getItem(5).getItemMeta().getPersistentDataContainer().get(Main.keySorter, PersistentDataType.INTEGER);
        }
    }

    private Inventory setPage(Inventory gui, int page) {
        ItemStack item = gui.getItem(49);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Main.keyPage, PersistentDataType.INTEGER, page);
        item.setItemMeta(meta);


        return gui;
    }

    private int getPage(Inventory gui) {
        if (gui.getItem(49).getItemMeta().getPersistentDataContainer().get(Main.keyPage, PersistentDataType.INTEGER) == null) {
            return 1;
        } else {
            return gui.getItem(49).getItemMeta().getPersistentDataContainer().get(Main.keyPage, PersistentDataType.INTEGER);
        }
    }

    private Inventory setDesc(Inventory gui, boolean desc) {
        ItemStack item = gui.getItem(6);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Main.keyDesc, PersistentDataType.BOOLEAN, desc);
        item.setItemMeta(meta);
        return gui;
    }


    private boolean getDesc(Inventory gui) {
        if (gui.getItem(6).getItemMeta().getPersistentDataContainer().get(Main.keyDesc, PersistentDataType.BOOLEAN) == null) {
            return true;
        } else {
            return gui.getItem(6).getItemMeta().getPersistentDataContainer().get(Main.keyDesc, PersistentDataType.BOOLEAN);
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
                String locale = player.getLocale().toLowerCase();
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
                            if (Main.getDatabase().getPanelsStorageItem(id) != null) {
                                if (processingClickEvent) return;
                                Storage storage = Main.storages.get(id);
                                byte[] serializedItem = Main.getDatabase().getPanelsStorageItem(id);
                                ItemStack storageItem = Main.deserializeItemStack(serializedItem);
                                ItemMeta storageItemMeta = storageItem.getItemMeta();
                                PersistentDataContainer dataContainerStorageItem = storageItemMeta.getPersistentDataContainer();
                                int[] chests = dataContainerStorageItem.get(Main.keyStorage, PersistentDataType.INTEGER_ARRAY);

                                ItemStack newItem = storage.insertItemsIntoStorage(item, player);

                                event.getClickedInventory().setItem(event.getSlot(), newItem);

                                if (!newItem.isEmpty()) {
                                    processingClickEvent = true;
                                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                        processingClickEvent = false;
                                    }, 20L);
                                }

                                int amount = item.getAmount() - newItem.getAmount();
                                storage.updateStorageMap(item, amount);
                                Main.storages.put(id, storage);

                                gui = loadGui(gui, id, world, locale);
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

                                        gui = loadGui(gui, id, world, locale);
                                    }
                                } else if (dataContainer.has(Main.keyStorageContent) && Main.getDatabase().getPanelsStorageItem(id) != null) {
                                    Storage storage = Main.storages.get(id);



                                    ItemStack newItem = storage.insertItemsIntoStorage(cursorItem, player);

                                    player.setItemOnCursor(newItem);
                                    int amount = cursorItem.getAmount() - newItem.getAmount();
                                    storage.updateStorageMap(cursorItem, amount);
                                    Main.storages.put(id, storage);

                                    gui = loadGui(gui, id, world, locale);
                                }
                            } else if (dataContainer.has(Main.keyStorageContent)) {
                                Storage storage = Main.storages.get(id);
                                byte[] serializedItem = Main.getDatabase().getPanelsStorageItem(id);
                                ItemStack storageItem = Main.deserializeItemStack(serializedItem);
                                ItemMeta storageItemMeta = storageItem.getItemMeta();
                                PersistentDataContainer dataContainerStorageItem = storageItemMeta.getPersistentDataContainer();
                                int[] chests = dataContainerStorageItem.get(Main.keyStorage, PersistentDataType.INTEGER_ARRAY);


                                ItemStack newItem = storage.getItemsFromStorage(item, player);

                                if (!newItem.isEmpty()) {
                                    player.setItemOnCursor(newItem);
                                    int amount = newItem.getAmount();
                                    storage.updateStorageMap(newItem, -(amount));
                                    Main.storages.put(id, storage);
                                    gui = loadGui(gui, id, world, locale);
                                }
                            } else if (dataContainer.has(Main.keyStorage)) {
                                Main.getDatabase().savePanelsData(id, null);
                                player.setItemOnCursor(item);
                                Main.storages.remove(id);
                                gui = loadGui(gui, id, world, locale);
                            } else if (dataContainer.has(Main.keyRightArrow)) {
                                int page = getPage(gui) + 1;
                                gui = setPage(gui, page);
                                gui = loadGui(gui, id, world, locale);
                            } else if (dataContainer.has(Main.keyLeftArrow)) {
                                int page = getPage(gui) - 1;
                                gui = setPage(gui, page);
                                gui = loadGui(gui, id, world, locale);
                            } else if (dataContainer.has(Main.keyDesc)) {
                                boolean desc = dataContainer.get(Main.keyDesc, PersistentDataType.BOOLEAN);
                                setDesc(gui, !desc);


                                gui = loadGui(gui, id, world, locale);
                            } else if (dataContainer.has(Main.keySorter)) {
                                int sorter = dataContainer.get(Main.keySorter, PersistentDataType.INTEGER);

                                sorter = (sorter+1) % 2;

                                setSorter(gui, sorter);


                                gui = loadGui(gui, id, world, locale);
                            }

                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                processingClickEvent = false;
                            }, 5L);
                        }
                    }
            }
    }

}