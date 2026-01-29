package com.nickrodi.levels.service;

import com.nickrodi.levels.model.PlayerData;
import com.nickrodi.levels.service.ChatFormatService;
import com.nickrodi.levels.service.HealthService;
import com.nickrodi.levels.service.HungerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ProgressionService {
    private final JavaPlugin plugin;
    private final LevelCurve levelCurve;
    private final StorageService storageService;
    private final HealthService healthService;
    private final HungerService hungerService;
    private final Map<UUID, PlayerData> cache = new HashMap<>();
    private final Set<UUID> debugEnabled = new HashSet<>();
    private final Map<UUID, PendingXpBar> pendingXpBars = new HashMap<>();
    private static final long XP_ACTIONBAR_STACK_WINDOW_TICKS = 20L * 3L;
    private static final long BIG_XP_THRESHOLD = 1000L;
    private static final TextColor BIG_XP_GRADIENT_START = TextColor.color(0xD24CFF);
    private static final TextColor BIG_XP_GRADIENT_END = TextColor.color(0x7B2CFF);
    private static final TextColor XP_GRADIENT_START = TextColor.color(0xDDAA00);
    private static final TextColor XP_GRADIENT_END = TextColor.color(0xBE5C00);

    public ProgressionService(JavaPlugin plugin, LevelCurve levelCurve, StorageService storageService, HealthService healthService, HungerService hungerService) {
        this.plugin = plugin;
        this.levelCurve = levelCurve;
        this.storageService = storageService;
        this.healthService = healthService;
        this.hungerService = hungerService;
    }

    public PlayerData getData(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) {
            data = storageService.load(uuid);
            cache.put(uuid, data);
        }
        return data;
    }

    public void cache(UUID uuid, PlayerData data) {
        cache.put(uuid, data);
    }

    public void remove(UUID uuid) {
        cache.remove(uuid);
        debugEnabled.remove(uuid);
        PendingXpBar pending = pendingXpBars.remove(uuid);
        if (pending != null && pending.task != null) {
            pending.task.cancel();
        }
    }

    public void save(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data != null) {
            storageService.save(uuid, data);
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, PlayerData> entry : cache.entrySet()) {
            storageService.save(entry.getKey(), entry.getValue());
        }
    }

    public int addXp(UUID uuid, long amount, String reason) {
        if (amount <= 0L) {
            return 0;
        }

        PlayerData data = getData(uuid);
        int oldLevel = data.getLevel();
        long newTotal = data.getTotalXp() + amount;
        data.setTotalXp(newTotal);

        int newLevel = levelCurve.getLevelForTotalXp(newTotal);
        if (newLevel > levelCurve.getMaxLevel()) {
            newLevel = levelCurve.getMaxLevel();
        }

        int gained = newLevel - oldLevel;
        if (gained > 0) {
            for (int level = oldLevel + 1; level <= newLevel; level++) {
                notifyLevelUp(uuid, level, reason);
            }
            data.setLevel(newLevel);

            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                ChatFormatService.applyDisplayName(player, newLevel);
                healthService.apply(player, oldLevel, newLevel);
                hungerService.apply(player, oldLevel, newLevel);
            }
        }

        sendDebug(uuid, amount, reason);
        if (reason == null || !reason.equalsIgnoreCase("playtime")) {
            queueXpActionBar(uuid, amount, reason);
        }

        return gained;
    }

    public double getProgress(PlayerData data) {
        return levelCurve.getProgress(data.getLevel(), data.getTotalXp());
    }

    public double getSurvivalHours(PlayerData data, long now) {
        long lastDeathAt = data.getLastDeathAt();
        if (lastDeathAt <= 0L) {
            data.setLastDeathAt(now);
            return 0.0;
        }
        double hours = (now - lastDeathAt) / 3600000.0;
        return Math.max(0.0, hours);
    }

    public double getSurvivalMultiplier(PlayerData data, long now) {
        double hours = getSurvivalHours(data, now);
        double multiplier = 1.0 + (0.05 * hours);
        if (multiplier > 2.0) {
            return 2.0;
        }
        if (multiplier < 1.0) {
            return 1.0;
        }
        return multiplier;
    }

    public int setTotalXp(UUID uuid, long newTotal, String reason) {
        if (newTotal < 0L) {
            newTotal = 0L;
        }
        PlayerData data = getData(uuid);
        long oldTotal = data.getTotalXp();
        int oldLevel = data.getLevel();
        data.setTotalXp(newTotal);

        int newLevel = levelCurve.getLevelForTotalXp(newTotal);
        if (newLevel > levelCurve.getMaxLevel()) {
            newLevel = levelCurve.getMaxLevel();
        }
        data.setLevel(newLevel);

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            ChatFormatService.applyDisplayName(player, newLevel);
            healthService.apply(player, oldLevel, newLevel);
            hungerService.apply(player, oldLevel, newLevel);
        }

        long delta = newTotal - oldTotal;
        if (delta > 0) {
            sendDebug(uuid, delta, reason);
        }

        return newLevel - oldLevel;
    }

    public boolean toggleDebug(UUID uuid) {
        if (debugEnabled.contains(uuid)) {
            debugEnabled.remove(uuid);
            return false;
        }
        debugEnabled.add(uuid);
        return true;
    }

    public boolean isDebugEnabled(UUID uuid) {
        return debugEnabled.contains(uuid);
    }

    private void queueXpActionBar(UUID uuid, long amount, String reason) {
        if (amount <= 0L) {
            return;
        }
        PendingXpBar pending = pendingXpBars.get(uuid);
        if (pending == null) {
            pending = new PendingXpBar();
            pendingXpBars.put(uuid, pending);
        }
        pending.total += amount;
        pending.addReason(reason);
        if (amount >= BIG_XP_THRESHOLD) {
            pending.largeGain = true;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            boolean big = pending.largeGain;
            TextColor start = big ? BIG_XP_GRADIENT_START : XP_GRADIENT_START;
            TextColor end = big ? BIG_XP_GRADIENT_END : XP_GRADIENT_END;
            String suffix = pending.reasons.isEmpty()
                    ? ""
                    : " (" + String.join(", ", pending.reasons) + ")";
            Component message = gradientText("+" + pending.total + " xp" + suffix, start, end, big);
            player.sendActionBar(message);
            if (amount >= BIG_XP_THRESHOLD) {
                player.playSound(player.getLocation(), Sound.EVENT_MOB_EFFECT_RAID_OMEN, 1.0f, 1.0f);
            }
        }
        if (pending.task != null) {
            pending.task.cancel();
        }
        PendingXpBar finalPending = pending;
        pending.task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            PendingXpBar current = pendingXpBars.get(uuid);
            if (current == finalPending) {
                pendingXpBars.remove(uuid);
            }
        }, XP_ACTIONBAR_STACK_WINDOW_TICKS);
    }

    private Component gradientText(String text, TextColor start, TextColor end, boolean bold) {
        TextComponent.Builder builder = Component.text();
        int len = text.length();
        int startRgb = start.value();
        int endRgb = end.value();
        int startR = (startRgb >> 16) & 0xFF;
        int startG = (startRgb >> 8) & 0xFF;
        int startB = startRgb & 0xFF;
        int endR = (endRgb >> 16) & 0xFF;
        int endG = (endRgb >> 8) & 0xFF;
        int endB = endRgb & 0xFF;
        for (int i = 0; i < len; i++) {
            double t = len <= 1 ? 0.0 : (double) i / (len - 1);
            int r = (int) Math.round(startR + (endR - startR) * t);
            int g = (int) Math.round(startG + (endG - startG) * t);
            int b = (int) Math.round(startB + (endB - startB) * t);
            TextColor color = TextColor.color(r, g, b);
            Component ch = Component.text(String.valueOf(text.charAt(i)), color);
            if (bold) {
                ch = ch.decorate(TextDecoration.BOLD);
            }
            builder.append(ch);
        }
        return builder.build();
    }

    private void sendDebug(UUID uuid, long amount, String reason) {
        if (amount <= 0L) {
            return;
        }
        if (!debugEnabled.contains(uuid)) {
            return;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            String safeReason = (reason == null || reason.isBlank()) ? "unknown" : reason;
            player.sendMessage("XP +" + amount + " (" + safeReason + ")");
        }
    }

    private void notifyLevelUp(UUID uuid, int level, String reason) {
        String suffix = (reason == null || reason.isBlank()) ? "" : " (" + reason + ")";
        plugin.getLogger().info("Player " + uuid + " reached level " + level + suffix + ".");
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.sendMessage("You reached level " + level + "!");
            Component name = ChatFormatService.formatDisplayName(level, player.getName());
            Bukkit.getServer().broadcast(
                    Component.text()
                            .append(name)
                            .append(Component.text(" reached level " + level + "!", NamedTextColor.GOLD))
                            .build()
            );
            TextColor levelColor = ChatFormatService.nameColorForLevel(level);
            Title title = Title.title(
                    Component.text("LEVEL UP!", levelColor, TextDecoration.BOLD),
                    Component.text("Level " + level, levelColor),
                    Title.Times.times(Duration.ofMillis(250), Duration.ofMillis(1400), Duration.ofMillis(350))
            );
            player.showTitle(title);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
            player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 1.2, 0), 120, 0.6, 0.8, 0.6, 0.08);
        }
    }

    private static class PendingXpBar {
        private long total;
        private BukkitTask task;
        private boolean largeGain;
        private final java.util.LinkedHashSet<String> reasons = new java.util.LinkedHashSet<>();

        private void addReason(String reason) {
            String safeReason = (reason == null || reason.isBlank()) ? "unknown" : reason;
            reasons.add(safeReason);
        }
    }
}




