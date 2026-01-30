package com.nickrodi.nir.listener;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.nickrodi.nir.service.BlockTrackerService;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.WorldAccess;

/**
 * Mining + harvesting XP.
 *
 * Harvesting rules:
 * - XP is awarded on BREAKING the crop block.
 * - Only when the crop is FULLY GROWN.
 * - Crops planted by players are "placed" at age 0, but once they GROW naturally (or via bonemeal),
 *   we unmark them as placed so harvesting works normally.
 * - Player place/break spam on age-0 crops yields 0 XP.
 */
public class BlockListener implements Listener {
    private static final Map<Material, Integer> ORE_XP = new EnumMap<>(Material.class);
    private static final Map<Material, Integer> CROP_XP = new EnumMap<>(Material.class);
    private static final Set<Material> TRACK_PLACED = EnumSet.noneOf(Material.class);
    private static final Set<Material> TRACK_PLACED_CROPS = EnumSet.noneOf(Material.class);

    static {
        // --- ores ---
        ORE_XP.put(Material.COPPER_ORE, 15);
        ORE_XP.put(Material.DEEPSLATE_COPPER_ORE, 15);
        ORE_XP.put(Material.COAL_ORE, 15);
        ORE_XP.put(Material.DEEPSLATE_COAL_ORE, 15);
        ORE_XP.put(Material.NETHER_QUARTZ_ORE, 40);
        ORE_XP.put(Material.NETHER_GOLD_ORE, 44);
        ORE_XP.put(Material.IRON_ORE, 64);
        ORE_XP.put(Material.DEEPSLATE_IRON_ORE, 64);
        ORE_XP.put(Material.REDSTONE_ORE, 72);
        ORE_XP.put(Material.DEEPSLATE_REDSTONE_ORE, 72);
        ORE_XP.put(Material.LAPIS_ORE, 72);
        ORE_XP.put(Material.DEEPSLATE_LAPIS_ORE, 72);
        ORE_XP.put(Material.GOLD_ORE, 80);
        ORE_XP.put(Material.DEEPSLATE_GOLD_ORE, 80);
        ORE_XP.put(Material.EMERALD_ORE, 176);
        ORE_XP.put(Material.DEEPSLATE_EMERALD_ORE, 176);
        ORE_XP.put(Material.DIAMOND_ORE, 180);
        ORE_XP.put(Material.DEEPSLATE_DIAMOND_ORE, 180);
        ORE_XP.put(Material.ANCIENT_DEBRIS, 520);
        ORE_XP.put(Material.SCULK_SHRIEKER, 72);

        // --- ageable crops ---
        CROP_XP.put(Material.WHEAT, 6);
        CROP_XP.put(Material.CARROTS, 6);
        CROP_XP.put(Material.POTATOES, 6);
        CROP_XP.put(Material.BEETROOTS, 6);
        CROP_XP.put(Material.NETHER_WART, 6);
        CROP_XP.put(Material.COCOA, 6);
        CROP_XP.put(Material.SWEET_BERRY_BUSH, 6);
        CROP_XP.put(Material.TORCHFLOWER_CROP, 6);
        CROP_XP.put(Material.PITCHER_CROP, 6);

        // Track "placed" for ores (to block player-placed ore XP)
        TRACK_PLACED.addAll(ORE_XP.keySet());

        // Track "placed" for crops too, but we will UNMARK them once they grow.
        TRACK_PLACED_CROPS.addAll(CROP_XP.keySet());
        TRACK_PLACED.addAll(TRACK_PLACED_CROPS);

        // other tracked blocks (unchanged)
        TRACK_PLACED.add(Material.MELON);
        TRACK_PLACED.add(Material.PUMPKIN);
        TRACK_PLACED.add(Material.CAVE_VINES);
        TRACK_PLACED.add(Material.CAVE_VINES_PLANT);
        TRACK_PLACED.add(Material.CHEST);
        TRACK_PLACED.add(Material.TRAPPED_CHEST);
        TRACK_PLACED.add(Material.BARREL);
        TRACK_PLACED.add(Material.SPAWNER);
    }

