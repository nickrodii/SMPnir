package com.nickrodi.levels.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoordsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Location loc = player.getLocation();
        String msg = player.getName() + ": " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
        player.getServer().broadcast(Component.text(msg));
        return true;
    }
}
