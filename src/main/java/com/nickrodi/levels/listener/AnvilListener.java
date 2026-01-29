package com.nickrodi.levels.listener;

import com.nickrodi.levels.service.ExperienceUtil;
import com.nickrodi.levels.service.ProgressionService;
import com.nickrodi.levels.service.WorldAccess;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AnvilListener implements Listener {
    private final JavaPlugin plugin;
    private final ProgressionService progressionService;
    private final WorldAccess worldAccess;

    public AnvilListener(JavaPlugin plugin, ProgressionService progressionService, WorldAccess worldAccess) {
        this.plugin = plugin;
        this.progressionService = progressionService;
        this.worldAccess = worldAccess;
    }

    @EventHandler(ignoreCancelled = true)
    public void onAnvilUse(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof AnvilInventory)) {
            return;
        }
        if (event.getRawSlot() != 2) {
            return;
        }
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!worldAccess.isAllowed(player)) {
            return;
        }
        int before = ExperienceUtil.getTotalExperience(player);
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            int after = ExperienceUtil.getTotalExperience(player);
            int spent = Math.max(0, before - after);
            if (spent > 0) {
                progressionService.addXp(player.getUniqueId(), spent, "vanilla-xp");
                var data = progressionService.getData(player.getUniqueId());
                data.setVanillaXpSpent(data.getVanillaXpSpent() + spent);
                data.setVanillaXpGained(data.getVanillaXpGained() + spent);
            }
        });
    }
}
