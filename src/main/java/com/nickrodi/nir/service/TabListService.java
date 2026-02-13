package com.nickrodi.nir.service;

import java.util.Collection;

import org.bukkit.entity.Player;

import com.nickrodi.nir.model.PlayerData;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Shows each player's OWN level header (same vibe as the top of /questbook)
 * in their tab list header.
 */
public class TabListService {
    private static final int LINE_WIDTH = 19;
    private static final int BAR_SEGMENTS = 18;

    // Warm gold gradient for filled bar
    private static final TextColor COLOR_BAR_START = TextColor.color(0xFFD45A);
    private static final TextColor COLOR_BAR_END = TextColor.color(0xFF8A00);

    // Tab list background is dark -> use brighter "empty" and text colors
    private static final NamedTextColor COLOR_EMPTY = NamedTextColor.GRAY;
    private static final NamedTextColor COLOR_HINT = NamedTextColor.WHITE;
    private static final NamedTextColor COLOR_STATS = NamedTextColor.LIGHT_PURPLE;

    private final ProgressionService progressionService;
    private final LevelCurve levelCurve;

    public TabListService(ProgressionService progressionService, LevelCurve levelCurve) {
        this.progressionService = progressionService;
        this.levelCurve = levelCurve;
    }

    public void refreshAll(Collection<? extends Player> players) {
        if (players == null || players.isEmpty()) {
            return;
        }
        for (Player player : players) {
            refresh(player);
        }
    }

    public void refresh(Player player) {
        if (player == null) {
            return;
        }
        Component header = buildHeader(player);
        player.sendPlayerListHeaderAndFooter(header, Component.empty());
    }

    private Component buildHeader(Player player) {
        PlayerData data = progressionService.getData(player.getUniqueId());
        int level = data.getLevel();
        long totalXp = data.getTotalXp();

        long current = totalXp - levelCurve.getTotalXpForLevel(level);
        long next = levelCurve.getTotalXpForLevel(level + 1) - levelCurve.getTotalXpForLevel(level);
        if (level >= levelCurve.getMaxLevel()) {
            current = totalXp;
            next = 0;
        }

        double progress = levelCurve.getProgress(level, totalXp);

        TextComponent.Builder b = Component.text();
        b.append(centeredLine("[Level " + level + "]", ChatFormatService.nameColorForLevel(level), TextDecoration.BOLD));
        b.append(Component.newline());
        b.append(centeredProgressBar(progress));
        b.append(Component.newline());

        // Make XP text pop more on tab background
        if (level >= levelCurve.getMaxLevel()) {
            b.append(centeredLine(" " + CompactNumberFormatter.format(current) + " XP", COLOR_STATS));
        } else {
            b.append(centeredLine("(" + CompactNumberFormatter.format(current) + " / "
                    + CompactNumberFormatter.format(next) + " XP)", COLOR_HINT));
        }

        return b.build();
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
                ? gradientText(filledBar, COLOR_BAR_START, COLOR_BAR_END).decorate(TextDecoration.BOLD)
                : Component.empty();

        Component emptyComponent = Component.text(emptyBar, COLOR_EMPTY).decorate(TextDecoration.BOLD);

        return Component.text()
                .append(left)
                .append(filledComponent)
                .append(emptyComponent)
                .append(right)
                .build();
    }

    private Component centeredLine(String text, TextColor color, TextDecoration... decorations) {
        return Component.text(padCenter(text, LINE_WIDTH), color, decorations);
    }

    private String padCenter(String text, int width) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= width) {
            return text;
        }
        int pad = width - text.length();
        int left = pad / 2;
        int right = pad - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    private Component gradientText(String text, TextColor start, TextColor end) {
        TextComponent.Builder builder = Component.text();
        int len = text.length();
        if (len <= 0) {
            return Component.empty();
        }

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
}
