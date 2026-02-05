package com.nickrodi.nir.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import com.nickrodi.nir.service.WorldAccess;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class HealthService {
    private static final double DEFAULT_HEALTH = 20.0;

    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 100;
    private static final Component HEALTH_INCREASED = Component.text("Health increased!", NamedTextColor.GOLD);

    private final WorldAccess worldAccess;
    private final int minHearts;
    private final int maxHearts;

    public HealthService(WorldAccess worldAccess, int minHearts, int maxHearts) {
        this.worldAccess = worldAccess;
        int clampedMin = Math.max(1, minHearts);
        int clampedMax = Math.max(1, maxHearts);
        if (clampedMax < clampedMin) {
            clampedMax = clampedMin;
        }
        this.minHearts = clampedMin;
        this.maxHearts = clampedMax;
    }

    public void apply(Player player, int oldLevel, int newLevel) {
        if (player == null) {
            return;
        }
        if (!worldAccess.isAllowed(player)) {
            setMaxHealth(player, DEFAULT_HEALTH);
            return;
        }
        int oldHearts = heartsForLevel(oldLevel);
        int newHearts = heartsForLevel(newLevel);
        double newMax = newHearts * 2.0;
        setMaxHealth(player, newMax);
        if (newHearts > oldHearts) {
            player.sendMessage(HEALTH_INCREASED);
        }
    }

    public void apply(Player player, int level) {
        apply(player, level, level);
    }

    public void applyForWorld(Player player, int level, World world) {
        if (player == null || world == null) {
            return;
        }
        if (!worldAccess.isAllowed(world)) {
            setMaxHealth(player, DEFAULT_HEALTH);
            return;
        }
        int hearts = heartsForLevel(level);
        double maxHealth = hearts * 2.0;
        setMaxHealth(player, maxHealth);
    }

    public void applyAndHeal(Player player, int level) {
        if (player == null) {
            return;
        }
        if (!worldAccess.isAllowed(player)) {
            if (setMaxHealth(player, DEFAULT_HEALTH)) {
                player.setHealth(DEFAULT_HEALTH);
            }
            return;
        }
        int hearts = heartsForLevel(level);
        double maxHealth = hearts * 2.0;
        if (setMaxHealth(player, maxHealth)) {
            player.setHealth(maxHealth);
        }
    }

    public int heartsForLevel(int level) {
        int clamped = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
        if (clamped <= MIN_LEVEL) {
            return minHearts;
        }
        if (clamped >= MAX_LEVEL) {
            return maxHearts;
        }
        double t = (clamped - MIN_LEVEL) / (double) (MAX_LEVEL - MIN_LEVEL);
        return minHearts + (int) Math.floor((maxHearts - minHearts) * t);
    }

    private boolean setMaxHealth(Player player, double maxHealth) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) {
            return false;
        }
        attribute.setBaseValue(maxHealth);
        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
        return true;
    }
}
