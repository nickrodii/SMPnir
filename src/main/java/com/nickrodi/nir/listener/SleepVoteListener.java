package com.nickrodi.nir.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.TimeSkipEvent;

import com.nickrodi.nir.service.SleepVoteService;
import com.nickrodi.nir.service.WorldAccess;

public class SleepVoteListener implements Listener {
    private final SleepVoteService sleepVoteService;
    private final WorldAccess worldAccess;

    public SleepVoteListener(SleepVoteService sleepVoteService, WorldAccess worldAccess) {
        this.sleepVoteService = sleepVoteService;
        this.worldAccess = worldAccess;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }
        sleepVoteService.startVote(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBedLeave(PlayerBedLeaveEvent event) {
        sleepVoteService.handleBedLeave(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        sleepVoteService.handleJoin(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event) {
        sleepVoteService.handleQuit(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTimeSkip(TimeSkipEvent event) {
        if (event.getSkipReason() != TimeSkipEvent.SkipReason.NIGHT_SKIP) {
            return;
        }
        if (!worldAccess.isAllowed(event.getWorld()) || worldAccess.isNetherOrEnd(event.getWorld())) {
            return;
        }
        if (!sleepVoteService.isActive() && event.getWorld().getServer().getOnlinePlayers().size() <= 1) {
            return;
        }
        event.setCancelled(true);
        sleepVoteService.handleNightSkip(event.getWorld());
    }
}
