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
import net.kyori.adventure.text.format.TextDecoration;

public class PlayerJoinListener implements Listener {
    private static final String UPDATE_MESSAGE_VERSION = "0.1.4";
    private final JavaPlugin plugin;
    private final StorageService storageService;
    private final ProgressionService progressionService;
    private final ActivityService activityService;
    private final HealthService healthService;
    private final HungerService hungerService;
    private final StatDisplayService statDisplayService;
    private final WelcomeService welcomeService;
    private final BuildReviewService buildReviewService;

    public PlayerJoinListener(
            JavaPlugin plugin,
            StorageService storageService,
            ProgressionService progressionService,
            ActivityService activityService,
            HealthService healthService,
            HungerService hungerService,
            StatDisplayService statDisplayService,
            WelcomeService welcomeService,
            BuildReviewService buildReviewService
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.storageService = storageService;
        this.progressionService = progressionService;
        this.activityService = activityService;
        this.healthService = healthService;
        this.hungerService = hungerService;
        this.statDisplayService = statDisplayService;
        this.welcomeService = Objects.requireNonNull(welcomeService, "welcomeService");
        this.buildReviewService = Objects.requireNonNull(buildReviewService, "buildReviewService");
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

    private void maybeSendUpdateMessage(org.bukkit.entity.Player player, PlayerData data) {
        if (player == null || data == null) {
            return;
        }
        if (!progressionService.isUpdateNotesDebug()) {
            String lastSeen = data.getLastUpdateMessageVersion();
            if (!isVersionLess(lastSeen, UPDATE_MESSAGE_VERSION)) {
                return;
            }
        }
        Component header = Component.text("SMPnir 0.1.4 UPDATE NOTES", NamedTextColor.AQUA, TextDecoration.BOLD);
        Component divider = Component.text("----", NamedTextColor.DARK_GRAY);
        Component line1 = Component.text("You can now get XP from building!", NamedTextColor.GREEN);
        Component line2 = Component.text("Use ", NamedTextColor.GRAY)
                .append(Component.text("/build submit <id>", NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.text(" or ", NamedTextColor.GRAY))
                .append(Component.text("/build groupsubmit <id> <players>", NamedTextColor.YELLOW, TextDecoration.BOLD));
        Component line3 = Component.text("When evaluated, claim with ", NamedTextColor.GRAY)
                .append(Component.text("/build list", NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.text(" (click CLAIM).", NamedTextColor.GRAY));
        Component line4 = Component.text("Check total build XP with ", NamedTextColor.GRAY)
                .append(Component.text("/build xp", NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.text(".", NamedTextColor.GRAY));
        player.sendMessage(header);
        player.sendMessage(divider);
        player.sendMessage(Component.text(" "));
        player.sendMessage(line1);
        player.sendMessage(Component.text(" "));
        player.sendMessage(line2);
        player.sendMessage(line3);
        player.sendMessage(Component.text(" "));
        player.sendMessage(line4);
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
}
