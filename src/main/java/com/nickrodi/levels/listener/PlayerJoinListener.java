package com.nickrodi.levels.listener;

import com.nickrodi.levels.model.PlayerData;
import com.nickrodi.levels.service.ActivityService;
import com.nickrodi.levels.service.ChatFormatService;
import com.nickrodi.levels.service.HealthService;
import com.nickrodi.levels.service.HungerService;
import com.nickrodi.levels.service.StatDisplayService;
import com.nickrodi.levels.service.ProgressionService;
import com.nickrodi.levels.service.StorageService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class PlayerJoinListener implements Listener {
    private final JavaPlugin plugin;
    private final StorageService storageService;
    private final ProgressionService progressionService;
    private final ActivityService activityService;
    private final HealthService healthService;
    private final HungerService hungerService;
    private final StatDisplayService statDisplayService;

    public PlayerJoinListener(
            JavaPlugin plugin,
            StorageService storageService,
            ProgressionService progressionService,
            ActivityService activityService,
            HealthService healthService,
            HungerService hungerService,
            StatDisplayService statDisplayService
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.storageService = storageService;
        this.progressionService = progressionService;
        this.activityService = activityService;
        this.healthService = healthService;
        this.hungerService = hungerService;
        this.statDisplayService = statDisplayService;
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
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            ChatFormatService.applyDisplayName(event.getPlayer(), data.getLevel());
            healthService.apply(event.getPlayer(), data.getLevel());
        hungerService.apply(event.getPlayer(), data.getLevel());
        });
        activityService.setLastActiveAt(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }
}
