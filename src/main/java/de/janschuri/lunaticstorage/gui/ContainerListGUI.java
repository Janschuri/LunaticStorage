package de.janschuri.lunaticstorage.gui;

import de.janschuri.lunaticlib.CommandMessageKey;
import de.janschuri.lunaticlib.MessageKey;
import de.janschuri.lunaticlib.PlayerSender;
import de.janschuri.lunaticlib.common.config.LunaticMessageKey;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.DecisionGUI;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.GUIManager;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.InventoryButton;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.list.ListGUI;
import de.janschuri.lunaticlib.platform.bukkit.inventorygui.list.PaginatedList;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.config.LanguageConfig;
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
import java.util.Map;

import static de.janschuri.lunaticstorage.LunaticStorage.getMessage;
import static de.janschuri.lunaticstorage.LunaticStorage.getMessageAsLegacyString;

public class ContainerListGUI extends ListGUI<StorageContainer> implements PaginatedList<StorageContainer> {


    private final MessageKey confirmMK = new LunaticMessageKey("remove_container_confirm")
            .defaultMessage("en", "&cAre you sure you want to remove the container from the storageitem?")
            .defaultMessage("de", "&cBist du dir sicher, dass du den Container vom Storageitem entfernen willst?");

    private final MessageKey cancelMK = new LunaticMessageKey("remove_container_cancel")
            .defaultMessage("en", "&cCancelled removing the container from the storageitem.")
            .defaultMessage("de", "&cAbbrechen des Entfernens des Containers vom Storageitem.");

    private final MessageKey removedMK = new LunaticMessageKey("container_removed")
            .defaultMessage("en", "&aSuccessfully removed the container from storageitem in %world% at %x% %y% %z%")
            .defaultMessage("de", "&aContainer erfolgreich vom Storageitem in %world% bei %x% %y% %z% entfernt.");

    private final MessageKey guiTitleMK = new LunaticMessageKey("remove_container_gui_title")
            .defaultMessage("en", "&eRemove Container")
            .defaultMessage("de", "&eContainer entfernen");

    private final MessageKey rightClickMK = new LunaticMessageKey("right_click")
            .defaultMessage("en", "Right click")
            .defaultMessage("de", "Rechtsklick");

    private final MessageKey shiftRightClickMK = new LunaticMessageKey("shift_right_click")
            .defaultMessage("en", "Shift right click")
            .defaultMessage("de", "Shift-Rechtsklick");

    private final MessageKey leftClickMK = new LunaticMessageKey("left_click")
            .defaultMessage("en", "Left click")
            .defaultMessage("de", "Linksklick");

    private final MessageKey shiftLeftClickMK = new LunaticMessageKey("shift_left_click")
            .defaultMessage("en", "Shift left click")
            .defaultMessage("de", "Shift-Linksklick");

    private final MessageKey showContainerMK = new LunaticMessageKey("show_container")
            .defaultMessage("en", "Show container")
            .defaultMessage("de", "Container anzeigen");

    private final MessageKey removeContainerMK = new LunaticMessageKey("remove_container")
            .defaultMessage("en", "Remove container")
            .defaultMessage("de", "Container entfernen");



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

        itemMeta.setDisplayName("&eContainer");

        List<String> lore = List.of(
                "&7Coordinates: " + coordinates,
                "&7World: " + block.getWorld().getName(),
                "",
                "&e&l" + getMessageAsLegacyString(rightClickMK) + " &r&8- " + getMessageAsLegacyString(showContainerMK),
                "&4&l" + getMessageAsLegacyString(shiftRightClickMK) + " &r&8- " + getMessageAsLegacyString(removeContainerMK)
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
                                        player.sendMessage("&cAn error occurred while trying to highlight the container.");
                                    }
                                }, 20 * 30);
                            } catch (ReflectiveOperationException e) {
                                player.sendMessage("&cAn error occurred while trying to highlight the container.");
                            }
                        } else {
                            player.closeInventory();

                            DecisionGUI decisionGUI = new DecisionGUI(getMessage(guiTitleMK))
                                    .accept(event2 -> {
                                        Utils.removeContainerFromStorageItem(storageContainer, storageItem);
                                        player.sendMessage(
                                                getMessage(
                                                        removedMK,
                                                                placeholder("%world%", block.getWorld().getName()),
                                                                placeholder("%x%", String.valueOf(block.getX())),
                                                                placeholder("%y%", String.valueOf(block.getY())),
                                                                placeholder("%z%", String.valueOf(block.getZ()))
                                                )
                                        );

                                        GUIManager.openGUI(this, player);
                                        this.reloadGui();
                                    })
                                    .deny(event2 -> {
                                        player.sendMessage(getMessage(cancelMK));

                                        GUIManager.openGUI(this, player);
                                        this.reloadGui();
                                    });

                            GUIManager.openGUI(decisionGUI, player);
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
