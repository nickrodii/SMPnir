package com.nickrodi.levels.listener;

import com.nickrodi.levels.service.ActivityService;
import com.nickrodi.levels.service.ProgressionService;
import com.nickrodi.levels.service.StatDisplayService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuitListener implements Listener {
    private final ProgressionService progressionService;
    private final ActivityService activityService;
    private final StatDisplayService statDisplayService;

    public PlayerQuitListener(
            ProgressionService progressionService,
            ActivityService activityService,
            StatDisplayService statDisplayService
    ) {
        this.progressionService = progressionService;
        this.activityService = activityService;
        this.statDisplayService = statDisplayService;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        progressionService.save(uuid);
        progressionService.remove(uuid);
        activityService.remove(uuid);
        statDisplayService.clear(event.getPlayer());
    }
}
