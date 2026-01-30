package com.nickrodi.nir.service;

import com.nickrodi.nir.model.PlayerData;
import com.nickrodi.nir.service.BestiaryCatalog;
import com.nickrodi.nir.service.BestiaryService;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatDisplayService {
    private static final String OBJECTIVE_NAME = "smpnir_stat";
    private static final Component OBJECTIVE_TITLE = Component.text("", NamedTextColor.WHITE);

    private final ProgressionService progressionService;
    private final Map<UUID, StatDisplayType> selections = new ConcurrentHashMap<>();

    public StatDisplayService(ProgressionService progressionService) {
        this.progressionService = progressionService;
    }

    public void ensureDefault(Player player) {
        if (player == null) {
            return;
        }
        selections.putIfAbsent(player.getUniqueId(), StatDisplayType.PLAYTIME);
    }

    public void setDisplay(Player player, StatDisplayType type) {
        if (player == null || type == null) {
            return;
        }
        selections.put(player.getUniqueId(), type);
        refreshAll();
    }

    public void clear(Player player) {
        if (player == null) {
            return;
        }
        selections.remove(player.getUniqueId());
        Objective objective = ensureObjective();
        if (objective == null) {
            return;
        }
        objective.getScore(player.getName()).resetScore();
    }

    public void refreshAll() {
        Objective objective = ensureObjective();
        if (objective == null) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            StatDisplayType type = selections.get(online.getUniqueId());
            if (type == null) {
                continue;
            }
            int value = resolveStatValue(type, online.getUniqueId());
            var score = objective.getScore(online.getName());
            score.setScore(value);
            score.numberFormat(NumberFormat.fixed(labelFor(type, value)));
        }
    }

    private Objective ensureObjective() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            return null;
        }
        Scoreboard board = manager.getMainScoreboard();
        Objective objective = board.getObjective(OBJECTIVE_NAME);
        if (objective == null) {
            objective = board.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, OBJECTIVE_TITLE);
        }
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        objective.displayName(OBJECTIVE_TITLE);
        return objective;
    }

    private Component labelFor(StatDisplayType type, int value) {
        if (type == StatDisplayType.STREAK) {
            String text = String.format(java.util.Locale.US, "%.2f%% streak", value / 100.0);
            return Component.text(text, NamedTextColor.WHITE);
        }
        String label = switch (type) {
            case PLAYTIME -> "hours played";
            case ORES -> "ores mined";
            case CROPS -> "crops harvested";
            case MOBS -> "mobs killed";
            case MONSTERS -> "monsters killed";
            case DRAGON -> "dragon kills";
            case WITHER -> "wither kills";
            case WARDEN -> "warden kills";
            case PLAYERS -> "players killed";
            case TRADES -> "trades";
            case XP_ORBS -> "xp orbs used";
            case ADVANCEMENTS -> "advancements";
            case QUESTS -> "quests done";
            case BRED -> "mobs bred";
            case TAMED -> "mobs tamed";
            case BESTIARY -> "bestiary found";
            case ENCHANTS -> "enchants found";
            case BIOMES -> "biomes visited";
            case FISHING -> "fishing found";
            case NIGHT -> "night events";
            case CHESTS -> "chests opened";
            case STREAK -> "";
        };
        return Component.text(value + " " + label, NamedTextColor.WHITE);
    }

    private int resolveStatValue(StatDisplayType type, UUID uuid) {
        PlayerData data = progressionService.getData(uuid);
        return switch (type) {
            case PLAYTIME -> clampToInt(data.getActivePlaytimeMinutes() / 60L);
            case STREAK -> clampToInt(Math.round(progressionService.getSurvivalMultiplier(data, System.currentTimeMillis()) * 100.0));
            case ORES -> clampToInt(data.getOresMined());
            case CROPS -> clampToInt(data.getCropsHarvested());
            case MOBS -> clampToInt(data.getMobsKilled());
            case MONSTERS -> clampToInt(data.getMonstersKilled());
            case DRAGON -> clampToInt(data.getDragonKills());
            case WITHER -> clampToInt(data.getWitherKills());
            case WARDEN -> clampToInt(data.getWardenKills());
            case PLAYERS -> clampToInt(data.getPlayerKills());
            case TRADES -> clampToInt(data.getVillagerTrades());
            case XP_ORBS -> clampToInt(data.getVanillaXpSpent());
            case ADVANCEMENTS -> clampToInt(data.getAdvancementsDone());
            case QUESTS -> clampToInt(data.getQuestsDone());
            case BRED -> clampToInt(data.getMobsBred());
            case TAMED -> clampToInt(data.getMobsTamed());
            case BESTIARY -> clampToInt(BestiaryService.countFound(data, BestiaryCatalog.allEntries()));
            case ENCHANTS -> clampToInt(countFoundEnchants(data));
            case BIOMES -> clampToInt(countUnique(data.getBiomesVisited()));
            case FISHING -> clampToInt(countUnique(data.getFishingItemsFound()));
            case NIGHT -> clampToInt(data.getNightEventsFound());
            case CHESTS -> clampToInt(data.getStructureChestsOpened());
        };
    }

    private int clampToInt(long value) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, value));
    }

    private long countFoundEnchants(PlayerData data) {
        List<String> tiers = data.getEnchantmentTiersFound();
        if (tiers == null || tiers.isEmpty()) {
            return 0L;
        }
        return new HashSet<>(tiers).size();
    }

    private long countUnique(List<String> values) {
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        Set<String> unique = new HashSet<>(values);
        return unique.size();
    }
}


