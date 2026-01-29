package com.nickrodi.levels.service;

import java.util.Locale;

public enum StatDisplayType {
    PLAYTIME("playtime"),
    STREAK("streak"),
    ORES("ores"),
    CROPS("crops"),
    MOBS("mobs"),
    MONSTERS("monsters"),
    DRAGON("dragon"),
    WITHER("wither"),
    WARDEN("warden"),
    PLAYERS("players"),
    TRADES("trades"),
    XP_ORBS("xporbs"),
    ADVANCEMENTS("advancements"),
    QUESTS("quests"),
    BRED("bred"),
    TAMED("tamed"),
    BESTIARY("bestiary"),
    ENCHANTS("enchants"),
    BIOMES("biomes"),
    FISHING("fishing"),
    NIGHT("night"),
    CHESTS("chests");

    private final String id;

    StatDisplayType(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static StatDisplayType fromId(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String normalized = input.toLowerCase(Locale.US).replaceAll("[^a-z]", "");
        return switch (normalized) {
            case "playtime" -> PLAYTIME;
            case "streak" -> STREAK;
            case "ores", "ore" -> ORES;
            case "crops", "crop" -> CROPS;
            case "mobs", "mob" -> MOBS;
            case "monsters", "monster" -> MONSTERS;
            case "dragon", "enderdragon" -> DRAGON;
            case "wither" -> WITHER;
            case "warden" -> WARDEN;
            case "players", "playerkills", "player" -> PLAYERS;
            case "trades", "trade" -> TRADES;
            case "xporbs", "xp", "orbs" -> XP_ORBS;
            case "advancements", "advancement" -> ADVANCEMENTS;
            case "quests", "quest" -> QUESTS;
            case "bred", "breeding" -> BRED;
            case "tamed", "tame", "taming" -> TAMED;
            case "bestiary", "bestiaries" -> BESTIARY;
            case "enchants", "enchantments", "enchant" -> ENCHANTS;
            case "biomes", "biome" -> BIOMES;
            case "fishing", "fish" -> FISHING;
            case "night", "nightevents" -> NIGHT;
            case "chests", "chest" -> CHESTS;
            default -> null;
        };
    }
}
