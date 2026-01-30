package com.nickrodi.nir.listener;

import com.nickrodi.nir.service.HealthService;
import com.nickrodi.nir.service.HungerService;
import com.nickrodi.nir.service.ProgressionService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class PlayerWorldListener implements Listener {
    private final JavaPlugin plugin;
    private final ProgressionService progressionService;
    private final HealthService healthService;
    private final HungerService hungerService;

    public PlayerWorldListener(JavaPlugin plugin, ProgressionService progressionService, HealthService healthService, HungerService hungerService) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.progressionService = Objects.requireNonNull(progressionService, "progressionService");
        this.healthService = Objects.requireNonNull(healthService, "healthService");
        this.hungerService = Objects.requireNonNull(hungerService, "hungerService");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        int level = progressionService.getData(player.getUniqueId()).getLevel();
        healthService.apply(player, level);
        hungerService.apply(player, level);

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            int freshLevel = progressionService.getData(player.getUniqueId()).getLevel();
            healthService.apply(player, freshLevel);
            hungerService.apply(player, freshLevel);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld() == event.getTo().getWorld()) {
            return;
        }
        Player player = event.getPlayer();
        int level = progressionService.getData(player.getUniqueId()).getLevel();
        healthService.applyForWorld(player, level, event.getTo().getWorld());
    }
}
