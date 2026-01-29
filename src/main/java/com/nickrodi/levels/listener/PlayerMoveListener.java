package com.nickrodi.levels.listener;

import com.nickrodi.levels.model.PlayerData;
import com.nickrodi.levels.service.ActivityService;
import com.nickrodi.levels.service.ProgressionService;
import com.nickrodi.levels.service.WorldAccess;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerMoveListener implements Listener {
    private static final int BIOME_XP_OVERWORLD = 50;
    private static final int BIOME_XP_NETHER_END = 50;
    private static final int BIOME_XP_RARE_BONUS = 0;

    private static final Set<Biome> RARE_BIOMES = Set.of();

    private final ProgressionService progressionService;
    private final ActivityService activityService;
    private final WorldAccess worldAccess;
    private final Map<UUID, Biome> lastBiome = new HashMap<>();

    public PlayerMoveListener(ProgressionService progressionService, ActivityService activityService, WorldAccess worldAccess) {
        this.progressionService = progressionService;
        this.activityService = activityService;
        this.worldAccess = worldAccess;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        if (from.getWorld() == null || to.getWorld() == null) {
            return;
        }
        if (!from.getWorld().equals(to.getWorld())) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        double distance = from.distance(to);
        if (distance > 0.01) {
            activityService.markActive(uuid);
        }

        if (worldAccess.isAllowed(player)) {
            handleBiome(player, to);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastBiome.remove(event.getPlayer().getUniqueId());
    }

    private void handleBiome(Player player, Location to) {
        Biome current = to.getBlock().getBiome();
        UUID uuid = player.getUniqueId();
        Biome previous = lastBiome.get(uuid);
        if (previous != null && previous == current) {
            return;
        }
        lastBiome.put(uuid, current);

        PlayerData data = progressionService.getData(uuid);
        Set<String> visited = data.getBiomesVisited() == null
                ? new HashSet<>()
                : new HashSet<>(data.getBiomesVisited());

        String biomeKey = current.getKey().toString();
        if (visited.contains(biomeKey)) {
            return;
        }

        long xp = isNetherOrEnd(player.getWorld()) ? BIOME_XP_NETHER_END : BIOME_XP_OVERWORLD;
        if (RARE_BIOMES.contains(current)) {
            xp += BIOME_XP_RARE_BONUS;
        }
        progressionService.addXp(uuid, xp, "\"" + formatBiomeName(current) + "\" added to Biomes collection");
        data.setBiomesXpGained(data.getBiomesXpGained() + xp);
        visited.add(biomeKey);
        data.setBiomesVisited(new ArrayList<>(visited));
    }

    private boolean isNetherOrEnd(World world) {
        return world.getEnvironment() == World.Environment.NETHER
                || world.getEnvironment() == World.Environment.THE_END;
    }

    private String formatBiomeName(Biome biome) {
        String key = biome.getKey().getKey();
        String[] parts = key.toLowerCase(java.util.Locale.US).split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }
}
