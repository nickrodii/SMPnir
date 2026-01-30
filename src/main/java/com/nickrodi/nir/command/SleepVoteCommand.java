package com.nickrodi.nir.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nickrodi.nir.service.SleepVoteService;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class SleepVoteCommand implements CommandExecutor {
    private final SleepVoteService sleepVoteService;

    public SleepVoteCommand(SleepVoteService sleepVoteService) {
        this.sleepVoteService = sleepVoteService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can vote.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /sleepvote yes", NamedTextColor.GRAY));
            return true;
        }
        String choice = args[0].toLowerCase();
        if ("yes".equals(choice) || "y".equals(choice)) {
            sleepVoteService.recordYes(player);
            return true;
        }
        player.sendMessage(Component.text("Usage: /sleepvote yes", NamedTextColor.GRAY));
        return true;
    }
}
