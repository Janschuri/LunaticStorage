package de.janschuri.lunaticstorage.gui;

import de.janschuri.lunaticlib.config.LunaticMessageKey;
import de.janschuri.lunaticlib.config.MessageKey;
import de.janschuri.lunaticlib.platform.paper.inventorygui.buttons.InventoryButton;
import de.janschuri.lunaticlib.platform.paper.inventorygui.buttons.PlayerInvButton;
import de.janschuri.lunaticlib.platform.paper.inventorygui.guis.ListGUI;
import de.janschuri.lunaticlib.platform.paper.inventorygui.handler.GUIManager;
import de.janschuri.lunaticlib.platform.paper.inventorygui.interfaces.list.PaginatedList;
import de.janschuri.lunaticlib.platform.paper.inventorygui.interfaces.list.SearchableList;
import de.janschuri.lunaticlib.platform.paper.inventorygui.interfaces.list.SortedList;
import de.janschuri.lunaticlib.platform.paper.utils.ItemStackUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.Storage;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static de.janschuri.lunaticstorage.utils.Utils.isAllowedInteract;

public class StorageGUI
        extends
        ListGUI<Map.Entry<ItemStack, Integer>>
        implements
        PaginatedList<Map.Entry<ItemStack, Integer>>,
        SearchableList<Map.Entry<ItemStack, Integer>>,
        SortedList<Map.Entry<ItemStack, Integer>>
{

    private static final MessageKey STORAGE_FULL_MK = new LunaticMessageKey("storage_full")
            .defaultMessage("en", "The storage is full.")
            .defaultMessage("de", "Der Speicher ist voll.");
    private static final MessageKey AMOUNT_MK = new LunaticMessageKey("amount")
            .defaultMessage("en", "Amount: %amount%")
            .defaultMessage("de", "Menge: %amount%");
    private static final MessageKey PAGE_MK = new LunaticMessageKey("page")
            .defaultMessage("en", "Page %page%/%pages%")
            .defaultMessage("de", "Seite %page%/%pages%");
    private static final MessageKey TOTAL_ITEMS_MK = new LunaticMessageKey("total_items")
            .defaultMessage("en", "Total Items: %amount%")
            .defaultMessage("de", "Gesamtanzahl der Gegenstände: %amount%");
    private static final MessageKey LOADED_CONTAINERS_MK = new LunaticMessageKey("loaded_containers")
            .defaultMessage("en", "Loaded Containers: %amount%")
            .defaultMessage("de", "Geladene Container: %amount%");
    private static final MessageKey TOTAL_CONTAINERS_MK = new LunaticMessageKey("total_containers")
            .defaultMessage("en", "Total Containers: %amount%")
            .defaultMessage("de", "Gesamtanzahl der Container: %amount%");
    private static final MessageKey RANGE_MK = new LunaticMessageKey("range")
            .defaultMessage("en", "Range: %range%")
            .defaultMessage("de", "Reichweite: %range%");
    private static final MessageKey STORAGE_GUI_TITLE_MK = new LunaticMessageKey("storage_gui_title")
            .defaultMessage("en", "Storage GUI")
            .defaultMessage("de", "Lager GUI");
    private static final MessageKey SORT_AFTER_NAME_MK = new LunaticMessageKey("sort_after_name")
            .defaultMessage("en", "Sort after Name")
            .defaultMessage("de", "Sortieren nach Name");
    private static final MessageKey SORT_AFTER_AMOUNT_MK = new LunaticMessageKey("sort_after_amount")
            .defaultMessage("en", "Sort after Amount")
            .defaultMessage("de", "Sortieren nach Menge");
    private static final MessageKey SORT_ASCENDING_MK = new LunaticMessageKey("sort_ascending")
            .defaultMessage("en", "Sort Ascending")
            .defaultMessage("de", "Aufsteigend sortieren");
    private static final MessageKey SORT_DESCENDING_MK = new LunaticMessageKey("sort_descending")
            .defaultMessage("en", "Sort Descending")
            .defaultMessage("de", "Absteigend sortieren");
    private static final MessageKey SEARCH_ITEM_MK = new LunaticMessageKey("search_item")
            .defaultMessage("en", "Search Item...")
            .defaultMessage("de", "Item suchen...");
    private static final MessageKey STORAGE_ITEM_SLOT_MK = new LunaticMessageKey("storage_item_slot")
            .defaultMessage("en", "Storage Item Slot")
            .defaultMessage("de", "Storage-Linker Slot");
    private static final MessageKey RANGE_ITEM_SLOT_MK = new LunaticMessageKey("range_item_slot")
            .defaultMessage("en", "Range Item Slot")
            .defaultMessage("de", "Range-Upgrade Slot");
    private static final MessageKey RESET_SEARCH_MK = new LunaticMessageKey("reset_search")
            .defaultMessage("en", "Reset Search")
            .defaultMessage("de", "Suche zurücksetzen");
    private static final MessageKey CURRENT_SEARCH_MK = new LunaticMessageKey("current_search")
            .defaultMessage("en", "Current Search: %search%")
            .defaultMessage("de", "Aktuelle Suche: %search%");

    private final static Map<Integer, Integer> pageMap = new HashMap<>();
    private final static Map<Integer, String> searchMap = new HashMap<>();
    private final static Map<Integer, Block> blockMap = new HashMap<>();
    private final static Map<Integer, Player> playerMap = new HashMap<>();
    private final static Set<Integer> descendingMap = new HashSet<>();
    private final static Map<Integer, Integer> sorterMap = new HashMap<>();
    private final static Set<Integer> storageFullTimeoutMap = new HashSet<>();

    private static final Map<Block, Map<UUID, Integer>> playerStorageGUI = new HashMap<>();

    private StorageGUI(Player player, Block block) {
        super();
        blockMap.put(getId(), block);
        playerMap.put(getId(), player);

        playerStorageGUI.putIfAbsent(block, new HashMap<>());
        playerStorageGUI.get(block).put(player.getUniqueId(), getId());
    }

    public static StorageGUI getStorageGUI(Player player, Block block) {
        if (playerStorageGUI.containsKey(block)) {
            Map<UUID, Integer> storageGUIs = playerStorageGUI.get(block);
            if (storageGUIs.containsKey(player.getUniqueId())) {
                int id = storageGUIs.get(player.getUniqueId());
                return (StorageGUI) getGUI(id);
            }
        }

        StorageGUI gui = new StorageGUI(player, block);
        return gui;
    }

    @Override
    public String getDefaultTitle() {
        return getString(STORAGE_GUI_TITLE_MK);
    }

    @Override
    public void init(Player player) {
        Storage storage = getStorage();

        createStorageButton(storage.getStorageItem());
        createRangeButton(storage.getRangeItem());
        addButton(createItemButton(storage));
        addButton(createStoragePlayerInvButton());

        if (!getSearch().isEmpty()) {
            addButton(1, resetSearchButton());
        }

        super.init(player);
    }

    public Block getBlock() {
        return blockMap.get(getId());
    }

    public Player getPlayer() {
        return playerMap.get(getId());
    }

    public Storage getStorage() {
        return Storage.getStorage(getBlock());
    }

    @Override
    public void setSorterIndex(int i) {
        sorterMap.put(getId(), i);
    }

    @Override
    public int getSorterIndex() {
        sorterMap.putIfAbsent(getId(), 0);
        return sorterMap.get(getId());
    }

    @Override
    public List<Sorter<Map.Entry<ItemStack, Integer>>> getSorters() {
        return List.of(
            new Sorter<Map.Entry<ItemStack, Integer>>("by_name")
                    .creator(player -> {
                        ItemStack sorterItem = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/bc35e72022e2249c9a13e5ed8a4583717a626026773f5416440d573a938c93");
                        ItemMeta meta = sorterItem.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName(getString(SORT_AFTER_NAME_MK));
                        sorterItem.setItemMeta(meta);
                        return getItemWithGuiId(sorterItem, "sorter-by-name");
                    })
                    .comparator(player -> {
                        String locale = player.getLocale();
                        Comparator<Map.Entry<ItemStack, Integer>> comparator;
                        comparator = Comparator.comparing(entry -> Utils.getMCLanguage(entry.getKey(), locale));
                        return comparator;
                    }),
                new Sorter<Map.Entry<ItemStack, Integer>>("by_amount")
                        .creator(player -> {
                            ItemStack sorterItem = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/5a990d613ba553ddc5501e0436baabc17ce22eb4dc656d01e777519f8c9af23a");
                            ItemMeta meta = sorterItem.getItemMeta();
                            assert meta != null;
                            meta.setDisplayName(getString(SORT_AFTER_AMOUNT_MK));
                            sorterItem.setItemMeta(meta);
                            return getItemWithGuiId(sorterItem, "sorter-by-amount");
                        })
                        .comparator(player -> {
                            Comparator<Map.Entry<ItemStack, Integer>> comparator;
                            comparator = Map.Entry.comparingByValue();
                            return comparator;
                        })
        );
    }

    @Override
    public boolean isDescending() {
        return descendingMap.contains(getId());
    }

    @Override
    public void setDescending(boolean descending) {
        if (descending) {
            descendingMap.add(getId());
        } else {
            descendingMap.remove(getId());
        }
    }

    public boolean isStorageFullTimeout() {
        return storageFullTimeoutMap.contains(getId());
    }

    public void setStorageFullTimeout() {
        storageFullTimeoutMap.add(getId());

        Utils.scheduleTask(() -> {
            storageFullTimeoutMap.remove(getId());
        }, 3000, TimeUnit.MILLISECONDS);
    }

    private void createStorageButton(ItemStack storageItem) {
        if (storageItem != null) {
            this.addButton(8, createStorageItemButton(storageItem));
        } else {
            this.addButton(8, createStoragePaneButton());
        }
    }

    private void createRangeButton(ItemStack rangeItem) {
        if (rangeItem != null) {
            this.addButton(7, createRangeItemButton(rangeItem));
        } else {
            this.addButton(7, createRangePaneButton());
        }
    }

    private InventoryButton createStoragePaneButton() {

        ItemStack item = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(getString(STORAGE_ITEM_SLOT_MK));
        item.setItemMeta(meta);


        return new InventoryButton()
                .creator(player -> item)
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();

                    if (!isAllowedInteract(player)) {
                        return;
                    }

                    ItemStack cursor = event.getCursor();

                    ItemStack newItem = getStorage().insertStorageItem(cursor, false);
                    player.setItemOnCursor(newItem);

                    reloadGui();
                });
    }

    private InventoryButton createRangePaneButton() {

        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(getString(RANGE_ITEM_SLOT_MK));
        item.setItemMeta(meta);

        return new InventoryButton()
                .creator(player -> item)
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();

                    if (!isAllowedInteract(player)) {
                        return;
                    }

                    ItemStack cursor = event.getCursor();

                    ItemStack newItem = getStorage().insertRangeItem(cursor, false);
                    player.setItemOnCursor(newItem);

                    reloadGui();
                });
    }

    private InventoryButton createStorageItemButton(ItemStack item) {
        return new InventoryButton()
                .creator(player -> item)
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();

                    if (!isAllowedInteract(player)) {
                        return;
                    }

                    if (event.isShiftClick() && event.isRightClick()) {
                        Map<UUID, List<String>> containers = Utils.getStorageContainerCoordsMap(item);

                        ContainerListGUI gui = ContainerListGUI.getContainerListGUI(containers, (int) getStorage().getRange(), getBlock().getLocation())
                                .onClose(closeEvent -> {
                                    Player p = (Player) closeEvent.getWhoClicked();
                                    GUIManager.openGUI(StorageGUI.getStorageGUI(p, getBlock()), p);
                                    reloadGui();
                                })
                                .onContainerChange((addEvent, newContainers) -> {

                                        StorageContainer.setChestsToPersistentDataContainer(item, newContainers);

                                        getStorage().setStorageItem(item);

                                });
                        GUIManager.openGUI(gui, player);
                        return;
                    }

                    ItemStack cursor = event.getCursor() == null ? new ItemStack(Material.AIR) : event.getCursor().clone();

                    ItemStack newItem = getStorage().insertStorageItem(cursor, true);

                    player.setItemOnCursor(newItem);

                    reloadGui();
                });
    }

    private InventoryButton createRangeItemButton(ItemStack item) {
        return new InventoryButton()
                .creator(player -> item)
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();

                    if (!isAllowedInteract(player)) {
                        return;
                    }

                    ItemStack cursor = event.getCursor() == null ? new ItemStack(Material.AIR) : event.getCursor().clone();

                    ItemStack newItem = getStorage().insertRangeItem(cursor, true);
                    player.setItemOnCursor(newItem);

                    reloadGui();
                });
    }

    @Override
    public int getPage() {
        pageMap.putIfAbsent(getId(), 0);

        return pageMap.get(getId());
    }

    @Override
    public void setPage(int page) {
        pageMap.put(getId(), page);
    }

    @Override
    public ItemStack currentPageItem(Player player) {
        Storage storage = getStorage();

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(getString(PAGE_MK)
                .replace("%page%", String.valueOf(getPage() + 1))
                .replace("%pages%", String.valueOf(getPageCount() + 1)));
        List<String> lore = new ArrayList<>();

        String totalItems = getString(TOTAL_ITEMS_MK)
                .replace("%amount%", storage.getTotalAmount() + "");
        String range = getString(RANGE_MK)
                .replace("%range%", storage.getRange() + "");
        String loadedContainers = getString(LOADED_CONTAINERS_MK)
                .replace("%amount%", storage.getLoadedContainersAmount() + "");
        String totalContainers = getString(TOTAL_CONTAINERS_MK)
                .replace("%amount%", storage.getContainerAmount() + "");

        lore.add(totalItems);
        lore.add(range);
        lore.add(loadedContainers);
        lore.add(totalContainers);

        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private PlayerInvButton createItemButton(Storage storage) {
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

                    return !Utils.isStorageItem(item) && !Utils.isRangeItem(item);
                })
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();

                    if (!isAllowedInteract(player)) {
                        return;
                    }

                    ItemStack cursorItem = event.getCurrentItem();

                    ItemStack newItem = insertItem(storage, player, cursorItem);
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

                    Player player = (Player) event.getWhoClicked();

                    if (!isAllowedInteract(player)) {
                        return false;
                    }

                    return Utils.isStorageItem(item) || Utils.isRangeItem(item);
                })
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }


                    ItemStack item = event.getCurrentItem();

                    ItemStack newItem = new ItemStack(Material.AIR);

                    if (Utils.isStorageItem(item)) {
                        newItem = getStorage().insertStorageItem(item, false);
                    }

                    if (Utils.isRangeItem(item)) {
                        newItem = getStorage().insertRangeItem(item, false);
                    }

                    event.setCurrentItem(newItem);

                    reloadGui();
                });
    }

    @Override
    public List<Map.Entry<ItemStack, Integer>> getItems() {
        Storage storage = getStorage();
        return storage.getStorageList();
    }

    @Override
    public Predicate<Map.Entry<ItemStack, Integer>> getSearchFilter(Player player) {
        final String locale = player.getLocale();
        final String search = getSearch();
        return entry -> {
            if (search.isEmpty()) {
                return true;
            }

            ItemStack item = entry.getKey();

            String language = Utils.getMCLanguage(item, locale);

            Component displayNameComponent = item.displayName();

            String name = displayNameComponent == null ? "" : PlainTextComponentSerializer.plainText().serialize(displayNameComponent);

            String type = item.getType().name();

            List<Component> lore = entry.getKey().getItemMeta() != null ? entry.getKey().getItemMeta().lore() : null;
            String concanetedLore = "";
            if (lore != null) {
                for (Component line : lore) {
                    if (line instanceof TextComponent textComponent) {
                        concanetedLore = concanetedLore + textComponent.content() + " ";
                    }
                }
            }

            String concanetedEnchantLore = "";

            Set<Enchantment> enchants = entry.getKey().getEnchantments().keySet();

            for (Enchantment enchantment : enchants) {
                String key = enchantment.getKey().getKey();
                Logger.info("Enchantment Key: " + key);
                String enchantName = Utils.getMCLanguageByKey("enchantment.minecraft." + key, locale);

                if (enchantName == null || enchantName.isEmpty()) {
                    enchantName = key;
                }

                concanetedEnchantLore = concanetedEnchantLore + enchantName + " ";
            }


            Logger.info("Search Filter Values:");
            Logger.info("Language: " + language);
            Logger.info("Name: " + name);
            Logger.info("Type: " + type);
            Logger.info("Enchantment Lore: " + concanetedEnchantLore);
            Logger.info("Lore: " + concanetedLore);

            return language.toLowerCase().contains(search.toLowerCase()) ||
                    name.toLowerCase().contains(search.toLowerCase()) ||
                    type.toLowerCase().contains(search.toLowerCase()) ||
                    concanetedEnchantLore.toLowerCase().contains(search.toLowerCase()) ||
                    concanetedLore.toLowerCase().contains(search.toLowerCase());
        };
    }

    @Override
    public String getSearch() {
        searchMap.putIfAbsent(getId(), "");
        return searchMap.get(getId());
    }

    @Override
    public void setSearch(String s) {
        searchMap.put(getId(), s);
    }

    @Override
    public InventoryButton listItemButton(Map.Entry<ItemStack, Integer> entry) {
        ItemStack item = entry.getKey();
        int amount = entry.getValue();

        ItemStack displayItem = item.clone();
        ItemMeta meta = displayItem.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (meta != null) {
            lore = meta.getLore();
        }

        if (lore == null) {
            lore = new ArrayList<>();
        }

        String amountText = getString(AMOUNT_MK).replace("%amount%", String.valueOf(amount));
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

                    if (!isAllowedInteract(player)) {
                        return;
                    }

                    ItemStack cursorItem = event.getCursor() == null ? new ItemStack(Material.AIR) : event.getCursor().clone();

                    if (event.isShiftClick()) {
                        boolean hasEmptySlot = player.getInventory().firstEmpty() != -1;

                        int amountInInventory = 0;

                        for (ItemStack invItem : player.getInventory().getContents()) {
                            if (invItem != null && invItem.isSimilar(item)) {
                                amountInInventory += invItem.getAmount();
                            }
                        }

                        int maxStackSize = item.getMaxStackSize();
                        int spaceInInventory = amountInInventory % maxStackSize;

                        int itemsToGet = spaceInInventory;

                        if (hasEmptySlot) {
                            itemsToGet =  maxStackSize;
                        }

                        ItemStack newItem = getStorage().getItemsFromStorage(item, player, itemsToGet);

                        ItemStack addItem = newItem.clone();
                        addItem.setAmount(Math.min(spaceInInventory, newItem.getAmount()));

                        player.getInventory().addItem(addItem);

                        ItemStack setItem = newItem.clone();
                        setItem.setAmount(newItem.getAmount() - addItem.getAmount());

                        int[] indexOrder = {
                                8, 7, 6, 5, 4, 3, 2, 1, 0,
                                35, 34, 33, 32, 31, 30, 29, 28, 27,
                                26, 25, 24, 23, 22, 21, 20, 19, 18,
                                17, 16, 15, 14, 13, 12, 11, 10, 9
                        };

                        for (int i = 0; i < indexOrder.length; i++) {
                            int slot = indexOrder[i];

                            ItemStack stack = player.getInventory().getItem(slot);
                            if (stack == null || stack.getType().equals(Material.AIR)) {
                                player.getInventory().setItem(slot, setItem);
                                break;
                            }
                        }

                    } else if (cursorItem.getType().equals(Material.AIR)) {
                        ItemStack newItem = getStorage().getItemsFromStorage(item, player);

                        if (!newItem.getType().equals(Material.AIR)) {
                            player.setItemOnCursor(newItem);
                        }
                    } else {
                        Storage storage = getStorage();

                        ItemStack newItem = insertItem(storage, player, cursorItem);
                        player.setItemOnCursor(newItem);
                    }

                    reloadGui();
                });
    }

    private ItemStack insertItem(Storage storage, Player player, ItemStack item) {
        if (isStorageFullTimeout()) {
            return item;
        }

        ItemStack newItem = storage.insertItemsIntoStorage(item, player);

        if (newItem.getAmount() > 0 && !newItem.getType().equals(Material.AIR)) {
            player.sendMessage(LunaticStorage.getLanguageConfig().getMessage(STORAGE_FULL_MK));
            setStorageFullTimeout();
        }

        return newItem;
    }

    public static void updateStorageGUIs(Block block) {
        if (playerStorageGUI.containsKey(block)) {
            for (int id : playerStorageGUI.get(block).values()) {
                getGUI(id).reloadGui();
            }
        }
    }

    private String getString(MessageKey messageKey) {
        return LunaticStorage.getLanguageConfig().getMessageAsLegacyString(messageKey.noPrefix());
    }

    @Override
    public InventoryButton emptyListItemButton(int slot) {
        return new InventoryButton()
                .creator(player -> new ItemStack(Material.AIR))
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();

                    if (!isAllowedInteract(player)) {
                        return;
                    }

                    ItemStack cursorItem = event.getCursor() == null ? new ItemStack(Material.AIR) : event.getCursor().clone();

                    if (!cursorItem.getType().equals(Material.AIR)) {
                        Storage storage = getStorage();

                        ItemStack newItem = insertItem(storage, player, cursorItem);
                        player.setItemOnCursor(newItem);
                    }

                    reloadGui();
                });
    }

    @Override
    public ItemStack getSearchItem(Player player) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(getString(SEARCH_ITEM_MK));

        List<String> lore = new ArrayList<>();

        if (!getSearch().isEmpty()) {
            String currentSearch = getString(CURRENT_SEARCH_MK)
                    .replace("%search%", getSearch());
            lore.add("");
            lore.add(currentSearch);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private InventoryButton resetSearchButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(getString(RESET_SEARCH_MK));

        List<String> lore = new ArrayList<>();

        if (!getSearch().isEmpty()) {
            String currentSearch = getString(CURRENT_SEARCH_MK)
                    .replace("%search%", getSearch());
            lore.add("");
            lore.add(currentSearch);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return new InventoryButton()
                .creator(player -> item)
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    setSearch("");

                    reloadGui();
                });
    }

    @Override
    public ItemStack getAscendingIcon() {
        ItemStack arrow = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/b221da4418bd3bfb42eb64d2ab429c61decb8f4bf7d4cfb77a162be3dcb0b927");
        ItemMeta meta = arrow.getItemMeta();
        assert meta != null;
        meta.setDisplayName(getString(SORT_ASCENDING_MK));
        arrow.setItemMeta(meta);

        return getItemWithGuiId(arrow, "ascending");
    }

    @Override
    public ItemStack getDescendingIcon() {
        ItemStack arrow = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/a3852bf616f31ed67c37de4b0baa2c5f8d8fca82e72dbcafcba66956a81c4");
        ItemMeta meta = arrow.getItemMeta();
        assert meta != null;
        meta.setDisplayName(getString(SORT_DESCENDING_MK));
        arrow.setItemMeta(meta);

        return getItemWithGuiId(arrow, "descending");
    }

}
