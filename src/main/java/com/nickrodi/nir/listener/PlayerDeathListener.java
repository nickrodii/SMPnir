package com.nickrodi.nir.listener;

import com.nickrodi.nir.model.PlayerData;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.QuestService;
import com.nickrodi.nir.service.WorldAccess;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
    private static final int MAX_XP = 4800;
    private static final int MAX_HOURS = 20;

    private final ProgressionService progressionService;
    private final QuestService questService;
    private final WorldAccess worldAccess;

    public PlayerDeathListener(ProgressionService progressionService, QuestService questService, WorldAccess worldAccess) {
        this.progressionService = progressionService;
        this.questService = questService;
        this.worldAccess = worldAccess;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        long now = System.currentTimeMillis();

        PlayerData victimData = progressionService.getData(victim.getUniqueId());
        double hoursAlive = progressionService.getSurvivalHours(victimData, now);
        double ratio = Math.min(1.0, Math.max(0.0, hoursAlive / MAX_HOURS));
        int xp = (int) Math.round(MAX_XP * ratio);

        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId()) && worldAccess.isAllowed(killer)) {
            progressionService.addXp(killer.getUniqueId(), xp, "pvp");
            PlayerData killerData = progressionService.getData(killer.getUniqueId());
            killerData.setPlayerKills(killerData.getPlayerKills() + 1);
            killerData.setPlayerKillsXpGained(killerData.getPlayerKillsXpGained() + xp);
            if (ratio >= 1.0) {
                questService.complete(killer, QuestService.KILL_MAX_STREAK);
            }
        }

        victimData.setLastDeathAt(now);
        victimData.setLastDeathPlaytimeMinutes(victimData.getActivePlaytimeMinutes());
    }
}
