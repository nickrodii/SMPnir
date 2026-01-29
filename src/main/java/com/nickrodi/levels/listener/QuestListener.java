package com.nickrodi.levels.listener;

import com.nickrodi.levels.model.PlayerData;
import com.nickrodi.levels.service.ProgressionService;
import com.nickrodi.levels.service.QuestService;
import com.nickrodi.levels.service.WorldAccess;
import io.papermc.paper.math.Position;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.generator.structure.Structure;
import org.bukkit.util.StructureSearchResult;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class QuestListener implements Listener {
    private static final int TNT_REQUIRED = 9;
    private static final double TNT_RADIUS = 1.1;
    private static final double HALF_HEART = 1.0;
    private static final double WARDEN_PLAYER_RADIUS = 32.0;
    private static final int WARDEN_REQUIRED = 5;
    private static final int WARDEN_STRUCTURE_RADIUS_CHUNKS = 16;

    private final ProgressionService progressionService;
    private final QuestService questService;
    private final WorldAccess worldAccess;
    private final Map<UUID, Boolean> lowHealthPending = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> wardenCounts = new HashMap<>();

    public QuestListener(ProgressionService progressionService, QuestService questService, WorldAccess worldAccess) {
        this.progressionService = Objects.requireNonNull(progressionService, "progressionService");
        this.questService = Objects.requireNonNull(questService, "questService");
        this.worldAccess = Objects.requireNonNull(worldAccess, "worldAccess");
    }

    @EventHandler(ignoreCancelled = true)
    public void onTntExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed tnt)) {
            return;
        }
        Entity source = tnt.getSource();
        if (!(source instanceof Player player)) {
            return;
        }
        if (!worldAccess.isAllowed(player)) {
            return;
        }
        PlayerData data = progressionService.getData(player.getUniqueId());
        if (questService.isComplete(data, QuestService.TNT_3X3)) {
            return;
        }
        Location location = tnt.getLocation();
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        int count = 0;
        for (Entity entity : world.getNearbyEntities(location, TNT_RADIUS, TNT_RADIUS, TNT_RADIUS, e -> e instanceof TNTPrimed)) {
            count++;
        }
        if (count >= TNT_REQUIRED) {
            questService.complete(player, QuestService.TNT_3X3);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!worldAccess.isAllowed(player)) {
            return;
        }
        PlayerData data = progressionService.getData(player.getUniqueId());
        if (questService.isComplete(data, QuestService.HALF_HEART_FULL)) {
            return;
        }
        double healthAfter = player.getHealth() - event.getFinalDamage();
        if (healthAfter > 0.0 && healthAfter <= HALF_HEART) {
            lowHealthPending.put(player.getUniqueId(), Boolean.TRUE);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHeal(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (!lowHealthPending.containsKey(uuid)) {
            return;
        }
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = attribute != null ? attribute.getValue() : 20.0;
        double healthAfter = Math.min(maxHealth, player.getHealth() + event.getAmount());
        if (healthAfter >= maxHealth - 0.01) {
            questService.complete(player, QuestService.HALF_HEART_FULL);
            lowHealthPending.remove(uuid);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWardenSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.WARDEN) {
            return;
        }
        Location location = event.getLocation();
        if (!worldAccess.isAllowed(location)) {
            return;
        }
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        if (!world.hasStructureAt(Position.block(location), Structure.ANCIENT_CITY)) {
            return;
        }
        StructureSearchResult result = world.locateNearestStructure(
                location,
                Structure.ANCIENT_CITY,
                WARDEN_STRUCTURE_RADIUS_CHUNKS,
                false
        );
        if (result == null) {
            return;
        }
        Player player = findNearestPlayer(world, location);
        if (player == null) {
            return;
        }
        PlayerData data = progressionService.getData(player.getUniqueId());
        if (questService.isComplete(data, QuestService.WARDENS_ANCIENT_CITY)) {
            return;
        }
        String key = structureKey(world, result.getLocation());
        Map<String, Integer> counts = wardenCounts.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        int count = counts.getOrDefault(key, 0) + 1;
        counts.put(key, count);
        if (count >= WARDEN_REQUIRED) {
            questService.complete(player, QuestService.WARDENS_ANCIENT_CITY);
            counts.remove(key);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        lowHealthPending.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lowHealthPending.remove(uuid);
        wardenCounts.remove(uuid);
    }

    private Player findNearestPlayer(World world, Location location) {
        Collection<Player> players = world.getNearbyPlayers(location, WARDEN_PLAYER_RADIUS);
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Player player : players) {
            if (!worldAccess.isAllowed(player)) {
                continue;
            }
            double distance = player.getLocation().distanceSquared(location);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    private String structureKey(World world, Location location) {
        return world.getUID() + ":" + location.getBlockX() + ":" + location.getBlockZ();
    }
}
