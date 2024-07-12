package de.janschuri.lunaticStorages.gui;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.database.tables.PanelsTable;
import de.janschuri.lunaticStorages.storage.Key;
import de.janschuri.lunaticStorages.storage.Storage;
import de.janschuri.lunaticStorages.utils.Logger;
import de.janschuri.lunaticStorages.utils.Utils;
import de.janschuri.lunaticlib.MessageKey;
import de.janschuri.lunaticlib.platform.bukkit.external.AdventureAPI;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.GUIManager;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.InventoryButton;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.InventoryGUI;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.PlayerInvButton;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StorageGUI extends InventoryGUI {

    private static final MessageKey STORAGE_FULL = new MessageKey("storage_full");
    private static final MessageKey AMOUNT = new MessageKey("amount");
    private static final MessageKey PAGE = new MessageKey("page");
    private static final MessageKey GUI_TITLE = new MessageKey("gui_title");

    protected static final AtomicInteger requestIdGenerator = new AtomicInteger(0);
    private static final Map<Integer, StorageGUI> storageGUIs = new HashMap<>();

    private String locale;
    private final int id;

    private boolean storageFullTimeout = false;
    private boolean processingClickEvent = false;

    private int panelId;
    private Storage storage;

    byte[] serializedStorageItem;
    int sorter = 0;

    private int page = 0;
    private boolean descending = false;
    private String search = "";
    private final Player player;



    private StorageGUI(Player player, int panelId, String locale) {
        super(createInventory());
        this.id = requestIdGenerator.getAndIncrement();
        this.player = player;
        this.panelId = panelId;
        this.locale = locale;
        this.serializedStorageItem = PanelsTable.getPanelsStorageItem(panelId);
        this.storage = Storage.getStorage(panelId,  serializedStorageItem);
        storageGUIs.put(id, this);
    }

    private StorageGUI(StorageGUI storageGUI) {
        super(storageGUI.getInventory());
        this.id = storageGUI.id;
        this.player = storageGUI.player;
        this.panelId = storageGUI.panelId;
        this.locale = storageGUI.locale;

        this.page = storageGUI.page;
        this.descending = storageGUI.descending;
        this.search = storageGUI.search;
        this.sorter = storageGUI.sorter;
        this.processingClickEvent = storageGUI.processingClickEvent;
        this.storageFullTimeout = storageGUI.storageFullTimeout;

        this.serializedStorageItem = PanelsTable.getPanelsStorageItem(panelId);
        this.storage = Storage.getStorage(panelId,  serializedStorageItem);
        decorate(player);
        storageGUIs.put(id, this);
    }

    private static Inventory createInventory() {
        String title = LunaticStorage.getLanguageConfig().getMessageAsString(GUI_TITLE);
        Inventory inv = Bukkit.createInventory(null, 54, title);

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            inv.setItem(i + 45, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }

        return inv;
    }

    @Override
    public void decorate(Player player) {
        setStorageItem();
        loadStorage();
        addButton(createItemButton());
        addButton(createStoragePlayerInvButton());
        addButton(48, createArrowLeft());
        addButton(49, createPageButton());
        addButton(50, createArrowRight());
        super.decorate(player);
    }

    private void setStorageItem() {
        if (serializedStorageItem != null) {
            this.addButton(8, createStorageButton());
        } else {
            this.addButton(8, createStoragePane());
        }
    }

    private InventoryButton createStoragePane() {
        return new InventoryButton()
                .creator(player -> new ItemStack(Material.CYAN_STAINED_GLASS_PANE))
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();
                    ItemStack cursor = event.getCursor();

                    ItemStack newItem = insertStorageItem(cursor, false);
                    player.setItemOnCursor(newItem);

                    reloadGui();
                });
    }

    private InventoryButton createStorageButton() {
        ItemStack chest = ItemStackUtils.deserializeItemStack(serializedStorageItem);
        return new InventoryButton()
                .creator(player -> chest)
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();
                    ItemStack cursor = event.getCursor().clone();

                    ItemStack newItem = insertStorageItem(cursor, true, event.isRightClick());
                    player.setItemOnCursor(newItem);

                    reloadGui();
                });
    }

    private InventoryButton createStorageContentButton(ItemStack item, int amount) {
        ItemStack displayItem = item.clone();
        ItemMeta meta = displayItem.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (meta != null) {
            lore = meta.getLore();
        }

        if (lore == null) {
            lore = new ArrayList<>();
        }

        String amountText = LunaticStorage.getLanguageConfig().getMessageAsLegacyString(AMOUNT).replace("%amount%", String.valueOf(amount));
        lore.add(amountText);

        if (meta != null) {
            meta.setLore(lore);
        }
        displayItem.setItemMeta(meta);

        return new InventoryButton()
                .creator(player -> displayItem)
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();
                    ItemStack cursorItem = event.getCursor();

                    if (cursorItem.getType().equals(Material.AIR)) {
                        ItemStack newItem = storage.getItemsFromStorage(item, player);

                        if (!newItem.getType().equals(Material.AIR)) {
                            player.setItemOnCursor(newItem);
                        }

                        reloadGui();
                    } else {
                        ItemStack newItem = insertItem(player, cursorItem);
                        player.setItemOnCursor(newItem);
                    }
                });
    }

    private InventoryButton createPageButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack(Material.PAPER);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(LunaticStorage.getLanguageConfig().getMessageAsLegacyString(PAGE, false)
                            .replace("%page%", String.valueOf(page + 1))
                            .replace("%pages%", String.valueOf(storage.getPages())));
                    item.setItemMeta(meta);
                    return item;
                });
    }

    private InventoryButton createArrowLeft() {

        return new InventoryButton()
                .creator(player -> {
                    ItemStack arrow = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/f6dab7271f4ff04d5440219067a109b5c0c1d1e01ec602c0020476f7eb612180");
                    ItemMeta meta = arrow.getItemMeta();
                    meta.setDisplayName("<<<");

                    arrow.setItemMeta(meta);
                    return arrow;
                })
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    setPage(page - 1);
                    reloadGui();
                });
    }

    private InventoryButton createArrowRight() {

        return new InventoryButton()
                .creator(player -> {
                    ItemStack arrow = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/8aa187fede88de002cbd930575eb7ba48d3b1a06d961bdc535800750af764926");
                    ItemMeta meta = arrow.getItemMeta();
                    meta.setDisplayName(">>>");

                    arrow.setItemMeta(meta);
                    return arrow;
                })
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    setPage(page + 1);
                    reloadGui();
                });
    }

    private PlayerInvButton createItemButton() {
        return new PlayerInvButton()
                .condition(event -> {
                    ItemStack item = event.getCurrentItem();

                    if (item == null) {
                        return false;
                    }

                    if (item.getType().equals(Material.AIR)) {
                        return false;
                    }

                    if (!event.isShiftClick()) {
                        return false;
                    }

                    if (serializedStorageItem == null) {
                        return false;
                    }

                    return !isStorageItem(item);
                })
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();
                    ItemStack cursorItem = event.getCurrentItem();

                    ItemStack newItem = insertItem(player, cursorItem);
                    event.setCurrentItem(newItem);

                    reloadGui();
                });
    }

    private PlayerInvButton createStoragePlayerInvButton() {
        return new PlayerInvButton()
                .condition(event -> {
                    ItemStack item = event.getCurrentItem();

                    if (item == null) {
                        return false;
                    }

                    if (item.getType().equals(Material.AIR)) {
                        return false;
                    }

                    if (!event.isShiftClick()) {
                        return false;
                    }

                    return isStorageItem(item);
                })
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }


                    ItemStack item = event.getCurrentItem();

                    ItemStack newItem = insertStorageItem(item, true);
                    event.setCurrentItem(newItem);

                    reloadGui();
                });
    }

    private void setPage(int page) {
        if (page > storage.getPages()) {
            page = storage.getPages();
        }
        if (page < 0) {
            page = 0;
        }

        this.page = page;
    }

    private void loadStorage() {
        List<Map.Entry<ItemStack, Integer>> storageItems = storage.getStorageList(locale, sorter, descending, search, page);
        setPage(page);

        if (serializedStorageItem != null) {
            for (int i = 0; i < 36; i++) {
                if (i < storageItems.size()) {
                    Map.Entry<ItemStack, Integer> entry = storageItems.get(i);
                    this.addButton(9 + i, createStorageContentButton(entry.getKey(), entry.getValue()));
                } else {
                    this.addButton(9 + i, createStorageContentButton(new ItemStack(Material.AIR), 0));
                }
            }
        } else {
            for (int i = 0; i < 36; i++) {
                this.addButton(9 + i, createStorageContentButton(new ItemStack(Material.AIR), 0));
            }
        }
    }

    public static StorageGUI getStorageGUI(Player player, int panelId) {
        return new StorageGUI(player, panelId, player.getLocale());
    }

    private void reloadGui() {
        GUIManager.openGUI(new StorageGUI(this), player, false);
    }

    private ItemStack insertItem(Player player, ItemStack item) {
        if (storageFullTimeout) {
            return item;
        }

        ItemStack newItem = storage.insertItemsIntoStorage(item, player);

        if (newItem.getAmount() > 0) {
            AdventureAPI.sendMessage(player, LunaticStorage.getLanguageConfig().getMessage(STORAGE_FULL));
            storageFullTimeout = true;
            Runnable runnable = () -> {
                storageGUIs.get(id).storageFullTimeout = false;
            };

            Utils.scheduleTask(runnable, 1000, TimeUnit.MILLISECONDS);
        }

        reloadGui();

        return newItem;
    }

    private static boolean isStorageItem(ItemStack item) {
        if (item == null) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return false;
        }

        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

        return dataContainer.has(Key.STORAGE);
    }

    private boolean processingClickEvent() {
        boolean result = processingClickEvent;

        processingClickEvent = true;
        Runnable runnable = () -> {
            storageGUIs.get(id).processingClickEvent = false;
        };

        Utils.scheduleTask(runnable, 100, TimeUnit.MILLISECONDS);
        return result;
    }

    private ItemStack insertStorageItem(ItemStack item, boolean swapItems) {
        return insertStorageItem(item, swapItems, false);
    }

    private ItemStack insertStorageItem(ItemStack item, boolean swapItems, boolean isRightClick) {

        if (!isStorageItem(item) && !item.getType().equals(Material.AIR)) {
            Logger.debugLog("insertStorageItem: not storage item");
            return item;
        }

        ItemStack result;
        ItemStack storageItem;

        if (serializedStorageItem == null) {
            storageItem = item.clone();
            result = new ItemStack(Material.AIR);
        } else {
            storageItem = ItemStackUtils.deserializeItemStack(serializedStorageItem);

            if (storageItem.isSimilar(item) && item.getType() != Material.AIR) {
                if (storageItem.getMaxStackSize() > storageItem.getAmount()) {
                    int itemAmount = item.getAmount();
                    int storageItemAmount = storageItem.getAmount();
                    int totalAmount = itemAmount + storageItemAmount;
                    if (totalAmount <= storageItem.getMaxStackSize()) {
                        storageItem.setAmount(totalAmount);
                        result = new ItemStack(Material.AIR);
                    } else {
                        storageItem.setAmount(storageItem.getMaxStackSize());
                        int newItemAmount = totalAmount - storageItem.getMaxStackSize();
                        item.setAmount(newItemAmount);
                        result = item.clone();
                    }
                } else {
                    result = item;
                }
            } else {
                if (swapItems) {
                    result = storageItem.clone();
                    storageItem = item.clone();
                } else {
                    result = item.clone();
                }
            }
        }

        if (!storageItem.getType().isAir()) {
            serializedStorageItem = ItemStackUtils.serializeItemStack(storageItem);
            this.addButton(8, createStorageButton());
        } else {
            serializedStorageItem = null;
            this.addButton(8, createStoragePane());
        }

        PanelsTable.savePanelsData(panelId, serializedStorageItem);


        Logger.debugLog("insertStorageItem: " + storageItem);
        return result;
    }
}
