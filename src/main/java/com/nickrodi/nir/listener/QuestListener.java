package com.nickrodi.nir.listener;

import com.nickrodi.nir.model.PlayerData;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.QuestService;
import com.nickrodi.nir.service.WorldAccess;
import io.papermc.paper.math.Position;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.generator.structure.Structure;
import org.bukkit.util.StructureSearchResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class QuestListener implements Listener {
    private static final int TNT_REQUIRED = 9;
    private static final double TNT_RADIUS = 1.1;
    private static final double HALF_HEART = 1.0;
    private static final double WARDEN_PLAYER_RADIUS = 32.0;
    private static final int WARDEN_REQUIRED = 6;
    private static final int WARDEN_STRUCTURE_RADIUS_CHUNKS = 16;
    private static final double CAVES_CLIFFS_START_Y = 319.5;
    private static final double CAVES_CLIFFS_END_Y = -62.0;
    private static final double CAVES_CLIFFS_MIN_MLG_DROP = 200.0;

    private final ProgressionService progressionService;
    private final QuestService questService;
    private final WorldAccess worldAccess;
    private final Map<UUID, Boolean> lowHealthPending = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> wardenCounts = new HashMap<>();
    private final Map<UUID, FallQuestState> cavesCliffsFalls = new HashMap<>();
    private final Map<UUID, Boolean> lastOnGround = new HashMap<>();

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
        String key = structureKey(world, result.getLocation());
        List<Player> eligiblePlayers = findEligibleWardenPlayers(world, location, result, key);
        if (eligiblePlayers.isEmpty()) {
            return;
        }
        for (Player player : eligiblePlayers) {
            PlayerData data = progressionService.getData(player.getUniqueId());
            if (questService.isComplete(data, QuestService.WARDENS_ANCIENT_CITY)) {
                continue;
            }
            Map<String, Integer> counts = wardenCounts.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
            int count = counts.getOrDefault(key, 0) + 1;
            counts.put(key, count);
            if (count >= WARDEN_REQUIRED) {
                questService.complete(player, QuestService.WARDENS_ANCIENT_CITY);
                counts.remove(key);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!worldAccess.isAllowed(player) || worldAccess.isNetherOrEnd(player.getWorld())) {
            clearCavesCliffs(player.getUniqueId());
            return;
        }
        UUID uuid = player.getUniqueId();
        if (questService.isComplete(progressionService.getData(uuid), QuestService.CAVES_AND_CLIFFS)) {
            clearCavesCliffs(uuid);
            return;
        }
        boolean onGround = player.isOnGround();
        boolean wasOnGround = lastOnGround.getOrDefault(uuid, onGround);
        lastOnGround.put(uuid, onGround);
        if (wasOnGround && !onGround && to.getY() >= CAVES_CLIFFS_START_Y) {
            cavesCliffsFalls.put(uuid, new FallQuestState(to.getY(), player.getWorld().getUID()));
        }
        FallQuestState state = cavesCliffsFalls.get(uuid);
        if (state == null) {
            return;
        }
        if (!player.getWorld().getUID().equals(state.worldId)) {
            clearCavesCliffs(uuid);
            return;
        }
        if (onGround && to.getY() > CAVES_CLIFFS_END_Y) {
            clearCavesCliffs(uuid);
            return;
        }
        if (state.waterPlaced && to.getY() <= CAVES_CLIFFS_END_Y && isInWater(to)) {
            questService.complete(player, QuestService.CAVES_AND_CLIFFS);
            clearCavesCliffs(uuid);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.getBucket() != Material.WATER_BUCKET) {
            return;
        }
        Player player = event.getPlayer();
        if (!worldAccess.isAllowed(player) || worldAccess.isNetherOrEnd(player.getWorld())) {
            return;
        }
        if (player.isOnGround()) {
            return;
        }
        UUID uuid = player.getUniqueId();
        FallQuestState state = cavesCliffsFalls.get(uuid);
        if (state == null) {
            return;
        }
        if (!player.getWorld().getUID().equals(state.worldId)) {
            clearCavesCliffs(uuid);
            return;
        }
        double drop = state.startY - player.getLocation().getY();
        if (drop < CAVES_CLIFFS_MIN_MLG_DROP) {
            return;
        }
        state.waterPlaced = true;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        UUID uuid = event.getEntity().getUniqueId();
        lowHealthPending.remove(uuid);
        clearCavesCliffs(uuid);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lowHealthPending.remove(uuid);
        wardenCounts.remove(uuid);
        clearCavesCliffs(uuid);
    }

    private List<Player> findEligibleWardenPlayers(World world, Location spawn, StructureSearchResult cityResult, String cityKey) {
        List<Player> players = world.getPlayers();
        if (players.isEmpty()) {
            return List.of();
        }
        List<Player> eligible = new java.util.ArrayList<>();
        double nearRadiusSquared = WARDEN_PLAYER_RADIUS * WARDEN_PLAYER_RADIUS;
        for (Player player : players) {
            if (!worldAccess.isAllowed(player)) {
                continue;
            }
            Location playerLocation = player.getLocation();
            if (!playerLocation.getWorld().equals(world)) {
                continue;
            }
            if (playerLocation.distanceSquared(spawn) <= nearRadiusSquared) {
                eligible.add(player);
                continue;
            }
            if (!world.hasStructureAt(Position.block(playerLocation), Structure.ANCIENT_CITY)) {
                continue;
            }
            StructureSearchResult playerResult = world.locateNearestStructure(
                    playerLocation,
                    Structure.ANCIENT_CITY,
                    WARDEN_STRUCTURE_RADIUS_CHUNKS,
                    false
            );
            if (playerResult == null) {
                continue;
            }
            if (cityKey.equals(structureKey(world, playerResult.getLocation()))) {
                eligible.add(player);
            }
        }
        return eligible;
    }

    private String structureKey(World world, Location location) {
        return world.getUID() + ":" + location.getBlockX() + ":" + location.getBlockZ();
    }

    private void clearCavesCliffs(UUID uuid) {
        cavesCliffsFalls.remove(uuid);
        lastOnGround.remove(uuid);
    }

    private boolean isInWater(Location location) {
        return location.getBlock().getType() == Material.WATER;
    }

    private static final class FallQuestState {
        private final double startY;
        private final UUID worldId;
        private boolean waterPlaced;

        private FallQuestState(double startY, UUID worldId) {
            this.startY = startY;
            this.worldId = worldId;
        }
    }
}