    private final ProgressionService progressionService;
    private final BlockTrackerService blockTrackerService;
    private final WorldAccess worldAccess;

    public BlockListener(ProgressionService progressionService, BlockTrackerService blockTrackerService, WorldAccess worldAccess) {
        this.progressionService = progressionService;
        this.blockTrackerService = blockTrackerService;
        this.worldAccess = worldAccess;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!worldAccess.isAllowed(event.getBlock().getWorld())) {
            return;
        }

        Block block = event.getBlockPlaced();
        Material type = block.getType();

        if (!TRACK_PLACED.contains(type)) {
            return;
        }

        // Mark placed blocks generally.
        // For crops: they'll be unmarked as soon as they grow at least once.
        blockTrackerService.markPlaced(block);
    }

    /**
     * KEY FIX:
     * When crops grow naturally (or via bonemeal), remove the "placed" mark so harvesting can award XP.
     */
    @EventHandler(ignoreCancelled = true)
    public void onGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        Material type = block.getType();

        if (!TRACK_PLACED_CROPS.contains(type)) {
            return;
        }

        // If it was planted/placed by a player, it will be marked.
        // Once it grows (age increases), consider it "naturally grown" and allow XP later.
        BlockState newState = event.getNewState();
        BlockData data = newState.getBlockData();
        if (!(data instanceof Ageable ageable)) {
            return;
        }

        // Only unmark after it's actually progressed beyond the initial planted state.
        if (ageable.getAge() > 0) {
            blockTrackerService.unmarkPlaced(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!worldAccess.isAllowed(event.getBlock().getWorld())) {
            return;
        }

        Block block = event.getBlock();
        Material type = block.getType();
        Player player = event.getPlayer();

        boolean trackable = TRACK_PLACED.contains(type);
        boolean wasPlaced = trackable && blockTrackerService.isPlaced(block);

        // --- Mining XP ---
        Integer oreXp = ORE_XP.get(type);
        if (oreXp != null) {
            boolean hasDrops = !block.getDrops(player.getInventory().getItemInMainHand(), player).isEmpty();
            if (!wasPlaced && hasDrops) {
                progressionService.addXp(player.getUniqueId(), oreXp, "mining");
                var data = progressionService.getData(player.getUniqueId());
                data.setOresMined(data.getOresMined() + 1);
                data.setMiningXpGained(data.getMiningXpGained() + oreXp);
            }
            if (wasPlaced) {
                blockTrackerService.unmarkPlaced(block);
            }
            return;
        }

        // --- Spawner ---
        if (type == Material.SPAWNER) {
            if (!wasPlaced) {
                progressionService.addXp(player.getUniqueId(), 100, "spawner");
            }
            if (wasPlaced) {
                blockTrackerService.unmarkPlaced(block);
            }
            return;
        }

        // --- Harvesting XP ---
        Integer cropXp = CROP_XP.get(type);
        if (cropXp != null) {
            BlockState state = block.getState();
            if (!wasPlaced && isFullyGrown(state)) {
                boolean hasDrops = !block.getDrops(player.getInventory().getItemInMainHand(), player).isEmpty();
                if (hasDrops) {
                    progressionService.addXp(player.getUniqueId(), cropXp, "harvest");
                    var data = progressionService.getData(player.getUniqueId());
                    data.setCropsHarvested(data.getCropsHarvested() + 1);
                    data.setCropsXpGained(data.getCropsXpGained() + cropXp);
                }
            }

            // Clean up marker if it still existed
            if (wasPlaced) {
                blockTrackerService.unmarkPlaced(block);
            }
            return;
        }

        // For other tracked blocks, clean up the placed marker if it existed.
        if (wasPlaced) {
            blockTrackerService.unmarkPlaced(block);
        }
    }

    private boolean isFullyGrown(BlockState state) {
        BlockData data = state.getBlockData();
        if (!(data instanceof Ageable ageable)) {
            return false;
        }
        return ageable.getAge() >= ageable.getMaximumAge();
    }
}
