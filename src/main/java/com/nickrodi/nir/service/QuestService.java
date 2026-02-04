package com.nickrodi.nir.service;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.nickrodi.nir.model.PlayerData;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

public class QuestService {
    public static final QuestDefinition TNT_3X3 = new QuestDefinition("tnt-3x3", "Explode a 3x3 of TNT", 3000);
    public static final QuestDefinition HALF_HEART_FULL = new QuestDefinition(
            "half-heart-full",
            "Get from half heart to full HP without dying",
            4000
    );
    public static final QuestDefinition WARDENS_ANCIENT_CITY = new QuestDefinition(
            "wardens-ancient-city",
            "Spawn 6 wardens in one ancient city at the same time",
            18000
    );
    public static final QuestDefinition DRAGON_FIRST = new QuestDefinition(
            "dragon-first",
            "Kill the Ender Dragon for the first time",
            15000
    );
    public static final QuestDefinition WITHER_FIRST = new QuestDefinition(
            "wither-first",
            "Kill the Wither for the first time",
            9000
    );
    public static final QuestDefinition KILL_MAX_STREAK = new QuestDefinition(
            "kill-max-streak",
            "Kill a player with max survival streak",
            25000
    );
    public static final QuestDefinition CAVES_AND_CLIFFS = new QuestDefinition(
            "caves-and-cliffs",
            "MLG water bucket from the build height to bedrock",
            60000
    );

    private static final List<QuestDefinition> QUESTS = List.of(
            TNT_3X3,
            HALF_HEART_FULL,
            WARDENS_ANCIENT_CITY,
            DRAGON_FIRST,
            WITHER_FIRST,
            KILL_MAX_STREAK,
            CAVES_AND_CLIFFS
    );
    private static final TextColor QUEST_GRADIENT_START = TextColor.color(0xDDAA00);
    private static final TextColor QUEST_GRADIENT_END = TextColor.color(0xBE5C00);

    private final ProgressionService progressionService;

    public QuestService(ProgressionService progressionService) {
        this.progressionService = progressionService;
    }

    public List<QuestDefinition> quests() {
        return QUESTS;
    }

    public boolean isComplete(PlayerData data, QuestDefinition quest) {
        if (data == null || quest == null) {
            return false;
        }
        List<String> completed = data.getQuestsCompleted();
        return completed != null && completed.contains(quest.id());
    }

    public boolean complete(Player player, QuestDefinition quest) {
        if (player == null || quest == null) {
            return false;
        }
        PlayerData data = progressionService.getData(player.getUniqueId());
        List<String> completed = data.getQuestsCompleted();
        if (completed == null) {
            completed = new ArrayList<>();
            data.setQuestsCompleted(completed);
        }
        if (completed.contains(quest.id())) {
            return false;
        }
        completed.add(quest.id());
        data.setQuestsDone(data.getQuestsDone() + 1);
        data.setQuestsXpGained(data.getQuestsXpGained() + quest.xp());
        progressionService.addXp(player.getUniqueId(), quest.xp(), "quest");
        player.sendMessage(gradientText("Quest: " + quest.title() + " has been completed!", QUEST_GRADIENT_START, QUEST_GRADIENT_END));
        return true;
    }

    public record QuestDefinition(String id, String title, long xp) {
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
            TextColor color = TextColor.color(r, g, b);
            builder.append(Component.text(String.valueOf(text.charAt(i)), color));
        }
        return builder.build();
    }
}
