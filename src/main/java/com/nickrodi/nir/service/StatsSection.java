package com.nickrodi.nir.service;

import java.util.Locale;

public enum StatsSection {
    PLAYTIME("playtime"),
    MINING("mining"),
    COMBAT("combat"),
    EXPLORATION("exploration"),
    COLLECTIONS("collections"),
    EXTRA("extra"),
    MOBS("mobs"),
    BESTIARY("bestiary"),
    ENCHANTS("enchants"),
    BIOMES("biomes"),
    FISHING("fishing"),
    NIGHT("night"),
    DISCS("discs");

    private final String id;

    StatsSection(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static StatsSection from(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String normalized = input.toLowerCase(Locale.US).replaceAll("[^a-z]", "");
        return switch (normalized) {
            case "playtime" -> PLAYTIME;
            case "mining" -> MINING;
            case "combat" -> COMBAT;
            case "exploration" -> EXPLORATION;
            case "collection", "collections" -> COLLECTIONS;
            case "extra", "extras" -> EXTRA;
            case "mobs", "mob", "breeding", "breed", "taming", "tame" -> MOBS;
            case "bestiary", "bests", "beastiary" -> BESTIARY;
            case "enchant", "enchants", "enchantments" -> ENCHANTS;
            case "biome", "biomes" -> BIOMES;
            case "fishing", "fish" -> FISHING;
            case "night", "nightevent", "nightevents" -> NIGHT;
            case "disc", "discs", "disk", "disks" -> DISCS;
            default -> null;
        };
    }
}
