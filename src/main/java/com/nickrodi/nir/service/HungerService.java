package com.nickrodi.nir.service;

import com.nickrodi.nir.service.WorldAccess;
import org.bukkit.entity.Player;

public class HungerService {
    private final WorldAccess worldAccess;
    private final int minFood;
    private final int maxFood;
    private final int effectiveMin;
    private final int effectiveMax;

    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 100;

    public HungerService(WorldAccess worldAccess, int minFood, int maxFood, int effectiveMin, int effectiveMax) {
        this.worldAccess = worldAccess;
        int clampedMin = Math.max(0, Math.min(20, minFood));
        int clampedMax = Math.max(0, Math.min(20, maxFood));
        if (clampedMax < clampedMin) {
            clampedMax = clampedMin;
        }
        this.minFood = clampedMin;
        this.maxFood = clampedMax;
        int effMin = Math.max(1, effectiveMin);
        int effMax = Math.max(1, effectiveMax);
        if (effMax < effMin) {
            effMax = effMin;
        }
        this.effectiveMin = effMin;
        this.effectiveMax = effMax;
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
        int baseFood = maxFoodForLevel(level);
        double effective = effectiveFoodForLevel(level);
        if (effective <= 0.0) {
            return 1.0;
        }
        return baseFood / effective;
    }

    public int displayHungerFor(Player player, int level) {
        if (!worldAccess.isAllowed(player)) {
            return maxFoodFor(player, level);
        }
        double effective = effectiveFoodForLevel(level);
        if (effective <= 0.0) {
            return maxFoodFor(player, level);
        }
        return (int) Math.round(effective);
    }

    private double effectiveFoodForLevel(int level) {
        int clamped = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
        if (clamped <= MIN_LEVEL) {
            return effectiveMin;
        }
        if (clamped >= MAX_LEVEL) {
            return effectiveMax;
        }
        double t = (clamped - MIN_LEVEL) / (double) (MAX_LEVEL - MIN_LEVEL);
        return effectiveMin + (effectiveMax - effectiveMin) * t;
    }

    public int minSprintFoodFor(Player player, int level) {
        if (!worldAccess.isAllowed(player)) {
            return 7;
        }
        double multiplier = changeMultiplierFor(player, level);
        if (multiplier >= 1.0) {
            return 7;
        }
        double threshold = 6.0 * multiplier;
        int rounded = (int) Math.round(threshold);
        return Math.max(1, Math.min(6, rounded));
    }
}
