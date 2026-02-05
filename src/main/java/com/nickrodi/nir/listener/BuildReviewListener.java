package com.nickrodi.nir.listener;

import com.nickrodi.nir.service.BuildReviewSessionService;
import com.nickrodi.nir.service.BuildReviewSessionService.ReviewSession;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

public class BuildReviewListener implements Listener {
    private final BuildReviewSessionService reviewSessionService;

    public BuildReviewListener(BuildReviewSessionService reviewSessionService) {
        this.reviewSessionService = Objects.requireNonNull(reviewSessionService, "reviewSessionService");
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ReviewSession session = reviewSessionService.getSession(player);
        if (session == null) {
            return;
        }
        Location to = event.getTo();
        Location center = session.center();
        if (to == null || center == null) {
            return;
        }
        if (to.getWorld() == null || center.getWorld() == null) {
            return;
        }
        if (!to.getWorld().equals(center.getWorld())) {
            event.setTo(center);
            return;
        }
        double dx = to.getX() - center.getX();
        double dz = to.getZ() - center.getZ();
        double limit = session.radiusBlocks();
        if ((dx * dx + dz * dz) > (limit * limit)) {
            event.setTo(center);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        ReviewSession session = reviewSessionService.getSession(player);
        if (session == null) {
            return;
        }
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        reviewSessionService.clearSession(event.getPlayer());
    }
}
