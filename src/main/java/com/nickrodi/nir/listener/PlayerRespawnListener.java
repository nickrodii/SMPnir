package com.nickrodi.nir.listener;

import com.nickrodi.nir.service.HealthService;
import com.nickrodi.nir.service.ProgressionService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerRespawnListener implements Listener {
    private final JavaPlugin plugin;
    private final ProgressionService progressionService;
    private final HealthService healthService;

    public PlayerRespawnListener(JavaPlugin plugin, ProgressionService progressionService, HealthService healthService) {
        this.plugin = plugin;
        this.progressionService = progressionService;
        this.healthService = healthService;
    }

    @EventHandler(ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        int level = progressionService.getData(player.getUniqueId()).getLevel();
        plugin.getServer().getScheduler().runTask(
                plugin,
                () -> healthService.applyAndHeal(player, level)
        );
    }
}
