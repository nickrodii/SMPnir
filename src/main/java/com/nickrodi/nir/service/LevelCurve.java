package com.nickrodi.nir.service;

public class LevelCurve {
    private static final int MAX_LEVEL = 100;
    private static final double EXPONENT = 1.35;
    private static final double CURVE_A = 0.837124916161829;
    private static final double CURVE_B = 0.007275362752072346;
    private static final double XP_PER_HOUR = 10000.0;

    private final long[] xpToNext;
    private final long[] totalXpForLevel;

    public LevelCurve() {
        this.xpToNext = new long[MAX_LEVEL + 1];
        this.totalXpForLevel = new long[MAX_LEVEL + 2];
        precompute();
    }

    private void precompute() {
        totalXpForLevel[1] = 0L;
        for (int level = 1; level <= MAX_LEVEL; level++) {
            long xp = Math.round(XP_PER_HOUR * (CURVE_A + (CURVE_B * Math.pow(level, EXPONENT))));
            xpToNext[level] = xp;
            totalXpForLevel[level + 1] = totalXpForLevel[level] + xp;
        }
    }

    public int getMaxLevel() {
        return MAX_LEVEL;
    }

    public long getXpToNext(int level) {
        if (level >= MAX_LEVEL) {
            return 0L;
        }
        if (level < 1) {
            return xpToNext[1];
        }
        return xpToNext[level];
    }

    public long getTotalXpForLevel(int level) {
        if (level <= 1) {
            return 0L;
        }
        if (level > MAX_LEVEL + 1) {
            return totalXpForLevel[MAX_LEVEL + 1];
        }
        return totalXpForLevel[level];
    }

    public int getLevelForTotalXp(long totalXp) {
        int level = 1;
        for (int next = 2; next <= MAX_LEVEL + 1; next++) {
            if (totalXp < totalXpForLevel[next]) {
                break;
            }
            level = next;
        }
        if (level > MAX_LEVEL) {
            return MAX_LEVEL;
        }
        return level;
    }

    public double getProgress(int level, long totalXp) {
        if (level >= MAX_LEVEL) {
            return 1.0;
        }
        long current = getTotalXpForLevel(level);
        long next = getTotalXpForLevel(level + 1);
        if (next <= current) {
            return 1.0;
        }
        double progress = (double) (totalXp - current) / (double) (next - current);
        if (progress < 0.0) {
            return 0.0;
        }
        if (progress > 1.0) {
            return 1.0;
        }
        return progress;
    }
}
