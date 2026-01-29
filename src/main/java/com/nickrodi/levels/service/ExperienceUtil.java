package com.nickrodi.levels.service;

import org.bukkit.entity.Player;

public final class ExperienceUtil {
    private ExperienceUtil() {
    }

    public static int getTotalExperience(Player player) {
        if (player == null) {
            return 0;
        }
        return getTotalExperience(player.getLevel(), player.getExp());
    }

    public static int getTotalExperience(int level, float progress) {
        int base = expForLevel(level);
        int toNext = expToNextLevel(level);
        int progressPoints = (int) Math.round(progress * toNext);
        return base + Math.max(0, progressPoints);
    }

    public static int expForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level <= 16) {
            return level * level + (6 * level);
        }
        if (level <= 31) {
            long value = (5L * level * level) - (81L * level) + 720L;
            return (int) (value / 2L);
        }
        long value = (9L * level * level) - (325L * level) + 4440L;
        return (int) (value / 2L);
    }

    public static int expToNextLevel(int level) {
        if (level <= 15) {
            return (2 * level) + 7;
        }
        if (level <= 30) {
            return (5 * level) - 38;
        }
        return (9 * level) - 158;
    }
}
