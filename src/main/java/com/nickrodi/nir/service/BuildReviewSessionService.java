package com.nickrodi.nir.service;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BuildReviewSessionService {
    private static final int DEFAULT_RADIUS_BLOCKS = 80;

    private final int radiusBlocks;
    private final Map<UUID, ReviewSession> sessions = new ConcurrentHashMap<>();

    public BuildReviewSessionService() {
        this(DEFAULT_RADIUS_BLOCKS);
    }

    public BuildReviewSessionService(int radiusBlocks) {
        this.radiusBlocks = Math.max(16, radiusBlocks);
    }

    public ReviewSession startSession(Player player, String buildId, String builderLabel, Location center) {
        if (player == null || center == null) {
            return null;
        }
        UUID uuid = player.getUniqueId();
        ReviewSession session = new ReviewSession(
                buildId,
                builderLabel,
                player.getLocation().clone(),
                player.getGameMode(),
                center.clone(),
                radiusBlocks
        );
        sessions.put(uuid, session);
        return session;
    }

    public ReviewSession getSession(Player player) {
        if (player == null) {
            return null;
        }
        return sessions.get(player.getUniqueId());
    }

    public ReviewSession getSession(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return sessions.get(uuid);
    }

    public boolean isReviewing(Player player) {
        return getSession(player) != null;
    }

    public boolean endSession(Player player) {
        if (player == null) {
            return false;
        }
        ReviewSession session = sessions.remove(player.getUniqueId());
        if (session == null) {
            return false;
        }
        if (session.origin() != null) {
            player.teleport(session.origin());
        }
        if (session.originMode() != null) {
            player.setGameMode(session.originMode());
        }
        return true;
    }

    public void clearSession(Player player) {
        if (player == null) {
            return;
        }
        sessions.remove(player.getUniqueId());
    }

    public void setPendingGrade(Player player, PendingGrade pendingGrade) {
        ReviewSession session = getSession(player);
        if (session == null) {
            return;
        }
        session.setPendingGrade(pendingGrade);
    }

    public PendingGrade getPendingGrade(Player player) {
        ReviewSession session = getSession(player);
        if (session == null) {
            return null;
        }
        return session.getPendingGrade();
    }

    public void clearPendingGrade(Player player) {
        ReviewSession session = getSession(player);
        if (session == null) {
            return;
        }
        session.setPendingGrade(null);
    }

    public static class ReviewSession {
        private final String buildId;
        private final String builderLabel;
        private final Location origin;
        private final GameMode originMode;
        private final Location center;
        private final int radiusBlocks;
        private PendingGrade pendingGrade;

        private ReviewSession(
                String buildId,
                String builderLabel,
                Location origin,
                GameMode originMode,
                Location center,
                int radiusBlocks
        ) {
            this.buildId = buildId == null ? "" : buildId;
            this.builderLabel = builderLabel == null ? "" : builderLabel;
            this.origin = origin;
            this.originMode = originMode;
            this.center = center;
            this.radiusBlocks = radiusBlocks;
        }

        public String buildId() {
            return buildId;
        }

        public String builderLabel() {
            return builderLabel;
        }

        public Location origin() {
            return origin;
        }

        public GameMode originMode() {
            return originMode;
        }

        public Location center() {
            return center;
        }

        public int radiusBlocks() {
            return radiusBlocks;
        }

        public PendingGrade getPendingGrade() {
            return pendingGrade;
        }

        public void setPendingGrade(PendingGrade pendingGrade) {
            this.pendingGrade = pendingGrade;
        }
    }

    public record PendingGrade(long xpTotal, double effort, double aesthetics, long bonus) {
    }
}
