package com.nickrodi.levels.listener;

import com.nickrodi.levels.service.ChatFormatService;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class ChatFormatListener implements Listener {
    private final ChatFormatService chatFormatService;

    public ChatFormatListener(ChatFormatService chatFormatService) {
        this.chatFormatService = Objects.requireNonNull(chatFormatService, "chatFormatService");
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        event.renderer((source, sourceDisplayName, message, viewer) ->
                chatFormatService.format(source, message)
        );
    }
}
