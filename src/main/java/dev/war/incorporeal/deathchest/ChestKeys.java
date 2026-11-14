package dev.war.incorporeal.deathchest;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class ChestKeys {
    public final NamespacedKey owner;
    public final NamespacedKey items;
    public final NamespacedKey format;
    public final NamespacedKey display;
    public final NamespacedKey opened;
    public final NamespacedKey created;

    public ChestKeys(Plugin plugin) {
        owner = new NamespacedKey(plugin, "owner_uuid");
        items = new NamespacedKey(plugin, "stored_items");
        format = new NamespacedKey(plugin, "items_format");
        display = new NamespacedKey(plugin, "linked_display");
        opened = new NamespacedKey(plugin, "opened");
        created = new NamespacedKey(plugin, "created_at");
    }
}
