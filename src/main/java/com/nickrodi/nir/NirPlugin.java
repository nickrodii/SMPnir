package com.nickrodi.nir;

import java.util.logging.Level;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import com.nickrodi.nir.command.CoordsCommand;
import com.nickrodi.nir.command.LeaderboardCommand;
import com.nickrodi.nir.command.NirCommand;
import com.nickrodi.nir.command.QuestBookCommand;
import com.nickrodi.nir.command.SmpHelpCommand;
import com.nickrodi.nir.command.SleepVoteCommand;
import com.nickrodi.nir.command.XpListCommand;
import com.nickrodi.nir.listener.ActivityListener;
import com.nickrodi.nir.listener.AdvancementListener;
import com.nickrodi.nir.listener.AnvilListener;
import com.nickrodi.nir.listener.BestiaryListener;
import com.nickrodi.nir.listener.BlockListener;
import com.nickrodi.nir.listener.BreedTameListener;
import com.nickrodi.nir.listener.ChatFormatListener;
import com.nickrodi.nir.listener.CollectionsMenuListener;
import com.nickrodi.nir.listener.DeathChestListener;
import com.nickrodi.nir.listener.EnchantedBookListener;
import com.nickrodi.nir.listener.EnchantingListener;
import com.nickrodi.nir.listener.EntityDeathListener;
import com.nickrodi.nir.listener.EntitySpawnListener;
import com.nickrodi.nir.listener.HungerListener;
import com.nickrodi.nir.listener.ItemPickupListener;
import com.nickrodi.nir.listener.PlayerDeathListener;
import com.nickrodi.nir.listener.PlayerFishListener;
import com.nickrodi.nir.listener.PlayerJoinListener;
import com.nickrodi.nir.listener.PlayerMoveListener;
import com.nickrodi.nir.listener.PlayerQuitListener;
import com.nickrodi.nir.listener.PlayerRespawnListener;
import com.nickrodi.nir.listener.PlayerWorldListener;
import com.nickrodi.nir.listener.QuestListener;
import com.nickrodi.nir.listener.SleepVoteListener;
import com.nickrodi.nir.listener.StatsMenuListener;
import com.nickrodi.nir.listener.StructureLootListener;
import com.nickrodi.nir.listener.TradeListener;
import com.nickrodi.nir.service.ActivityService;
import com.nickrodi.nir.service.BlockTrackerService;
import com.nickrodi.nir.service.BoardService;
import com.nickrodi.nir.service.ChatFormatService;
import com.nickrodi.nir.service.CollectionsMenuService;
import com.nickrodi.nir.service.DeathChestService;
import com.nickrodi.nir.service.HealthService;
import com.nickrodi.nir.service.HungerService;
import com.nickrodi.nir.service.LevelCurve;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.QuestBookService;
import com.nickrodi.nir.service.QuestService;
import com.nickrodi.nir.service.SleepVoteService;
import com.nickrodi.nir.service.StatDisplayService;
import com.nickrodi.nir.service.StatsMenuService;
import com.nickrodi.nir.service.StorageService;
import com.nickrodi.nir.service.StructureRewardService;
import com.nickrodi.nir.service.TabListService;
import com.nickrodi.nir.service.WelcomeService;
import com.nickrodi.nir.service.WorldAccess;
import com.nickrodi.nir.util.Keys;

public class NirPlugin extends JavaPlugin {
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
    private DeathChestService deathChestService;
    private SleepVoteService sleepVoteService;
    private TabListService tabListService;
    private WelcomeService welcomeService;

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
        deathChestService = new DeathChestService(this, worldAccess);
        deathChestService.start();
        sleepVoteService = new SleepVoteService(this, worldAccess);
        questBookService = new QuestBookService(progressionService, levelCurve, healthService, hungerService, questService);
        statsMenuService = new StatsMenuService();
        collectionsMenuService = new CollectionsMenuService();
        chatFormatService = new ChatFormatService(progressionService);
        statDisplayService = new StatDisplayService(progressionService);
        tabListService = new TabListService(progressionService, levelCurve);
        activityService = new ActivityService();
        welcomeService = new WelcomeService(this);
        blockTrackerService = new BlockTrackerService(this);
        blockTrackerService.load();
        structureRewardService = new StructureRewardService(this);
        structureRewardService.load();

        getServer().getOnlinePlayers().forEach(player -> {
            var data = storageService.load(player.getUniqueId());
            if (data.getLastDeathAt() <= 0L) {
                data.setLastDeathAt(System.currentTimeMillis());
                data.setLastDeathPlaytimeMinutes(data.getActivePlaytimeMinutes());
            }
            progressionService.cache(player.getUniqueId(), data);
            ChatFormatService.applyDisplayName(player, data.getLevel());
            healthService.apply(player, data.getLevel());
            hungerService.apply(player, data.getLevel());
        });

        getServer().getPluginManager().registerEvents(
                new PlayerJoinListener(
                        this,
                        storageService,
                        progressionService,
                        activityService,
                        healthService,
                        hungerService,
                        statDisplayService,
                        welcomeService
                ),
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
                new DeathChestListener(deathChestService, worldAccess, progressionService),
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
        getServer().getPluginManager().registerEvents(
                new SleepVoteListener(sleepVoteService, worldAccess),
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

        // NEW: update per-player tab header with their own level/progress
        getServer().getScheduler().runTaskTimer(
                this,
                this::refreshTabListHeaders,
                20L,
                20L
        );

        PluginCommand nirCommand = getCommand("nir");
        if (nirCommand != null) {
            NirCommand nirExecutor = new NirCommand(progressionService, levelCurve, storageService);
            nirCommand.setExecutor(nirExecutor);
            nirCommand.setTabCompleter(nirExecutor);
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
                    new QuestBookCommand(
                            questBookService,
                            statsMenuService,
                            collectionsMenuService,
                            statDisplayService,
                            progressionService,
                            storageService,
                            levelCurve
                    )
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

        PluginCommand sleepVoteCommand = getCommand("sleepvote");
        if (sleepVoteCommand != null) {
            sleepVoteCommand.setExecutor(new SleepVoteCommand(sleepVoteService));
        } else {
            getLogger().log(Level.WARNING, () -> "Command 'sleepvote' not found in plugin.yml.");
        }

        PluginCommand smphelpCommand = getCommand("smphelp");
        if (smphelpCommand != null) {
            smphelpCommand.setExecutor(new SmpHelpCommand(welcomeService));
        } else {
            getLogger().log(Level.WARNING, () -> "Command 'smphelp' not found in plugin.yml.");
        }

        PluginCommand xpListCommand = getCommand("xplist");
        if (xpListCommand != null) {
            xpListCommand.setExecutor(new XpListCommand());
        } else {
            getLogger().log(Level.WARNING, () -> "Command 'xplist' not found in plugin.yml.");
        }
    }

    private void refreshDisplayNames() {
        getServer().getOnlinePlayers().forEach(player -> {
            var data = progressionService.getData(player.getUniqueId());
            ChatFormatService.applyDisplayName(player, data.getLevel());
        });
    }

    private void refreshTabListHeaders() {
        if (tabListService == null) {
            return;
        }
        tabListService.refreshAll(getServer().getOnlinePlayers());
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
        if (deathChestService != null) {
            deathChestService.shutdown();
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
