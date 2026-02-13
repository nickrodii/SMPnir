package com.nickrodi.nir.listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import com.nickrodi.nir.service.DeathChestService;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.WorldAccess;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Player death chest feature.
 *
 * Rules:
 * - Copies the death drops into a single double chest (54 slots).
 * - For suffocation deaths, places at the highest available block at X/Z.
 * - Prevents duplicate drops (vanilla drops are cleared and keepInventory is enabled).
 * - If the death chest inventory becomes empty, it is destroyed (no drops).
 * - If broken, contents drop but the chest item does not.
 * - Explosions never destroy death chest blocks.
 */
public class DeathChestListener implements Listener {
    private final DeathChestService deathChestService;
    private final WorldAccess worldAccess;
    private final ProgressionService progressionService;

    public DeathChestListener(DeathChestService deathChestService, WorldAccess worldAccess, ProgressionService progressionService) {
        this.deathChestService = deathChestService;
        this.worldAccess = worldAccess;
        this.progressionService = progressionService;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        sendDeathCoords(player);

        if (event.getKeepInventory()) {
            return;
        }
        if (!worldAccess.isAllowed(player)) {
            return;
        }
        if (!progressionService.getData(player.getUniqueId()).isDeathChestEnabled()) {
            return;
        }

        // IMPORTANT: event.getDrops() already contains everything that would drop.
        // Do NOT also read the player's inventory, or you'll duplicate items.
        List<ItemStack> drops = new ArrayList<>();
        for (ItemStack item : event.getDrops()) {
            if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                continue;
            }
            drops.add(item.clone());
        }
        if (drops.isEmpty()) {
            return;
        }

        Location origin = chestOriginForDeath(event);

        // Try to place chest FIRST. If placement fails, we do NOTHING and vanilla drops proceed.
        DeathChestService.SpawnResult result = deathChestService.spawnDeathChest(player, drops, origin);
        if (!result.placed()) {
            return;
        }

        // Chest placed -> prevent vanilla drops and clear player's inventory.
        event.setKeepInventory(true);
        event.getDrops().clear();

        player.getInventory().clear();
        player.updateInventory();

        // If chest overflowed, drop leftovers naturally at death origin.
        if (!result.leftover().isEmpty()) {
            World world = origin.getWorld();
            if (world != null) {
                for (ItemStack item : result.leftover()) {
                    if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                        continue;
                    }
                    world.dropItemNaturally(origin, item);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        // If a death chest was opened and is now empty, remove it.
        deathChestService.scheduleCleanupCheck(event.getInventory());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        protectDeathChests(event.blockList());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        protectDeathChests(event.blockList());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!deathChestService.isDeathChestBlock(event.getBlock())) {
            return;
        }
        // Contents drop; chest item never drops.
        event.setDropItems(false);
        event.setExpToDrop(0);
        deathChestService.breakDeathChest(event.getBlock());
    }

    /**
     * If the player died to suffocation, place chest at the highest available block at that X/Z.
     */
    private Location chestOriginForDeath(PlayerDeathEvent event) {
        Location loc = event.getEntity().getLocation();
        var last = event.getEntity().getLastDamageCause();
        if (last != null && last.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
            World world = loc.getWorld();
            if (world == null) {
                return loc;
            }
            int x = loc.getBlockX();
            int z = loc.getBlockZ();
            int y = world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
            return new Location(world, x, y + 1, z);
        }
        return loc;
    }

    private void sendDeathCoords(Player player) {
        Location loc = player.getLocation();
        String worldName = loc.getWorld() == null ? "unknown" : loc.getWorld().getName();
        String message = "You died at X: " + loc.getBlockX()
                + " Y: " + loc.getBlockY()
                + " Z: " + loc.getBlockZ()
                + " (" + worldName + ")";
        player.sendMessage(Component.text(message, NamedTextColor.RED));
    }

    private void protectDeathChests(List<Block> explodingBlocks) {
        explodingBlocks.removeIf(deathChestService::isDeathChestBlock);
    }
}
