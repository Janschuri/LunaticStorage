package de.janschuri.lunaticstorage.gui;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.Key;
import de.janschuri.lunaticstorage.storage.Storage;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import de.janschuri.lunaticlib.MessageKey;
import de.janschuri.lunaticlib.platform.bukkit.external.AdventureAPI;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.GUIManager;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.InventoryButton;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.InventoryGUI;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.PlayerInvButton;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StorageGUI extends InventoryGUI {

    private static final MessageKey STORAGE_FULL_MK = new MessageKey("storage_full");
    private static final MessageKey AMOUNT_MK = new MessageKey("amount");
    private static final MessageKey PAGE_MK = new MessageKey("page");
    private static final MessageKey TOTAL_ITEMS_MK = new MessageKey("total_items");
    private static final MessageKey LOADED_CONTAINERS_MK = new MessageKey("loaded_containers");
    private static final MessageKey TOTAL_CONTAINERS_MK = new MessageKey("total_containers");
    private static final MessageKey RANGE_MK = new MessageKey("range");
    private static final MessageKey GUI_TITLE_MK = new MessageKey("gui_title");

    private static final Map<Integer, StorageGUI> storageGUIs = new HashMap<>();
    private static final Map<UUID, Map<Integer, Integer>> playerStorageGUIs = new HashMap<>();

    private static final AtomicInteger atomicInteger = new AtomicInteger(0);
    private final int id;
    private String locale;

    private boolean storageFullTimeout = false;
    private boolean processingClickEvent = false;

    private Block block;
    private int panelId;
    private Storage storage;

    int sorter = 0;
    private int page = 0;
    private int pages = 0;
    private boolean descending = false;
    private String search = "";
    private final Player player;



    private StorageGUI(Player player, Block block, String locale) {
        super(createInventory());
        this.id = atomicInteger.getAndIncrement();
        this.player = player;
        this.block = block;
        this.panelId = block.hashCode();
        this.locale = locale;
        this.storage = Storage.getStorage(block);
        storageGUIs.put(id, this);
        if (playerStorageGUIs.containsKey(player.getUniqueId())) {
            Map<Integer, Integer> ids = playerStorageGUIs.get(player.getUniqueId());
            ids.put(panelId, id);
            playerStorageGUIs.put(player.getUniqueId(), ids);
        } else {
            playerStorageGUIs.put(player.getUniqueId(), new HashMap<>(Map.of(panelId, id)));
        }
    }

    private StorageGUI(StorageGUI storageGUI) {
        super(storageGUI.getInventory());
        this.page = storageGUI.page;
        this.id = storageGUI.id;
        this.player = storageGUI.player;
        this.block = storageGUI.block;
        this.panelId = storageGUI.panelId;
        this.locale = storageGUI.locale;

        this.descending = storageGUI.descending;
        this.search = storageGUI.search;
        this.sorter = storageGUI.sorter;
        this.processingClickEvent = storageGUI.processingClickEvent;
        this.storageFullTimeout = storageGUI.storageFullTimeout;

        this.storage = Storage.getStorage(block);
        decorate(player);
        storageGUIs.put(id, this);

    }

    private StorageGUI(int id) {
        super(createInventory());
        StorageGUI storageGUI = storageGUIs.get(id);
        this.page = storageGUI.page;
        this.id = storageGUI.id;
        this.player = storageGUI.player;
        this.block = storageGUI.block;
        this.panelId = storageGUI.panelId;
        this.locale = storageGUI.locale;

        this.descending = storageGUI.descending;
        this.search = storageGUI.search;
        this.sorter = storageGUI.sorter;
        this.processingClickEvent = storageGUI.processingClickEvent;
        this.storageFullTimeout = storageGUI.storageFullTimeout;

        this.storage = Storage.getStorage(block);
        decorate(player);
        storageGUIs.put(id, this);

    }

    public static StorageGUI getStorageGUI(Player player, Block block) {
        if (playerStorageGUIs.containsKey(player.getUniqueId())) {
            int panelId = block.hashCode();
            Map<Integer, Integer> ids = playerStorageGUIs.get(player.getUniqueId());
            if (ids.containsKey(panelId)) {
                int id = ids.get(panelId);
                return new StorageGUI(id);
            }
        }
        return new StorageGUI(player, block, player.getLocale());
    }

    private static Inventory createInventory() {
        String title = LunaticStorage.getLanguageConfig().getMessageAsString(GUI_TITLE_MK);
        Inventory inv = Bukkit.createInventory(null, 54, title);

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            inv.setItem(i + 45, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }

        return inv;
    }

    @Override
    public void decorate(Player player) {
        createStorageButton();
        createRangeButton();
        createStorageItemsButtons();
        addButton(createItemButton());
        addButton(createStoragePlayerInvButton());
        addButton(0, createSearchButton());
        addButton(4, createSorterButton());
        addButton(5, createDescendingButton());

        if (page > 0) {
            addButton(48, createArrowLeft());
        } else {
            addButton(48, createEmptyButton());
        }

        addButton(49, createPageButton());

        if (page < pages) {
            addButton(50, createArrowRight());
        } else {
            addButton(50, createEmptyButton());
        }
        super.decorate(player);
    }

    private void createStorageButton() {
        ItemStack storageItem = storage.getStorageItem();

        if (storageItem != null) {
            this.addButton(8, createStorageItemButton());
        } else {
            this.addButton(8, createStoragePaneButton());
        }
    }

    private void createRangeButton() {
        ItemStack rangeItem = storage.getRangeItem();

        if (rangeItem != null) {
            this.addButton(7, createRangeItemButton());
        } else {
            this.addButton(7, createRangePaneButton());
        }
    }

    private InventoryButton createEmptyButton() {
        return new InventoryButton()
                .creator(player -> new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
    }

    private InventoryButton createStoragePaneButton() {
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

    private InventoryButton createRangePaneButton() {
        return new InventoryButton()
                .creator(player -> new ItemStack(Material.RED_STAINED_GLASS_PANE))
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();
                    ItemStack cursor = event.getCursor();

                    ItemStack newItem = insertRangeItem(cursor, false);
                    player.setItemOnCursor(newItem);

                    reloadGui();
                });
    }

    private InventoryButton createStorageItemButton() {
        ItemStack item = storage.getStorageItem();
        return new InventoryButton()
                .creator(player -> item)
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();
                    ItemStack cursor = event.getCursor().clone();

                    Logger.debugLog("Cursor: " + cursor);

                    ItemStack newItem = insertStorageItem(cursor, true, event.isRightClick());

                    Logger.debugLog("New Item: " + newItem);
                    player.setItemOnCursor(newItem);

                    reloadGui();
                });
    }

    private InventoryButton createRangeItemButton() {
        ItemStack item = storage.getRangeItem();
        return new InventoryButton()
                .creator(player -> item)
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();
                    ItemStack cursor = event.getCursor().clone();

                    ItemStack newItem = insertRangeItem(cursor, true, event.isRightClick());
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

        String amountText = LunaticStorage.getLanguageConfig().getMessageAsLegacyString(AMOUNT_MK).replace("%amount%", String.valueOf(amount));
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
                    meta.setDisplayName(LunaticStorage.getLanguageConfig().getMessageAsLegacyString(PAGE_MK, false)
                            .replace("%page%", String.valueOf(page + 1))
                            .replace("%pages%", String.valueOf(pages + 1)));
                    List<String> lore = new ArrayList<>();

                    String totalItems = LunaticStorage.getLanguageConfig().getMessageAsLegacyString(TOTAL_ITEMS_MK, false)
                            .replace("%amount%", storage.getTotalAmount() + "");
                    String range = LunaticStorage.getLanguageConfig().getMessageAsLegacyString(RANGE_MK, false)
                            .replace("%range%", storage.getRange() + "");
                    String loadedContainers = LunaticStorage.getLanguageConfig().getMessageAsLegacyString(LOADED_CONTAINERS_MK, false)
                            .replace("%amount%", storage.getLoadedContainersAmount() + "");
                    String totalContainers = LunaticStorage.getLanguageConfig().getMessageAsLegacyString(TOTAL_CONTAINERS_MK, false)
                            .replace("%amount%", storage.getContainerAmount() + "");

                    lore.add(totalItems);
                    lore.add(range);
                    lore.add(loadedContainers);
                    lore.add(totalContainers);

                    meta.setLore(lore);

                    item.setItemMeta(meta);
                    return item;
                });
    }

    private InventoryButton createDescendingButton() {
        return new InventoryButton()
                .creator(player -> {
                    if (descending) {
                        ItemStack arrow = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/a3852bf616f31ed67c37de4b0baa2c5f8d8fca82e72dbcafcba66956a81c4");
                        ItemMeta meta = arrow.getItemMeta();
                        meta.setDisplayName("Descended");
                        arrow.setItemMeta(meta);
                        return arrow;
                    } else {
                        ItemStack arrow = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/b221da4418bd3bfb42eb64d2ab429c61decb8f4bf7d4cfb77a162be3dcb0b927");
                        ItemMeta meta = arrow.getItemMeta();
                        meta.setDisplayName("Ascended");
                        arrow.setItemMeta(meta);
                        return arrow;
                    }
                })
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    descending = !descending;
                    reloadGui();
                });
    }

    private InventoryButton createSorterButton() {
        return new InventoryButton()
                .creator(player -> {
                    if (sorter == 0) {
                        ItemStack sorterItem = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/bc35e72022e2249c9a13e5ed8a4583717a626026773f5416440d573a938c93");
                        ItemMeta meta = sorterItem.getItemMeta();
                        meta.setDisplayName("by name");
                        sorterItem.setItemMeta(meta);
                        return sorterItem;
                    } else {
                        ItemStack sorterItem = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/5a990d613ba553ddc5501e0436baabc17ce22eb4dc656d01e777519f8c9af23a");
                        ItemMeta meta = sorterItem.getItemMeta();
                        meta.setDisplayName("by amount");
                        sorterItem.setItemMeta(meta);
                        return sorterItem;
                    }
                })
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    sorter = (sorter + 1) % 2;
                    reloadGui();
                });
    }

    private InventoryButton createSearchButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack(Material.COMPASS);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName("Search");
                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();

                    SignGUI gui = SignGUI.builder()
                            .setType(Material.DARK_OAK_SIGN)
                            .setHandler((p, result) -> {
                                StringBuilder search = new StringBuilder();
                                for (int i = 0; i < 4; i++) {
                                    search.append(result.getLine(i));
                                }

                                return List.of(
                                        SignGUIAction.run(() ->{
                                                    Bukkit.getScheduler().runTask(LunaticStorage.getInstance(), () -> {
                                                        this.search = search.toString();
                                                        GUIManager.openGUI(new StorageGUI(this), player);
                                                    });
                                                })
                                );
                            })
                            .build();

                    gui.open(player);
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

                    if (storage.getStorageItem() == null) {
                        return false;
                    }

                    return !Utils.isStorageItem(item);
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

                    return Utils.isStorageItem(item);
                })
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }


                    ItemStack item = event.getCurrentItem();

                    ItemStack newItem = insertStorageItem(item, false);
                    event.setCurrentItem(newItem);

                    reloadGui();
                });
    }

    private void setPage(int page) {
        if (page > pages) {
            page = pages;
        }
        if (page < 0) {
            page = 0;
        }

        this.page = page;
    }

    private void createStorageItemsButtons() {
        List<Map.Entry<ItemStack, Integer>> allStorageItems = storage.getStorageList(locale, sorter, descending, search);
        int pageSize = 36;
        pages = allStorageItems.size() / pageSize;
        setPage(page);

        int startIndex = page * pageSize;
        int endIndex = startIndex + pageSize;

        if (endIndex > allStorageItems.size()) {
            endIndex = allStorageItems.size();
        }

        List<Map.Entry<ItemStack, Integer>> storageItems = allStorageItems.subList(startIndex, endIndex);

            for (int i = 0; i < 36; i++) {
                if (i < storageItems.size()) {
                    Map.Entry<ItemStack, Integer> entry = storageItems.get(i);
                    this.addButton(9 + i, createStorageContentButton(entry.getKey(), entry.getValue()));
                } else {
                    this.addButton(9 + i, createStorageContentButton(new ItemStack(Material.AIR), 0));
                }
            }
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
            AdventureAPI.sendMessage(player, LunaticStorage.getLanguageConfig().getMessage(STORAGE_FULL_MK));
            storageFullTimeout = true;
            Runnable runnable = () -> {
                storageGUIs.get(id).storageFullTimeout = false;
            };

            Utils.scheduleTask(runnable, 1000, TimeUnit.MILLISECONDS);
        }

        reloadGui();

        return newItem;
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

        if (!Utils.isStorageItem(item) && !item.getType().equals(Material.AIR)) {
            Logger.debugLog("insertStorageItem: not storage item");
            return item;
        }

        ItemStack result;
        ItemStack storageItem;

        if (storage.getStorageItem() == null) {
            storageItem = item.clone();
            result = new ItemStack(Material.AIR);
        } else {
            storageItem = storage.getStorageItem().clone();

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
            storage.setStorageItem(storageItem);
//            this.addButton(7, createStorageItemButton());
        } else {
            storage.setStorageItem(null);
//            this.addButton(7, createStoragePaneButton());
        }

        createStorageButton();

        Logger.debugLog("insertStorageItem: storageItem: " + storageItem);

        return result;
    }

    private ItemStack insertRangeItem(ItemStack item, boolean swapItems) {
        return insertRangeItem(item, swapItems, false);
    }

    private ItemStack insertRangeItem(ItemStack item, boolean swapItems, boolean isRightClick) {

        if (!Utils.isRangeItem(item) && !item.getType().equals(Material.AIR)) {
            Logger.debugLog("insertRangeItem: not range item");
            return item;
        }

        ItemStack result;
        ItemStack rangeItem;

        if (storage.getRangeItem() == null) {
            rangeItem = item.clone();
            result = new ItemStack(Material.AIR);
        } else {
            rangeItem = storage.getRangeItem().clone();

            if (rangeItem.isSimilar(item) && item.getType() != Material.AIR) {
                if (rangeItem.getMaxStackSize() > rangeItem.getAmount()) {
                    int itemAmount = item.getAmount();
                    int rangeItemAmount = rangeItem.getAmount();
                    int totalAmount = itemAmount + rangeItemAmount;
                    if (totalAmount <= rangeItem.getMaxStackSize()) {
                        rangeItem.setAmount(totalAmount);
                        result = new ItemStack(Material.AIR);
                    } else {
                        rangeItem.setAmount(rangeItem.getMaxStackSize());
                        int newItemAmount = totalAmount - rangeItem.getMaxStackSize();
                        item.setAmount(newItemAmount);
                        result = item.clone();
                    }
                } else {
                    result = item;
                }
            } else {
                if (swapItems) {
                    result = rangeItem.clone();
                    rangeItem = item.clone();
                } else {
                    result = item.clone();
                }
            }
        }

        if (!rangeItem.getType().isAir()) {
            storage.setRangeItem(rangeItem);
        } else {
            storage.setRangeItem(null);
        }

        createRangeButton();

        return result;
    }

    public static void updateStorageGUIs(Block block) {
        for (StorageGUI storageGUI : storageGUIs.values()) {
            if (storageGUI.block.equals(block)) {
                storageGUI.reloadGui();
            }
        }
    }
}
