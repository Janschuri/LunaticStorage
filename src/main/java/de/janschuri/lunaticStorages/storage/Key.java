package de.janschuri.lunaticStorages.storage;

import org.bukkit.NamespacedKey;

public final class Key {

    public static final String PLUGIN_NAMESPACE = "lunaticstorage";
    public static final NamespacedKey PANEL_ID = new NamespacedKey(PLUGIN_NAMESPACE, "panel_id");
    public static final NamespacedKey PANE = new NamespacedKey(PLUGIN_NAMESPACE, "gui_pane");
    public static final NamespacedKey STORAGE_PANE = new NamespacedKey(PLUGIN_NAMESPACE, "gui_storage_pane");
    public static final NamespacedKey STORAGE_CONTENT = new NamespacedKey(PLUGIN_NAMESPACE, "gui_storage_content");
    public static final NamespacedKey KEY_LIMIT = new NamespacedKey(PLUGIN_NAMESPACE, "limit");
    public static final NamespacedKey PANEL_BLOCK = new NamespacedKey(PLUGIN_NAMESPACE, "panel_block");
    public static final NamespacedKey STORAGE = new NamespacedKey(PLUGIN_NAMESPACE, "invs");
    public static final NamespacedKey LEFT_ARROW = new NamespacedKey(PLUGIN_NAMESPACE, "left_arrow");
    public static final NamespacedKey RIGHT_ARROW = new NamespacedKey(PLUGIN_NAMESPACE, "right_arrow");
    public static final NamespacedKey PAGE = new NamespacedKey(PLUGIN_NAMESPACE, "page");
    public static final NamespacedKey SEARCH = new NamespacedKey(PLUGIN_NAMESPACE, "search");
    public static final NamespacedKey DESC = new NamespacedKey(PLUGIN_NAMESPACE, "desc");
    public static final NamespacedKey SORTER = new NamespacedKey(PLUGIN_NAMESPACE, "sorter");

    private Key() {
        // Prevent instantiation
    }
}
