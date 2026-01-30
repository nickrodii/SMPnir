package com.nickrodi.nir.command;

import com.nickrodi.nir.model.PlayerData;
import com.nickrodi.nir.service.ChatFormatService;
import com.nickrodi.nir.service.LevelCurve;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.StorageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class LeaderboardCommand implements CommandExecutor {
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;
    private static final int LINE_WIDTH = 19;
    private static final int BAR_SEGMENTS = 18;
    private static final NamedTextColor BAR_BRACKET = NamedTextColor.GRAY;
    private static final TextColor BAR_GRADIENT_START = TextColor.color(0xDDAA00);
    private static final TextColor BAR_GRADIENT_END = TextColor.color(0xBE5C00);
    private static final TextColor TITLE_GRADIENT_START = TextColor.color(0xFFE780);
    private static final TextColor TITLE_GRADIENT_END = TextColor.color(0xDDAA00);
    private static final NamedTextColor BAR_EMPTY = NamedTextColor.DARK_GRAY;

    private final StorageService storageService;
    private final LevelCurve levelCurve;
    private final ProgressionService progressionService;

    public LeaderboardCommand(StorageService storageService, LevelCurve levelCurve, ProgressionService progressionService) {
        this.storageService = storageService;
        this.levelCurve = levelCurve;
        this.progressionService = progressionService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int limit = DEFAULT_LIMIT;
        if (args.length >= 1) {
            try {
                limit = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                sender.sendMessage("Usage: /leaderboard [count]");
                return true;
            }
        }
        if (limit < 1) {
            sender.sendMessage("Count must be at least 1.");
            return true;
        }
        if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }

        List<LeaderboardEntry> entries = loadEntries();
        if (entries.isEmpty()) {
            sender.sendMessage("No player data found.");
            return true;
        }

        entries.sort(Comparator
                .comparingLong(LeaderboardEntry::totalXp).reversed()
                .thenComparing(LeaderboardEntry::name, String.CASE_INSENSITIVE_ORDER)
        );

        int shown = Math.min(limit, entries.size());
        Component header = gradientText("=== TOP PLAYERS ===", TITLE_GRADIENT_START, TITLE_GRADIENT_END)
                .decorate(TextDecoration.BOLD);
        broadcast(header);

        for (int i = 0; i < shown; i++) {
            LeaderboardEntry entry = entries.get(i);
            Component displayName = ChatFormatService.formatDisplayName(entry.level(), entry.name());
            Component line = Component.text()
                    .append(Component.text(String.format(Locale.US, "%2d. ", i + 1), NamedTextColor.GRAY))
                    .append(displayName)
                    .build();
            broadcast(line);

            double progress = levelCurve.getProgress(entry.level(), entry.totalXp());
            Component bar = progressBar(progress);
            broadcast(bar);
        }

        return true;
    }

    private List<LeaderboardEntry> loadEntries() {
        Map<UUID, LeaderboardEntry> byUuid = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = progressionService.getData(player.getUniqueId());
            byUuid.put(player.getUniqueId(), new LeaderboardEntry(player.getUniqueId(), player.getName(), data.getLevel(), data.getTotalXp()));
        }

        List<UUID> uuids = storageService.listPlayerUuids();
        for (UUID uuid : uuids) {
            if (byUuid.containsKey(uuid)) {
                continue;
            }
            PlayerData data = storageService.load(uuid);
            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            String name = offline.getName();
            if (name == null || name.isBlank()) {
                name = uuid.toString();
            }
            byUuid.put(uuid, new LeaderboardEntry(uuid, name, data.getLevel(), data.getTotalXp()));
        }

        return new ArrayList<>(byUuid.values());
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

    private void broadcast(Component message) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
        Bukkit.getConsoleSender().sendMessage(message);
    }

    private record LeaderboardEntry(UUID uuid, String name, int level, long totalXp) {
    }
}
