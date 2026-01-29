package com.nickrodi.levels.listener;

import com.nickrodi.levels.service.ProgressionService;
import com.nickrodi.levels.service.WorldAccess;
import io.papermc.paper.advancement.AdvancementDisplay;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AdvancementListener implements Listener {
    private final ProgressionService progressionService;
    private final WorldAccess worldAccess;

    public AdvancementListener(ProgressionService progressionService, WorldAccess worldAccess) {
        this.progressionService = progressionService;
        this.worldAccess = worldAccess;
    }

    @EventHandler(ignoreCancelled = true)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        if (!worldAccess.isAllowed(event.getPlayer())) {
            return;
        }
        Advancement advancement = event.getAdvancement();
        AdvancementDisplay display = advancement.getDisplay();
        if (display == null) {
            return;
        }
        if (display.isHidden() || !display.doesShowToast()) {
            return;
        }
        AdvancementDisplay.Frame frame = display.frame();
        long xp = switch (frame) {
            case CHALLENGE -> 2000;
            case GOAL -> 750;
            default -> 250;
        };
        progressionService.addXp(event.getPlayer().getUniqueId(), xp, "advancement");
        var data = progressionService.getData(event.getPlayer().getUniqueId());
        data.setAdvancementsDone(data.getAdvancementsDone() + 1);
        data.setAdvancementsXpGained(data.getAdvancementsXpGained() + xp);
    }
}
