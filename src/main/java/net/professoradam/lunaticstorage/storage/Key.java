package net.professoradam.lunaticstorage.storage;

import org.bukkit.NamespacedKey;

public final class Key {

    public static final String PLUGIN_NAMESPACE = "lunaticstorage";
    public static final NamespacedKey PANEL_BLOCK = new NamespacedKey(PLUGIN_NAMESPACE, "panel_block");
    public static final NamespacedKey STORAGE = new NamespacedKey(PLUGIN_NAMESPACE, "storage");
    public static final NamespacedKey STORAGE_ITEM = new NamespacedKey(PLUGIN_NAMESPACE, "storage_item");
    public static final NamespacedKey STORAGE_ITEM_WORLDS = new NamespacedKey(PLUGIN_NAMESPACE, "storage_item_worlds");
    public static final NamespacedKey STORAGE_CONTAINER = new NamespacedKey(PLUGIN_NAMESPACE, "storage_container");
    public static final NamespacedKey PANEL_RANGE = new NamespacedKey(PLUGIN_NAMESPACE, "range_panel");
    public static final NamespacedKey RANGE_ITEM = new NamespacedKey(PLUGIN_NAMESPACE, "range_item");
    public static final NamespacedKey RANGE = new NamespacedKey(PLUGIN_NAMESPACE, "range");

    public static final NamespacedKey WHITELIST = new NamespacedKey(PLUGIN_NAMESPACE, "whitelist");
    public static final NamespacedKey WHITELIST_ENABLED = new NamespacedKey(PLUGIN_NAMESPACE, "whitelist_enabled");
    public static final NamespacedKey BLACKLIST = new NamespacedKey(PLUGIN_NAMESPACE, "blacklist");
    public static final NamespacedKey BLACKLIST_ENABLED = new NamespacedKey(PLUGIN_NAMESPACE, "blacklist_enabled");

    private Key() {
        // Prevent instantiation
    }

    public static NamespacedKey getKey(String name) {
        return new NamespacedKey(PLUGIN_NAMESPACE, name);
    }
}
