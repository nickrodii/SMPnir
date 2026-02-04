package com.nickrodi.nir.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class XpListCommand implements CommandExecutor {
    private static final NamedTextColor HEADER = NamedTextColor.GOLD;
    private static final NamedTextColor LABEL = NamedTextColor.AQUA;
    private static final NamedTextColor TEXT = NamedTextColor.GRAY;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        player.sendMessage(Component.text("XP Sources (from /book)", HEADER, TextDecoration.BOLD));
        player.sendMessage(line("Playtime:", "20 XP per active minute"));
        player.sendMessage(line("Streak bonus:", "+0 to +20 XP/min (multiplier 1.0x to 2.0x)"));
        player.sendMessage(line("Ores:", "15 to 520 XP depending on ore"));
        player.sendMessage(line("Ore details:", "Copper/Coal 15, Quartz 40, Nether Gold 44, Iron 64"));
        player.sendMessage(line("Ore details:", "Redstone/Lapis/Sculk 72, Gold 80, Emerald 176, Diamond 180, Ancient Debris 520"));
        player.sendMessage(line("Crops:", "6 XP per fully-grown crop"));
        player.sendMessage(line("Mobs killed:", "10 XP (passive/ambient/water)"));
        player.sendMessage(line("Monsters killed:", "60 XP; silverfish 4 XP; elites 120 XP; spawner monsters 7 XP"));
        player.sendMessage(line("Boss kills:", "Dragon 5000, Wither 3000, Warden 8000 (1/day)"));
        player.sendMessage(line("Player kills:", "0 to 4800 XP (based on victim streak, max at 20h)"));
        player.sendMessage(line("Mobs bred:", "20 XP"));
        player.sendMessage(line("Mobs tamed:", "50 XP"));
        player.sendMessage(line("Biomes:", "50 XP per new biome"));
        player.sendMessage(line("Structure chests:", "40 to 650 XP depending on structure"));
        player.sendMessage(line("Structure details:", "40 village/portal/trial/igloo, 60 shipwreck, 80 outpost/ocean ruin"));
        player.sendMessage(line("Structure details:", "120 desert, 150 fortress, 160 jungle, 200 mansion"));
        player.sendMessage(line("Structure details:", "220 mineshaft/stronghold, 300 end city, 400 bastion/treasure, 650 ancient city"));
        player.sendMessage(line("Fishing catches:", "Junk 10, Fish 40, Treasure 100 XP"));
        player.sendMessage(line("Fishing collection:", "+300 XP per new item"));
        player.sendMessage(line("Enchants collection:", "200 + 50*(level-1) + 1000 if max tier"));
        player.sendMessage(line("Discs collection:", "600 XP per new disc"));
        player.sendMessage(line("Bestiary collection:", "300 XP per new entry"));
        player.sendMessage(line("Night events:", "Not active yet"));
        player.sendMessage(line("Trades:", "15 to 35 XP (villager level bonus)"));
        player.sendMessage(line("XP orbs used:", "1 XP per vanilla XP spent (anvil/enchant)"));
        player.sendMessage(line("Advancements:", "250 task, 750 goal, 2000 challenge"));
        player.sendMessage(line("Quests:", "Varies by quest (shown in /book quests)"));
        return true;
    }

    private Component line(String label, String items) {
        TextComponent.Builder builder = Component.text();
        builder.append(Component.text(label, LABEL, TextDecoration.BOLD));
        builder.append(Component.text(" " + items, TEXT));
        return builder.build();
    }
}
