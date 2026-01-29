package com.nickrodi.levels.listener;

import com.nickrodi.levels.service.CollectionsMenuHolder;
import com.nickrodi.levels.service.CollectionsMenuService;
import com.nickrodi.levels.service.QuestBookService;
import com.nickrodi.levels.service.StatsSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class CollectionsMenuListener implements Listener {
    private final JavaPlugin plugin;
    private final QuestBookService questBookService;
    private final CollectionsMenuService collectionsMenuService;

    public CollectionsMenuListener(
            JavaPlugin plugin,
            QuestBookService questBookService,
            CollectionsMenuService collectionsMenuService
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.questBookService = Objects.requireNonNull(questBookService, "questBookService");
        this.collectionsMenuService = Objects.requireNonNull(collectionsMenuService, "collectionsMenuService");
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof CollectionsMenuHolder)) {
            return;
        }
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() == null || event.getClickedInventory() != top) {
            return;
        }

        if (collectionsMenuService.isBackSlot(event.getSlot())) {
            player.closeInventory();
            plugin.getServer().getScheduler().runTask(plugin, () -> questBookService.openFor(player));
            return;
        }

        StatsSection section = collectionsMenuService.getSectionForSlot(event.getSlot());
        if (section == null) {
            return;
        }

        player.closeInventory();
        if (section == StatsSection.ENCHANTS) {
            plugin.getServer().getScheduler().runTask(plugin, () -> questBookService.openEnchantsOverview(player));
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> questBookService.openStatsSection(player, section));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof CollectionsMenuHolder) {
            event.setCancelled(true);
        }
    }
}
