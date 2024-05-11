package de.janschuri.lunaticStorages.listener;

import de.janschuri.lunaticStorages.Keys;
import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.Storage;
import de.janschuri.lunaticStorages.StoragePanelGUI;
import de.janschuri.lunaticStorages.database.tables.PanelsTable;
import de.janschuri.lunaticStorages.nms.SignGUI;
import de.janschuri.lunaticlib.utils.ItemStackUtils;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class InventoryListener implements Listener {
    private final LunaticStorage plugin;
    private boolean processingClickEvent = false;
    private final Map<Integer, String> searchInputs = new HashMap<>();

    public InventoryListener(LunaticStorage plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

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
            if (gui.contains(StoragePanelGUI.GUI_PANE)) {
                event.setCancelled(true);
                int id = gui.getItem(8).getItemMeta().getPersistentDataContainer().get(Keys.PANEL_ID, PersistentDataType.INTEGER);
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
                            if (PanelsTable.getPanelsStorageItem(id) != null) {
                                if (processingClickEvent) return;
                                Storage storage = LunaticStorage.getStorage(id);
                                byte[] serializedItem =PanelsTable.getPanelsStorageItem(id);
                                ItemStack storageItem = ItemStackUtils.deserializeItemStack(serializedItem);
                                ItemMeta storageItemMeta = storageItem.getItemMeta();
                                PersistentDataContainer dataContainerStorageItem = storageItemMeta.getPersistentDataContainer();
                                int[] chests = dataContainerStorageItem.get(Keys.STORAGE, PersistentDataType.INTEGER_ARRAY);

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
                                LunaticStorage.addStorage(id, storage);

                                gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                            }
                        } else {
                            if (processingClickEvent) return;
                            processingClickEvent = true;

                            if (!cursorItem.isEmpty()) {
                                if (dataContainer.has(Keys.STORAGE_PANE)) {
                                    ItemStack itemCursor = event.getCursor();
                                    ItemMeta metaCursor = itemCursor.getItemMeta();

                                    PersistentDataContainer dataContainerCursor = metaCursor.getPersistentDataContainer();
                                    if (dataContainerCursor.has(Keys.STORAGE)) {
                                        ItemStack cursorClone = itemCursor.clone();
                                        cursorClone.setAmount(1);
                                        byte[] storage = ItemStackUtils.serializeItemStack(cursorClone);
                                        PanelsTable.savePanelsData(id, storage);
                                        gui.setItem(8, cursorClone);
                                        event.getCursor().subtract(1);

                                        gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                                    }
                                } else if (dataContainer.has(Keys.STORAGE_CONTENT) && PanelsTable.getPanelsStorageItem(id) != null) {
                                    Storage storage = LunaticStorage.getStorage(id);



                                    ItemStack newItem = storage.insertItemsIntoStorage(cursorItem, player);

                                    player.setItemOnCursor(newItem);
                                    int amount = cursorItem.getAmount() - newItem.getAmount();
                                    storage.updateStorageMap(cursorItem, amount);
                                    LunaticStorage.addStorage(id, storage);

                                    gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                                }
                            } else if (dataContainer.has(Keys.STORAGE_CONTENT)) {
                                Storage storage = LunaticStorage.getStorage(id);
                                byte[] serializedItem = PanelsTable.getPanelsStorageItem(id);
                                ItemStack storageItem = ItemStackUtils.deserializeItemStack(serializedItem);
                                ItemMeta storageItemMeta = storageItem.getItemMeta();
                                PersistentDataContainer dataContainerStorageItem = storageItemMeta.getPersistentDataContainer();
                                int[] chests = dataContainerStorageItem.get(Keys.STORAGE, PersistentDataType.INTEGER_ARRAY);


                                ItemStack newItem = storage.getItemsFromStorage(item, player);

                                if (!newItem.isEmpty()) {
                                    player.setItemOnCursor(newItem);
                                    int amount = newItem.getAmount();
                                    storage.updateStorageMap(newItem, -(amount));
                                    LunaticStorage.addStorage(id, storage);
                                    gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                                }
                            } else if (dataContainer.has(Keys.SEARCH)) {
                                gui.close();
                                SignGUI signGui = new SignGUI(plugin);

                                Inventory finalGui = gui;
                                signGui.sendSign(player, lines -> {
                                    // The lines array will always have a length of 4, filling empty prompts with empty strings
                                    String search = String.join(" ", lines).trim(); // Join lines to get a single message, then trim it
                                    StoragePanelGUI.setSearch(finalGui, search);
                                    player.sendMessage("Your message was: " + search);
                                    StoragePanelGUI.loadGui(finalGui, id, world, locale);
                                    player.openInventory(finalGui);
                                });

                            } else if (dataContainer.has(Keys.STORAGE)) {
                                PanelsTable.savePanelsData(id, null);
                                player.setItemOnCursor(item);
                                LunaticStorage.removeStorage(id);
                                gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                            } else if (dataContainer.has(Keys.RIGHT_ARROW)) {
                                int page = StoragePanelGUI.getPage(gui) + 1;
                                gui = StoragePanelGUI.setPage(gui, page);
                                gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                            } else if (dataContainer.has(Keys.LEFT_ARROW)) {
                                int page = StoragePanelGUI.getPage(gui) - 1;
                                gui = StoragePanelGUI.setPage(gui, page);
                                gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                            } else if (dataContainer.has(Keys.DESC)) {
                                boolean desc = dataContainer.get(Keys.DESC, PersistentDataType.BOOLEAN);
                                StoragePanelGUI.setDesc(gui, !desc);
                                gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                            } else if (dataContainer.has(Keys.STORAGE)) {
                                int sorter = dataContainer.get(Keys.STORAGE, PersistentDataType.INTEGER);

                                sorter = (sorter+1) % 2;
                                StoragePanelGUI.setSorter(gui, sorter);
                                gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                            }

                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                processingClickEvent = false;
                            }, 5L);
                        }
                    }
            }
    }

}