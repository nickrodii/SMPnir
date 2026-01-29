package com.nickrodi.levels.command;

import com.nickrodi.levels.service.BoardService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class BoardCommand implements CommandExecutor {
    private final BoardService boardService;

    public BoardCommand(BoardService boardService) {
        this.boardService = Objects.requireNonNull(boardService, "boardService");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        boardService.openFor(player);
        return true;
    }
}
