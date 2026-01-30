package com.nickrodi.nir.listener;

import com.nickrodi.nir.service.HungerService;
import com.nickrodi.nir.service.ProgressionService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map;

public class HungerListener implements Listener {
    private final JavaPlugin plugin;
    private final ProgressionService progressionService;
    private final HungerService hungerService;
    private final Map<UUID, Double> gainRemainders = new HashMap<>();
    private final Map<UUID, Double> lossRemainders = new HashMap<>();
    private final Set<UUID> sprintAssist = new HashSet<>();

    public HungerListener(JavaPlugin plugin, ProgressionService progressionService, HungerService hungerService) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.progressionService = Objects.requireNonNull(progressionService, "progressionService");
        this.hungerService = Objects.requireNonNull(hungerService, "hungerService");
        startSprintAssistTask();
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        int level = progressionService.getData(player.getUniqueId()).getLevel();
        int maxFood = hungerService.maxFoodFor(player, level);
        int current = player.getFoodLevel();
        int target = event.getFoodLevel();
        double multiplier = hungerService.changeMultiplierFor(player, level);
        int adjusted = target;
        if (multiplier != 1.0 && target != current) {
            UUID uuid = player.getUniqueId();
            int delta = target - current;
            Map<UUID, Double> remainders = delta > 0 ? gainRemainders : lossRemainders;
            double remainder = remainders.getOrDefault(uuid, 0.0);
            double scaled = delta * multiplier + remainder;
            int applied = scaled >= 0.0 ? (int) Math.floor(scaled) : (int) Math.ceil(scaled);
            remainder = scaled - applied;
            remainders.put(uuid, remainder);
            adjusted = current + applied;
        }
        if (adjusted > maxFood) {
            adjusted = maxFood;
        }
        if (adjusted < 0) {
            adjusted = 0;
        }
        if (adjusted != target) {
            event.setFoodLevel(adjusted);
        }
        if (player.getSaturation() > maxFood) {
            player.setSaturation(maxFood);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onToggleSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!event.isSprinting()) {
            sprintAssist.remove(uuid);
            return;
        }
        int level = progressionService.getData(uuid).getLevel();
        int minSprintFood = hungerService.minSprintFoodFor(player, level);
        if (minSprintFood >= 7) {
            return;
        }
        if (player.getFoodLevel() >= minSprintFood) {
            sprintAssist.add(uuid);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (player.isOnline() && sprintAssist.contains(uuid)) {
                    player.setSprinting(true);
                }
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        gainRemainders.remove(uuid);
        lossRemainders.remove(uuid);
        sprintAssist.remove(uuid);
    }

    private void startSprintAssistTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (sprintAssist.isEmpty()) {
                return;
            }
            var iterator = sprintAssist.iterator();
            while (iterator.hasNext()) {
                UUID uuid = iterator.next();
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) {
                    iterator.remove();
                    continue;
                }
                int level = progressionService.getData(uuid).getLevel();
                int minSprintFood = hungerService.minSprintFoodFor(player, level);
                if (minSprintFood >= 7) {
                    iterator.remove();
                    continue;
                }
                if (player.getFoodLevel() < minSprintFood) {
                    iterator.remove();
                    continue;
                }
                if (!player.isSprinting()) {
                    player.setSprinting(true);
                }
            }
        }, 1L, 1L);
    }
}
