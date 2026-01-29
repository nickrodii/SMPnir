package com.nickrodi.levels;

import com.nickrodi.levels.command.SlCommand;
import com.nickrodi.levels.command.CoordsCommand;
import com.nickrodi.levels.command.LeaderboardCommand;
import com.nickrodi.levels.command.QuestBookCommand;
import com.nickrodi.levels.listener.ActivityListener;
import com.nickrodi.levels.listener.AdvancementListener;
import com.nickrodi.levels.listener.AnvilListener;
import com.nickrodi.levels.listener.BlockListener;
import com.nickrodi.levels.listener.BestiaryListener;
import com.nickrodi.levels.listener.ChatFormatListener;
import com.nickrodi.levels.listener.CollectionsMenuListener;
import com.nickrodi.levels.listener.BreedTameListener;
import com.nickrodi.levels.listener.EnchantedBookListener;
import com.nickrodi.levels.listener.EnchantingListener;
import com.nickrodi.levels.listener.EntityDeathListener;
import com.nickrodi.levels.listener.EntitySpawnListener;
import com.nickrodi.levels.listener.ItemPickupListener;
import com.nickrodi.levels.listener.HungerListener;
import com.nickrodi.levels.listener.PlayerDeathListener;
import com.nickrodi.levels.listener.PlayerFishListener;
import com.nickrodi.levels.listener.PlayerJoinListener;
import com.nickrodi.levels.listener.PlayerMoveListener;
import com.nickrodi.levels.listener.PlayerQuitListener;
import com.nickrodi.levels.listener.PlayerRespawnListener;
import com.nickrodi.levels.listener.PlayerWorldListener;
import com.nickrodi.levels.listener.QuestListener;
import com.nickrodi.levels.listener.TradeListener;
import com.nickrodi.levels.listener.StructureLootListener;
import com.nickrodi.levels.listener.StatsMenuListener;
import com.nickrodi.levels.service.ActivityService;
import com.nickrodi.levels.service.BlockTrackerService;
import com.nickrodi.levels.service.BoardService;
import com.nickrodi.levels.service.ChatFormatService;
import com.nickrodi.levels.service.CollectionsMenuService;
import com.nickrodi.levels.service.LevelCurve;
import com.nickrodi.levels.service.HealthService;
import com.nickrodi.levels.service.HungerService;
import com.nickrodi.levels.service.ProgressionService;
import com.nickrodi.levels.service.QuestBookService;
import com.nickrodi.levels.service.QuestService;
import com.nickrodi.levels.service.StatDisplayService;
import com.nickrodi.levels.service.StatsMenuService;
import com.nickrodi.levels.service.StorageService;
import com.nickrodi.levels.service.StructureRewardService;
import com.nickrodi.levels.service.WorldAccess;
import com.nickrodi.levels.util.Keys;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public class LevelsPlugin extends JavaPlugin {
    private static final long PLAYTIME_INTERVAL_TICKS = 20L * 60L;
    private static final long AFK_TIMEOUT_MS = 15L * 60L * 1000L;

    private LevelCurve levelCurve;
    private HealthService healthService;
    private HungerService hungerService;
    private StorageService storageService;
    private ProgressionService progressionService;
    private QuestService questService;
    private ActivityService activityService;
    private BlockTrackerService blockTrackerService;
    private WorldAccess worldAccess;
    private QuestBookService questBookService;
    private StructureRewardService structureRewardService;
    private StatsMenuService statsMenuService;
    private CollectionsMenuService collectionsMenuService;
    private ChatFormatService chatFormatService;
    private StatDisplayService statDisplayService;
    private BoardService boardService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Keys.init(this);

        levelCurve = new LevelCurve();
        storageService = new StorageService(this);
        worldAccess = new WorldAccess(this);
        healthService = new HealthService(worldAccess);
        int minFood = getConfig().getInt("hunger.min-food", 20);
        int maxFood = getConfig().getInt("hunger.max-food", 20);
        hungerService = new HungerService(worldAccess, minFood, maxFood);
        progressionService = new ProgressionService(this, levelCurve, storageService, healthService, hungerService);
        questService = new QuestService(progressionService);
        boardService = new BoardService(this);
        boardService.load();
        questBookService = new QuestBookService(progressionService, levelCurve, healthService, hungerService, questService);
        statsMenuService = new StatsMenuService();
        collectionsMenuService = new CollectionsMenuService();
        chatFormatService = new ChatFormatService(progressionService);
        statDisplayService = new StatDisplayService(progressionService);
        activityService = new ActivityService();
        blockTrackerService = new BlockTrackerService(this);
        blockTrackerService.load();
        structureRewardService = new StructureRewardService(this);
        structureRewardService.load();

        getServer().getOnlinePlayers().forEach(player -> {
            var data = storageService.load(player.getUniqueId());
            if (data.getLastDeathAt() <= 0L) {
                data.setLastDeathAt(System.currentTimeMillis());
            }
            progressionService.cache(player.getUniqueId(), data);
            ChatFormatService.applyDisplayName(player, data.getLevel());
            healthService.apply(player, data.getLevel());
            hungerService.apply(player, data.getLevel());
        });

        getServer().getPluginManager().registerEvents(
                new PlayerJoinListener(this, storageService, progressionService, activityService, healthService, hungerService, statDisplayService),
                this
        );
        getServer().getPluginManager().registerEvents(
                new PlayerQuitListener(progressionService, activityService, statDisplayService),
                this
        );
        getServer().getPluginManager().registerEvents(
                new PlayerRespawnListener(this, progressionService, healthService),
                this
        );
        getServer().getPluginManager().registerEvents(
                new ActivityListener(this, activityService),
                this
        );
        getServer().getPluginManager().registerEvents(
                new PlayerMoveListener(progressionService, activityService, worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new PlayerWorldListener(this, progressionService, healthService, hungerService),
                this
        );
        getServer().getPluginManager().registerEvents(
                new BlockListener(progressionService, blockTrackerService, worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new EntitySpawnListener(worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new EntityDeathListener(progressionService, questService, worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new PlayerDeathListener(progressionService, questService, worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new PlayerFishListener(progressionService, worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new AdvancementListener(progressionService, worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new TradeListener(progressionService, worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new EnchantingListener(this, progressionService, worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new AnvilListener(this, progressionService, worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new BreedTameListener(progressionService, worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new EnchantedBookListener(progressionService, worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new ItemPickupListener(progressionService, worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new HungerListener(this, progressionService, hungerService),
                this
        );
        getServer().getPluginManager().registerEvents(
                new StructureLootListener(progressionService, blockTrackerService, structureRewardService, worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new StatsMenuListener(this, questBookService, statsMenuService),
                this
        );
        getServer().getPluginManager().registerEvents(
                new CollectionsMenuListener(this, questBookService, collectionsMenuService),
                this
        );
        getServer().getPluginManager().registerEvents(
                new BestiaryListener(progressionService, worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new QuestListener(progressionService, questService, worldAccess),
                this
        );
        getServer().getPluginManager().registerEvents(
                new ChatFormatListener(chatFormatService),
                this
        );

        getServer().getScheduler().runTaskTimer(
                this,
                this::awardPlaytimeXp,
                PLAYTIME_INTERVAL_TICKS,
                PLAYTIME_INTERVAL_TICKS
        );

        getServer().getScheduler().runTaskTimer(
                this,
                statDisplayService::refreshAll,
                20L,
                20L
        );

        getServer().getScheduler().runTaskTimer(
                this,
                this::refreshDisplayNames,
                20L,
                20L
        );

        PluginCommand slCommand = getCommand("nir");
        if (slCommand != null) {
            SlCommand slExecutor = new SlCommand(progressionService, levelCurve, storageService);
            slCommand.setExecutor(slExecutor);
            slCommand.setTabCompleter(slExecutor);
        } else {
            getLogger().log(Level.WARNING, () -> "Command 'nir' not found in plugin.yml.");
        }

        PluginCommand coordsCommand = getCommand("coords");
        if (coordsCommand != null) {
            coordsCommand.setExecutor(new CoordsCommand());
        } else {
            getLogger().log(Level.WARNING, () -> "Command 'coords' not found in plugin.yml.");
        }

        PluginCommand questBookCommand = getCommand("questbook");
        if (questBookCommand != null) {
            questBookCommand.setExecutor(
                    new QuestBookCommand(questBookService, statsMenuService, collectionsMenuService, statDisplayService)
            );
        } else {
            getLogger().log(Level.WARNING, () -> "Command 'questbook' not found in plugin.yml.");
        }

        PluginCommand leaderboardCommand = getCommand("leaderboard");
        if (leaderboardCommand != null) {
            leaderboardCommand.setExecutor(new LeaderboardCommand(storageService, levelCurve, progressionService));
        } else {
            getLogger().log(Level.WARNING, () -> "Command 'leaderboard' not found in plugin.yml.");
        }
    }

    private void refreshDisplayNames() {
        getServer().getOnlinePlayers().forEach(player -> {
            var data = progressionService.getData(player.getUniqueId());
            ChatFormatService.applyDisplayName(player, data.getLevel());
        });
    }

    @Override
    public void onDisable() {
        if (progressionService != null) {
            progressionService.saveAll();
        }
        if (blockTrackerService != null) {
            blockTrackerService.save();
        }
        if (structureRewardService != null) {
            structureRewardService.save();
        }
    }

    private void awardPlaytimeXp() {
        long now = System.currentTimeMillis();
        getServer().getOnlinePlayers().forEach(player -> {
            if (!activityService.isActive(player.getUniqueId(), now, AFK_TIMEOUT_MS)) {
                return;
            }
            var data = progressionService.getData(player.getUniqueId());
            double multiplier = progressionService.getSurvivalMultiplier(data, now);
            long xp = Math.round(20.0 * multiplier);
            if (xp > 0) {
                data.setActivePlaytimeMinutes(data.getActivePlaytimeMinutes() + 1);
                data.setPlaytimeXpGained(data.getPlaytimeXpGained() + xp);
                long bonus = Math.max(0L, xp - 20L);
                if (bonus > 0) {
                    data.setSurvivalBonusXp(data.getSurvivalBonusXp() + bonus);
                }
                progressionService.addXp(player.getUniqueId(), xp, "playtime");
            }
        });
    }
}
