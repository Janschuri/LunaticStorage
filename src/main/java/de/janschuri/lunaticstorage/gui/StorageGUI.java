package de.janschuri.lunaticstorage.gui;

import de.janschuri.lunaticlib.platform.bukkit.inventorygui.*;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.Storage;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import de.janschuri.lunaticlib.MessageKey;
import de.janschuri.lunaticlib.platform.bukkit.external.AdventureAPI;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class StorageGUI extends ListGUI<Map.Entry<ItemStack, Integer>> {

    private static final MessageKey STORAGE_FULL_MK = new MessageKey("storage_full");
    private static final MessageKey AMOUNT_MK = new MessageKey("amount");
    private static final MessageKey PAGE_MK = new MessageKey("page");
    private static final MessageKey TOTAL_ITEMS_MK = new MessageKey("total_items");
    private static final MessageKey LOADED_CONTAINERS_MK = new MessageKey("loaded_containers");
    private static final MessageKey TOTAL_CONTAINERS_MK = new MessageKey("total_containers");
    private static final MessageKey RANGE_MK = new MessageKey("range");
    private static final MessageKey GUI_TITLE_MK = new MessageKey("gui_title");

    private final static Map<Block, Map<Player, StorageGUI>> storageGUIs = new HashMap<>();
    private final static Map<Integer, Block> blockMap = new HashMap<>();
    private final static Map<Integer, Player> playerMap = new HashMap<>();
    private final static Set<Integer> descendingMap = new HashSet<>();
    private final static Map<Integer, Integer> sorterMap = new HashMap<>();
    private final static Map<Integer, String> searchMap = new HashMap<>();
    private final static Set<Integer> storageFullTimeoutMap = new HashSet<>();

    public StorageGUI(Player player, Block block) {
        super(getId(player, block));
        blockMap.put(getId(), block);
        playerMap.put(getId(), player);
        storageGUIs.putIfAbsent(block, new HashMap<>());
        storageGUIs.get(block).put(player, this);
    }

    private static int getId(Player player, Block block) {
        return storageGUIs.get(block).get(player).getId();
    }

    @Override
    public String getTitle() {
        return LunaticStorage.getLanguageConfig().getMessageAsString(GUI_TITLE_MK);
    }

    @Override
    public void decorate(Player player) {
        Storage storage = getStorage();

        createStorageButton(storage.getStorageItem());
        createRangeButton(storage.getRangeItem());
        addButton(createItemButton(storage));
        addButton(createStoragePlayerInvButton());
        addButton(0, createSearchButton());
        addButton(4, createSorterButton());
        addButton(5, createDescendingButton());

        super.decorate(player);
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

    public boolean isDescending() {
        return descendingMap.contains(getId());
    }

    public void setDescending(boolean descending) {
        if (descending) {
            descendingMap.add(getId());
        } else {
            descendingMap.remove(getId());
        }
    }

    public void toggleDescending() {
        if (isDescending()) {
            descendingMap.remove(getId());
        } else {
            descendingMap.add(getId());
        }
    }

    public int getSorter() {
        sorterMap.putIfAbsent(getId(), 0);

        return sorterMap.get(getId());
    }

    public void setSorter(int sorter) {
        sorterMap.put(getId(), sorter);
    }

    public String getSearch() {
        searchMap.putIfAbsent(getId(), "");

        return searchMap.get(getId());
    }

    public void setSearch(String search) {
        searchMap.put(getId(), search);
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

                    ItemStack newItem = insertStorageItem(cursor, false);
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

                    ItemStack newItem = insertRangeItem(cursor, false);
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

                    ItemStack newItem = insertStorageItem(cursor, true, event.isRightClick());

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

                    ItemStack newItem = insertRangeItem(cursor, true, event.isRightClick());
                    player.setItemOnCursor(newItem);

                    reloadGui(player);
                });
    }

    @Override
    protected ItemStack currentPageItem(Player player) {
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

    private InventoryButton createDescendingButton() {
        return new InventoryButton()
                .creator(player -> {
                    if (isDescending()) {
                        ItemStack arrow = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/a3852bf616f31ed67c37de4b0baa2c5f8d8fca82e72dbcafcba66956a81c4");
                        ItemMeta meta = arrow.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName("Descended");
                        arrow.setItemMeta(meta);
                        return arrow;
                    } else {
                        ItemStack arrow = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/b221da4418bd3bfb42eb64d2ab429c61decb8f4bf7d4cfb77a162be3dcb0b927");
                        ItemMeta meta = arrow.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName("Ascended");
                        arrow.setItemMeta(meta);
                        return arrow;
                    }
                })
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();

                    toggleDescending();

                    reloadGui(player);
                });
    }

    private InventoryButton createSorterButton() {
        return new InventoryButton()
                .creator(player -> {
                    int sorter = getSorter();

                    if (sorter == 0) {
                        ItemStack sorterItem = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/bc35e72022e2249c9a13e5ed8a4583717a626026773f5416440d573a938c93");
                        ItemMeta meta = sorterItem.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName("by name");
                        sorterItem.setItemMeta(meta);
                        return sorterItem;
                    } else {
                        ItemStack sorterItem = ItemStackUtils.getSkullFromURL("https://textures.minecraft.net/texture/5a990d613ba553ddc5501e0436baabc17ce22eb4dc656d01e777519f8c9af23a");
                        ItemMeta meta = sorterItem.getItemMeta();
                        assert meta != null;
                        meta.setDisplayName("by amount");
                        sorterItem.setItemMeta(meta);
                        return sorterItem;
                    }
                })
                .consumer(event -> {
                    if (processingClickEvent()) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();

                    setSorter((getSorter() + 1) % 2);
                    reloadGui(player);
                });
    }

    private InventoryButton createSearchButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack(Material.COMPASS);
                    ItemMeta meta = item.getItemMeta();
                    assert meta != null;
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
                                                        setSearch(search.toString());
                                                        reloadGui(player);
                                                    });
                                                })
                                );
                            })
                            .build();

                    gui.open(player);
                });


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

                    ItemStack newItem = insertStorageItem(item, false);
                    event.setCurrentItem(newItem);

                    reloadGui(player);
                });
    }

    @Override
    protected List<Map.Entry<ItemStack, Integer>> getItems() {
        Storage storage = getStorage();
        String locale = getPlayer().getLocale();
        return storage.getStorageList(locale, getSorter(), isDescending(), getSearch());
    }

    @Override
    protected InventoryButton listItemButton(Map.Entry<ItemStack, Integer> entry) {
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

                        reloadGui(player);
                    } else {
                        Storage storage = getStorage();

                        ItemStack newItem = insertItem(storage, player, cursorItem);
                        player.setItemOnCursor(newItem);
                    }
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

        reloadGui();

        return newItem;
    }

    private ItemStack insertStorageItem(ItemStack item, boolean swapItems) {
        return insertStorageItem(item, swapItems, false);
    }

    private ItemStack insertStorageItem(ItemStack item, boolean swapItems, boolean isRightClick) {

        Storage storage = getStorage();

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
        } else {
            storage.setStorageItem(null);
        }

        createStorageButton(storageItem);

        Logger.debugLog("insertStorageItem: storageItem: " + storageItem);

        return result;
    }

    private ItemStack insertRangeItem(ItemStack item, boolean swapItems) {
        return insertRangeItem(item, swapItems, false);
    }

    private ItemStack insertRangeItem(ItemStack item, boolean swapItems, boolean isRightClick) {

        Storage storage = getStorage();

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

        createRangeButton(rangeItem);

        return result;
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
