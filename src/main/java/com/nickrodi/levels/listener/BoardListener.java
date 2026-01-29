package com.nickrodi.levels.listener;

import com.nickrodi.levels.service.BoardService;
import com.nickrodi.levels.util.Keys;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class BoardListener implements Listener {
    private final BoardService boardService;

    public BoardListener(BoardService boardService) {
        this.boardService = Objects.requireNonNull(boardService, "boardService");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEdit(PlayerEditBookEvent event) {
        BookMeta previous = event.getPreviousBookMeta();
        boolean isBoard = previous.getPersistentDataContainer().has(Keys.BOARD_BOOK, PersistentDataType.BYTE);
        if (!isBoard && !boardService.isEditor(event.getPlayer().getUniqueId())) {
            return;
        }
        boardService.updateFrom(event.getNewBookMeta());
        boardService.clearEditor(event.getPlayer().getUniqueId());
        event.setSigning(false);
        event.setCancelled(true);
    }
}
