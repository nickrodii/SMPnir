package com.nickrodi.levels.listener;

import com.nickrodi.levels.service.BlockTrackerService;
import com.nickrodi.levels.service.ProgressionService;
import com.nickrodi.levels.service.WorldAccess;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.entity.Item;
import org.bukkit.persistence.PersistentDataType;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.nickrodi.levels.util.Keys;

public class BlockListener implements Listener {
    private static final Map<Material, Integer> ORE_XP = new EnumMap<>(Material.class);
    private static final Map<Material, Integer> CROP_XP = new EnumMap<>(Material.class);
    private static final Set<Material> TRACK_PLACED = EnumSet.noneOf(Material.class);

    static {
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

        CROP_XP.put(Material.WHEAT, 6);
        CROP_XP.put(Material.CARROTS, 6);
        CROP_XP.put(Material.POTATOES, 6);
        CROP_XP.put(Material.BEETROOTS, 6);
        CROP_XP.put(Material.NETHER_WART, 6);
        CROP_XP.put(Material.COCOA, 6);
        CROP_XP.put(Material.SWEET_BERRY_BUSH, 6);
        CROP_XP.put(Material.TORCHFLOWER_CROP, 6);
        CROP_XP.put(Material.PITCHER_CROP, 6);

        TRACK_PLACED.addAll(ORE_XP.keySet());
        TRACK_PLACED.addAll(CROP_XP.keySet());
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
        if (TRACK_PLACED.contains(block.getType())) {
            blockTrackerService.markPlaced(block);
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
        boolean wasPlaced = TRACK_PLACED.contains(type) && blockTrackerService.isPlaced(block);


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

        if (type == Material.SPAWNER) {
            if (!wasPlaced) {
                progressionService.addXp(player.getUniqueId(), 100, "spawner");
            }
            if (wasPlaced) {
                blockTrackerService.unmarkPlaced(block);
            }
            return;
        }


        if (type == Material.MELON || type == Material.PUMPKIN) {
            if (wasPlaced) {
                blockTrackerService.unmarkPlaced(block);
            }
            return;
        }

        Integer cropXp = CROP_XP.get(type);
        if (wasPlaced) {
            blockTrackerService.unmarkPlaced(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(BlockDropItemEvent event) {
        if (!worldAccess.isAllowed(event.getBlock().getWorld())) {
            return;
        }
        Block block = event.getBlock();
        BlockState state = event.getBlockState();
        Material type = state.getType();
        if (type == Material.MELON || type == Material.PUMPKIN) {
            if (blockTrackerService.isPlaced(block)) {
                return;
            }
            for (Item item : event.getItems()) {
                tagCropXp(item, 20, 1);
                break;
            }
            return;
        }
        Integer cropXp = CROP_XP.get(type);
        if (cropXp == null) {
            return;
        }
        if (!isFullyGrown(state)) {
            return;
        }
        if (blockTrackerService.isPlaced(block)) {
            return;
        }
        for (Item item : event.getItems()) {
            tagCropXp(item, cropXp, 1);
            break;
        }
    }

    private boolean isFullyGrown(BlockState state) {
        BlockData data = state.getBlockData();
        if (!(data instanceof Ageable ageable)) {
            return false;
        }
        return ageable.getAge() >= ageable.getMaximumAge();
    }


    private boolean isSugarCanePlaced(Block block) {
        Block base = block;
        while (base.getRelative(0, -1, 0).getType() == Material.SUGAR_CANE) {
            base = base.getRelative(0, -1, 0);
        }
        Block current = base;
        while (current.getType() == Material.SUGAR_CANE) {
            if (blockTrackerService.isPlaced(current)) {
                return true;
            }
            current = current.getRelative(0, 1, 0);
        }
        return false;
    }

    private void tagCropXp(Item item, int xp, int count) {
        var container = item.getPersistentDataContainer();
        container.set(Keys.CROP_XP_VALUE, PersistentDataType.INTEGER, xp);
        container.set(Keys.CROP_XP_COUNT, PersistentDataType.INTEGER, count);
        container.set(Keys.CROP_XP_AWARDED, PersistentDataType.BYTE, (byte) 0);

        var stack = item.getItemStack();
        var meta = stack.getItemMeta();
        if (meta != null) {
            var metaData = meta.getPersistentDataContainer();
            metaData.set(Keys.CROP_XP_VALUE, PersistentDataType.INTEGER, xp);
            metaData.set(Keys.CROP_XP_COUNT, PersistentDataType.INTEGER, count);
            metaData.set(Keys.CROP_XP_AWARDED, PersistentDataType.BYTE, (byte) 0);
            stack.setItemMeta(meta);
            item.setItemStack(stack);
        }
    }
}




