package com.nickrodi.levels.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class BestiaryCatalog {
    public enum Group {
        MAIN,
        CATS,
        FROGS,
        HORSES,
        TROPICAL_FISH,
        VILLAGERS,
        FOXES,
        WOLVES
    }

    public record Entry(String id, String displayName, Group group) {
    }

    private static final List<Entry> MAIN_ENTRIES = List.of(
            entry("allay", "Allay", Group.MAIN),
            entry("armadillo", "Armadillo", Group.MAIN),
            entry("axolotl", "Axolotl", Group.MAIN),
            entry("bat", "Bat", Group.MAIN),
            entry("camel", "Camel", Group.MAIN),
            entry("camel_husk", "Camel Husk", Group.MAIN),
            entry("chicken", "Chicken", Group.MAIN),
            entry("cod", "Cod", Group.MAIN),
            entry("copper_golem", "Copper Golem", Group.MAIN),
            entry("cow", "Cow", Group.MAIN),
            entry("donkey", "Donkey", Group.MAIN),
            entry("glow_squid", "Glow Squid", Group.MAIN),
            entry("happy_ghast", "Happy Ghast", Group.MAIN),
            entry("mooshroom", "Mooshroom", Group.MAIN),
            entry("mule", "Mule", Group.MAIN),
            entry("ocelot", "Ocelot", Group.MAIN),
            entry("parrot", "Parrot", Group.MAIN),
            entry("pig", "Pig", Group.MAIN),
            entry("rabbit", "Rabbit", Group.MAIN),
            entry("salmon", "Salmon", Group.MAIN),
            entry("sheep", "Sheep", Group.MAIN),
            entry("skeleton_horse", "Skeleton Horse", Group.MAIN),
            entry("sniffer", "Sniffer", Group.MAIN),
            entry("snow_golem", "Snow Golem", Group.MAIN),
            entry("squid", "Squid", Group.MAIN),
            entry("strider", "Strider", Group.MAIN),
            entry("tadpole", "Tadpole", Group.MAIN),
            entry("turtle", "Turtle", Group.MAIN),
            entry("wandering_trader", "Wandering Trader", Group.MAIN),
            entry("zombie_horse", "Zombie Horse", Group.MAIN),
            entry("bee", "Bee", Group.MAIN),
            entry("cave_spider", "Cave Spider", Group.MAIN),
            entry("dolphin", "Dolphin", Group.MAIN),
            entry("drowned", "Drowned", Group.MAIN),
            entry("enderman", "Enderman", Group.MAIN),
            entry("goat", "Goat", Group.MAIN),
            entry("iron_golem", "Iron Golem", Group.MAIN),
            entry("llama", "Llama", Group.MAIN),
            entry("nautilus", "Nautilus", Group.MAIN),
            entry("panda", "Panda", Group.MAIN),
            entry("piglin_trade", "Piglin", Group.MAIN),
            entry("polar_bear", "Polar Bear", Group.MAIN),
            entry("pufferfish", "Pufferfish", Group.MAIN),
            entry("spider", "Spider", Group.MAIN),
            entry("trader_llama", "Trader Llama", Group.MAIN),
            entry("zombie_nautilus", "Zombie Nautilus", Group.MAIN),
            entry("zombified_piglin", "Zombified Piglin", Group.MAIN),
            entry("blaze", "Blaze", Group.MAIN),
            entry("bogged", "Bogged", Group.MAIN),
            entry("breeze", "Breeze", Group.MAIN),
            entry("creaking", "Creaking", Group.MAIN),
            entry("creeper", "Creeper", Group.MAIN),
            entry("elder_guardian", "Elder Guardian", Group.MAIN),
            entry("ender_dragon", "Ender Dragon", Group.MAIN),
            entry("endermite", "Endermite", Group.MAIN),
            entry("evoker", "Evoker", Group.MAIN),
            entry("ghast", "Ghast", Group.MAIN),
            entry("guardian", "Guardian", Group.MAIN),
            entry("hoglin", "Hoglin", Group.MAIN),
            entry("husk", "Husk", Group.MAIN),
            entry("magma_cube", "Magma Cube", Group.MAIN),
            entry("parched", "Parched", Group.MAIN),
            entry("phantom", "Phantom", Group.MAIN),
            entry("piglin_brute", "Piglin Brute", Group.MAIN),
            entry("pillager", "Pillager", Group.MAIN),
            entry("ravager", "Ravager", Group.MAIN),
            entry("shulker", "Shulker", Group.MAIN),
            entry("silverfish", "Silverfish", Group.MAIN),
            entry("skeleton", "Skeleton", Group.MAIN),
            entry("slime", "Slime", Group.MAIN),
            entry("stray", "Stray", Group.MAIN),
            entry("vex", "Vex", Group.MAIN),
            entry("vindicator", "Vindicator", Group.MAIN),
            entry("warden", "Warden", Group.MAIN),
            entry("witch", "Witch", Group.MAIN),
            entry("wither", "Wither", Group.MAIN),
            entry("wither_skeleton", "Wither Skeleton", Group.MAIN),
            entry("zoglin", "Zoglin", Group.MAIN),
            entry("zombie", "Zombie", Group.MAIN),
            entry("zombie_villager", "Zombie Villager", Group.MAIN)
    );

    private static final List<Entry> CAT_ENTRIES = List.of(
            entry("cat_black", "Black", Group.CATS),
            entry("cat_british_shorthair", "British Shorthair", Group.CATS),
            entry("cat_calico", "Calico", Group.CATS),
            entry("cat_jellie", "Jellie", Group.CATS),
            entry("cat_persian", "Persian", Group.CATS),
            entry("cat_ragdoll", "Ragdoll", Group.CATS),
            entry("cat_red", "Red", Group.CATS),
            entry("cat_siamese", "Siamese", Group.CATS),
            entry("cat_tabby", "Tabby", Group.CATS),
            entry("cat_tuxedo", "Tuxedo", Group.CATS),
            entry("cat_white", "White", Group.CATS)
    );

    private static final List<Entry> FROG_ENTRIES = List.of(
            entry("frog_temperate", "Temperate", Group.FROGS),
            entry("frog_warm", "Warm", Group.FROGS),
            entry("frog_cold", "Cold", Group.FROGS)
    );

    private static final List<Entry> HORSE_ENTRIES = List.of(
            entry("horse_white_stockings", "White Stockings", Group.HORSES),
            entry("horse_white_field", "White Field", Group.HORSES),
            entry("horse_white_spotted", "White Spotted", Group.HORSES),
            entry("horse_black_dotted", "Black Dotted", Group.HORSES)
    );

    private static final List<Entry> FOX_ENTRIES = List.of(
            entry("fox_red", "Fox", Group.FOXES),
            entry("fox_snow", "Snow Fox", Group.FOXES)
    );

    private static final List<Entry> WOLF_ENTRIES = List.of(
            entry("wolf_woods", "Woods Wolf", Group.WOLVES),
            entry("wolf_ashen", "Ashen Wolf", Group.WOLVES),
            entry("wolf_black", "Black Wolf", Group.WOLVES),
            entry("wolf_chestnut", "Chestnut Wolf", Group.WOLVES),
            entry("wolf_rusty", "Rusty Wolf", Group.WOLVES),
            entry("wolf_spotted", "Spotted Wolf", Group.WOLVES),
            entry("wolf_striped", "Striped Wolf", Group.WOLVES),
            entry("wolf_snowy", "Snowy Wolf", Group.WOLVES)
    );

    private static final List<Entry> VILLAGER_ENTRIES = List.of(
            entry("villager_unemployed", "Unemployed", Group.VILLAGERS),
            entry("villager_nitwit", "Nitwit", Group.VILLAGERS),
            entry("villager_armorer", "Armorer", Group.VILLAGERS),
            entry("villager_butcher", "Butcher", Group.VILLAGERS),
            entry("villager_cartographer", "Cartographer", Group.VILLAGERS),
            entry("villager_cleric", "Cleric", Group.VILLAGERS),
            entry("villager_farmer", "Farmer", Group.VILLAGERS),
            entry("villager_fisherman", "Fisherman", Group.VILLAGERS),
            entry("villager_fletcher", "Fletcher", Group.VILLAGERS),
            entry("villager_leatherworker", "Leatherworker", Group.VILLAGERS),
            entry("villager_librarian", "Librarian", Group.VILLAGERS),
            entry("villager_mason", "Mason", Group.VILLAGERS),
            entry("villager_shepherd", "Shepherd", Group.VILLAGERS),
            entry("villager_toolsmith", "Toolsmith", Group.VILLAGERS),
            entry("villager_weaponsmith", "Weaponsmith", Group.VILLAGERS)
    );

    private static final List<Entry> TROPICAL_FISH_ENTRIES = List.of(
            entry("tropical_anemone", "Anemone", Group.TROPICAL_FISH),
            entry("tropical_black_tang", "Black Tang", Group.TROPICAL_FISH),
            entry("tropical_blue_tang", "Blue Tang", Group.TROPICAL_FISH),
            entry("tropical_butterflyfish", "Butterflyfish", Group.TROPICAL_FISH),
            entry("tropical_cichlid", "Cichlid", Group.TROPICAL_FISH),
            entry("tropical_clownfish", "Clownfish", Group.TROPICAL_FISH),
            entry("tropical_cotton_candy_betta", "Cotton Candy Betta", Group.TROPICAL_FISH),
            entry("tropical_dottyback", "Dottyback", Group.TROPICAL_FISH),
            entry("tropical_emperor_red_snapper", "Emperor Red Snapper", Group.TROPICAL_FISH),
            entry("tropical_goatfish", "Goatfish", Group.TROPICAL_FISH),
            entry("tropical_moorish_idol", "Moorish Idol", Group.TROPICAL_FISH),
            entry("tropical_ornate_butterflyfish", "Ornate Butterflyfish", Group.TROPICAL_FISH),
            entry("tropical_parrotfish", "Parrotfish", Group.TROPICAL_FISH),
            entry("tropical_queen_angelfish", "Queen Angelfish", Group.TROPICAL_FISH),
            entry("tropical_red_cichlid", "Red Cichlid", Group.TROPICAL_FISH),
            entry("tropical_red_lipped_blenny", "Red Lipped Blenny", Group.TROPICAL_FISH),
            entry("tropical_red_snapper", "Red Snapper", Group.TROPICAL_FISH),
            entry("tropical_threadfin", "Threadfin", Group.TROPICAL_FISH),
            entry("tropical_tomato_clownfish", "Tomato Clownfish", Group.TROPICAL_FISH),
            entry("tropical_triggerfish", "Triggerfish", Group.TROPICAL_FISH),
            entry("tropical_yellowtail_parrotfish", "Yellowtail Parrotfish", Group.TROPICAL_FISH),
            entry("tropical_yellow_tang", "Yellow Tang", Group.TROPICAL_FISH)
    );

    private static Entry entry(String id, String name, Group group) {
        return new Entry(id, name, group);
    }

    public static List<Entry> entries() {
        return MAIN_ENTRIES;
    }

    public static List<Entry> allEntries() {
        List<Entry> all = new ArrayList<>();
        all.addAll(MAIN_ENTRIES);
        all.addAll(CAT_ENTRIES);
        all.addAll(FROG_ENTRIES);
        all.addAll(HORSE_ENTRIES);
        all.addAll(TROPICAL_FISH_ENTRIES);
        all.addAll(VILLAGER_ENTRIES);
        all.addAll(FOX_ENTRIES);
        all.addAll(WOLF_ENTRIES);
        return all;
    }

    public static List<Entry> cats() {
        return CAT_ENTRIES;
    }

    public static List<Entry> frogs() {
        return FROG_ENTRIES;
    }

    public static List<Entry> horses() {
        return HORSE_ENTRIES;
    }

    public static List<Entry> tropicalFish() {
        return TROPICAL_FISH_ENTRIES;
    }

    public static List<Entry> villagers() {
        return VILLAGER_ENTRIES;
    }

    public static List<Entry> foxes() {
        return FOX_ENTRIES;
    }

    public static List<Entry> wolves() {
        return WOLF_ENTRIES;
    }

    public static Map<String, Entry> entryById() {
        return allEntries().stream().collect(Collectors.toMap(Entry::id, entry -> entry));
    }
}
