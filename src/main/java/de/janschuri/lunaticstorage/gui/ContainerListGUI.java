package de.janschuri.lunaticstorage.gui;

import de.janschuri.lunaticlib.config.LunaticMessageKey;
import de.janschuri.lunaticlib.config.MessageKey;
import de.janschuri.lunaticlib.platform.paper.inventorygui.buttons.InventoryButton;
import de.janschuri.lunaticlib.platform.paper.inventorygui.guis.DecisionGUI;
import de.janschuri.lunaticlib.platform.paper.inventorygui.guis.ListGUI;
import de.janschuri.lunaticlib.platform.paper.inventorygui.handler.GUIManager;
import de.janschuri.lunaticlib.platform.paper.inventorygui.interfaces.list.PaginatedList;
import de.janschuri.lunaticstorage.LunaticStorage;
import de.janschuri.lunaticstorage.storage.StorageContainer;
import de.janschuri.lunaticstorage.utils.Logger;
import de.janschuri.lunaticstorage.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static de.janschuri.lunaticstorage.LunaticStorage.getMessage;

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

    private final MessageKey addContainerMK = new LunaticMessageKey("add_container_button")
            .noPrefix()
            .defaultMessage("en", "&aAlle Container in Reichweite hinzufügen")
            .defaultMessage("de", "&aAdd all containers in range");

    private final MessageKey containersInRangeMK = new LunaticMessageKey("containers_in_range")
            .noPrefix()
            .defaultMessage("en", "&eContainers in range: %count%")
            .defaultMessage("de", "&eContainer in Reichweite: %count%");

    private final MessageKey returnMK = new LunaticMessageKey("return_button")
            .noPrefix()
            .defaultMessage("en", "&cClose")
            .defaultMessage("de", "&cSchließen");

    private final MessageKey loadingMK = new LunaticMessageKey("loading")
            .noPrefix()
            .defaultMessage("en", "&eLoading containers in range... (%percent%%)")
            .defaultMessage("de", "&eLade Container in Reichweite... (%percent%%)");

    private final MessageKey containersAddedMK = new LunaticMessageKey("containers_added")
            .defaultMessage("en", "&aSuccessfully added %count% containers to the storageitem.")
            .defaultMessage("de", "&aErfolgreich %count% Container zum Storageitem hinzugefügt.");

    private final MessageKey rangeTooLargeMK = new LunaticMessageKey("range_too_large")
            .defaultMessage("en", "&cThe specified range is too large to search for containers.")
            .defaultMessage("de", "&cDie angegebene Reichweite ist zu groß, um nach Containern zu suchen.");


    private boolean rangeTooLarge = false;
    private int page = 0;
    private Map<UUID, List<String>> containerCoords;
    private org.bukkit.scheduler.BukkitTask  task = null;

    private int loading = -1;
    private List<String> containersInRange = new ArrayList<>();
    private int range;
    private Location loc;
    private Consumer<InventoryClickEvent> onCloseConsumer = null;
    private BiConsumer<InventoryClickEvent, Map<UUID, List<String>>> onContainerChange = null;

    private static Map<Location, ContainerListGUI> guiCache = new java.util.HashMap<>();

    private boolean reloadTimeout = false;

    private ContainerListGUI(Map<UUID, List<String>> containerCoords, int range, Location loc) {
        super();
        this.containerCoords = containerCoords;
        this.range = range;
        this.loc = loc;

        if (this.range >= 0 && this.loc != null) {
            this.loading = 0;
            this.loadContainersInRange();
        }
    }

    public static ContainerListGUI getContainerListGUI(Map<UUID, List<String>> containerCoords, int range, Location loc) {
        ContainerListGUI gui = guiCache.computeIfAbsent(loc, k -> new ContainerListGUI(containerCoords, range, loc));
        return gui;
    }

    public static ContainerListGUI getContainerListGUI(Map<UUID, List<String>> containerCoords) {
        return new ContainerListGUI(containerCoords, -1, null);
    }

    @Override
    public void init(Player player) {
        if (range >= 0 && loc != null) {
            this.addButton(4, addContainerButton());
        }

        if (this.onCloseConsumer != null) {
            this.addButton(0, returnButton());
        }

        super.init(player);
    }

    private void loadContainersInRange() {

        if (this.range < 0 || this.loc == null) {
            this.loading = -1;
            return;
        }

        if (range > 128) {
            this.rangeTooLarge = true;
            Bukkit.getScheduler().runTaskLater(LunaticStorage.getInstance(), () -> {
                this.loading = -1;
                this.reloadGui();
            }, 10L);
            return;
        }

        task = Bukkit.getScheduler().runTaskAsynchronously(LunaticStorage.getInstance(), () -> {
            List<String> foundContainers = new ArrayList<>();
            List<String> existingContainers = this.containerCoords.getOrDefault(loc.getWorld().getUID(), new ArrayList<>());

            List<int[]> coordinatesToCheck = new ArrayList<>();

            for (int x = -this.range; x <= this.range; x++) {
                for (int y = -this.range; y <= this.range; y++) {
                    for (int z = -this.range; z <= this.range; z++) {
                        coordinatesToCheck.add(new int[]{x, y, z});
                    }
                }
            }

            int total = coordinatesToCheck.size();

            long processed = 0;

            for (int[] offset : coordinatesToCheck) {
                Block block = loc.getBlock().getRelative(offset[0], offset[1], offset[2]);

                if (Utils.isContainerBlock(block.getType())) {
                    String containerCoords = Utils.serializeCoords(block.getLocation());

                    if (!existingContainers.contains(containerCoords)) {
                        foundContainers.add(Utils.serializeCoords(block.getLocation()));
                    }
                }
                processed++;

                int newLoading = (int) ((processed * 100) / total);
                if (newLoading != this.loading) {
                    this.loading = newLoading;
                    reloadGui();
                }
            }

            Bukkit.getScheduler().runTaskLater(LunaticStorage.getInstance(), () -> {
                containersInRange = foundContainers;
                this.loading = -1;
                this.reloadGui();
            }, 10L);
        });

        return;
    }

    public static void destroy(Location loc) {
        ContainerListGUI gui = guiCache.remove(loc);
        if (gui != null && gui.task != null) {
            gui.task.cancel();
        }
    }

    public static void destroyAll() {
        for (Location loc : guiCache.keySet()) {
            destroy(loc);
        }
        guiCache.clear();
    }

    public ContainerListGUI onClose(Consumer<InventoryClickEvent> consumer) {
        this.onCloseConsumer = consumer;
        return this;
    }

    public ContainerListGUI onContainerChange(BiConsumer<InventoryClickEvent, Map<UUID, List<String>>> onContainerChange) {
        this.onContainerChange = onContainerChange;
        return this;
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
                                }, 20L * 30);
                            } catch (ReflectiveOperationException e) {
                                player.sendMessage("&cAn error occurred while trying to highlight the container.");
                            }
                        } else {
                            player.closeInventory();

                            DecisionGUI decisionGUI = new DecisionGUI(getMessage(guiTitleMK))
                                    .accept(event2 -> {
                                        UUID worldUUID = storageContainer.getBlock().getWorld().getUID();
                                        String coords = Utils.serializeCoords(storageContainer.getBlock().getLocation());
                                        containerCoords.get(worldUUID).remove(coords);

                                        if (this.onContainerChange != null) {
                                            this.onContainerChange.accept(event2, containerCoords);
                                        }

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
    public void reloadGui() {
        if (reloadTimeout) {
            return;
        }

        reloadTimeout = true;

        Bukkit.getScheduler().runTaskLater(LunaticStorage.getInstance(), () -> {
            super.reloadGui();
            reloadTimeout = false;
        }, 20L);
    }

    @Override
    public List<StorageContainer> getItems() {
        return Utils.getStorageChests(this.containerCoords).stream().toList();
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public void setPage(int i) {
        page = i;
    }

    private InventoryButton addContainerButton() {
        ItemStack itemStack = new ItemStack(Material.LIGHTNING_ROD);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.displayName(getMessage(addContainerMK));

        List<Component> lore = new ArrayList<>();

        if (this.loading >= 0) {
            lore.add(getMessage(loadingMK,
                    placeholder("%percent%", String.valueOf(this.loading))
            ));
        } else if (this.rangeTooLarge) {
            lore.add(getMessage(rangeTooLargeMK));
        } else {
            lore.add(
                    getMessage(containersInRangeMK,
                            placeholder("%count%", String.valueOf(containersInRange.size()))
                    )
            );
        }

        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);

        return new InventoryButton()
                .creator(player -> itemStack)
                .consumer(event -> {
                    if (this.onContainerChange == null || this.loading >= 0 || loc == null) {
                        return;
                    }

                    Player player = (Player) event.getWhoClicked();

                    List<String> containersInRange = new ArrayList<>(this.containersInRange);

                    int addedContainers = containersInRange.size();

                    UUID worldUUID = loc.getWorld().getUID();

                    this.containerCoords.putIfAbsent(worldUUID, new ArrayList<>());
                    this.containerCoords.get(worldUUID).addAll(containersInRange);

                    this.onContainerChange.accept(event, containerCoords);

                    Component message = getMessage(
                            containersAddedMK,
                            placeholder("%count%", String.valueOf(addedContainers))
                    );

                    player.sendMessage(message);
                });
    }

    private InventoryButton returnButton() {
        ItemStack itemStack = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.displayName(getMessage(returnMK));
        itemStack.setItemMeta(itemMeta);

        return new InventoryButton()
                .creator(player -> itemStack)
                .consumer(onCloseConsumer);
    }
}
