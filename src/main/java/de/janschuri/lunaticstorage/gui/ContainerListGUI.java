package de.janschuri.lunaticstorage.gui;

import de.janschuri.lunaticlib.PlayerSender;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.InventoryButton;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.list.ListGUI;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.list.PaginatedList;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ContainerListGUI extends ListGUI<StorageContainer> implements PaginatedList<StorageContainer> {

    private int page = 0;
    private ItemStack storageItem;

    public ContainerListGUI(ItemStack itemStack) {
        super();
        storageItem = itemStack;
    }

    @Override
    public InventoryButton listItemButton(StorageContainer storageContainer) {
        ItemStack itemStack = new ItemStack(storageContainer.getBlock().getType());
        ItemMeta itemMeta = itemStack.getItemMeta();

        Block block = storageContainer.getBlock();

        String coordinates = block.getX() + " " + block.getY() + " " + block.getZ();

        itemMeta.setDisplayName("§eContainer");

        List<String> lore = List.of(
                "§7Coordinates: " + coordinates
        );

        itemMeta.setLore(lore);

        itemStack.setItemMeta(itemMeta);

        return new InventoryButton()
                .creator(player -> itemStack)
                .consumer(event -> {
                    Player player = (Player) event.getWhoClicked();

                    if (event.isRightClick()) {
                        if (!event.isShiftClick()) {
                            try {
                                LunaticStorage.getGlowingBlocks().setGlowing(storageContainer.getBlock(), player, ChatColor.AQUA);

                                Bukkit.getScheduler().runTaskLater(LunaticStorage.getInstance(), () -> {
                                    try {
                                        LunaticStorage.getGlowingBlocks().unsetGlowing(storageContainer.getBlock(), player);
                                    } catch (ReflectiveOperationException e) {
                                        player.sendMessage("§cAn error occurred while trying to highlight the container.");
                                    }
                                }, 20 * 30);
                            } catch (ReflectiveOperationException e) {
                                player.sendMessage("§cAn error occurred while trying to highlight the container.");
                            }
                        } else {
                            player.closeInventory();
                            player.performCommand("lunaticstorage remove " + storageContainer.getBlock().getWorld().getUID() + " " + storageContainer.getBlockCoords());
                        }
                    }
                });
    }

    @Override
    public List<StorageContainer> getItems() {
        return Utils.getStorageChests(storageItem).stream().toList();
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public void setPage(int i) {
        page = i;
    }
}
