package com.nickrodi.levels.service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockTrackerService {
    private static final String KEY_LIST = "placed";

    private final JavaPlugin plugin;
    private final File file;
    private final Set<String> placed = new HashSet<>();

    public BlockTrackerService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "placed-blocks.yml");
    }

    public void load() {
        placed.clear();
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            plugin.getLogger().log(Level.WARNING, () -> "Failed to create data folder for placed blocks.");
        }
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        placed.addAll(config.getStringList(KEY_LIST));
    }

    public void save() {
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            plugin.getLogger().log(Level.WARNING, () -> "Failed to create data folder for placed blocks.");
        }
        YamlConfiguration config = new YamlConfiguration();
        config.set(KEY_LIST, placed.stream().toList());
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, () -> "Failed to save placed blocks: " + e.getMessage());
        }
    }

    public void markPlaced(Block block) {
        placed.add(key(block));
    }

    public void unmarkPlaced(Block block) {
        placed.remove(key(block));
    }

    public boolean isPlaced(Block block) {
        return placed.contains(key(block));
    }

    public boolean isAnyPlaced(Collection<Block> blocks) {
        for (Block block : blocks) {
            if (isPlaced(block)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getPlacedSnapshot() {
        return Collections.unmodifiableSet(placed);
    }

    private String key(Block block) {
        UUID worldId = block.getWorld().getUID();
        return worldId + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }
}
