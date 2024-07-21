package de.janschuri.lunaticstorage.gui;

import de.janschuri.lunaticlib.platform.bukkit.inventorygui.GUIManager;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.InventoryButton;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.InventoryGUI;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ContainerGUI extends InventoryGUI {

    private final StorageContainer container;
    private final Player player;


    public ContainerGUI(Player player, StorageContainer storageContainer) {
        super(createInventory());
        this.container = storageContainer;
        this.player = player;
    }

    private ContainerGUI(ContainerGUI containerGUI) {
        super(containerGUI.getInventory());
        this.container = containerGUI.container;
        this.player = containerGUI.player;
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
        Map<ItemStack, Boolean> whitelistItems = container.getWhitelist();
        createListButtons(whitelistItems, true, 1);

        addButton(8, createAddContainerInvToWhitelistButton());

        super.decorate(player);
    }

    private void createListButtons(Map<ItemStack, Boolean> items, boolean whitelist, int page) {
        int i = 9;
        int j = 0;
        int max = 45;
        int start = 45 * (page - 1);
        int end = Math.min(items.size(), start + max);

        for (Map.Entry<ItemStack, Boolean> entry : items.entrySet()) {
            if (j >= start && j < end) {
                ItemStack item = entry.getKey();
                boolean matchNBT = entry.getValue();

                InventoryButton button = createListButton(whitelist, item, matchNBT);
                addButton(i, button);
                i++;
            }
            j++;
        }
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

        return new InventoryButton()
                .creator(player -> itemClone)
                .consumer(event -> {

                    if (whitelist) {
                        container.toggleWhitelist(item);
                    } else {
                        container.toggleBlacklist(item);
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
                    ItemStack[] contents = container.getSnapshotInventory().getContents();
                    for (ItemStack item : contents) {
                        if (item != null) {
                            container.addToWhitelist(item, true);
                        }
                    }
                    reloadGui();
                });
    }



    private void reloadGui() {
        GUIManager.openGUI(new ContainerGUI(this), player, false);
    }
}
