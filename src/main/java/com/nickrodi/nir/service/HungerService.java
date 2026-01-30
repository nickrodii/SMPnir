package com.nickrodi.nir.service;

import com.nickrodi.nir.service.WorldAccess;
import org.bukkit.entity.Player;

public class HungerService {
    private final WorldAccess worldAccess;
    private final int minFood;
    private final int maxFood;

    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 100;

    public HungerService(WorldAccess worldAccess, int minFood, int maxFood) {
        this.worldAccess = worldAccess;
        int clampedMin = Math.max(0, Math.min(20, minFood));
        int clampedMax = Math.max(0, Math.min(20, maxFood));
        if (clampedMax < clampedMin) {
            clampedMax = clampedMin;
        }
        this.minFood = clampedMin;
        this.maxFood = clampedMax;
    }

    public void apply(Player player, int oldLevel, int newLevel) {
        if (player == null) {
            return;
        }
        int maxFood = maxFoodFor(player, newLevel);
        if (player.getFoodLevel() > maxFood) {
            player.setFoodLevel(maxFood);
        }
        if (player.getSaturation() > maxFood) {
            player.setSaturation(maxFood);
        }
    }

    public void apply(Player player, int level) {
        apply(player, level, level);
    }

    public int maxFoodFor(Player player, int level) {
        if (!worldAccess.isAllowed(player)) {
            return 20;
        }
        return maxFoodForLevel(level);
    }

    public int maxFoodForLevel(int level) {
        int clamped = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
        if (clamped <= MIN_LEVEL) {
            return minFood;
        }
        if (clamped >= MAX_LEVEL) {
            return maxFood;
        }
        double t = (clamped - MIN_LEVEL) / (double) (MAX_LEVEL - MIN_LEVEL);
        return minFood + (int) Math.floor((maxFood - minFood) * t);
    }

    public double changeMultiplierFor(Player player, int level) {
        if (!worldAccess.isAllowed(player)) {
            return 1.0;
        }
        int clamped = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
        if (clamped <= MIN_LEVEL) {
            return 1.0;
        }
        if (clamped >= MAX_LEVEL) {
            return 0.25;
        }
        double t = (clamped - MIN_LEVEL) / (double) (MAX_LEVEL - MIN_LEVEL);
        return 1.0 + (0.25 - 1.0) * t;
    }

    public int displayHungerFor(Player player, int level) {
        int baseFood = maxFoodFor(player, level);
        double bars = baseFood / 2.0;
        double multiplier = changeMultiplierFor(player, level);
        if (multiplier <= 0.0) {
            return (int) Math.round(bars);
        }
        return (int) Math.round(bars / multiplier);
    }

    public int minSprintFoodFor(Player player, int level) {
        if (!worldAccess.isAllowed(player)) {
            return 7;
        }
        double multiplier = changeMultiplierFor(player, level);
        double threshold = 6.0 + ((multiplier - 0.25) / 0.75);
        int rounded = (int) Math.round(threshold);
        return Math.max(6, Math.min(7, rounded));
    }
}
