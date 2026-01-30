package com.nickrodi.nir.service;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class StructureRewardService {
    private static final String KEY_LIST = "claimed";

    private final JavaPlugin plugin;
    private final File file;
    private final Set<String> claimed = new HashSet<>();

    public StructureRewardService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "structure-claims.yml");
    }

    public void load() {
        claimed.clear();
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            plugin.getLogger().log(Level.WARNING, () -> "Failed to create data folder for structure claims.");
        }
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        claimed.addAll(config.getStringList(KEY_LIST));
    }

    public void save() {
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            plugin.getLogger().log(Level.WARNING, () -> "Failed to create data folder for structure claims.");
        }
        YamlConfiguration config = new YamlConfiguration();
        config.set(KEY_LIST, claimed.stream().toList());
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, () -> "Failed to save structure claims: " + e.getMessage());
        }
    }

    public boolean isClaimed(String key) {
        return claimed.contains(key);
    }

    public void claim(String key) {
        claimed.add(key);
    }

    public String key(Location location) {
        UUID worldId = location.getWorld().getUID();
        return worldId + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }

    public String key(UUID uuid) {
        return "entity:" + uuid;
    }

    public Set<String> getSnapshot() {
        return Collections.unmodifiableSet(claimed);
    }
}
