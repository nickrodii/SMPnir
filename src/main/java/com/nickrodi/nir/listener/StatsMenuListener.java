package com.nickrodi.nir.listener;

import java.util.Objects;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import com.nickrodi.nir.service.QuestBookService;
import com.nickrodi.nir.service.StatsMenuHolder;
import com.nickrodi.nir.service.StatsMenuService;
import com.nickrodi.nir.service.StatsSection;

public class StatsMenuListener implements Listener {
    private final JavaPlugin plugin;
    private final QuestBookService questBookService;
    private final StatsMenuService statsMenuService;
    public StatsMenuListener(
            JavaPlugin plugin,
            QuestBookService questBookService,
            StatsMenuService statsMenuService
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.questBookService = Objects.requireNonNull(questBookService, "questBookService");
        this.statsMenuService = Objects.requireNonNull(statsMenuService, "statsMenuService");
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof StatsMenuHolder)) {
            return;
        }
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() == null || event.getClickedInventory() != top) {
            return;
        }

        if (statsMenuService.isBackSlot(event.getSlot())) {
            player.closeInventory();
            plugin.getServer().getScheduler().runTask(plugin, () -> questBookService.openFor(player));
            return;
        }

        StatsSection section = statsMenuService.getSectionForSlot(event.getSlot());
        if (section == null) {
            return;
        }

        player.closeInventory();
        plugin.getServer().getScheduler().runTask(plugin, () -> questBookService.openStatsSection(player, section));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof StatsMenuHolder) {
            event.setCancelled(true);
        }
    }
}
