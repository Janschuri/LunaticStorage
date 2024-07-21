package de.janschuri.lunaticstorage.gui;

import de.janschuri.lunaticlib.MessageKey;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.GUIManager;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.InventoryButton;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.InventoryGUI;
import de.janschuri.lunaticlib.platform.bukkit.util.ItemStackUtils;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ContainerGUI extends InventoryGUI {

    private static final MessageKey PAGE_MK = new MessageKey("page");

    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private static final Map<Integer, StorageContainer> containers = new HashMap<>();
    private static final Map<Integer, Player> players = new HashMap<>();
    private static final Map<Integer, Boolean> isWhitelist = new HashMap<>();
    private static final Map<Integer, Integer> whitelistPages = new HashMap<>();
    private static final Map<Integer, Integer> blacklistPages = new HashMap<>();

    private static final Map<UUID, Map<Block, Integer>> playerContainerGUIs = new HashMap<>();

    private final int id;

    private ContainerGUI(Player player, StorageContainer storageContainer) {
        super(createInventory());
        playerContainerGUIs.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        Map<Block, Integer> containerGUIs = playerContainerGUIs.get(player.getUniqueId());

        if (containerGUIs.containsKey(storageContainer.getBlock())) {
            id = containerGUIs.get(storageContainer.getBlock());
            return;
        }

        id = idCounter.getAndIncrement();
        containerGUIs.put(storageContainer.getBlock(), id);
        playerContainerGUIs.put(player.getUniqueId(), containerGUIs);

        setWhitelistPage(0);
        setBlacklistPage(0);
        setWhitelist(true);
        setStorageContainer(storageContainer);
        setPlayer(player);
    }

    public static ContainerGUI getContainerGUI(Player player, StorageContainer storageContainer) {
        return new ContainerGUI(player, storageContainer);
    }

    private ContainerGUI(ContainerGUI containerGUI) {
        super(containerGUI.getInventory());
        this.id = containerGUI.id;
        decorate(getPlayer());
    }

    public StorageContainer getStorageContainer() {
        return containers.get(id);
    }

    public void setStorageContainer(StorageContainer storageContainer) {
        containers.put(id, storageContainer);
    }

    public void setPlayer(Player player) {
        players.put(id, player);
    }

    public Player getPlayer() {
        return players.get(id);
    }

    public void setWhitelist(boolean whitelist) {
        isWhitelist.put(id, whitelist);
    }

    public boolean isWhitelist() {
        return isWhitelist.get(id);
    }

    public void setWhitelistPage(int page) {
        whitelistPages.put(id, page);
    }

    public int getWhitelistPage() {
        return whitelistPages.get(id);
    }

    private void setBlacklistPage(int page) {
        blacklistPages.put(id, page);
    }

    private int getBlacklistPage() {
        return blacklistPages.get(id);
    }

    private static Inventory createInventory() {
        Inventory inv = Bukkit.createInventory(null, 54, "Storage Container GUI");

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            inv.setItem(i + 45, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }

        return inv;
    }

    @Override
    public void decorate(Player player) {
        addButton(0, toggleModeButton());
        addButton(2, createAddItemToListButton());

        if (isWhitelist()) {
            addButton(1, createToggleWhitelistButton());
            addButton(7, createClearWhitelistButton());
            createListButtons(getStorageContainer().getWhitelist());

            addButton(49, createWhitelistPageButton());

            addButton(8, createAddContainerInvToWhitelistButton());

            if (getWhitelistPage() == getStorageContainer().getWhiteListPages()) {
                addButton(50, createGrayPaneButton());
            } else {
                addButton(50, createArrowRight());
            }

            if (getWhitelistPage() == 0) {
                addButton(48, createGrayPaneButton());
            } else {
                addButton(48, createArrowLeft());
            }
        } else {
            addButton(1, createToggleBlacklistButton());
            addButton(7, createClearBlacklistButton());
            createListButtons(getStorageContainer().getBlacklist());

            addButton(49, createBlacklistPageButton());

            addButton(8, createGrayPaneButton());

            if (getBlacklistPage() == getStorageContainer().getBlackListPages()) {
                addButton(50, createGrayPaneButton());
            } else {
                addButton(50, createArrowRight());
            }

            if (getBlacklistPage() == 0) {
                addButton(48, createGrayPaneButton());
            } else {
                addButton(48, createArrowLeft());
            }
        }



        super.decorate(player);
    }

    private InventoryButton toggleModeButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack((isWhitelist() ? Material.WHITE_WOOL : Material.BLACK_WOOL));
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName((isWhitelist() ? "§aWhitelist Mode" : "§cBlacklist Mode"));

                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    setWhitelist(!isWhitelist());
                    reloadGui();
                });
    }

    private void createListButtons(Map<ItemStack, Boolean> items) {
        int page = isWhitelist() ? getWhitelistPage() : getBlacklistPage();

        int pageSize = 36;

        int startIndex = page * pageSize;
        int endIndex = startIndex + pageSize;

        if (endIndex > items.size()) {
            endIndex = items.size();
        }

        Map<ItemStack, Boolean> subMap = Utils.getSubMap(items, startIndex, endIndex);

        for (int i = 0; i < 36; i++) {
            if (i < subMap.size()) {
                ItemStack item = (ItemStack) subMap.keySet().toArray()[i];
                boolean matchNBT = subMap.get(item);

                InventoryButton button = createListButton(isWhitelist(), item, matchNBT);
                addButton(i+9, button);
            } else {
                addButton(i+9, createAirButton());
            }
        }
    }

    private InventoryButton createAirButton() {
        return new InventoryButton()
                .creator(player -> new ItemStack(Material.AIR));
    }

    private InventoryButton createGrayPaneButton() {
        return new InventoryButton()
                .creator(player -> new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
    }

    private InventoryButton createListButton(boolean whitelist, ItemStack item, boolean matchNBT) {
        ItemStack itemClone = item.clone();
        ItemMeta meta = itemClone.getItemMeta();
        List<String> lore = meta.getLore();
        String matchNBTString = "§7Match NBT: " + (matchNBT ? "§aYes" : "§cNo");

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
                    meta.setDisplayName("§aAdd Containers Inventory to Whitelist");
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
                    meta.setDisplayName("§cClear Whitelist");
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
                    meta.setDisplayName("§cClear Blacklist");
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
                    meta.setDisplayName("§fToggle Whitelist");

                    List<String> lore = (getStorageContainer().isWhitelistEnabled() ? List.of("§aWhitelist is enabled") : List.of("§cWhitelist is disabled"));
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
                    meta.setDisplayName("§fToggle Blacklist");

                    List<String> lore = (getStorageContainer().isBlacklistEnabled() ? List.of("§aBlacklist is enabled") : List.of("§cBlacklist is disabled"));
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
                    meta.setDisplayName(LunaticStorage.getLanguageConfig().getMessageAsLegacyString(PAGE_MK, false)
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
                    meta.setDisplayName(LunaticStorage.getLanguageConfig().getMessageAsLegacyString(PAGE_MK, false)
                            .replace("%page%", String.valueOf(getBlacklistPage() + 1))
                            .replace("%pages%", String.valueOf(getStorageContainer().getBlackListPages() + 1))
                    );
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

                    if (isWhitelist()) {
                        setWhitelistPage(getWhitelistPage() - 1);
                    } else {
                        setBlacklistPage(getBlacklistPage() - 1);
                    }

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

                    if (isWhitelist()) {
                        setWhitelistPage(getWhitelistPage() + 1);
                    } else {
                        setBlacklistPage(getBlacklistPage() + 1);
                    }

                    reloadGui();
                });
    }

    private InventoryButton createAddItemToListButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName("§aAdd Item to " + (isWhitelist() ? "Whitelist" : "Blacklist"));
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


    private void reloadGui() {
        GUIManager.openGUI(new ContainerGUI(this), getPlayer(), false);
    }
}
