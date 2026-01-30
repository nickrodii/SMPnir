package com.nickrodi.nir.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class Keys {
    public static NamespacedKey BOOK_CLAIMED;
    public static NamespacedKey SPAWNER_TAG;
    public static NamespacedKey CANE_PLAYER_DROPPED;
    public static NamespacedKey CANE_XP_AWARDED;
    public static NamespacedKey CROP_XP_VALUE;
    public static NamespacedKey CROP_XP_AWARDED;
    public static NamespacedKey CROP_XP_COUNT;
    public static NamespacedKey DISC_COLLECTED;
    public static NamespacedKey BOARD_BOOK;
    public static NamespacedKey DEATH_CHEST;
    public static NamespacedKey DEATH_CHEST_OWNER;
    public static NamespacedKey DEATH_CHEST_CREATED;
    public static NamespacedKey DEATH_CHEST_BURN_START;

    private Keys() {
    }

    public static void init(JavaPlugin plugin) {
        BOOK_CLAIMED = new NamespacedKey(plugin, "book_claimed");
        SPAWNER_TAG = new NamespacedKey(plugin, "from_spawner");
        CANE_PLAYER_DROPPED = new NamespacedKey(plugin, "cane_player_dropped");
        CANE_XP_AWARDED = new NamespacedKey(plugin, "cane_xp_awarded");
        CROP_XP_VALUE = new NamespacedKey(plugin, "crop_xp_value");
        CROP_XP_AWARDED = new NamespacedKey(plugin, "crop_xp_awarded");
        CROP_XP_COUNT = new NamespacedKey(plugin, "crop_xp_count");
        DISC_COLLECTED = new NamespacedKey(plugin, "disc_collected");
        BOARD_BOOK = new NamespacedKey(plugin, "board_book");
        DEATH_CHEST = new NamespacedKey(plugin, "death_chest");
        DEATH_CHEST_OWNER = new NamespacedKey(plugin, "death_chest_owner");
        DEATH_CHEST_CREATED = new NamespacedKey(plugin, "death_chest_created");
        DEATH_CHEST_BURN_START = new NamespacedKey(plugin, "death_chest_burn_start");
    }
}
