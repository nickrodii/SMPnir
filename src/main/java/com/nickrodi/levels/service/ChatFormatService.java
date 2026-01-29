package com.nickrodi.levels.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.Objects;

public class ChatFormatService {
    private static final String[] SYMBOLS = {
            "\u22c6", "\u2055", "\u2736", "\u2733", "\u2734", "\u2737", "\u273a", "\u2763", "\u2730", "\u2748", "\u269d"
    };

    private static final int[] GRADIENT_STARTS = {
            0x9A9A9A,
            0xCFCFCF,
            0xFFD45A,
            0x6FFFEA,
            0x7EC9FF,
            0xFF8A8A,
            0x7CFF7C,
            0xFF7AC8,
            0x0DA8A8,
            0xB36BFF,
            0x8B1E2D
    };
    private static final int[] GRADIENT_ENDS = {
            0x6F6F6F,
            0xB0B0B0,
            0xE6A800,
            0x38DCCF,
            0x3D84FF,
            0xFF5C5C,
            0x2FD86A,
            0xFF4F9E,
            0x0E7A5A,
            0x7B2CDE,
            0x5E145A
    };

    private final ProgressionService progressionService;

    public ChatFormatService(ProgressionService progressionService) {
        this.progressionService = Objects.requireNonNull(progressionService, "progressionService");
    }

    public static TextColor nameColorForLevel(int level) {
        int band = bandForLevel(level);
        return TextColor.color(GRADIENT_STARTS[band]);
    }

    public static Component formatDisplayName(int level, String name) {
        int band = bandForLevel(level);
        String symbol = SYMBOLS[band];
        TextColor start = TextColor.color(GRADIENT_STARTS[band]);
        TextColor end = TextColor.color(GRADIENT_ENDS[band]);
        String prefixText = "[" + level + symbol + "] " + name;
        boolean bold = band >= 5;
        return gradientText(prefixText, start, end, bold, symbol);
    }

    public static void applyDisplayName(Player player, int level) {
        if (player == null) {
            return;
        }
        Component display = formatDisplayName(level, player.getName());
        player.displayName(display);
        player.playerListName(display);
        player.customName(display);
        player.setCustomNameVisible(true);
    }

    private static int bandForLevel(int level) {
        if (level >= 100) {
            return SYMBOLS.length - 1;
        }
        int band = Math.max(0, level / 10);
        if (band >= SYMBOLS.length) {
            band = SYMBOLS.length - 1;
        }
        return band;
    }

    private static Component gradientText(String text, TextColor start, TextColor end, boolean bold, String noBoldChar) {
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
            String chStr = String.valueOf(text.charAt(i));
            Component ch = Component.text(chStr, color);
            if (bold && !chStr.equals(noBoldChar)) {
                ch = ch.decorate(TextDecoration.BOLD);
            }
            builder.append(ch);
        }
        return builder.build();
    }

    public Component format(Player player, Component message) {
        int level = progressionService.getData(player.getUniqueId()).getLevel();
        Component prefix = formatDisplayName(level, player.getName());
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(message);

        return Component.text()
                .append(prefix)
                .append(Component.text(": ", NamedTextColor.WHITE))
                .append(Component.text(plainMessage, NamedTextColor.WHITE))
                .build();
    }
}







