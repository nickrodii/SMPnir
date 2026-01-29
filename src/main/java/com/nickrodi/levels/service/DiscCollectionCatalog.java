package com.nickrodi.levels.service;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public final class DiscCollectionCatalog {
    private static final List<DiscEntry> ENTRIES;
    private static final Map<Material, DiscEntry> BY_MATERIAL;

    static {
        List<DiscEntry> entries = new ArrayList<>();
        Map<Material, DiscEntry> byMaterial = new HashMap<>();
        for (Material material : Material.values()) {
            if (!material.isRecord()) {
                continue;
            }
            String displayName = formatName(material);
            DiscEntry entry = new DiscEntry(material.getKey().toString(), displayName, material);
            entries.add(entry);
            byMaterial.put(material, entry);
        }
        entries.sort(Comparator.comparing(DiscEntry::displayName, String.CASE_INSENSITIVE_ORDER));
        ENTRIES = List.copyOf(entries);
        BY_MATERIAL = Map.copyOf(byMaterial);
    }

    private DiscCollectionCatalog() {
    }

    public static List<DiscEntry> entries() {
        return ENTRIES;
    }

    public static DiscEntry byMaterial(Material material) {
        return BY_MATERIAL.get(material);
    }

    private static String formatName(Material material) {
        String name = material.name();
        String raw = name.startsWith("MUSIC_DISC_")
                ? name.substring("MUSIC_DISC_".length())
                : name;
        String[] parts = raw.toLowerCase(Locale.US).split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    public record DiscEntry(String id, String displayName, Material material) {
    }
}
