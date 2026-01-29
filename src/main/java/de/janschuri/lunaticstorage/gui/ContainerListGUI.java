package de.janschuri.lunaticstorage.gui;

import de.janschuri.lunaticlib.config.MessageKey;
import de.janschuri.lunaticlib.config.impl.LunaticMessageKey;
import de.janschuri.lunaticlib.inventorygui.buttons.InventoryButton;
import de.janschuri.lunaticlib.inventorygui.guis.DecisionGUI;
import de.janschuri.lunaticlib.inventorygui.guis.ListGUI;
import de.janschuri.lunaticlib.inventorygui.handler.GUIManager;
import de.janschuri.lunaticlib.inventorygui.interfaces.list.PaginatedList;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

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
            .noPrefix()
            .defaultMessage("en", "&eRemove Container")
            .defaultMessage("de", "&eContainer entfernen");

    private final MessageKey rightClickMK = new LunaticMessageKey("right_click")
            .noPrefix()
            .defaultMessage("en", "&e&lRight click &r&8- ")
            .defaultMessage("de", "&e&lRechtsklick &r&8- ");

    private final MessageKey shiftRightClickMK = new LunaticMessageKey("shift_right_click")
            .noPrefix()
            .defaultMessage("en", "&4&lShift right click &r&8- ")
            .defaultMessage("de", "&4&lShift-Rechtsklick &r&8- ");

    private final MessageKey showContainerMK = new LunaticMessageKey("show_container")
            .noPrefix()
            .defaultMessage("en", "Show container")
            .defaultMessage("de", "Container anzeigen");

    private final MessageKey removeContainerMK = new LunaticMessageKey("remove_container")
            .noPrefix()
            .defaultMessage("en", "Remove container")
            .defaultMessage("de", "Container entfernen");

    private final MessageKey coordinatesMK = new LunaticMessageKey("coordinates")
            .noPrefix()
            .defaultMessage("en", "Coordinates")
            .defaultMessage("de", "Koordinaten");

    private final MessageKey worldMK = new LunaticMessageKey("world")
            .noPrefix()
            .defaultMessage("en", "World")
            .defaultMessage("de", "Welt");

    private final MessageKey containerMK = new LunaticMessageKey("container")
            .noPrefix()
            .defaultMessage("en", "Container")
            .defaultMessage("de", "Container");



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

        itemMeta.displayName(getMessage(containerMK));

        List<Component> lore = List.of(
                getMessage(coordinatesMK).append(Component.text(": " + coordinates)),
                getMessage(worldMK).append(Component.text(": " + block.getWorld().getName())),
                Component.text(" "),
                getMessage(rightClickMK).append(Component.text(": ")).append(getMessage(showContainerMK)),
                getMessage(shiftRightClickMK).append(Component.text(": ")).append(getMessage(removeContainerMK))
        );

        itemMeta.lore(lore);

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
