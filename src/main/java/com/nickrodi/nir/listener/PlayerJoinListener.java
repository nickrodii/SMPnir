package com.nickrodi.nir.listener;

import java.util.Objects;

import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.nickrodi.nir.model.PlayerData;
import com.nickrodi.nir.service.ActivityService;
import com.nickrodi.nir.service.BuildReviewService;
import com.nickrodi.nir.service.ChatFormatService;
import com.nickrodi.nir.service.HealthService;
import com.nickrodi.nir.service.HungerService;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.StatDisplayService;
import com.nickrodi.nir.service.StorageService;
import com.nickrodi.nir.service.WelcomeService;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class PlayerJoinListener implements Listener {
    private static final String UPDATE_MESSAGE_VERSION = "0.1.5";
    private final JavaPlugin plugin;
    private final StorageService storageService;
    private final ProgressionService progressionService;
    private final ActivityService activityService;
    private final HealthService healthService;
    private final HungerService hungerService;
    private final StatDisplayService statDisplayService;
    private final WelcomeService welcomeService;
    private final BuildReviewService buildReviewService;
    private final boolean buildEnabled;

    public PlayerJoinListener(
            JavaPlugin plugin,
            StorageService storageService,
            ProgressionService progressionService,
            ActivityService activityService,
            HealthService healthService,
            HungerService hungerService,
            StatDisplayService statDisplayService,
            WelcomeService welcomeService,
            BuildReviewService buildReviewService,
            boolean buildEnabled
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.storageService = storageService;
        this.progressionService = progressionService;
        this.activityService = activityService;
        this.healthService = healthService;
        this.hungerService = hungerService;
        this.statDisplayService = statDisplayService;
        this.welcomeService = Objects.requireNonNull(welcomeService, "welcomeService");
        this.buildReviewService = buildReviewService;
        this.buildEnabled = buildEnabled;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerData data = storageService.load(event.getPlayer().getUniqueId());
        if (data.getLastDeathAt() <= 0L) {
            data.setLastDeathAt(System.currentTimeMillis());
        }
        progressionService.cache(event.getPlayer().getUniqueId(), data);
        statDisplayService.ensureDefault(event.getPlayer());
        statDisplayService.refreshAll();
        ChatFormatService.applyDisplayName(event.getPlayer(), data.getLevel());
        healthService.apply(event.getPlayer(), data.getLevel());
        hungerService.apply(event.getPlayer(), data.getLevel());
        if (!event.getPlayer().hasPlayedBefore()) {
            plugin.getServer().getScheduler().runTaskLater(
                    plugin,
                    () -> welcomeService.send(event.getPlayer()),
                    20L * 10L
            );
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            ChatFormatService.applyDisplayName(event.getPlayer(), data.getLevel());
            healthService.apply(event.getPlayer(), data.getLevel());
            hungerService.apply(event.getPlayer(), data.getLevel());
        });
        activityService.setLastActiveAt(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        if (buildEnabled && buildReviewService != null) {
            if (buildReviewService.hasUnclaimed(event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage(Component.text(
                        "You have unclaimed build rewards! Use /build list to claim.",
                        NamedTextColor.YELLOW
                ));
            }
            if (event.getPlayer().hasPermission("smpnir.admin")) {
                int pending = buildReviewService.listPending().size();
                if (pending > 0) {
                    event.getPlayer().sendMessage("There are " + pending + " build submissions waiting for review. Use /build submissions.");
                }
            }
            if (event.getPlayer().hasPlayedBefore()) {
                maybeSendUpdateMessage(event.getPlayer(), data);
            }
        }
    }

    private void maybeSendUpdateMessage(org.bukkit.entity.Player player, PlayerData data) {
        if (!buildEnabled) {
            return;
        }
        if (player == null || data == null) {
            return;
        }
        if (!progressionService.isUpdateNotesDebug()) {
            String lastSeen = data.getLastUpdateMessageVersion();
            if (!isVersionLess(lastSeen, UPDATE_MESSAGE_VERSION)) {
                return;
            }
        }
        Component header = Component.text()
                .append(Component.text("SMP", NamedTextColor.WHITE, TextDecoration.BOLD))
                .append(gradientText("nir", TextColor.color(0xE9DCFF), TextColor.color(0xD9F1FF), TextDecoration.BOLD))
                .append(Component.text(" 0.1.5 UPDATE NOTES", NamedTextColor.AQUA, TextDecoration.BOLD))
                .build();
        Component divider = Component.text("----", NamedTextColor.DARK_GRAY);
        Component line1 = Component.text("Bug fixes and admin QoL changes", NamedTextColor.GREEN);
        player.sendMessage(header);
        player.sendMessage(divider);
        player.sendMessage(Component.text(" "));
        player.sendMessage(line1);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.3f);
        if (!progressionService.isUpdateNotesDebug()) {
            data.setLastUpdateMessageVersion(UPDATE_MESSAGE_VERSION);
        }
    }

    private boolean isVersionLess(String current, String target) {
        int[] currentParts = parseVersion(current);
        int[] targetParts = parseVersion(target);
        int max = Math.max(currentParts.length, targetParts.length);
        for (int i = 0; i < max; i++) {
            int a = i < currentParts.length ? currentParts[i] : 0;
            int b = i < targetParts.length ? targetParts[i] : 0;
            if (a < b) {
                return true;
            }
            if (a > b) {
                return false;
            }
        }
        return false;
    }

    private int[] parseVersion(String version) {
        if (version == null || version.isBlank()) {
            return new int[] {0, 0, 0};
        }
        String[] parts = version.trim().split("\\.");
        int[] out = new int[Math.max(3, parts.length)];
        for (int i = 0; i < out.length; i++) {
            if (i >= parts.length) {
                out[i] = 0;
                continue;
            }
            try {
                out[i] = Integer.parseInt(parts[i].replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                out[i] = 0;
            }
        }
        return out;
    }

    private Component gradientText(String text, TextColor start, TextColor end, TextDecoration... decorations) {
        net.kyori.adventure.text.TextComponent.Builder builder = Component.text();
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
            builder.append(Component.text(String.valueOf(text.charAt(i)), color, decorations));
        }
        return builder.build();
    }
}
