package com.nickrodi.nir.service;

import org.bukkit.Material;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class FishingCollectionCatalog {
    private static final List<FishingEntry> ENTRIES = List.of(
            new FishingEntry("cod", "Raw Cod", Material.COD),
            new FishingEntry("salmon", "Raw Salmon", Material.SALMON),
            new FishingEntry("tropical_fish", "Tropical Fish", Material.TROPICAL_FISH),
            new FishingEntry("pufferfish", "Pufferfish", Material.PUFFERFISH),
            new FishingEntry("bow", "Bow", Material.BOW),
            new FishingEntry("enchanted_book", "Enchanted Book", Material.ENCHANTED_BOOK),
            new FishingEntry("fishing_rod", "Fishing Rod", Material.FISHING_ROD),
            new FishingEntry("name_tag", "Name Tag", Material.NAME_TAG),
            new FishingEntry("nautilus_shell", "Nautilus Shell", Material.NAUTILUS_SHELL),
            new FishingEntry("saddle", "Saddle", Material.SADDLE),
            new FishingEntry("lily_pad", "Lily Pad", Material.LILY_PAD),
            new FishingEntry("bowl", "Bowl", Material.BOWL),
            new FishingEntry("leather", "Leather", Material.LEATHER),
            new FishingEntry("leather_boots", "Leather Boots", Material.LEATHER_BOOTS),
            new FishingEntry("rotten_flesh", "Rotten Flesh", Material.ROTTEN_FLESH),
            new FishingEntry("stick", "Stick", Material.STICK),
            new FishingEntry("string", "String", Material.STRING),
            new FishingEntry("water_bottle", "Water Bottle", Material.POTION),
            new FishingEntry("bone", "Bone", Material.BONE),
            new FishingEntry("ink_sac", "Ink Sac", Material.INK_SAC),
            new FishingEntry("tripwire_hook", "Tripwire Hook", Material.TRIPWIRE_HOOK),
            new FishingEntry("bamboo", "Bamboo", Material.BAMBOO),
            new FishingEntry("cocoa_beans", "Cocoa Beans", Material.COCOA_BEANS)
    );

    private static final Map<Material, FishingEntry> BY_MATERIAL;

    static {
        Map<Material, FishingEntry> map = new EnumMap<>(Material.class);
        for (FishingEntry entry : ENTRIES) {
            map.put(entry.material(), entry);
        }
        BY_MATERIAL = Collections.unmodifiableMap(map);
    }

    private FishingCollectionCatalog() {
    }

    public static List<FishingEntry> entries() {
        return ENTRIES;
    }

    public static FishingEntry byMaterial(Material material) {
        return BY_MATERIAL.get(material);
    }

    public record FishingEntry(String id, String displayName, Material material) {
    }
}
