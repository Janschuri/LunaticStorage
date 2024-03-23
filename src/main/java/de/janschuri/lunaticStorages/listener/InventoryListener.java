package de.janschuri.lunaticStorages.listener;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.Storage;
import de.janschuri.lunaticStorages.StoragePanelGUI;
import de.janschuri.lunaticStorages.nms.SignGUI;
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
                int id = gui.getItem(8).getItemMeta().getPersistentDataContainer().get(LunaticStorage.keyPanelID, PersistentDataType.INTEGER);
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
                            if (LunaticStorage.getDatabase().getPanelsStorageItem(id) != null) {
                                if (processingClickEvent) return;
                                Storage storage = LunaticStorage.storages.get(id);
                                byte[] serializedItem = LunaticStorage.getDatabase().getPanelsStorageItem(id);
                                ItemStack storageItem = LunaticStorage.deserializeItemStack(serializedItem);
                                ItemMeta storageItemMeta = storageItem.getItemMeta();
                                PersistentDataContainer dataContainerStorageItem = storageItemMeta.getPersistentDataContainer();
                                int[] chests = dataContainerStorageItem.get(LunaticStorage.keyStorage, PersistentDataType.INTEGER_ARRAY);

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
                                LunaticStorage.storages.put(id, storage);

                                gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                            }
                        } else {
                            if (processingClickEvent) return;
                            processingClickEvent = true;

                            if (!cursorItem.isEmpty()) {
                                if (dataContainer.has(LunaticStorage.keyStoragePane)) {
                                    ItemStack itemCursor = event.getCursor();
                                    ItemMeta metaCursor = itemCursor.getItemMeta();

                                    PersistentDataContainer dataContainerCursor = metaCursor.getPersistentDataContainer();
                                    if (dataContainerCursor.has(LunaticStorage.keyStorage)) {
                                        ItemStack cursorClone = itemCursor.clone();
                                        cursorClone.setAmount(1);
                                        byte[] storage = LunaticStorage.serializeItemStack(cursorClone);
                                        LunaticStorage.getDatabase().savePanelsData(id, storage);
                                        gui.setItem(8, cursorClone);
                                        event.getCursor().subtract(1);

                                        gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                                    }
                                } else if (dataContainer.has(LunaticStorage.keyStorageContent) && LunaticStorage.getDatabase().getPanelsStorageItem(id) != null) {
                                    Storage storage = LunaticStorage.storages.get(id);



                                    ItemStack newItem = storage.insertItemsIntoStorage(cursorItem, player);

                                    player.setItemOnCursor(newItem);
                                    int amount = cursorItem.getAmount() - newItem.getAmount();
                                    storage.updateStorageMap(cursorItem, amount);
                                    LunaticStorage.storages.put(id, storage);

                                    gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                                }
                            } else if (dataContainer.has(LunaticStorage.keyStorageContent)) {
                                Storage storage = LunaticStorage.storages.get(id);
                                byte[] serializedItem = LunaticStorage.getDatabase().getPanelsStorageItem(id);
                                ItemStack storageItem = LunaticStorage.deserializeItemStack(serializedItem);
                                ItemMeta storageItemMeta = storageItem.getItemMeta();
                                PersistentDataContainer dataContainerStorageItem = storageItemMeta.getPersistentDataContainer();
                                int[] chests = dataContainerStorageItem.get(LunaticStorage.keyStorage, PersistentDataType.INTEGER_ARRAY);


                                ItemStack newItem = storage.getItemsFromStorage(item, player);

                                if (!newItem.isEmpty()) {
                                    player.setItemOnCursor(newItem);
                                    int amount = newItem.getAmount();
                                    storage.updateStorageMap(newItem, -(amount));
                                    LunaticStorage.storages.put(id, storage);
                                    gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                                }
                            } else if (dataContainer.has(LunaticStorage.keySearch)) {
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

                            } else if (dataContainer.has(LunaticStorage.keyStorage)) {
                                LunaticStorage.getDatabase().savePanelsData(id, null);
                                player.setItemOnCursor(item);
                                LunaticStorage.storages.remove(id);
                                gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                            } else if (dataContainer.has(LunaticStorage.keyRightArrow)) {
                                int page = StoragePanelGUI.getPage(gui) + 1;
                                gui = StoragePanelGUI.setPage(gui, page);
                                gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                            } else if (dataContainer.has(LunaticStorage.keyLeftArrow)) {
                                int page = StoragePanelGUI.getPage(gui) - 1;
                                gui = StoragePanelGUI.setPage(gui, page);
                                gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                            } else if (dataContainer.has(LunaticStorage.keyDesc)) {
                                boolean desc = dataContainer.get(LunaticStorage.keyDesc, PersistentDataType.BOOLEAN);
                                StoragePanelGUI.setDesc(gui, !desc);
                                gui = StoragePanelGUI.loadGui(gui, id, world, locale);
                            } else if (dataContainer.has(LunaticStorage.keySorter)) {
                                int sorter = dataContainer.get(LunaticStorage.keySorter, PersistentDataType.INTEGER);

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