package de.janschuri.lunaticstorage.storage;

import org.bukkit.NamespacedKey;

public final class Key {

    public static final String PLUGIN_NAMESPACE = "lunaticstorage";
    public static final NamespacedKey PANEL_BLOCK = new NamespacedKey(PLUGIN_NAMESPACE, "panel_block");
    public static final NamespacedKey STORAGE = new NamespacedKey(PLUGIN_NAMESPACE, "storage");
    public static final NamespacedKey STORAGE_ITEM = new NamespacedKey(PLUGIN_NAMESPACE, "storage_item");
    public static final NamespacedKey STORAGE_ITEM_WORLDS = new NamespacedKey(PLUGIN_NAMESPACE, "storage_item_worlds");
    public static final NamespacedKey STORAGE_CONTAINER = new NamespacedKey(PLUGIN_NAMESPACE, "storage_container");

    private Key() {
        // Prevent instantiation
    }

    public static NamespacedKey getKey(String name) {
        return new NamespacedKey(PLUGIN_NAMESPACE, name);
    }
}
