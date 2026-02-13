package com.nickrodi.nir.service;

import com.nickrodi.nir.model.PlayerData;
import com.nickrodi.nir.service.EnchantmentCatalog.EnchantCategory;
import com.nickrodi.nir.service.EnchantmentCatalog.EnchantEntry;
import com.nickrodi.nir.service.QuestService.QuestDefinition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class QuestBookService {
    private static final int LINE_WIDTH = 19;
    private static final int BAR_SEGMENTS = 18;
    private static final int QUEST_PAGE_LINES = 14;
    private static final int QUEST_HEADER_LINES = 3;
    private static final int QUEST_ENTRY_LINES = 2;
    private static final int QUEST_FOOTER_LINES = 2;
    private static final int TOTAL_FISHING = FishingCollectionCatalog.entries().size();
    private static final int TOTAL_DISCS = DiscCollectionCatalog.entries().size();
    private static final int TOTAL_BESTIARY = BestiaryCatalog.allEntries().size();
    private static final int TOTAL_NIGHT_EVENTS = 0;

    private static final TextColor COLOR_STATS_GRADIENT_START = TextColor.color(0x0096EC);
    private static final TextColor COLOR_STATS_GRADIENT_END = TextColor.color(0x0DDDC3);
    private static final TextColor COLOR_QUESTS_GRADIENT_START = TextColor.color(0xDDAA00);
    private static final TextColor COLOR_QUESTS_GRADIENT_END = TextColor.color(0xBE5C00);
    private static final TextColor COLOR_INFO_GRADIENT_START = TextColor.color(0x25D366);
    private static final TextColor COLOR_INFO_GRADIENT_END = TextColor.color(0x0FA958);
    private static final TextColor COLOR_HEALTH_GRADIENT_START = TextColor.color(0xFF4D4D);
    private static final TextColor COLOR_HEALTH_GRADIENT_END = TextColor.color(0xD32F2F);
    private static final TextColor COLOR_HUNGER_GRADIENT_START = TextColor.color(0xFFB347);
    private static final TextColor COLOR_HUNGER_GRADIENT_END = TextColor.color(0xF57C00);
    private static final TextColor COLOR_ALL_MOBS_GRADIENT_START = TextColor.color(0x8A8A8A);
    private static final TextColor COLOR_ALL_MOBS_GRADIENT_END = TextColor.color(0xD0D0D0);

    private static final NamedTextColor COLOR_TITLE = NamedTextColor.GRAY;
    private static final NamedTextColor COLOR_HINT = NamedTextColor.DARK_GRAY;
    private static final TextColor COLOR_STATS = NamedTextColor.DARK_PURPLE;
    private static final TextColor COLOR_QUESTS = TextColor.color(0xB08D00);
    private static final NamedTextColor COLOR_INCOMPLETE = NamedTextColor.GRAY;
    private static final TextColor COLOR_COMPLETE = TextColor.color(0xB08D00);
    private static final NamedTextColor COLOR_FILL = NamedTextColor.DARK_GREEN;
    private static final NamedTextColor COLOR_EMPTY = NamedTextColor.DARK_GRAY;

    private final ProgressionService progressionService;
    private final LevelCurve levelCurve;
    private final HealthService healthService;
    private final HungerService hungerService;
    private final QuestService questService;

    public QuestBookService(ProgressionService progressionService, LevelCurve levelCurve, HealthService healthService, HungerService hungerService, QuestService questService) {
        this.progressionService = progressionService;
        this.levelCurve = levelCurve;
        this.healthService = healthService;
        this.hungerService = hungerService;
        this.questService = questService;
    }

    public void openFor(Player player) {
        ItemStack book = buildBook(player);
        player.openBook(book);
    }

    public void openPlayerInfo(Player player) {
        ItemStack book = buildPlayerInfoBook(player);
        player.openBook(book);
    }

    public void openQuests(Player player) {
        ItemStack book = buildQuestBook(player);
        player.openBook(book);
    }

    public void openStatsSection(Player player, StatsSection section) {
        if (section == StatsSection.ENCHANTS) {
            openEnchantsOverview(player);
            return;
        }
        if (section == StatsSection.FISHING) {
            openFishingCollection(player);
            return;
        }
        if (section == StatsSection.DISCS) {
            openDiscsCollection(player);
            return;
        }
        if (section == StatsSection.BESTIARY) {
            openBestiaryCollection(player);
            return;
        }
        if (section == StatsSection.BIOMES) {
            openBiomesCollection(player);
            return;
        }
        ItemStack book = buildStatsBook(player, section);
        player.openBook(book);
    }

    public void openFishingCollection(Player player) {
        ItemStack book = buildFishingBook(player);
        player.openBook(book);
    }

    public void openDiscsCollection(Player player) {
        ItemStack book = buildDiscsBook(player);
        player.openBook(book);
    }

    public void openBestiaryCollection(Player player) {
        ItemStack book = buildBestiaryOverviewBook(player);
        player.openBook(book);
    }

    public void openBestiaryCategory(Player player, String category) {
        String key = category == null ? "" : category.toLowerCase(Locale.ROOT);
        switch (key) {
            case "all", "allmobs", "mobs", "main" -> player.openBook(buildBestiaryCategoryBook(player, "BESTIARY", BestiaryCatalog.allEntries(), "/questbook collections"));
            case "cats", "cat" -> player.openBook(buildBestiaryCategoryBook(player, "CATS", BestiaryCatalog.cats(), "/questbook collections"));
            case "frogs", "frog" -> player.openBook(buildBestiaryCategoryBook(player, "FROGS", BestiaryCatalog.frogs(), "/questbook collections"));
            case "horses", "horse" -> player.openBook(buildBestiaryCategoryBook(player, "HORSES", BestiaryCatalog.horses(), "/questbook collections"));
            case "tropical", "tropicalfish", "fish" -> player.openBook(buildBestiaryCategoryBook(player, "TROPICAL FISH", BestiaryCatalog.tropicalFish(), "/questbook collections"));
            case "villagers", "villager" -> player.openBook(buildBestiaryCategoryBook(player, "VILLAGERS", BestiaryCatalog.villagers(), "/questbook collections"));
            case "foxes", "fox" -> player.openBook(buildBestiaryCategoryBook(player, "FOXES", BestiaryCatalog.foxes(), "/questbook collections"));
            case "wolves", "wolf" -> player.openBook(buildBestiaryCategoryBook(player, "WOLVES", BestiaryCatalog.wolves(), "/questbook collections"));
            default -> player.openBook(buildBestiaryOverviewBook(player));
        }
    }

    public void openBiomesCollection(Player player) {
        ItemStack book = buildBiomesBook(player);
        player.openBook(book);
    }

    public void openEnchantsOverview(Player player) {
        ItemStack book = buildEnchantsOverviewBook(player);
        player.openBook(book);
    }

    public void openEnchantDetail(Player player, EnchantEntry entry) {
        if (entry == null || entry.maxLevel() <= 1) {
            openEnchantsOverview(player);
            return;
        }
        ItemStack book = buildEnchantDetailBook(player, entry);
        player.openBook(book);
    }

    private ItemStack buildBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("SMPnir");
        meta.setAuthor(player.getName());

        meta.pages(List.of(buildLandingPage(player)));
        book.setItemMeta(meta);
        return book;
    }

    private ItemStack buildPlayerInfoBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("SMPnir");
        meta.setAuthor(player.getName());

        meta.pages(List.of(buildPlayerInfoPage(player)));
        book.setItemMeta(meta);
        return book;
    }

    private ItemStack buildQuestBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("SMPnir");
        meta.setAuthor(player.getName());

        meta.pages(buildQuestPages(player));
        book.setItemMeta(meta);
        return book;
    }

    private ItemStack buildStatsBook(Player player, StatsSection section) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("SMPnir");
        meta.setAuthor(player.getName());

        Component page = switch (section) {
            case PLAYTIME -> buildPlaytimePage(player);
            case MINING -> buildMiningPage(player);
            case COMBAT -> buildCombatPage(player);
            case EXPLORATION -> buildExplorationPage(player);
            case COLLECTIONS -> buildCollectionsStatsPage(player);
            case EXTRA -> buildExtraPage(player);
            case MOBS -> buildMobsPage(player);
            case BESTIARY -> buildCollectionsStatsPage(player);
            case ENCHANTS -> throw new IllegalArgumentException("Enchants uses the collection book.");
            case BIOMES -> buildBiomesPage(player);
            case FISHING -> buildFishingPage(player);
            case NIGHT -> buildNightEventsPage(player);
            case DISCS -> buildCollectionsStatsPage(player);
        };

        meta.pages(List.of(page));
        book.setItemMeta(meta);
        return book;
    }

    private Component buildLandingPage(Player player) {
        TextComponent.Builder page = Component.text();
        PlayerData data = progressionService.getData(player.getUniqueId());
        appendLevelHeader(page, data);
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredGradientButton("STATISTICS", COLOR_STATS_GRADIENT_START, COLOR_STATS_GRADIENT_END, ClickEvent.runCommand("/questbook stats")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredGradientButton("COLLECTIONS", COLOR_STATS_GRADIENT_START, COLOR_STATS_GRADIENT_END, ClickEvent.runCommand("/questbook collections")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredGradientButton("PLAYER INFO", COLOR_INFO_GRADIENT_START, COLOR_INFO_GRADIENT_END, ClickEvent.runCommand("/questbook info")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredGradientButton("QUESTS", COLOR_QUESTS_GRADIENT_START, COLOR_QUESTS_GRADIENT_END, ClickEvent.runCommand("/questbook quests")));
        return page.build();
    }

    private Component buildPlayerInfoPage(Player player) {
        TextComponent.Builder page = Component.text();
        PlayerData data = progressionService.getData(player.getUniqueId());
        int level = data.getLevel();
        appendLevelHeader(page, data);
        page.append(Component.newline());
        page.append(Component.newline());
        int hearts = healthService.heartsForLevel(level);
        page.append(centeredGradientLine("HEALTH: " + hearts, COLOR_HEALTH_GRADIENT_START, COLOR_HEALTH_GRADIENT_END));
        page.append(Component.newline());
        int hungerValue = hungerService.displayHungerFor(player, level);
        page.append(centeredGradientLine("HUNGER: " + hungerValue, COLOR_HUNGER_GRADIENT_START, COLOR_HUNGER_GRADIENT_END));
        page.append(Component.newline());
        int deaths = player.getStatistic(Statistic.DEATHS);
        page.append(centeredGradientLine("DEATHS: " + deaths, COLOR_HEALTH_GRADIENT_START, COLOR_HEALTH_GRADIENT_END)
                .clickEvent(ClickEvent.runCommand(statCommand("deaths"))));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook"));
        return page.build();
    }

    private List<Component> buildQuestPages(Player player) {
        PlayerData data = progressionService.getData(player.getUniqueId());
        List<QuestEntry> incomplete = new ArrayList<>();
        List<QuestEntry> complete = new ArrayList<>();
        for (QuestDefinition quest : questService.quests()) {
            String label = quest.title() + " (" + CompactNumberFormatter.format(quest.xp()) + " XP)";
            if (questService.isComplete(data, quest)) {
                complete.add(new QuestEntry(label + " (COMPLETE)", true));
            } else {
                incomplete.add(new QuestEntry(label, false));
            }
        }
        List<QuestEntry> entries = new ArrayList<>(incomplete.size() + complete.size());
        entries.addAll(incomplete);
        entries.addAll(complete);

        List<Component> pages = new ArrayList<>();
        TextComponent.Builder page = newQuestPageBuilder();
        int linesUsed = QUEST_HEADER_LINES;

        if (entries.isEmpty()) {
            page.append(centeredLine("No quests yet", COLOR_INCOMPLETE));
            linesUsed += 1;
        } else {
            for (QuestEntry entry : entries) {
                int maxLines = QUEST_PAGE_LINES - QUEST_FOOTER_LINES;
                int entryLines = wrappedLineCount(entry.text) + 2;
                if (linesUsed + entryLines > maxLines) {
                    page.append(Component.newline());
                    page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook"));
                    pages.add(page.build());
                    page = newQuestPageBuilder();
                    linesUsed = QUEST_HEADER_LINES;
                }
                if (entry.completed) {
                    page.append(centeredGradientLine(entry.text, COLOR_QUESTS_GRADIENT_START, COLOR_QUESTS_GRADIENT_END));
                } else {
                    page.append(centeredLine(entry.text, COLOR_INCOMPLETE));
                }
                page.append(Component.newline());
                page.append(Component.newline());
                linesUsed += entryLines;
            }
        }

        page.append(Component.newline());
        page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook"));
        pages.add(page.build());
        return pages;
    }

    private TextComponent.Builder newQuestPageBuilder() {
        TextComponent.Builder page = Component.text();
        page.append(centeredLine("QUESTS", COLOR_TITLE, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        page.append(Component.newline());
        page.append(Component.newline());
        return page;
    }

    private Component buildPlaytimePage(Player player) {
        PlayerData data = progressionService.getData(player.getUniqueId());
        long minutes = data.getActivePlaytimeMinutes();
        String playtime = formatHoursMinutes(minutes);
        long sinceDeathMinutes = progressionService.getSurvivalMinutes(data);
        String sinceDeath = formatDuration(sinceDeathMinutes * 60_000L) + " since death";
        double multiplier = progressionService.getSurvivalMultiplier(data, System.currentTimeMillis());

        TextComponent.Builder page = Component.text();
        page.append(centeredLine("PLAYTIME", COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("Total: " + playtime, data.getPlaytimeXpGained(), statCommand("playtime")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("Streak x" + formatMultiplier(multiplier), data.getSurvivalBonusXp(), statCommand("streak")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredLine(sinceDeath, COLOR_HINT));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook stats"));
        return page.build();
    }

    private Component buildMiningPage(Player player) {
        PlayerData data = progressionService.getData(player.getUniqueId());
        TextComponent.Builder page = Component.text();
        page.append(centeredLine("MINING", COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("Ores: " + data.getOresMined(), data.getMiningXpGained(), statCommand("ores")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("Crops: " + data.getCropsHarvested(), data.getCropsXpGained(), statCommand("crops")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook stats"));
        return page.build();
    }

    private Component buildCombatPage(Player player) {
        PlayerData data = progressionService.getData(player.getUniqueId());
        TextComponent.Builder page = Component.text();
        page.append(centeredLine("COMBAT", COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("Dragon kills: " + data.getDragonKills(), data.getDragonXpGained(), statCommand("dragon")));
        page.append(Component.newline());
        page.append(centeredStatLine("Wither kills: " + data.getWitherKills(), data.getWitherXpGained(), statCommand("wither")));
        page.append(Component.newline());
        page.append(centeredStatLine("Warden kills: " + data.getWardenKills(), data.getWardenXpGained(), statCommand("warden")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("Player kills: " + data.getPlayerKills(), data.getPlayerKillsXpGained(), statCommand("players")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook stats"));
        return page.build();
    }

    private Component buildExtraPage(Player player) {
        PlayerData data = progressionService.getData(player.getUniqueId());
        TextComponent.Builder page = Component.text();
        page.append(centeredLine("EXTRA", COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("Trades: " + data.getVillagerTrades(), data.getTradeXpGained(), statCommand("trades")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("XP Orbs used: " + data.getVanillaXpSpent(), data.getVanillaXpGained(), statCommand("xporbs")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("Advancements: " + data.getAdvancementsDone(), data.getAdvancementsXpGained(), statCommand("advancements")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("Quests: " + data.getQuestsDone(), data.getQuestsXpGained(), statCommand("quests")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook stats"));
        return page.build();
    }

    private Component buildMobsPage(Player player) {
        PlayerData data = progressionService.getData(player.getUniqueId());
        TextComponent.Builder page = Component.text();
        page.append(centeredLine("MOBS", COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("Mobs killed: " + data.getMobsKilled(), data.getMobsXpGained(), statCommand("mobs")));
        page.append(Component.newline());
        page.append(centeredStatLine("Monsters killed: " + data.getMonstersKilled(), data.getMonstersXpGained(), statCommand("monsters")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("Mobs bred: " + data.getMobsBred(), data.getMobsBredXpGained(), statCommand("bred")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("Mobs tamed: " + data.getMobsTamed(), data.getMobsTamedXpGained(), statCommand("tamed")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook stats"));
        return page.build();
    }

    private Component buildCollectionsStatsPage(Player player) {
        PlayerData data = progressionService.getData(player.getUniqueId());
        long enchantsFound = countFoundEnchants(data);
        int enchantsMax = totalEnchantTiers();
        long biomesFound = countUnique(data.getBiomesVisited());
        int biomesMax = Biome.values().length;
        long fishingFound = countUnique(data.getFishingItemsFound());
        long discsFound = countUnique(data.getDiscsFound());
        long bestiaryFound = BestiaryService.countFound(data, BestiaryCatalog.allEntries());
        long nightFound = data.getNightEventsFound();

        TextComponent.Builder page = Component.text();
        page.append(centeredLine("COLLECTIONS", COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        page.append(Component.newline());
        page.append(centeredCollectionStatLine("Enchants", enchantsFound, enchantsMax, data.getEnchantsXpGained(), statCommand("enchants")));
        page.append(Component.newline());
        page.append(centeredCollectionStatLine("Biomes", biomesFound, biomesMax, data.getBiomesXpGained(), statCommand("biomes")));
        page.append(Component.newline());
        page.append(centeredCollectionStatLine("Fishing", fishingFound, TOTAL_FISHING, data.getFishingXpGained(), statCommand("fishing")));
        page.append(Component.newline());
        page.append(centeredCollectionStatLine("Discs", discsFound, TOTAL_DISCS, 0, statCommand("discs")));
        page.append(Component.newline());
        page.append(centeredCollectionStatLine("Bestiary", bestiaryFound, TOTAL_BESTIARY, 0, statCommand("bestiary")));
        page.append(Component.newline());
        page.append(centeredCollectionStatLine("Night Events", nightFound, TOTAL_NIGHT_EVENTS, data.getNightEventsXpGained(), statCommand("night")));
        page.append(Component.newline());
        page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook stats"));
        return page.build();
    }

    private ItemStack buildEnchantsOverviewBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("SMPnir");
        meta.setAuthor(player.getName());

        PlayerData data = progressionService.getData(player.getUniqueId());
        List<Component> pages = new ArrayList<>();
        List<EnchantPageSpec> specs = new ArrayList<>();
        for (EnchantCategory category : EnchantmentCatalog.categories()) {
            specs.addAll(buildEnchantCategorySpecs(category));
        }
        for (int i = 0; i < specs.size(); i++) {
            pages.add(buildEnchantCategoryPage(data, specs, i));
        }

        meta.pages(pages);
        book.setItemMeta(meta);
        return book;
    }

    private ItemStack buildFishingBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("SMPnir");
        meta.setAuthor(player.getName());

        PlayerData data = progressionService.getData(player.getUniqueId());
        java.util.List<String> found = data.getFishingItemsFound();
        java.util.Set<String> foundSet = found == null ? java.util.Set.of() : new java.util.HashSet<>(found);
        java.util.List<FishingCollectionCatalog.FishingEntry> entries = FishingCollectionCatalog.entries();

        int perPage = 8;
        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) perPage));
        java.util.List<Component> pages = new java.util.ArrayList<>();

        for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
            int start = pageIndex * perPage;
            int end = Math.min(start + perPage, entries.size());
            TextComponent.Builder page = Component.text();
            page.append(centeredLine("FISHING", COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
            page.append(Component.newline());
            page.append(Component.newline());

            for (int i = start; i < end; i++) {
                FishingCollectionCatalog.FishingEntry entry = entries.get(i);
                boolean collected = foundSet.contains(entry.id());
                TextColor color = collected ? COLOR_COMPLETE : COLOR_HINT;
                TextDecoration[] deco = collected ? new TextDecoration[]{TextDecoration.BOLD} : new TextDecoration[]{};
                page.append(centeredLine(entry.displayName(), color, deco));
                page.append(Component.newline());
            }

            page.append(Component.newline());
            page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook collections"));
            pages.add(page.build());
        }

        meta.pages(pages);
        book.setItemMeta(meta);
        return book;
    }

    private ItemStack buildDiscsBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("SMPnir");
        meta.setAuthor(player.getName());

        PlayerData data = progressionService.getData(player.getUniqueId());
        List<String> found = data.getDiscsFound();
        Set<String> foundSet = found == null ? Set.of() : new HashSet<>(found);
        List<DiscCollectionCatalog.DiscEntry> entries = DiscCollectionCatalog.entries();

        int perPage = 8;
        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) perPage));
        List<Component> pages = new ArrayList<>();

        for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
            int start = pageIndex * perPage;
            int end = Math.min(start + perPage, entries.size());
            TextComponent.Builder page = Component.text();
            page.append(centeredLine("DISCS", COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
            page.append(Component.newline());
            page.append(Component.newline());

            for (int i = start; i < end; i++) {
                DiscCollectionCatalog.DiscEntry entry = entries.get(i);
                boolean collected = foundSet.contains(entry.id());
                TextColor color = collected ? COLOR_COMPLETE : COLOR_HINT;
                TextDecoration[] deco = collected ? new TextDecoration[]{TextDecoration.BOLD} : new TextDecoration[]{};
                page.append(centeredLine(entry.displayName(), color, deco));
                page.append(Component.newline());
            }

            page.append(Component.newline());
            page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook collections"));
            pages.add(page.build());
        }

        meta.pages(pages);
        book.setItemMeta(meta);
        return book;
    }

    private ItemStack buildBestiaryOverviewBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("SMPnir");
        meta.setAuthor(player.getName());

        PlayerData data = progressionService.getData(player.getUniqueId());
        meta.pages(List.of(buildBestiaryOverviewPage(data)));
        book.setItemMeta(meta);
        return book;
    }

    private Component buildBestiaryOverviewPage(PlayerData data) {
        TextComponent.Builder page = Component.text();
        page.append(centeredLine("BESTIARY", COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        page.append(Component.newline());
        page.append(Component.newline());

        page.append(centeredGradientButton(
        "[ALL MOBS]",
        COLOR_ALL_MOBS_GRADIENT_START,
        COLOR_ALL_MOBS_GRADIENT_END,
        ClickEvent.runCommand("/questbook bestiary all")
        ));

        page.append(Component.newline());
        page.append(Component.newline());

        page.append(centeredProgressCommandButton("Cats", BestiaryService.countFound(data, BestiaryCatalog.cats()), BestiaryCatalog.cats().size(), "/questbook bestiary cats"));
        page.append(Component.newline());
        page.append(centeredProgressCommandButton("Frogs", BestiaryService.countFound(data, BestiaryCatalog.frogs()), BestiaryCatalog.frogs().size(), "/questbook bestiary frogs"));
        page.append(Component.newline());
        page.append(centeredProgressCommandButton("Horses", BestiaryService.countFound(data, BestiaryCatalog.horses()), BestiaryCatalog.horses().size(), "/questbook bestiary horses"));
        page.append(Component.newline());
        page.append(centeredProgressCommandButton("Tropical Fish", BestiaryService.countFound(data, BestiaryCatalog.tropicalFish()), BestiaryCatalog.tropicalFish().size(), "/questbook bestiary tropical"));
        page.append(Component.newline());
        page.append(centeredProgressCommandButton("Villagers", BestiaryService.countFound(data, BestiaryCatalog.villagers()), BestiaryCatalog.villagers().size(), "/questbook bestiary villagers"));
        page.append(Component.newline());
        page.append(centeredProgressCommandButton("Foxes", BestiaryService.countFound(data, BestiaryCatalog.foxes()), BestiaryCatalog.foxes().size(), "/questbook bestiary foxes"));
        page.append(Component.newline());
        page.append(centeredProgressCommandButton("Wolves", BestiaryService.countFound(data, BestiaryCatalog.wolves()), BestiaryCatalog.wolves().size(), "/questbook bestiary wolves"));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook collections"));
        return page.build();
    }

    private ItemStack buildBestiaryCategoryBook(Player player, String title, List<BestiaryCatalog.Entry> entries, String backCommand) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("SMPnir");
        meta.setAuthor(player.getName());

        PlayerData data = progressionService.getData(player.getUniqueId());
        meta.pages(buildBestiaryEntryPages(title, entries, data, backCommand));
        book.setItemMeta(meta);
        return book;
    }

    private List<Component> buildBestiaryEntryPages(String title, List<BestiaryCatalog.Entry> entries, PlayerData data, String backCommand) {
        int perPage = 8;
        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) perPage));
        List<Component> pages = new ArrayList<>();
        java.util.Set<String> foundSet = data.getBestiaryFound() == null
                ? java.util.Set.of()
                : new java.util.HashSet<>(data.getBestiaryFound());

        for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
            int start = pageIndex * perPage;
            int end = Math.min(start + perPage, entries.size());
            TextComponent.Builder page = Component.text();
            page.append(centeredLine(title, COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
            page.append(Component.newline());
            page.append(Component.newline());

            for (int i = start; i < end; i++) {
                BestiaryCatalog.Entry entry = entries.get(i);
                boolean collected = foundSet.contains(entry.id());
                TextColor color = collected ? COLOR_COMPLETE : COLOR_HINT;
                TextDecoration[] deco = collected ? new TextDecoration[]{TextDecoration.BOLD} : new TextDecoration[]{};
                page.append(centeredLine(entry.displayName(), color, deco));
                page.append(Component.newline());
            }

            page.append(Component.newline());
            page.append(centeredCommandButton("BACK", NamedTextColor.RED, backCommand));
            pages.add(page.build());
        }
        return pages;
    }

    private ItemStack buildBiomesBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("SMPnir");
        meta.setAuthor(player.getName());

        PlayerData data = progressionService.getData(player.getUniqueId());
        List<String> visited = data.getBiomesVisited();
        Set<String> visitedSet = visited == null ? Set.of() : new HashSet<>(visited);
        Biome[] entries = Biome.values();

        int perPage = 8;
        int totalPages = Math.max(1, (int) Math.ceil(entries.length / (double) perPage));
        List<Component> pages = new ArrayList<>();

        for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
            int start = pageIndex * perPage;
            int end = Math.min(start + perPage, entries.length);
            TextComponent.Builder page = Component.text();
            page.append(centeredLine("BIOMES", COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
            page.append(Component.newline());
            page.append(Component.newline());

            for (int i = start; i < end; i++) {
                Biome biome = entries[i];
                boolean collected = visitedSet.contains(biome.getKey().toString());
                TextColor color = collected ? COLOR_COMPLETE : COLOR_HINT;
                TextDecoration[] deco = collected ? new TextDecoration[]{TextDecoration.BOLD} : new TextDecoration[]{};
                page.append(centeredLine(formatBiomeName(biome), color, deco));
                page.append(Component.newline());
            }

            page.append(Component.newline());
            page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook collections"));
            pages.add(page.build());
        }

        meta.pages(pages);
        book.setItemMeta(meta);
        return book;
    }

    private List<EnchantPageSpec> buildEnchantCategorySpecs(EnchantCategory category) {
        int maxEntriesPerPage = 8;
        List<EnchantPageSpec> specs = new ArrayList<>();
        List<EnchantEntry> entries = category.entries();
        for (int start = 0; start < entries.size(); start += maxEntriesPerPage) {
            int end = Math.min(start + maxEntriesPerPage, entries.size());
            List<EnchantEntry> slice = entries.subList(start, end);
            specs.add(new EnchantPageSpec(category.title(), slice));
        }
        return specs;
    }

    private Component buildEnchantCategoryPage(PlayerData data, List<EnchantPageSpec> specs, int index) {
        EnchantPageSpec spec = specs.get(index);
        TextComponent.Builder page = Component.text();
        page.append(centeredLine(spec.title(), COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        page.append(Component.newline());

        for (EnchantEntry entry : spec.entries()) {
            int found = countFoundTiers(data, entry);
            page.append(centeredEnchantProgressLine(entry, found));
            page.append(Component.newline());
        }

        if (specs.size() > 1) {
            page.append(Component.newline());
        }

        page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook collections"));
        return page.build();
    }

    private ItemStack buildEnchantDetailBook(Player player, EnchantEntry entry) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("SMPnir");
        meta.setAuthor(player.getName());

        meta.pages(List.of(buildEnchantDetailPage(player, entry)));
        book.setItemMeta(meta);
        return book;
    }

    private Component buildEnchantDetailPage(Player player, EnchantEntry entry) {
        PlayerData data = progressionService.getData(player.getUniqueId());
        TextComponent.Builder page = Component.text();
        page.append(centeredLine(entry.name().toUpperCase(Locale.US), COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        page.append(Component.newline());
        page.append(Component.newline());

        for (int level = 1; level <= entry.maxLevel(); level++) {
            boolean collected = hasTier(data, entry, level);
            TextColor color = collected ? COLOR_COMPLETE : COLOR_HINT;
            TextDecoration[] decorations = collected ? new TextDecoration[]{TextDecoration.BOLD} : new TextDecoration[]{};
            page.append(centeredLine(entry.name() + " " + roman(level), color, decorations));
            page.append(Component.newline());
        }

        page.append(Component.newline());
        page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook collections"));
        return page.build();
    }

    private Component buildBiomesPage(Player player) {
        PlayerData data = progressionService.getData(player.getUniqueId());
        int count = data.getBiomesVisited() == null ? 0 : data.getBiomesVisited().size();
        TextComponent.Builder page = Component.text();
        page.append(centeredLine("BIOMES", COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("Discovered: " + count, data.getBiomesXpGained(), statCommand("biomes")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook stats"));
        return page.build();
    }

    private Component buildFishingPage(Player player) {
        PlayerData data = progressionService.getData(player.getUniqueId());
        TextComponent.Builder page = Component.text();
        page.append(centeredLine("FISHING", COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredCollectionStatLine("Fishing", countUnique(data.getFishingItemsFound()), TOTAL_FISHING, data.getFishingXpGained(), statCommand("fishing")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook stats"));
        return page.build();
    }

    private Component buildNightEventsPage(Player player) {
        PlayerData data = progressionService.getData(player.getUniqueId());
        TextComponent.Builder page = Component.text();
        page.append(centeredLine("NIGHT EVENTS", COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredCollectionStatLine("Night Events", data.getNightEventsFound(), TOTAL_NIGHT_EVENTS, data.getNightEventsXpGained(), statCommand("night")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook stats"));
        return page.build();
    }

    private Component buildExplorationPage(Player player) {
        PlayerData data = progressionService.getData(player.getUniqueId());
        int biomes = data.getBiomesVisited() == null ? 0 : data.getBiomesVisited().size();
        TextComponent.Builder page = Component.text();
        page.append(centeredLine("EXPLORATION", COLOR_STATS, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("Biomes: " + biomes, data.getBiomesXpGained(), statCommand("biomes")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredStatLine("Chests: " + data.getStructureChestsOpened(), data.getStructureChestsXpGained(), statCommand("chests")));
        page.append(Component.newline());
        page.append(Component.newline());
        page.append(centeredCommandButton("BACK", NamedTextColor.RED, "/questbook stats"));
        return page.build();
    }

    private Component centeredProgressBar(double progress) {
        int filled = (int) Math.round(Math.max(0.0, Math.min(1.0, progress)) * BAR_SEGMENTS);
        int empty = BAR_SEGMENTS - filled;
        String filledBar = "=".repeat(Math.max(0, filled));
        String emptyBar = "-".repeat(Math.max(0, empty));
        int padding = Math.max(0, (LINE_WIDTH - BAR_SEGMENTS) / 2);
        Component left = Component.text(" ".repeat(padding));
        Component right = Component.text(" ".repeat(Math.max(0, LINE_WIDTH - BAR_SEGMENTS - padding)));
        Component filledComponent = filled > 0
                ? gradientBar(filledBar, COLOR_QUESTS_GRADIENT_START, COLOR_QUESTS_GRADIENT_END)
                : Component.empty();
        return Component.text()
                .append(left)
                .append(filledComponent)
                .append(Component.text(emptyBar, COLOR_EMPTY))
                .append(right)
                .build();
    }

    private Component centeredLine(String text, TextColor color, TextDecoration... decorations) {
        return Component.text(padCenter(text, LINE_WIDTH), color, decorations);
    }

    private Component centeredCommandButton(String label, TextColor color, String command) {
        String text = label.toUpperCase();
        String padded = padCenter(text, LINE_WIDTH);
        return Component.text(padded, color, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand(command));
    }

    private Component centeredProgressCommandButton(String label, int found, int total, String command) {
    boolean complete = total > 0 && found >= total; // only “complete” when full set is found
    TextColor color = complete ? COLOR_COMPLETE : COLOR_HINT;
    TextDecoration[] decorations = complete ? new TextDecoration[]{TextDecoration.BOLD} : new TextDecoration[]{};
    String text = label + " (" + found + "/" + total + ")";
    return Component.text(padCenter(text, LINE_WIDTH), color, decorations)
            .clickEvent(ClickEvent.runCommand(command));
    }

    private Component centeredGradientButton(String label, TextColor start, TextColor end, ClickEvent clickEvent) {
        int len = label.length();
        int leftPad = Math.max(0, (LINE_WIDTH - len) / 2);
        int rightPad = Math.max(0, LINE_WIDTH - len - leftPad);
        Component gradient = gradientText(label, start, end, true, true);
        return Component.text()
                .append(Component.text(" ".repeat(leftPad)))
                .append(gradient)
                .append(Component.text(" ".repeat(rightPad)))
                .clickEvent(clickEvent)
                .build();
    }

    private Component centeredGradientLine(String label, TextColor start, TextColor end) {
        int len = label.length();
        int leftPad = Math.max(0, (LINE_WIDTH - len) / 2);
        int rightPad = Math.max(0, LINE_WIDTH - len - leftPad);
        Component gradient = gradientText(label, start, end, true, false);
        return Component.text()
                .append(Component.text(" ".repeat(leftPad)))
                .append(gradient)
                .append(Component.text(" ".repeat(rightPad)))
                .build();
    }

    private void appendLevelHeader(TextComponent.Builder page, PlayerData data) {
        int level = data.getLevel();
        long totalXp = data.getTotalXp();
        long current = totalXp - levelCurve.getTotalXpForLevel(level);
        long next = levelCurve.getTotalXpForLevel(level + 1) - levelCurve.getTotalXpForLevel(level);
        if (level >= levelCurve.getMaxLevel()) {
            current = totalXp;
            next = 0;
        }
        double progress = levelCurve.getProgress(level, totalXp);
        page.append(centeredLine("[Level " + level + "]", ChatFormatService.nameColorForLevel(level), TextDecoration.BOLD));
        page.append(Component.newline());
        page.append(centeredProgressBar(progress));
        page.append(Component.newline());
        if (level >= levelCurve.getMaxLevel()) {
            page.append(centeredLine(" " + CompactNumberFormatter.format(current) + " xp", COLOR_STATS));
        } else {
            page.append(centeredLine(" (" + CompactNumberFormatter.format(current) + " / "
                    + CompactNumberFormatter.format(next) + " XP)", COLOR_HINT));
        }
    }

    private int wrappedLineCount(String text) {
        if (text == null || text.isEmpty()) {
            return 1;
        }
        int length = text.length();
        return Math.max(1, (length + LINE_WIDTH - 1) / LINE_WIDTH);
    }

    private Component gradientBar(String text, TextColor start, TextColor end) {
        return gradientText(text, start, end, false, false);
    }

    private Component gradientText(String text, TextColor start, TextColor end, boolean bold, boolean underline) {
        TextComponent.Builder builder = Component.text();
        int len = text.length();
        int startRgb = start.value();
        int endRgb = end.value();
        int startR = (startRgb >> 16) & 0xFF;
        int startG = (startRgb >> 8) & 0xFF;
        int startB = startRgb & 0xFF;
        int endR = (endRgb >> 16) & 0xFF;
        int endG = (endRgb >> 8) & 0xFF;
        int endB = endRgb & 0xFF;
        for (int i = 0; i < len; i++) {
            double t = len <= 1 ? 0.0 : (double) i / (len - 1);
            int r = (int) Math.round(startR + (endR - startR) * t);
            int g = (int) Math.round(startG + (endG - startG) * t);
            int b = (int) Math.round(startB + (endB - startB) * t);
            TextColor color = TextColor.color(r, g, b);
            Component ch = Component.text(String.valueOf(text.charAt(i)), color);
            if (bold) {
                ch = ch.decorate(TextDecoration.BOLD);
            }
            if (underline) {
                ch = ch.decorate(TextDecoration.UNDERLINED);
            }
            builder.append(ch);
        }
        return builder.build();
    }


    private String statCommand(String id) {
        return "/questbook stat " + id;
    }

    private Component centeredStatLine(String label, long xp, String command) {
        String left = label;
        String right = "(" + CompactNumberFormatter.format(xp) + " XP)";
        int totalLen = left.length() + 1 + right.length();
        int pad = Math.max(0, (LINE_WIDTH - totalLen) / 2);
        String padding = " ".repeat(pad);
        TextComponent.Builder builder = Component.text()
                .append(Component.text(padding + left + " ", COLOR_HINT))
                .append(Component.text(right, COLOR_COMPLETE));
        if (command != null) {
            builder.clickEvent(ClickEvent.runCommand(command));
        }
        return builder.build();
    }

    private Component centeredCollectionStatLine(String label, long found, long max, long xp, String command) {
        String left = label + ": " + found + "/" + max;
        String right = "(" + CompactNumberFormatter.format(xp) + " XP)";
        int totalLen = left.length() + 1 + right.length();
        int pad = Math.max(0, (LINE_WIDTH - totalLen) / 2);
        String padding = " ".repeat(pad);
        TextComponent.Builder builder = Component.text()
                .append(Component.text(padding + left + " ", COLOR_HINT))
                .append(Component.text(right, COLOR_COMPLETE));
        if (command != null) {
            builder.clickEvent(ClickEvent.runCommand(command));
        }
        return builder.build();
    }

    private Component centeredEnchantProgressLine(EnchantEntry entry, int found) {
        boolean collected = found > 0;
        TextColor nameColor = collected ? COLOR_COMPLETE : COLOR_HINT;
        TextDecoration[] nameDecorations = collected ? new TextDecoration[]{TextDecoration.BOLD} : new TextDecoration[]{};

        if (entry.maxLevel() <= 1) {
            String text = entry.name();
            return Component.text(padCenter(text, LINE_WIDTH), nameColor, nameDecorations);
        }

        String left = entry.name() + " ";
        String right = "(" + found + "/" + entry.maxLevel() + ")";
        int totalLen = left.length() + right.length();
        int pad = Math.max(0, (LINE_WIDTH - totalLen) / 2);
        String padding = " ".repeat(pad);
        TextColor countColor = collected ? COLOR_COMPLETE : COLOR_HINT;
        TextDecoration[] countDecorations = collected ? new TextDecoration[]{TextDecoration.BOLD} : new TextDecoration[]{};
        return Component.text()
                .append(Component.text(padding + left, nameColor, nameDecorations))
                .append(Component.text(right, countColor, countDecorations))
                .clickEvent(ClickEvent.runCommand("/questbook enchants " + entry.commandId()))
                .build();
    }

    private String padCenter(String text, int width) {
        if (text == null) {
            return "";
        }
        int len = text.length();
        if (len >= width) {
            return text;
        }
        int totalPad = width - len;
        int left = totalPad / 2;
        int right = totalPad - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    private String formatBiomeName(Biome biome) {
        String key = biome.getKey().getKey();
        String[] parts = key.split("_");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
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

    private String formatHoursMinutes(long minutes) {
        long hours = minutes / 60;
        long mins = minutes % 60;
        return hours + "h " + String.format(Locale.US, "%02dm", mins);
    }

    private String formatDuration(long millis) {
        if (millis < 0) {
            return "0m";
        }
        long totalMinutes = millis / 60000L;
        return formatHoursMinutes(totalMinutes);
    }

    private String formatMultiplier(double multiplier) {
        return String.format(Locale.US, "%.2f", multiplier);
    }

    private int totalEnchantTiers() {
        Set<String> keys = new HashSet<>();
        int total = 0;
        for (EnchantCategory category : EnchantmentCatalog.categories()) {
            for (EnchantEntry entry : category.entries()) {
                if (keys.add(entry.key())) {
                    total += entry.maxLevel();
                }
            }
        }
        return total;
    }

    private long countFoundEnchants(PlayerData data) {
        List<String> tiers = data.getEnchantmentTiersFound();
        if (tiers == null || tiers.isEmpty()) {
            return 0;
        }
        return new HashSet<>(tiers).size();
    }

    private long countUnique(List<String> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }
        return new HashSet<>(values).size();
    }

    private record EnchantPageSpec(String title, List<EnchantEntry> entries) {
    }

    private record QuestEntry(String text, boolean completed) {
    }

    private int countFoundTiers(PlayerData data, EnchantEntry entry) {
        List<String> tiers = data.getEnchantmentTiersFound();
        if (tiers == null || tiers.isEmpty()) {
            return 0;
        }
        String prefix = entry.key() + ":";
        int count = 0;
        for (String tier : tiers) {
            if (tier.startsWith(prefix)) {
                count++;
            }
        }
        return Math.min(count, entry.maxLevel());
    }

    private boolean hasTier(PlayerData data, EnchantEntry entry, int level) {
        List<String> tiers = data.getEnchantmentTiersFound();
        if (tiers == null || tiers.isEmpty()) {
            return false;
        }
        return tiers.contains(entry.key() + ":" + level);
    }

    private String roman(int value) {
        return switch (value) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(value);
        };
    }
}




