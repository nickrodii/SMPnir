package com.nickrodi.nir.listener;

import com.nickrodi.nir.service.ExperienceUtil;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.WorldAccess;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EnchantingListener implements Listener {
    private final JavaPlugin plugin;
    private final ProgressionService progressionService;
    private final WorldAccess worldAccess;

    public EnchantingListener(JavaPlugin plugin, ProgressionService progressionService, WorldAccess worldAccess) {
        this.plugin = plugin;
        this.progressionService = progressionService;
        this.worldAccess = worldAccess;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        if (!worldAccess.isAllowed(event.getEnchanter())) {
            return;
        }
        Player player = event.getEnchanter();
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
