package com.nickrodi.nir.command;

import com.nickrodi.nir.service.ChatFormatService;
import com.nickrodi.nir.service.CollectionsMenuService;
import com.nickrodi.nir.service.LevelCurve;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.QuestBookService;
import com.nickrodi.nir.service.StatDisplayService;
import com.nickrodi.nir.service.StatDisplayType;
import com.nickrodi.nir.service.StatsMenuService;
import com.nickrodi.nir.service.StatsSection;
import com.nickrodi.nir.service.StorageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class QuestBookCommand implements CommandExecutor {
    private static final int LINE_WIDTH = 19;
    private static final int BAR_SEGMENTS = 18;
    private static final NamedTextColor BAR_BRACKET = NamedTextColor.GRAY;
    private static final TextColor BAR_GRADIENT_START = TextColor.color(0xDDAA00);
    private static final TextColor BAR_GRADIENT_END = TextColor.color(0xBE5C00);
    private static final NamedTextColor BAR_EMPTY = NamedTextColor.DARK_GRAY;

    private final QuestBookService questBookService;
    private final StatsMenuService statsMenuService;
    private final CollectionsMenuService collectionsMenuService;
    private final StatDisplayService statDisplayService;
    private final ProgressionService progressionService;
    private final StorageService storageService;
    private final LevelCurve levelCurve;

    public QuestBookCommand(
            QuestBookService questBookService,
            StatsMenuService statsMenuService,
            CollectionsMenuService collectionsMenuService,
            StatDisplayService statDisplayService,
            ProgressionService progressionService,
            StorageService storageService,
            LevelCurve levelCurve
    ) {
        this.questBookService = Objects.requireNonNull(questBookService, "questBookService");
        this.statsMenuService = Objects.requireNonNull(statsMenuService, "statsMenuService");
        this.collectionsMenuService = Objects.requireNonNull(collectionsMenuService, "collectionsMenuService");
        this.statDisplayService = Objects.requireNonNull(statDisplayService, "statDisplayService");
        this.progressionService = Objects.requireNonNull(progressionService, "progressionService");
        this.storageService = Objects.requireNonNull(storageService, "storageService");
        this.levelCurve = Objects.requireNonNull(levelCurve, "levelCurve");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("level")) {
            if (args.length == 0) {
                if (sender instanceof Player player) {
                    var data = progressionService.getData(player.getUniqueId());
                    sendLevelProgress(sender, player.getName(), data.getLevel(), data.getTotalXp());
                    return true;
                }
                sender.sendMessage("Usage: /level <player>");
                return true;
            }
            if (args.length == 1) {
                return handleLevelLookup(sender, args[0]);
            }
            sender.sendMessage("Usage: /level <player>");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("stat")) {
            if (args.length >= 2) {
                StatDisplayType type = StatDisplayType.fromId(args[1]);
                if (type != null) {
                    statDisplayService.setDisplay(player, type);
                    player.sendMessage("Now displaying " + type.id() + " under your name.");
                } else {
                    player.sendMessage("Unknown stat. Use a stat from the book.");
                }
            } else {
                player.sendMessage("Usage: /questbook stat <statId>");
            }
            return true;
        }
        if (args.length >= 1 && (args[0].equalsIgnoreCase("enchants") || args[0].equalsIgnoreCase("enchant"))) {
            if (args.length >= 2) {
                String input = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                var entry = com.nickrodi.nir.service.EnchantmentCatalog.find(input);
                if (entry != null) {
                    questBookService.openEnchantDetail(player, entry);
                } else {
                    questBookService.openEnchantsOverview(player);
                }
            } else {
                questBookService.openEnchantsOverview(player);
            }
            return true;
        }
        if (args.length >= 1 && (args[0].equalsIgnoreCase("bestiary") || args[0].equalsIgnoreCase("beastiary"))) {
            if (args.length >= 2) {
                questBookService.openBestiaryCategory(player, args[1]);
            } else {
                questBookService.openBestiaryCollection(player);
            }
            return true;
        }
        if (args.length >= 1 && (args[0].equalsIgnoreCase("collections") || args[0].equalsIgnoreCase("collection"))) {
            collectionsMenuService.openFor(player);
            return true;
        }
        if (args.length >= 1 && (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("playerinfo"))) {
            questBookService.openPlayerInfo(player);
            return true;
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("quests")) {
            questBookService.openQuests(player);
            return true;
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("stats")) {
            if (args.length >= 2) {
                StatsSection section = StatsSection.from(args[1]);
                if (section != null) {
                    questBookService.openStatsSection(player, section);
                } else {
                    statsMenuService.openFor(player);
                }
            } else {
                statsMenuService.openFor(player);
            }
            return true;
        }
        questBookService.openFor(player);
        return true;
    }

    private boolean handleLevelLookup(CommandSender sender, String targetName) {
        Player online = Bukkit.getPlayerExact(targetName);
        if (online == null) {
            online = Bukkit.getPlayer(targetName);
        }
        if (online != null) {
            var data = progressionService.getData(online.getUniqueId());
            sendLevelProgress(sender, online.getName(), data.getLevel(), data.getTotalXp());
            return true;
        }

        OfflineLevel offline = findOfflineDataByName(targetName);
        if (offline == null) {
            sender.sendMessage("Player not found.");
            return true;
        }

        sendLevelProgress(sender, offline.name, offline.level, offline.totalXp);
        return true;
    }

    private OfflineLevel findOfflineDataByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String target = name.toLowerCase(Locale.US);
        for (UUID uuid : storageService.listPlayerUuids()) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            String offlineName = offline.getName();
            if (offlineName == null) {
                continue;
            }
            if (!offlineName.toLowerCase(Locale.US).equals(target)) {
                continue;
            }
            var data = storageService.load(uuid);
            return new OfflineLevel(offlineName, data.getLevel(), data.getTotalXp());
        }
        return null;
    }

    private void sendLevelProgress(CommandSender sender, String name, int level, long totalXp) {
        Component displayName = ChatFormatService.formatDisplayName(level, name);
        Component bar = progressBar(levelCurve.getProgress(level, totalXp));

        if (sender instanceof Player player) {
            player.sendMessage(displayName);
            player.sendMessage(bar);
        } else {
            sender.sendMessage(displayName);
            sender.sendMessage(bar);
        }
    }

    private Component progressBar(double progress) {
        int filled = (int) Math.round(Math.max(0.0, Math.min(1.0, progress)) * BAR_SEGMENTS);
        int empty = BAR_SEGMENTS - filled;
        String filledBar = "=".repeat(Math.max(0, filled));
        String emptyBar = "-".repeat(Math.max(0, empty));
        int padding = Math.max(0, (LINE_WIDTH - BAR_SEGMENTS) / 2);
        Component left = Component.text(" ".repeat(padding));
        Component right = Component.text(" ".repeat(Math.max(0, LINE_WIDTH - BAR_SEGMENTS - padding)));
        TextComponent.Builder builder = Component.text();
        builder.append(left);
        builder.append(Component.text("[", BAR_BRACKET));
        if (filled > 0) {
            builder.append(gradientText(filledBar, BAR_GRADIENT_START, BAR_GRADIENT_END));
        }
        if (empty > 0) {
            builder.append(Component.text(emptyBar, BAR_EMPTY));
        }
        builder.append(Component.text("]", BAR_BRACKET));
        builder.append(right);
        return builder.build();
    }

    private Component gradientText(String text, TextColor start, TextColor end) {
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
            builder.append(Component.text(String.valueOf(text.charAt(i)), TextColor.color(r, g, b)));
        }
        return builder.build();
    }

    private static class OfflineLevel {
        private final String name;
        private final int level;
        private final long totalXp;

        private OfflineLevel(String name, int level, long totalXp) {
            this.name = name;
            this.level = level;
            this.totalXp = totalXp;
        }
    }
}
