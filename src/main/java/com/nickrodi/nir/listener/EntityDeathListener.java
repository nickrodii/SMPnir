package com.nickrodi.nir.listener;

import com.nickrodi.nir.model.PlayerData;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.QuestService;
import com.nickrodi.nir.service.WorldAccess;
import com.nickrodi.nir.util.Keys;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Set;

public class EntityDeathListener implements Listener {
    private static final int XP_PASSIVE = 10;
    private static final int XP_MONSTER = 60;
    private static final int XP_SILVERFISH = 4;
    private static final int XP_SPAWNER_MONSTER = 7;
    private static final int XP_ELITE = 120;
    private static final int XP_DRAGON = 5000;
    private static final int XP_WITHER = 3000;
    private static final int XP_WARDEN = 8000;
    private static final double WITHER_QUEST_RADIUS = 32.0;

    private static final Set<EntityType> ELITE_TYPES = EnumSet.of(
            EntityType.ENDERMAN,
            EntityType.BLAZE,
            EntityType.PIGLIN_BRUTE,
            EntityType.EVOKER,
            EntityType.RAVAGER,
            EntityType.SHULKER
    );

    private final ProgressionService progressionService;
    private final QuestService questService;
    private final WorldAccess worldAccess;

    public EntityDeathListener(ProgressionService progressionService, QuestService questService, WorldAccess worldAccess) {
        this.progressionService = progressionService;
        this.questService = questService;
        this.worldAccess = worldAccess;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            return;
        }
        Player killer = entity.getKiller();
        if (killer == null) {
            return;
        }
        if (!worldAccess.isAllowed(killer)) {
            return;
        }
        EntityType type = entity.getType();
        if (isFromSpawner(entity)) {
            if (ELITE_TYPES.contains(type) || entity instanceof Monster) {
                progressionService.addXp(killer.getUniqueId(), XP_SPAWNER_MONSTER, "kill");
                var data = progressionService.getData(killer.getUniqueId());
                data.setMonstersKilled(data.getMonstersKilled() + 1);
                data.setMonstersXpGained(data.getMonstersXpGained() + XP_SPAWNER_MONSTER);
            }
            return;
        }
        if (type == EntityType.ENDER_DRAGON) {
            progressionService.addXp(killer.getUniqueId(), XP_DRAGON, "boss");
            var data = progressionService.getData(killer.getUniqueId());
            data.setDragonKills(data.getDragonKills() + 1);
            data.setDragonXpGained(data.getDragonXpGained() + XP_DRAGON);
            if (killer.getWorld().equals(entity.getWorld())) {
                questService.complete(killer, QuestService.DRAGON_FIRST);
            }
            return;
        }
        if (type == EntityType.WITHER) {
            progressionService.addXp(killer.getUniqueId(), XP_WITHER, "boss");
            var data = progressionService.getData(killer.getUniqueId());
            data.setWitherKills(data.getWitherKills() + 1);
            data.setWitherXpGained(data.getWitherXpGained() + XP_WITHER);
            if (isNear(killer, entity, WITHER_QUEST_RADIUS)) {
                questService.complete(killer, QuestService.WITHER_FIRST);
            }
            return;
        }
        if (type == EntityType.WARDEN || entity instanceof Warden) {
            if (canAwardWarden(killer)) {
                progressionService.addXp(killer.getUniqueId(), XP_WARDEN, "boss");
                var data = progressionService.getData(killer.getUniqueId());
                data.setWardenKills(data.getWardenKills() + 1);
                data.setWardenXpGained(data.getWardenXpGained() + XP_WARDEN);
            }
            return;
        }

        if (type == EntityType.SILVERFISH) {
            progressionService.addXp(killer.getUniqueId(), XP_SILVERFISH, "kill");
            var data = progressionService.getData(killer.getUniqueId());
            data.setMonstersKilled(data.getMonstersKilled() + 1);
            data.setMonstersXpGained(data.getMonstersXpGained() + XP_SILVERFISH);
            return;
        }

        if (ELITE_TYPES.contains(type)) {
            progressionService.addXp(killer.getUniqueId(), XP_ELITE, "kill");
            var data = progressionService.getData(killer.getUniqueId());
            data.setMonstersKilled(data.getMonstersKilled() + 1);
            data.setMonstersXpGained(data.getMonstersXpGained() + XP_ELITE);
            return;
        }
        if (entity instanceof Monster) {
            progressionService.addXp(killer.getUniqueId(), XP_MONSTER, "kill");
            var data = progressionService.getData(killer.getUniqueId());
            data.setMonstersKilled(data.getMonstersKilled() + 1);
            data.setMonstersXpGained(data.getMonstersXpGained() + XP_MONSTER);
            return;
        }
        if (entity instanceof Animals || entity instanceof Ambient || entity instanceof WaterMob) {
            progressionService.addXp(killer.getUniqueId(), XP_PASSIVE, "kill");
            var data = progressionService.getData(killer.getUniqueId());
            data.setMobsKilled(data.getMobsKilled() + 1);
            data.setMobsXpGained(data.getMobsXpGained() + XP_PASSIVE);
        }
    }

    private boolean isFromSpawner(Entity entity) {
        PersistentDataContainer data = entity.getPersistentDataContainer();
        return data.has(Keys.SPAWNER_TAG, PersistentDataType.BYTE);
    }

    private boolean canAwardWarden(Player killer) {
        PlayerData data = progressionService.getData(killer.getUniqueId());
        long today = LocalDate.now(ZoneId.systemDefault()).toEpochDay();
        if (data.getLastWardenKillDay() != today) {
            data.setLastWardenKillDay(today);
            data.setWardenKillsToday(0);
        }
        if (data.getWardenKillsToday() >= 1) {
            return false;
        }
        data.setWardenKillsToday(data.getWardenKillsToday() + 1);
        return true;
    }

    private boolean isNear(Player player, Entity entity, double radius) {
        if (player == null || entity == null) {
            return false;
        }
        if (!player.getWorld().equals(entity.getWorld())) {
            return false;
        }
        return player.getLocation().distanceSquared(entity.getLocation()) <= radius * radius;
    }
}
