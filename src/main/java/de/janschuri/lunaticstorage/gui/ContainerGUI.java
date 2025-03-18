package de.janschuri.lunaticstorage.gui;

import de.janschuri.lunaticlib.MessageKey;
import de.janschuri.lunaticlib.common.config.LunaticMessageKey;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.GUIManager;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.InventoryButton;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.InventoryGUI;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.InventoryHandler;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.list.ListGUI;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.list.PaginatedList;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.config.LanguageConfig;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ContainerGUI extends ListGUI<Map.Entry<ItemStack, Boolean>> implements PaginatedList<Map.Entry<ItemStack, Boolean>> {

    private final StorageContainer container;
    private final static Map<Integer, Boolean> isWhitelist = new HashMap<>();
    private final static Map<Integer, Integer> whitelistPages = new HashMap<>();
    private final static Map<Integer, Integer> blacklistPages = new HashMap<>();

    public static final MessageKey PAGE_MK = new LunaticMessageKey("page")
            .defaultMessage("en", "Page: %page%/%pages%")
            .defaultMessage("de", "Seite: %page%/%pages%");
    public static final MessageKey WHITELIST_MODE_MK = new LunaticMessageKey("whitelist_mode")
            .defaultMessage("en", "&aWhitelist Mode")
            .defaultMessage("de", "&aWhitelist-Modus");
    public static final MessageKey BLACKLIST_MODE_MK = new LunaticMessageKey("blacklist_mode")
            .defaultMessage("en", "&cBlacklist Mode")
            .defaultMessage("de", "&cBlacklist-Modus");
    public static final MessageKey MATCH_NBT_MK = new LunaticMessageKey("match_nbt")
            .defaultMessage("en", "&eMatch NBT")
            .defaultMessage("de", "&eNBT abgleichen");
    public static final MessageKey YES_MK = new LunaticMessageKey("yes")
            .defaultMessage("en", "&aYes")
            .defaultMessage("de", "&aJa");
    public static final MessageKey NO_MK = new LunaticMessageKey("no")
            .defaultMessage("en", "&cNo")
            .defaultMessage("de", "&cNein");
    public static final MessageKey ADD_CONTAINERS_INV_TO_WHITELIST_MK = new LunaticMessageKey("add_containers_inv_to_whitelist")
            .defaultMessage("en", "&aAdd containers inventory to whitelist")
            .defaultMessage("de", "&aInventar des Containers zur Whitelist hinzufügen");
    public static final MessageKey CLEAR_WHITELIST_MK = new LunaticMessageKey("clear_whitelist")
            .defaultMessage("en", "&aClear whitelist")
            .defaultMessage("de", "&aWhitelist leeren");
    public static final MessageKey CLEAR_BLACKLIST_MK = new LunaticMessageKey("clear_blacklist")
            .defaultMessage("en", "&cClear blacklist")
            .defaultMessage("de", "&cBlacklist leeren");
    public static final MessageKey TOGGLE_WHITELIST_MK = new LunaticMessageKey("toggle_whitelist")
            .defaultMessage("en", "&aToggle whitelist")
            .defaultMessage("de", "&aWhitelist umschalten");
    public static final MessageKey TOGGLE_BLACKLIST_MK = new LunaticMessageKey("toggle_blacklist")
            .defaultMessage("en", "&cToggle blacklist")
            .defaultMessage("de", "&cBlacklist umschalten");
    public static final MessageKey ADD_ITEM_TO_WHITELIST_MK = new LunaticMessageKey("add_item_to_whitelist")
            .defaultMessage("en", "&aAdd item to whitelist")
            .defaultMessage("de", "&aItem zur Whitelist hinzufügen");
    public static final MessageKey ADD_ITEM_TO_BLACKLIST_MK = new LunaticMessageKey("add_item_to_blacklist")
            .defaultMessage("en", "&cAdd item to blacklist")
            .defaultMessage("de", "&cItem zur Blacklist hinzufügen");
    public static final MessageKey WHITELIST_ENABLED_MK = new LunaticMessageKey("whitelist_enabled")
            .defaultMessage("en", "&aWhitelist is enabled")
            .defaultMessage("de", "&aWhitelist ist aktiviert");
    public static final MessageKey WHITELIST_DISABLED_MK = new LunaticMessageKey("whitelist_disabled")
            .defaultMessage("en", "&cWhitelist is disabled")
            .defaultMessage("de", "&cWhitelist ist deaktiviert");
    public static final MessageKey BLACKLIST_ENABLED_MK = new LunaticMessageKey("blacklist_enabled")
            .defaultMessage("en", "&aBlacklist is enabled")
            .defaultMessage("de", "&aBlacklist ist aktiviert");
    public static final MessageKey BLACKLIST_DISABLED_MK = new LunaticMessageKey("blacklist_disabled")
            .defaultMessage("en", "&cBlacklist is disabled")
            .defaultMessage("de", "&cBlacklist ist deaktiviert");
    public static final MessageKey CONTAINER_GUI_TITLE_MK = new LunaticMessageKey("container_gui_title")
            .defaultMessage("en", "&6Container GUI")
            .defaultMessage("de", "&6Container GUI");

    private static final Map<Block, Map<UUID, Integer>> playerContainerGUIs = new HashMap<>();

    private ContainerGUI(Player player, StorageContainer storageContainer) {
        super();
        container = storageContainer;

        isWhitelist.putIfAbsent(getId(), true);
        whitelistPages.putIfAbsent(getId(), 0);
        blacklistPages.putIfAbsent(getId(), 0);
        playerContainerGUIs.putIfAbsent(storageContainer.getBlock(), new HashMap<>());

        playerContainerGUIs.get(storageContainer.getBlock()).put(player.getUniqueId(), getId());
    }

    public static ContainerGUI getContainerGUI(Player player, StorageContainer container) {
        if (playerContainerGUIs.containsKey(container.getBlock())) {
            Map<UUID, Integer> containerGUIs = playerContainerGUIs.get(container.getBlock());
            if (containerGUIs.containsKey(player.getUniqueId())) {
                int id = containerGUIs.get(player.getUniqueId());
                return (ContainerGUI) getGUI(id);
            }
        }

        ContainerGUI gui = new ContainerGUI(player, container);
        return gui;
    }

    public StorageContainer getStorageContainer() {
        return container;
    }

    public void setWhitelist(boolean whitelist) {
        isWhitelist.put(getId(), whitelist);
    }

    public boolean isWhitelist() {
        return isWhitelist.get(getId());
    }

    public void setWhitelistPage(int page) {
        whitelistPages.put(getId(), page);
    }

    public int getWhitelistPage() {
        return whitelistPages.get(getId());
    }

    private void setBlacklistPage(int page) {
        blacklistPages.put(getId(), page);
    }

    private int getBlacklistPage() {
        return blacklistPages.get(getId());
    }

    @Override
    public void init(Player player) {
        addButton(0, toggleModeButton());
        addButton(2, createAddItemToListButton());

        if (isWhitelist()) {
            addButton(1, createToggleWhitelistButton());
            addButton(7, createClearWhitelistButton());

            addButton(49, createWhitelistPageButton());

            addButton(8, createAddContainerInvToWhitelistButton());
        } else {
            addButton(1, createToggleBlacklistButton());
            addButton(7, createClearBlacklistButton());

            addButton(49, createBlacklistPageButton());

            addButton(8, createGrayPaneButton());
        }

        super.init(player);
    }

    @Override
    public List getItems() {
        return Arrays.asList((isWhitelist() ? getStorageContainer().getWhitelist() : getStorageContainer().getBlacklist()).entrySet().toArray());
    }

    private InventoryButton toggleModeButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack((isWhitelist() ? Material.WHITE_WOOL : Material.BLACK_WOOL));
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName((isWhitelist() ? getString(WHITELIST_MODE_MK) : getString(BLACKLIST_MODE_MK)));

                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    setWhitelist(!isWhitelist());
                    reloadGui();
                });
    }

    private InventoryButton createGrayPaneButton() {
        return new InventoryButton()
                .creator(player -> new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
    }

    @Override
    public InventoryButton listItemButton(Map.Entry<ItemStack, Boolean> itemStackBooleanEntry) {
        ItemStack item = itemStackBooleanEntry.getKey();
        boolean matchNBT = itemStackBooleanEntry.getValue();
        boolean whitelist = isWhitelist();


        ItemStack itemClone = item.clone();
        ItemMeta meta = itemClone.getItemMeta();
        List<String> lore = meta.getLore();
        String matchNBTString = getString(MATCH_NBT_MK) + ": " + (matchNBT ? getString(YES_MK) : getString(NO_MK));

        if (lore != null) {
            lore.add(matchNBTString);
        } else {
            lore = List.of(matchNBTString);
        }

        meta.setLore(lore);
        itemClone.setItemMeta(meta);
        itemClone.setAmount(1);

        return new InventoryButton()
                .creator(player -> itemClone)
                .consumer(event -> {

                    if (whitelist) {
                        getStorageContainer().toggleWhitelistNBT(item);
                    } else {
                        getStorageContainer().toggleblacklistNBT(item);
                    }

                    reloadGui();
                });
    }

    private InventoryButton createAddContainerInvToWhitelistButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(getString(ADD_CONTAINERS_INV_TO_WHITELIST_MK));
                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    getStorageContainer().addInvToWhitelist();
                    reloadGui();
                });
    }

    private InventoryButton createClearWhitelistButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack(Material.BARRIER);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(getString(CLEAR_WHITELIST_MK));
                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    getStorageContainer().clearWhitelist();
                    reloadGui();
                });
    }

    private InventoryButton createClearBlacklistButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack(Material.BARRIER);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(getString(CLEAR_BLACKLIST_MK));
                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    getStorageContainer().clearBlacklist();
                    reloadGui();
                });
    }

    private InventoryButton createToggleWhitelistButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack((getStorageContainer().isWhitelistEnabled() ? Material.LIME_WOOL : Material.RED_WOOL));
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(getString(TOGGLE_WHITELIST_MK));

                    List<String> lore = (getStorageContainer().isWhitelistEnabled() ? List.of(getString(WHITELIST_ENABLED_MK)) : List.of(getString(WHITELIST_DISABLED_MK)));
                    meta.setLore(lore);

                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    getStorageContainer().toggleWhitelist();
                    reloadGui();
                });
    }

    private InventoryButton createToggleBlacklistButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack((getStorageContainer().isBlacklistEnabled() ? Material.LIME_WOOL : Material.RED_WOOL));
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(getString(TOGGLE_BLACKLIST_MK));

                    List<String> lore = (getStorageContainer().isBlacklistEnabled() ? List.of(getString(BLACKLIST_ENABLED_MK)) : List.of(getString(BLACKLIST_DISABLED_MK)));
                    meta.setLore(lore);

                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    getStorageContainer().toggleBlacklist();
                    reloadGui();
                });
    }

    private InventoryButton createWhitelistPageButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack(Material.PAPER);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(getString(PAGE_MK)
                            .replace("%page%", String.valueOf(getWhitelistPage() + 1))
                            .replace("%pages%", String.valueOf(getStorageContainer().getWhiteListPages() + 1))
                    );
                    item.setItemMeta(meta);
                    return item;
                });
    }

    private InventoryButton createBlacklistPageButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack(Material.PAPER);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(getString(PAGE_MK)
                            .replace("%page%", String.valueOf(getBlacklistPage() + 1))
                            .replace("%pages%", String.valueOf(getStorageContainer().getBlackListPages() + 1))
                    );
                    item.setItemMeta(meta);
                    return item;
                });
    }

    private InventoryButton createAddItemToListButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName((
                            isWhitelist() ?
                                    getString(ADD_ITEM_TO_WHITELIST_MK)
                                    :
                                    getString(ADD_ITEM_TO_BLACKLIST_MK)
                    ));
                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) {
                        return;
                    }

                    ItemStack item = event.getCursor();

                    if (isWhitelist()) {
                        getStorageContainer().addToWhitelist(item, true);
                    } else {
                        getStorageContainer().addToBlacklist(item, true);
                    }

                    reloadGui();
                });
    }

    private String getString(MessageKey messageKey) {
        return LunaticStorage.getLanguageConfig().getMessageAsLegacyString(messageKey.noPrefix());
    }

    @Override
    public int getPage() {
        return isWhitelist() ? getWhitelistPage() : getBlacklistPage();
    }

    @Override
    public void setPage(int i) {
        if (isWhitelist()) {
            setWhitelistPage(i);
        } else {
            setBlacklistPage(i);
        }
    }

    @Override
    public String getDefaultTitle() {
        return getString(CONTAINER_GUI_TITLE_MK);
    }
}
