package com.nickrodi.nir.listener;

import com.nickrodi.nir.model.PlayerData;
import com.nickrodi.nir.service.ActivityService;
import com.nickrodi.nir.service.ChatFormatService;
import com.nickrodi.nir.service.HealthService;
import com.nickrodi.nir.service.HungerService;
import com.nickrodi.nir.service.StatDisplayService;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.StorageService;
import com.nickrodi.nir.service.WelcomeService;
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
    private final WelcomeService welcomeService;

    public PlayerJoinListener(
            JavaPlugin plugin,
            StorageService storageService,
            ProgressionService progressionService,
            ActivityService activityService,
            HealthService healthService,
            HungerService hungerService,
            StatDisplayService statDisplayService,
            WelcomeService welcomeService
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.storageService = storageService;
        this.progressionService = progressionService;
        this.activityService = activityService;
        this.healthService = healthService;
        this.hungerService = hungerService;
        this.statDisplayService = statDisplayService;
        this.welcomeService = Objects.requireNonNull(welcomeService, "welcomeService");
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
    }
}
