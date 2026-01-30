package com.nickrodi.nir.command;

import com.nickrodi.nir.service.WelcomeService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class SmpHelpCommand implements CommandExecutor {
    private final WelcomeService welcomeService;

    public SmpHelpCommand(WelcomeService welcomeService) {
        this.welcomeService = Objects.requireNonNull(welcomeService, "welcomeService");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        welcomeService.send(player);
        return true;
    }
}
