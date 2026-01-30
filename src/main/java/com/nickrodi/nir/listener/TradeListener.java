package com.nickrodi.nir.listener;

import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.WorldAccess;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TradeListener implements Listener {
    private final ProgressionService progressionService;
    private final WorldAccess worldAccess;

    public TradeListener(ProgressionService progressionService, WorldAccess worldAccess) {
        this.progressionService = progressionService;
        this.worldAccess = worldAccess;
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrade(PlayerTradeEvent event) {
        if (!worldAccess.isAllowed(event.getPlayer())) {
            return;
        }
        Player player = event.getPlayer();
        int xp = 15 + getVillagerBonus(event.getMerchant());
        progressionService.addXp(player.getUniqueId(), xp, "trade");
        var data = progressionService.getData(player.getUniqueId());
        data.setVillagerTrades(data.getVillagerTrades() + 1);
        data.setTradeXpGained(data.getTradeXpGained() + xp);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPurchase(PlayerPurchaseEvent event) {
        if (!worldAccess.isAllowed(event.getPlayer())) {
            return;
        }
        if (event instanceof PlayerTradeEvent) {
            return;
        }
        Player player = event.getPlayer();
        progressionService.addXp(player.getUniqueId(), 15, "trade");
        var data = progressionService.getData(player.getUniqueId());
        data.setVillagerTrades(data.getVillagerTrades() + 1);
        data.setTradeXpGained(data.getTradeXpGained() + 15);
    }

    private int getVillagerBonus(AbstractVillager merchant) {
        if (merchant instanceof Villager villager) {
            return switch (villager.getVillagerLevel()) {
                case 2 -> 5;
                case 3 -> 10;
                case 4 -> 15;
                case 5 -> 20;
                default -> 0;
            };
        }
        return 0;
    }
}
