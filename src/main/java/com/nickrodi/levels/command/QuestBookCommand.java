package com.nickrodi.levels.command;

import com.nickrodi.levels.service.CollectionsMenuService;
import com.nickrodi.levels.service.QuestBookService;
import com.nickrodi.levels.service.StatDisplayService;
import com.nickrodi.levels.service.StatDisplayType;
import com.nickrodi.levels.service.StatsMenuService;
import com.nickrodi.levels.service.StatsSection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;

public class QuestBookCommand implements CommandExecutor {
    private final QuestBookService questBookService;
    private final StatsMenuService statsMenuService;
    private final CollectionsMenuService collectionsMenuService;
    private final StatDisplayService statDisplayService;

    public QuestBookCommand(
            QuestBookService questBookService,
            StatsMenuService statsMenuService,
            CollectionsMenuService collectionsMenuService,
            StatDisplayService statDisplayService
    ) {
        this.questBookService = Objects.requireNonNull(questBookService, "questBookService");
        this.statsMenuService = Objects.requireNonNull(statsMenuService, "statsMenuService");
        this.collectionsMenuService = Objects.requireNonNull(collectionsMenuService, "collectionsMenuService");
        this.statDisplayService = Objects.requireNonNull(statDisplayService, "statDisplayService");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
                var entry = com.nickrodi.levels.service.EnchantmentCatalog.find(input);
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
}
