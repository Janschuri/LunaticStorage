package de.janschuri.lunaticStorages.listener;

import de.janschuri.lunaticStorages.config.LanguageConfig;
import de.janschuri.lunaticStorages.storage.Key;
import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.storage.Storage;
import de.janschuri.lunaticStorages.storage.StoragePanelGUI;
import de.janschuri.lunaticStorages.database.tables.PanelsTable;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;


public class InventoryClickListener implements Listener {
    private boolean processingClickEvent = false;
    private boolean storageFullTimeout = false;

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Inventory gui = event.getInventory();
        Inventory playerInv = event.getWhoClicked().getInventory();

        if (gui.contains(StoragePanelGUI.GUI_PANE)) {
            event.setCancelled(true);
        }
        if (event.getInventory().equals(playerInv)) {
            event.setCancelled(false);
        }

    }


    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory gui = event.getView().getTopInventory();

        if (!gui.contains(StoragePanelGUI.GUI_PANE)) {
            return;
        }

        event.setCancelled(true);
        int id = gui.getItem(8).getItemMeta().getPersistentDataContainer().get(Key.PANEL_ID, PersistentDataType.INTEGER);
        Player player = (Player) event.getWhoClicked();
        String locale = player.getLocale().toLowerCase();
        World world = player.getWorld();
        ItemStack item = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        Inventory playerInv = event.getView().getBottomInventory();

        if (event.getClickedInventory() == playerInv && !event.isShiftClick()) {
            event.setCancelled(false);
            return;
        }

        if (storageFullTimeout) {
            return;
        }


        if (item == null || item.getType() == Material.AIR) {
            Storage storage = LunaticStorage.getStorage(id);
            ItemStack newItem = storage.insertItemsIntoStorage(cursorItem, player);

            if (!cursorItem.getType().equals(Material.AIR)) {
//                player.sendMessage(LanguageConfig.getLanguageConfig().getMessage("storage_full"));
                storageFullTimeout = true;
                Bukkit.getScheduler().runTaskLater(LunaticStorage.getInstance(), () -> {
                    storageFullTimeout = false;
                }, 40L);
            }

            player.setItemOnCursor(newItem);
            LunaticStorage.addStorage(id, storage);

            StoragePanelGUI.loadGui(gui, id, world, locale);
            return;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

        if (event.getClickedInventory() == playerInv && event.isShiftClick()) {
            if (PanelsTable.getPanelsStorageItem(id) != null) {
                if (processingClickEvent) return;
                Storage storage = LunaticStorage.getStorage(id);

                ItemStack newItem = storage.insertItemsIntoStorage(item, player);

                event.getClickedInventory().setItem(event.getSlot(), newItem);

//                if (!newItem.isEmpty()) {
//                    processingClickEvent = true;
//                    Bukkit.getScheduler().runTaskLater(LunaticStorage.getInstance(), () -> {
//                        processingClickEvent = false;
//                    }, 2L);
////                    player.sendMessage(LanguageConfig.getLanguageConfig().getMessage("storage_full"));
//                    storageFullTimeout = true;
//                    Bukkit.getScheduler().runTaskLater(LunaticStorage.getInstance(), () -> {
//                        storageFullTimeout = false;
//                    }, 40L);
//                }

                LunaticStorage.addStorage(id, storage);

                StoragePanelGUI.loadGui(gui, id, world, locale);
            }

            if (dataContainer.has(Key.STORAGE)) {
                    ItemStack clone = item.clone();
                    clone.setAmount(1);
                    byte[] storage = ItemStackUtils.serializeItemStack(clone);
                    PanelsTable.savePanelsData(id, storage);
                    gui.setItem(8, clone);
                    item.setAmount(item.getAmount() - 1);

                    StoragePanelGUI.loadGui(gui, id, world, locale);
                return;
            }

            return;
        }

        if (processingClickEvent) {
            return;
        }
        processingClickEvent = true;
        Bukkit.getScheduler().runTaskLater(LunaticStorage.getInstance(), () -> {
            processingClickEvent = false;
        }, 2L);

//        if (!cursorItem.isEmpty()) {
//            if (dataContainer.has(Key.STORAGE_PANE)) {
//                ItemStack itemCursor = event.getCursor();
//                ItemMeta metaCursor = itemCursor.getItemMeta();
//
//                PersistentDataContainer dataContainerCursor = metaCursor.getPersistentDataContainer();
//                if (dataContainerCursor.has(Key.STORAGE)) {
//                    ItemStack cursorClone = itemCursor.clone();
//                    cursorClone.setAmount(1);
//                    byte[] storage = ItemStackUtils.serializeItemStack(cursorClone);
//                    PanelsTable.savePanelsData(id, storage);
//                    gui.setItem(8, cursorClone);
////                    event.getCursor().subtract(1);
//
//                    StoragePanelGUI.loadGui(gui, id, world, locale);
//                }
//                return;
//            }
//
//            if (dataContainer.has(Key.STORAGE_CONTENT) && PanelsTable.getPanelsStorageItem(id) != null) {
//                Storage storage = LunaticStorage.getStorage(id);
//                ItemStack newItem = storage.insertItemsIntoStorage(cursorItem, player);
//
//                player.setItemOnCursor(newItem);
//
////                if (!newItem.isEmpty()) {
//////                    player.sendMessage(LanguageConfig.getLanguageConfig().getMessage("storage_full"));
////                    storageFullTimeout = true;
////                    Bukkit.getScheduler().runTaskLater(LunaticStorage.getInstance(), () -> {
////                        storageFullTimeout = false;
////                    }, 40L);
////                }
//
//                LunaticStorage.addStorage(id, storage);
//
//                StoragePanelGUI.loadGui(gui, id, world, locale);
//                return;
//            }
//            return;
//        }

        if (dataContainer.has(Key.STORAGE_CONTENT)) {
            Storage storage = LunaticStorage.getStorage(id);

            ItemStack newItem = storage.getItemsFromStorage(item, player);

//            if (!newItem.isEmpty()) {
                player.setItemOnCursor(newItem);
                LunaticStorage.addStorage(id, storage);
                StoragePanelGUI.loadGui(gui, id, world, locale);
//            }
            return;
        }

        if (dataContainer.has(Key.SEARCH)) {
//            gui.close();

//            Inventory finalGui = gui;
//            SignGUI.sendSign(LunaticStorage.getInstance(), player, lines -> {
//                String search = String.join(" ", lines).trim();
//                StoragePanelGUI.setSearch(finalGui, search);
//                StoragePanelGUI.loadGui(finalGui, id, world, locale);
//                player.openInventory(finalGui);
//            });
//            return;
        }

        if (dataContainer.has(Key.STORAGE)) {
            PanelsTable.savePanelsData(id, null);
            player.setItemOnCursor(item);
            LunaticStorage.removeStorage(id);
            StoragePanelGUI.loadGui(gui, id, world, locale);
            return;
        }

        if (dataContainer.has(Key.RIGHT_ARROW)) {
            int page = StoragePanelGUI.getPage(gui) + 1;
            StoragePanelGUI.setPage(gui, page);
            StoragePanelGUI.loadGui(gui, id, world, locale);
            return;
        }

        if (dataContainer.has(Key.LEFT_ARROW)) {
            int page = StoragePanelGUI.getPage(gui) - 1;
            StoragePanelGUI.setPage(gui, page);
            StoragePanelGUI.loadGui(gui, id, world, locale);
            return;
        }

        if (dataContainer.has(Key.DESC)) {
            boolean desc = dataContainer.get(Key.DESC, PersistentDataType.BOOLEAN);
            StoragePanelGUI.setDesc(gui, !desc);
            StoragePanelGUI.loadGui(gui, id, world, locale);
            return;
        }

        if (dataContainer.has(Key.SORTER)) {
            int sorter = dataContainer.get(Key.SORTER, PersistentDataType.INTEGER);
            sorter = (sorter + 1) % 2;
            StoragePanelGUI.setSorter(gui, sorter);
            StoragePanelGUI.loadGui(gui, id, world, locale);
            return;
        }
    }

}