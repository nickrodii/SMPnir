package com.nickrodi.nir.listener;

import com.nickrodi.nir.service.ActivityService;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.StatDisplayService;
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
