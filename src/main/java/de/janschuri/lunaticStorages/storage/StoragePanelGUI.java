package de.janschuri.lunaticStorages.storage;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.database.tables.PanelsTable;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class StoragePanelGUI {

    public static ItemStack GUI_PANE = createPane();

    public static void openGUI(Player player, int id) {
        Inventory gui = Bukkit.createInventory(null, 54, "Storage");
        World world = player.getWorld();

        String locale = player.getLocale().toLowerCase();
        gui = loadGui(gui, id, world, locale);

        player.openInventory(gui);
    }

    public static Inventory loadGui(Inventory gui, int panelID, World world, String locale) {
        int page;
        int pages;
        boolean desc;
        int sorter;
        String search;
        String totalAmount;

//        if (gui.getItem(49) != null && !gui.getItem(49).isEmpty()) {
//            page = getPage(gui);
//        } else {
//            page = 1;
//        }
//
//        if (gui.getItem(6) != null && !gui.getItem(6).isEmpty()) {
//            desc = getDesc(gui);
//        } else {
//            desc = true;
//        }
//
//        if (gui.getItem(5) != null && !gui.getItem(5).isEmpty()) {
//            sorter = getSorter(gui);
//        } else {
//            sorter = 0;
//        }
//        if (gui.getItem(0) != null && !gui.getItem(0).isEmpty()) {
//            search = StoragePanelGUI.getSearch(gui);
//        } else {
//            search = "";
//        }
        gui.clear();

        for (int i = 0; i < 9; i++) {
            gui.setItem(i, createPane());
            gui.setItem(45 + i, createPane());
        }

        if (PanelsTable.getPanelsStorageItem(panelID) != null) {
            byte[] serializedItem = PanelsTable.getPanelsStorageItem(panelID);
            ItemStack item = ItemStackUtils.deserializeItemStack(serializedItem);
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();

            int[] chests = container.get(Key.STORAGE, PersistentDataType.INTEGER_ARRAY);
            Storage storage;

            if (LunaticStorage.storageExists(panelID)) {
                storage = LunaticStorage.getStorage(panelID);
            } else {
                storage = Storage.getStorage(panelID, serializedItem);
                LunaticStorage.addStorage(panelID, storage);
            }

            totalAmount = "5";
//            gui = Storage.addMaptoInventory(gui, storage.getStorageList(locale, sorter, desc, search), panelID, page-1);

            container.set(Key.PANEL_ID, PersistentDataType.INTEGER, panelID);

            item.setItemMeta(meta);
            gui.setItem(8, item);
//            gui.setItem(0, createSearch(panelID, search));
//            gui.setItem(49, createPagePane(panelID, page, pages, totalAmount));
//            gui.setItem(6, createSortArrow(panelID, desc));
//            gui.setItem(5, createSorter(panelID, sorter));

//            if (page < pages) {
//                gui.setItem(50, createArrowRight(panelID));
//            }
//            if(page>1) {
//                gui.setItem(48, createArrowLeft(panelID));
//            }

        } else {
            gui.setItem(8, createStoragePane(panelID));
        }



        return gui;
    }

    private static ItemStack createPane() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();

        meta.getPersistentDataContainer().set(Key.PANE, PersistentDataType.BOOLEAN, true);

        pane.setItemMeta(meta);
        return pane;
    }

    private static ItemStack createStoragePane(int id) {
        ItemStack pane = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();

        meta.getPersistentDataContainer().set(Key.STORAGE_PANE, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(Key.PANEL_ID, PersistentDataType.INTEGER, id);

        pane.setItemMeta(meta);
        return pane;
    }

    private static ItemStack createSearch(int id, String search) {
        ItemStack item = new ItemStack(Material.SPYGLASS);
        ItemMeta meta = item.getItemMeta();
        if (search == null) {
            meta.getPersistentDataContainer().set(Key.SEARCH, PersistentDataType.STRING, "");
        } else {
            meta.getPersistentDataContainer().set(Key.SEARCH, PersistentDataType.STRING, search);
        }
        meta.getPersistentDataContainer().set(Key.PANEL_ID, PersistentDataType.INTEGER, id);

        item.setItemMeta(meta);
        return item;
    }

    public static Inventory setSearch(Inventory gui, String search) {
        ItemStack item = gui.getItem(0);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Key.SEARCH, PersistentDataType.STRING, search);
        item.setItemMeta(meta);
        return gui;
    }

    public static String getSearch(Inventory gui) {
        return gui.getItem(0).getItemMeta().getPersistentDataContainer().get(Key.SEARCH, PersistentDataType.STRING);
    }

    private static ItemStack createSortArrow(int id, boolean desc) {
        if(desc) {
            ItemStack arrow = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/a3852bf616f31ed67c37de4b0baa2c5f8d8fca82e72dbcafcba66956a81c4");
            ItemMeta meta = arrow.getItemMeta();
            meta.getPersistentDataContainer().set(Key.DESC, PersistentDataType.BOOLEAN, desc);
            meta.getPersistentDataContainer().set(Key.PANEL_ID, PersistentDataType.INTEGER, id);
            meta.setDisplayName("Descended");
            arrow.setItemMeta(meta);
            return arrow;
        } else {
            ItemStack arrow = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/b221da4418bd3bfb42eb64d2ab429c61decb8f4bf7d4cfb77a162be3dcb0b927");
            ItemMeta meta = arrow.getItemMeta();
            meta.getPersistentDataContainer().set(Key.DESC, PersistentDataType.BOOLEAN, desc);
            meta.getPersistentDataContainer().set(Key.PANEL_ID, PersistentDataType.INTEGER, id);
            meta.setDisplayName("Ascended");
            arrow.setItemMeta(meta);
            return arrow;
        }
    }

    private static ItemStack createSorter(int id, int sorter) {
        if (sorter == 1) {
            ItemStack sorterItem = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/bc35e72022e2249c9a13e5ed8a4583717a626026773f5416440d573a938c93");
            ItemMeta meta = sorterItem.getItemMeta();
            meta.getPersistentDataContainer().set(Key.SORTER, PersistentDataType.INTEGER, sorter);
            meta.setDisplayName("by name");
            sorterItem.setItemMeta(meta);
            return sorterItem;
        } else {
            ItemStack sorterItem = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/5a990d613ba553ddc5501e0436baabc17ce22eb4dc656d01e777519f8c9af23a");
            ItemMeta meta = sorterItem.getItemMeta();
            meta.getPersistentDataContainer().set(Key.SORTER, PersistentDataType.INTEGER, sorter);
            meta.setDisplayName("by amount");
            sorterItem.setItemMeta(meta);
            return sorterItem;
        }
    }

    public static Inventory setSorter(Inventory gui, int sorter) {
        ItemStack item = gui.getItem(5);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Key.SORTER, PersistentDataType.INTEGER, sorter);
        item.setItemMeta(meta);
        return gui;
    }

    public static int getSorter(Inventory gui) {
        if (gui.getItem(5).getItemMeta().getPersistentDataContainer().get(Key.SORTER, PersistentDataType.INTEGER) == null) {
            return 0;
        } else {
            return gui.getItem(5).getItemMeta().getPersistentDataContainer().get(Key.SORTER, PersistentDataType.INTEGER);
        }
    }

    public static Inventory setPage(Inventory gui, int page) {
        ItemStack item = gui.getItem(49);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Key.PAGE, PersistentDataType.INTEGER, page);
        item.setItemMeta(meta);


        return gui;
    }

    public static int getPage(Inventory gui) {
        if (gui.getItem(49).getItemMeta().getPersistentDataContainer().get(Key.PAGE, PersistentDataType.INTEGER) == null) {
            return 1;
        } else {
            return gui.getItem(49).getItemMeta().getPersistentDataContainer().get(Key.PAGE, PersistentDataType.INTEGER);
        }
    }

    public static Inventory setDesc(Inventory gui, boolean desc) {
        ItemStack item = gui.getItem(6);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Key.DESC, PersistentDataType.BOOLEAN, desc);
        item.setItemMeta(meta);
        return gui;
    }


    public static boolean getDesc(Inventory gui) {
        if (gui.getItem(6).getItemMeta().getPersistentDataContainer().get(Key.DESC, PersistentDataType.BOOLEAN) == null) {
            return true;
        } else {
            return gui.getItem(6).getItemMeta().getPersistentDataContainer().get(Key.DESC, PersistentDataType.BOOLEAN);
        }
    }
}
