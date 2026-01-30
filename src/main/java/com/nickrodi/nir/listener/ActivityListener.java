package com.nickrodi.nir.listener;

import com.nickrodi.nir.service.ActivityService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.event.player.AsyncChatEvent;

public class ActivityListener implements Listener {
    private final JavaPlugin plugin;
    private final ActivityService activityService;

    public ActivityListener(JavaPlugin plugin, ActivityService activityService) {
        this.plugin = plugin;
        this.activityService = activityService;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        activityService.markActive(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        activityService.markActive(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        plugin.getServer().getScheduler().runTask(
                plugin,
                () -> activityService.markActive(event.getPlayer().getUniqueId())
        );
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            activityService.markActive(player.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            activityService.markActive(player.getUniqueId());
        }
    }
}
