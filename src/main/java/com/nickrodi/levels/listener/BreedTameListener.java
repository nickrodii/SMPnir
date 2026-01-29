package com.nickrodi.levels.listener;

import com.nickrodi.levels.service.ProgressionService;
import com.nickrodi.levels.service.WorldAccess;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityTameEvent;

public class BreedTameListener implements Listener {
    private static final int BREED_XP = 20;
    private static final int TAME_XP = 50;

    private final ProgressionService progressionService;
    private final WorldAccess worldAccess;

    public BreedTameListener(ProgressionService progressionService, WorldAccess worldAccess) {
        this.progressionService = progressionService;
        this.worldAccess = worldAccess;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player player)) {
            return;
        }
        if (!worldAccess.isAllowed(player)) {
            return;
        }
        progressionService.addXp(player.getUniqueId(), BREED_XP, "breeding");
        var data = progressionService.getData(player.getUniqueId());
        data.setMobsBred(data.getMobsBred() + 1);
        data.setMobsBredXpGained(data.getMobsBredXpGained() + BREED_XP);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTame(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player player)) {
            return;
        }
        if (!worldAccess.isAllowed(player)) {
            return;
        }
        progressionService.addXp(player.getUniqueId(), TAME_XP, "taming");
        var data = progressionService.getData(player.getUniqueId());
        data.setMobsTamed(data.getMobsTamed() + 1);
        data.setMobsTamedXpGained(data.getMobsTamedXpGained() + TAME_XP);
    }
}
