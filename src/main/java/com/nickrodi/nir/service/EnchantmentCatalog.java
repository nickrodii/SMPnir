package com.nickrodi.nir.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EnchantmentCatalog {
    public record EnchantEntry(String name, String key, int maxLevel) {
        public String commandId() {
            int index = key.indexOf(':');
            if (index >= 0 && index + 1 < key.length()) {
                return key.substring(index + 1);
            }
            return key;
        }
    }
// testing submodules
    public record EnchantCategory(String title, List<EnchantEntry> entries) {
    }

    private static final EnchantEntry MENDING = entry("Mending", "minecraft:mending", 1);
    private static final EnchantEntry UNBREAKING = entry("Unbreaking", "minecraft:unbreaking", 3);
    private static final EnchantEntry VANISHING = entry("Curse of Vanishing", "minecraft:vanishing_curse", 1);

    private static final EnchantEntry AQUA_AFFINITY = entry("Aqua Affinity", "minecraft:aqua_affinity", 1);
    private static final EnchantEntry BLAST_PROTECTION = entry("Blast Protection", "minecraft:blast_protection", 4);
    private static final EnchantEntry BINDING = entry("Curse of Binding", "minecraft:binding_curse", 1);
    private static final EnchantEntry DEPTH_STRIDER = entry("Depth Strider", "minecraft:depth_strider", 3);
    private static final EnchantEntry FEATHER_FALLING = entry("Feather Falling", "minecraft:feather_falling", 4);
    private static final EnchantEntry FIRE_PROTECTION = entry("Fire Protection", "minecraft:fire_protection", 4);
    private static final EnchantEntry FROST_WALKER = entry("Frost Walker", "minecraft:frost_walker", 2);
    private static final EnchantEntry PROJECTILE_PROTECTION = entry("Projectile Protection", "minecraft:projectile_protection", 4);
    private static final EnchantEntry PROTECTION = entry("Protection", "minecraft:protection", 4);
    private static final EnchantEntry RESPIRATION = entry("Respiration", "minecraft:respiration", 3);
    private static final EnchantEntry SOUL_SPEED = entry("Soul Speed", "minecraft:soul_speed", 3);
    private static final EnchantEntry THORNS = entry("Thorns", "minecraft:thorns", 3);
    private static final EnchantEntry SWIFT_SNEAK = entry("Swift Sneak", "minecraft:swift_sneak", 3);

    private static final EnchantEntry BANE_OF_ARTHROPODS = entry("Bane of Arthropods", "minecraft:bane_of_arthropods", 5);
    private static final EnchantEntry BREACH = entry("Breach", "minecraft:breach", 4);
    private static final EnchantEntry DENSITY = entry("Density", "minecraft:density", 5);
    private static final EnchantEntry FIRE_ASPECT = entry("Fire Aspect", "minecraft:fire_aspect", 2);
    private static final EnchantEntry LOOTING = entry("Looting", "minecraft:looting", 3);
    private static final EnchantEntry LUNGE = entry("Lunge", "minecraft:lunge", 3);
    private static final EnchantEntry IMPALING = entry("Impaling", "minecraft:impaling", 5);
    private static final EnchantEntry KNOCKBACK = entry("Knockback", "minecraft:knockback", 2);
    private static final EnchantEntry SHARPNESS = entry("Sharpness", "minecraft:sharpness", 5);
    private static final EnchantEntry SMITE = entry("Smite", "minecraft:smite", 5);
    private static final EnchantEntry SWEEPING_EDGE = entry("Sweeping Edge", "minecraft:sweeping_edge", 3);
    private static final EnchantEntry WIND_BURST = entry("Wind Burst", "minecraft:wind_burst", 3);

    private static final EnchantEntry CHANNELING = entry("Channeling", "minecraft:channeling", 1);
    private static final EnchantEntry FLAME = entry("Flame", "minecraft:flame", 1);
    private static final EnchantEntry INFINITY = entry("Infinity", "minecraft:infinity", 1);
    private static final EnchantEntry LOYALTY = entry("Loyalty", "minecraft:loyalty", 3);
    private static final EnchantEntry RIPTIDE = entry("Riptide", "minecraft:riptide", 3);
    private static final EnchantEntry MULTISHOT = entry("Multishot", "minecraft:multishot", 1);
    private static final EnchantEntry PIERCING = entry("Piercing", "minecraft:piercing", 4);
    private static final EnchantEntry POWER = entry("Power", "minecraft:power", 5);
    private static final EnchantEntry PUNCH = entry("Punch", "minecraft:punch", 2);
    private static final EnchantEntry QUICK_CHARGE = entry("Quick Charge", "minecraft:quick_charge", 3);

    private static final EnchantEntry EFFICIENCY = entry("Efficiency", "minecraft:efficiency", 5);
    private static final EnchantEntry FORTUNE = entry("Fortune", "minecraft:fortune", 3);
    private static final EnchantEntry LUCK_OF_THE_SEA = entry("Luck of the Sea", "minecraft:luck_of_the_sea", 3);
    private static final EnchantEntry LURE = entry("Lure", "minecraft:lure", 3);
    private static final EnchantEntry SILK_TOUCH = entry("Silk Touch", "minecraft:silk_touch", 1);

    private static final List<EnchantCategory> CATEGORIES;
    private static final Map<String, EnchantEntry> LOOKUP;

    static {
        List<EnchantCategory> categories = new ArrayList<>();
        categories.add(new EnchantCategory("ALL PURPOSE", List.of(
                MENDING,
                UNBREAKING,
                VANISHING
        )));
        categories.add(new EnchantCategory("ARMOR", List.of(
                AQUA_AFFINITY,
                BLAST_PROTECTION,
                BINDING,
                DEPTH_STRIDER,
                FEATHER_FALLING,
                FIRE_PROTECTION,
                FROST_WALKER,
                PROJECTILE_PROTECTION,
                PROTECTION,
                RESPIRATION,
                SOUL_SPEED,
                THORNS,
                SWIFT_SNEAK
        )));
        categories.add(new EnchantCategory("WEAPONS", List.of(
                BANE_OF_ARTHROPODS,
                BREACH,
                DENSITY,
                FIRE_ASPECT,
                LOOTING,
                LUNGE,
                IMPALING,
                KNOCKBACK,
                SHARPNESS,
                SMITE,
                SWEEPING_EDGE,
                WIND_BURST,
                CHANNELING,
                FLAME,
                INFINITY,
                LOYALTY,
                RIPTIDE,
                MULTISHOT,
                PIERCING,
                POWER,
                PUNCH,
                QUICK_CHARGE
        )));
        categories.add(new EnchantCategory("TOOLS", List.of(
                EFFICIENCY,
                FORTUNE,
                LUCK_OF_THE_SEA,
                LURE,
                SILK_TOUCH
        )));
        CATEGORIES = Collections.unmodifiableList(categories);

        Map<String, EnchantEntry> lookup = new HashMap<>();
        for (EnchantCategory category : CATEGORIES) {
            for (EnchantEntry entry : category.entries()) {
                registerLookup(lookup, entry);
            }
        }
        LOOKUP = Collections.unmodifiableMap(lookup);
    }

    private EnchantmentCatalog() {
    }

    public static List<EnchantCategory> categories() {
        return CATEGORIES;
    }

    public static EnchantEntry find(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String normalized = normalize(input);
        if (normalized.isEmpty()) {
            return null;
        }
        return LOOKUP.get(normalized);
    }

    private static EnchantEntry entry(String name, String key, int maxLevel) {
        return new EnchantEntry(name, key, maxLevel);
    }

    private static void registerLookup(Map<String, EnchantEntry> lookup, EnchantEntry entry) {
        String nameKey = normalize(entry.name());
        lookup.putIfAbsent(nameKey, entry);

        String fullKey = normalize(entry.key());
        lookup.putIfAbsent(fullKey, entry);

        int index = entry.key().indexOf(':');
        if (index >= 0 && index + 1 < entry.key().length()) {
            String shortKey = normalize(entry.key().substring(index + 1));
            lookup.putIfAbsent(shortKey, entry);
        }
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]", "");
    }
}
