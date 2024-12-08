package de.janschuri.lunaticstorage.gui;

import de.janschuri.lunaticlib.platform.bukkit.inventorygui.InventoryButton;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.PlayerInvButton;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.list.ListGUI;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.list.PaginatedList;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.list.SearchableList;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.list.SortedList;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.Storage;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import de.janschuri.lunaticlib.MessageKey;
import de.janschuri.lunaticlib.platform.bukkit.external.AdventureAPI;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class StorageGUI
        extends
        ListGUI<Map.Entry<ItemStack, Integer>>
        implements
        PaginatedList<Map.Entry<ItemStack, Integer>>,
        SearchableList<Map.Entry<ItemStack, Integer>>,
        SortedList<Map.Entry<ItemStack, Integer>>
{

    private static final MessageKey STORAGE_FULL_MK = new MessageKey("storage_full");
    private static final MessageKey AMOUNT_MK = new MessageKey("amount");
    private static final MessageKey PAGE_MK = new MessageKey("page");
    private static final MessageKey TOTAL_ITEMS_MK = new MessageKey("total_items");
    private static final MessageKey LOADED_CONTAINERS_MK = new MessageKey("loaded_containers");
    private static final MessageKey TOTAL_CONTAINERS_MK = new MessageKey("total_containers");
    private static final MessageKey RANGE_MK = new MessageKey("range");
    private static final MessageKey GUI_TITLE_MK = new MessageKey("gui_title");

    private final static Map<Integer, Integer> pageMap = new HashMap<>();
    private final static Map<Integer, String> searchMap = new HashMap<>();

    private final static Map<Block, Map<Player, StorageGUI>> storageGUIs = new HashMap<>();
    private final static Map<Integer, Block> blockMap = new HashMap<>();
    private final static Map<Integer, Player> playerMap = new HashMap<>();
    private final static Set<Integer> descendingMap = new HashSet<>();
    private final static Map<Integer, Integer> sorterMap = new HashMap<>();
    private final static Set<Integer> storageFullTimeoutMap = new HashSet<>();

    public StorageGUI(Player player, Block block) {
        super(getId(player, block));
        blockMap.put(getId(), block);
        playerMap.put(getId(), player);
        storageGUIs.putIfAbsent(block, new HashMap<>());
        storageGUIs.get(block).put(player, this);
    }

    private static int getId(Player player, Block block) {
        if (storageGUIs.containsKey(block) && storageGUIs.get(block).containsKey(player)) {
            return storageGUIs.get(block).get(player).getId();
        }
        return -1;
    }

    @Override
    public String getTitle() {
        return LunaticStorage.getLanguageConfig().getMessageAsString(GUI_TITLE_MK);
    }

    @Override
    public void init(Player player) {
        Storage storage = getStorage();

        createStorageButton(storage.getStorageItem());
        createRangeButton(storage.getRangeItem());
        addButton(createItemButton(storage));
        addButton(createStoragePlayerInvButton());

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
                        meta.setDisplayName("by name");
                        sorterItem.setItemMeta(meta);
                        return sorterItem;
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
                            meta.setDisplayName("by amount");
                            sorterItem.setItemMeta(meta);
                            return sorterItem;
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
        return new InventoryButton()
                .creator(player -> new ItemStack(Material.CYAN_STAINED_GLASS_PANE))
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();
                    ItemStack cursor = event.getCursor();

                    ItemStack newItem = getStorage().insertStorageItem(cursor, false);
                    player.setItemOnCursor(newItem);

                    reloadGui(player);
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

                    ItemStack newItem = getStorage().insertRangeItem(cursor, false);
                    player.setItemOnCursor(newItem);

                    reloadGui(player);
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
                    ItemStack cursor = event.getCursor() == null ? new ItemStack(Material.AIR) : event.getCursor().clone();

                    Logger.debugLog("Cursor: " + cursor);

                    ItemStack newItem = getStorage().insertStorageItem(cursor, true);

                    Logger.debugLog("New Item: " + newItem);
                    player.setItemOnCursor(newItem);

                    reloadGui(player);
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
                    ItemStack cursor = event.getCursor() == null ? new ItemStack(Material.AIR) : event.getCursor().clone();

                    ItemStack newItem = getStorage().insertRangeItem(cursor, true);
                    player.setItemOnCursor(newItem);

                    reloadGui(player);
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
        meta.setDisplayName(LunaticStorage.getLanguageConfig().getMessageAsLegacyString(PAGE_MK, false)
                .replace("%page%", String.valueOf(getPage() + 1))
                .replace("%pages%", String.valueOf(getPageCount() + 1)));
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

                    return !Utils.isStorageItem(item);
                })
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();
                    ItemStack cursorItem = event.getCurrentItem();

                    ItemStack newItem = insertItem(storage, player, cursorItem);
                    event.setCurrentItem(newItem);

                    reloadGui(player);
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

                    Player player = (Player) event.getWhoClicked();

                    ItemStack item = event.getCurrentItem();

                    ItemStack newItem = getStorage().insertStorageItem(item, false);
                    event.setCurrentItem(newItem);

                    reloadGui(player);
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
            String language = Utils.getMCLanguage(entry.getKey(), locale);
            return language.toLowerCase().contains(search.toLowerCase());
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
                    ItemStack cursorItem = event.getCursor() == null ? new ItemStack(Material.AIR) : event.getCursor().clone();

                    if (cursorItem.getType().equals(Material.AIR)) {
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
            Logger.debugLog("insertItem: " + newItem);
            AdventureAPI.sendMessage(player, LunaticStorage.getLanguageConfig().getMessage(STORAGE_FULL_MK));
            setStorageFullTimeout();
        }

        return newItem;
    }

    public void reloadGui() {
        reloadGui(getPlayer());
    }

    public static void updateStorageGUIs(Block block) {
        if (storageGUIs.containsKey(block)) {
            for (StorageGUI storageGUI : storageGUIs.get(block).values()) {
                storageGUI.reloadGui();
            }
        }
    }
}
