package com.nickrodi.nir.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WelcomeService {
    private static final long CHUNK_DELAY_TICKS = 20L * 15L;

    private final JavaPlugin plugin;

    public WelcomeService(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void send(Player player) {
        if (player == null) {
            return;
        }
        List<Component> chunks = buildChunks();
        for (int i = 0; i < chunks.size(); i++) {
            Component chunk = chunks.get(i);
            long delay = CHUNK_DELAY_TICKS * i;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.sendMessage(chunk);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.2f);
                }
            }, delay);
        }
    }

    private List<Component> buildChunks() {
        Component title = Component.text("Welcome", NamedTextColor.GOLD, TextDecoration.BOLD);
        Component blank = Component.text("");

        Component intro1 = Component.text(
                "This server has a special leveling system (made by nick rodi) that rewards",
                NamedTextColor.YELLOW
        );
        Component intro2 = Component.text(
                "you with a cosmetic display name, more hearts, more hunger points and more.",
                NamedTextColor.YELLOW
        );
        Component bookLine = Component.text()
                .append(Component.text("/book", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text(" to view your current level, player info, stats, collections and quests.", NamedTextColor.GRAY))
                .build();
        Component tabLine = Component.text()
                .append(Component.text("(you can also hold ", NamedTextColor.GRAY))
                .append(Component.text("TAB", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text(" to view your level progress)", NamedTextColor.GRAY))
                .build();
        Component badgeLine = Component.text(
                "Every 10 levels rewards you with a new badge and color next to your name in chat.",
                NamedTextColor.YELLOW
        );
        Component collectLine1 = Component.text()
                .append(Component.text("You can collect many different things, and you can find this in the ", NamedTextColor.GRAY))
                .append(Component.text("COLLECTIONS", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
                .append(Component.text(" tab", NamedTextColor.GRAY))
                .build();
        Component collectLine2 = Component.text("within /book menu.", NamedTextColor.GRAY);
        Component statsLine1 = Component.text(
                "Statistics show you everything you can get xp from. Clicking a specific statistic in /book",
                NamedTextColor.GRAY
        );
        Component statsLine2 = Component.text(
                "will display it under your name for everyone to see.",
                NamedTextColor.GRAY
        );
        Component xpListLine = Component.text(
                "/xplist shows you how much xp you can gain from each source.",
                NamedTextColor.GRAY
        );
        Component leaderboardLine = Component.text()
                .append(Component.text("/leaderboard", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text(" to see top players with highest levels", NamedTextColor.GRAY))
                .build();
        Component alsoLine = Component.text("Also:", NamedTextColor.GOLD, TextDecoration.BOLD);
        Component deathLine1 = Component.text(
                "While the difficulty is HARD and you start with 8 hearts, dying will drop",
                NamedTextColor.YELLOW
        );
        Component deathLine2 = Component.text(
                "a chest instead of splashing your items everywhere. Warning: if you die in lava,",
                NamedTextColor.YELLOW
        );
        Component deathLine3 = Component.text(
                "a \"burn timer\" will begin for 30 minutes. After that time, it'll despawn.",
                NamedTextColor.YELLOW
        );
        Component creativeLine1 = Component.text()
                .append(Component.text("/creative", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text(" to send you to a special flat world to test builds and such.", NamedTextColor.GRAY))
                .build();
        Component creativeLine2 = Component.text()
                .append(Component.text("In this creative world: ", NamedTextColor.GRAY))
                .append(Component.text("/rtp", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text(" sends you to a random location, ", NamedTextColor.GRAY))
                .append(Component.text("/tp {player}", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text(" will send a", NamedTextColor.GRAY))
                .build();
        Component creativeLine3 = Component.text()
                .append(Component.text("teleport request to the player, and ", NamedTextColor.GRAY))
                .append(Component.text("//wand", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text(" to utilize WorldEdit features.", NamedTextColor.GRAY))
                .build();
        Component discordLine = Component.text("Click here to join the discord server", NamedTextColor.AQUA, TextDecoration.BOLD, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl("https://discord.gg/xG8Zb5r8kH"));
        Component repeatLine = Component.text()
                .append(Component.text("Type ", NamedTextColor.GRAY))
                .append(Component.text("/smphelp", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text(" to see this again", NamedTextColor.GRAY))
                .build();

        List<Component> chunks = new ArrayList<>();
        chunks.add(joinLines(title, blank, intro1, intro2, blank));
        chunks.add(joinLines(bookLine, tabLine, blank, badgeLine, blank));
        chunks.add(joinLines(collectLine1, collectLine2, blank, statsLine1, statsLine2, xpListLine, blank));
        chunks.add(joinLines(leaderboardLine, blank, alsoLine, deathLine1, deathLine2, deathLine3, blank));
        chunks.add(joinLines(creativeLine1, creativeLine2, creativeLine3, blank, discordLine, blank, repeatLine));
        return chunks;
    }

    private Component joinLines(Component... lines) {
        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < lines.length; i++) {
            builder.append(lines[i]);
            if (i < lines.length - 1) {
                builder.append(Component.newline());
            }
        }
        return builder.build();
    }
}
