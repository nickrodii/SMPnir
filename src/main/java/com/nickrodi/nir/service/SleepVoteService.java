package com.nickrodi.nir.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Sleep vote rules:
 * - A vote starts when ANY player successfully enters a bed.
 * - Broadcast: "{name} is trying to sleep. Skip to morning? [YES]" where [YES] is green, bold, clickable.
 * - ALL online players must vote YES.
 * - The sleeping player automatically votes YES.
 * - If the starter sleeper wakes up during the vote, it will NOT skip.
 * - To avoid chat spam: progress updates are shown via ACTION BAR, only when the count changes.
 *
 * IMPORTANT: when cancelling/passing, we CLEAR the vote BEFORE waking sleepers to avoid infinite recursion
 * from PlayerBedLeaveEvent -> handleBedLeave -> failVote -> wakeSleeping -> ...
 */
public class SleepVoteService {
    private static final long VOTE_TIMEOUT_TICKS = 20L * 120L; // 2 minutes

    private final JavaPlugin plugin;
    private final WorldAccess worldAccess;

    private SleepVote activeVote;

    public SleepVoteService(JavaPlugin plugin, WorldAccess worldAccess) {
        this.plugin = plugin;
        this.worldAccess = worldAccess;
    }

    public boolean isActive() {
        return activeVote != null;
    }

    /**
     * Called when a player enters a bed.
     * - If no vote is active, starts one and auto-votes YES for the sleeper.
     * - If a vote is already active, entering bed counts as an automatic YES.
     */
    public void startVote(Player sleeper) {
        if (sleeper == null) {
            return;
        }

        // If vote exists already: treat bed enter as an automatic YES.
        if (activeVote != null) {
            recordYes(sleeper);
            return;
        }

        World world = sleeper.getWorld();
        if (!worldAccess.isAllowed(world) || worldAccess.isNetherOrEnd(world)) {
            return;
        }
        // Don't start votes in clear daytime.
        if (world.isDayTime() && !world.hasStorm() && !world.isThundering()) {
            return;
        }

        Set<UUID> eligible = new HashSet<>();
        plugin.getServer().getOnlinePlayers().forEach(p -> {
            if (isEligible(p, world)) {
                eligible.add(p.getUniqueId());
            }
        });
        if (eligible.size() <= 1) {
            return;
        }

        activeVote = new SleepVote(world, sleeper.getUniqueId(), eligible);

        // Sleeper automatically votes YES.
        activeVote.votes.put(sleeper.getUniqueId(), true);

        activeVote.timeout = plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> failVote("Vote timed out."),
                VOTE_TIMEOUT_TICKS
        );

        broadcastVoteStart(sleeper);
        sendProgressIfChanged();

        // BedEnterEvent can fire BEFORE isSleeping() becomes true on Paper.
        // Delay initial completion checks by 1 tick.
        plugin.getServer().getScheduler().runTask(plugin, this::checkCompletion);
    }

    /**
     * Records a YES vote for the player.
     */
    public void recordYes(Player player) {
        if (player == null) {
            return;
        }
        if (activeVote == null) {
            player.sendMessage(Component.text("There is no active sleep vote.", NamedTextColor.GRAY));
            return;
        }
        if (!activeVote.eligible.contains(player.getUniqueId())) {
            player.sendMessage(Component.text("You are not eligible for this vote.", NamedTextColor.GRAY));
            return;
        }

        boolean wasYes = activeVote.votes.containsKey(player.getUniqueId());
        activeVote.votes.put(player.getUniqueId(), true);

        if (!wasYes) {
            sendProgressIfChanged();
        }
        checkCompletion();
    }

    /**
     * If the starter sleeper wakes up during the vote, it will NOT skip.
     */
    public void handleBedLeave(Player player) {
        if (activeVote == null || player == null) {
            return;
        }
        if (player.getUniqueId().equals(activeVote.starter)) {
            failVote("Vote cancelled (sleeper woke up).");
        }
    }

    /**
     * If someone joins mid-vote, they must also vote YES ("ALL players in server").
     */
    public void handleJoin(Player player) {
        if (activeVote == null || player == null) {
            return;
        }
        if (!isEligible(player, activeVote.world)) {
            return;
        }
        UUID id = player.getUniqueId();
        if (activeVote.eligible.add(id)) {
            sendVotePrompt(player);
            sendProgressIfChanged();
        }
    }

    public void handleQuit(Player player) {
        if (activeVote == null || player == null) {
            return;
        }
        UUID id = player.getUniqueId();
        if (id.equals(activeVote.starter)) {
            failVote("Vote cancelled (sleeper left).");
            return;
        }
        if (activeVote.eligible.remove(id)) {
            activeVote.votes.remove(id);
            sendProgressIfChanged();
            checkCompletion();
        }
    }

    public void handleWorldChange(Player player, World from, World to) {
        if (activeVote == null || player == null) {
            return;
        }
        UUID id = player.getUniqueId();
        boolean wasEligible = activeVote.eligible.contains(id);
        boolean nowEligible = to != null && to.equals(activeVote.world);
        if (wasEligible && !nowEligible) {
            if (id.equals(activeVote.starter)) {
                failVote("Vote cancelled (sleeper left the world).");
                return;
            }
            activeVote.eligible.remove(id);
            activeVote.votes.remove(id);
            sendProgressIfChanged();
            checkCompletion();
            return;
        }
        if (!wasEligible && nowEligible) {
            activeVote.eligible.add(id);
            sendVotePrompt(player);
            sendProgressIfChanged();
        }
    }

    /**
     * Listener cancels vanilla night skipping. If there isn't an active vote for this world,
     * wake sleepers so the night can't be skipped without unanimous YES.
     */
    public void handleNightSkip(World world) {
        if (world == null) {
            return;
        }
        if (activeVote == null || activeVote.world != world) {
            wakeSleeping(world);
        }
    }

    private void checkCompletion() {
        if (activeVote == null) {
            return;
        }

        Player starterPlayer = plugin.getServer().getPlayer(activeVote.starter);
        if (starterPlayer == null) {
            failVote("Vote cancelled (sleeper left).");
            return;
        }

        // Grace window to allow Paper to update isSleeping() after bed enter.
        if (!starterPlayer.isSleeping()) {
            long now = System.currentTimeMillis();
            if (now - activeVote.startedAtMs <= 2000L) {
                plugin.getServer().getScheduler().runTaskLater(plugin, this::checkCompletion, 1L);
                return;
            }
            failVote("Vote cancelled (sleeper is not sleeping).");
            return;
        }

        // Everyone online must have voted YES.
        if (activeVote.votes.size() < activeVote.eligible.size()) {
            return;
        }
        for (Boolean v : activeVote.votes.values()) {
            if (v == null || !v) {
                failVote("Vote failed.");
                return;
            }
        }

        // Must have at least one sleeping player in that world.
        if (!hasSleepingPlayer(activeVote.world)) {
            failVote("No one is sleeping.");
            return;
        }

        // PASS: clear vote FIRST, then do world changes and wake players.
        World world = activeVote.world;
        Set<UUID> recipients = new HashSet<>(activeVote.eligible);
        clearVote();

        skipToMorning(world);
        sendToPlayers(recipients, Component.text("Sleep vote passed. Skipping to morning.", NamedTextColor.GOLD));
    }

    private void skipToMorning(World world) {
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(0);
        world.setThunderDuration(0);
        world.setTime(1000L);
        wakeSleeping(world);
    }

    private void wakeSleeping(World world) {
        world.getPlayers().forEach(p -> {
            if (p.isSleeping()) {
                p.wakeup(true);
            }
        });
    }

    private boolean hasSleepingPlayer(World world) {
        return world.getPlayers().stream().anyMatch(Player::isSleeping);
    }

    private void failVote(String reason) {
        if (activeVote == null) {
            return;
        }

        // CANCEL: clear vote FIRST to prevent infinite recursion via bed leave events.
        World world = activeVote.world;
        Set<UUID> recipients = new HashSet<>(activeVote.eligible);
        clearVote();

        sendToPlayers(recipients, Component.text("Sleep vote cancelled. " + reason, NamedTextColor.RED));
        wakeSleeping(world);
    }

    private void clearVote() {
        if (activeVote != null && activeVote.timeout != null) {
            activeVote.timeout.cancel();
        }
        activeVote = null;
    }

    private void broadcastVoteStart(Player sleeper) {
        Component msg = Component.text(
                sleeper.getName() + " is trying to sleep. Skip to morning? ",
                NamedTextColor.GOLD
        ).append(yesButton());

        sendToEligible(msg);
    }

    private void sendVotePrompt(Player player) {
        if (activeVote == null) {
            return;
        }
        Player sleeper = plugin.getServer().getPlayer(activeVote.starter);
        String name = sleeper == null ? "Someone" : sleeper.getName();
        Component msg = Component.text(
                name + " is trying to sleep. Skip to morning? ",
                NamedTextColor.GOLD
        ).append(yesButton());
        player.sendMessage(msg);
    }

    private Component yesButton() {
        return Component.text("[YES]", NamedTextColor.GREEN)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/sleepvote yes"));
    }

    /**
     * Progress updates via ACTION BAR to avoid chat spam.
     * Only sends when the count (or total) changes.
     */
    private void sendProgressIfChanged() {
        if (activeVote == null) {
            return;
        }

        int yes = activeVote.votes.size();
        int total = activeVote.eligible.size();

        if (yes == activeVote.lastSentYes && total == activeVote.lastSentTotal) {
            return;
        }
        activeVote.lastSentYes = yes;
        activeVote.lastSentTotal = total;

        Component bar = Component.text("Sleep vote: ", NamedTextColor.GRAY)
                .append(Component.text(yes + "/" + total, NamedTextColor.GREEN).decorate(TextDecoration.BOLD))
                .append(Component.text(" YES", NamedTextColor.GRAY));

        for (UUID uuid : activeVote.eligible) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                player.sendActionBar(bar);
            }
        }
    }

    private void sendToEligible(Component message) {
        if (activeVote == null) {
            return;
        }
        sendToPlayers(activeVote.eligible, message);
    }

    private void sendToPlayers(Set<UUID> recipients, Component message) {
        if (recipients == null || recipients.isEmpty()) {
            return;
        }
        for (UUID uuid : recipients) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    private boolean isEligible(Player player, World voteWorld) {
        if (player == null || voteWorld == null) {
            return false;
        }
        return player.getWorld().equals(voteWorld);
    }

    public int countEligible(World world) {
        if (world == null) {
            return 0;
        }
        int total = 0;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (isEligible(player, world)) {
                total++;
            }
        }
        return total;
    }

    private static final class SleepVote {
        private final World world;
        private final UUID starter;
        private final Set<UUID> eligible;
        private final Map<UUID, Boolean> votes = new HashMap<>();
        private BukkitTask timeout;

        private int lastSentYes = -1;
        private int lastSentTotal = -1;

        private final long startedAtMs = System.currentTimeMillis();

        private SleepVote(World world, UUID starter, Set<UUID> eligible) {
            this.world = world;
            this.starter = starter;
            this.eligible = eligible;
        }
    }
}
