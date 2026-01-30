package com.nickrodi.nir.listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Barrel;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.InventoryHolder;

import com.nickrodi.nir.service.BlockTrackerService;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.StructureRewardService;
import com.nickrodi.nir.service.WorldAccess;

import io.papermc.paper.math.Position;

public class StructureLootListener implements Listener {
    private static final int XP_ANCIENT_CITY = 650;
    private static final int XP_MINESHAFT = 220;
    private static final int XP_JUNGLE = 160;
    private static final int XP_DESERT = 120;
    private static final int XP_OCEAN_RUIN = 80;
    private static final int XP_BURIED_TREASURE = 400;
    private static final int XP_STRONGHOLD = 220;
    private static final int XP_END_CITY = 300;
    private static final int XP_MANSION = 200;
    private static final int XP_OUTPOST = 80;
    private static final int XP_VILLAGE = 40;
    private static final int XP_RUINED_PORTAL = 40;
    private static final int XP_TRIAL = 40;
    private static final int XP_FORTRESS = 150;
    private static final int XP_IGLOO = 40;
    private static final int XP_SHIPWRECK = 60;
    private static final int XP_BASTION = 400;

    private final ProgressionService progressionService;
    private final BlockTrackerService blockTrackerService;
    private final StructureRewardService rewardService;
    private final WorldAccess worldAccess;
    private final List<StructureReward> rewards = new ArrayList<>();

    public StructureLootListener(
            ProgressionService progressionService,
            BlockTrackerService blockTrackerService,
            StructureRewardService rewardService,
            WorldAccess worldAccess
    ) {
        this.progressionService = progressionService;
        this.blockTrackerService = blockTrackerService;
        this.rewardService = rewardService;
        this.worldAccess = worldAccess;
        registerRewards();
    }

    @EventHandler(ignoreCancelled = true)
    public void onOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!worldAccess.isAllowed(player)) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof StorageMinecart minecart) {
            handleMinecart(player, minecart);
            return;
        }

        Location location = getContainerLocation(holder);
        if (location == null) {
            return;
        }
        if (blockTrackerService.isPlaced(location.getBlock())) {
            return;
        }

        int xp = getStructureXp(location);
        if (xp <= 0) {
            return;
        }

        String key = rewardService.key(location);
        if (rewardService.isClaimed(key)) {
            return;
        }

        rewardService.claim(key);
        progressionService.addXp(player.getUniqueId(), xp, "structure chest");
        var data = progressionService.getData(player.getUniqueId());
        data.setStructureChestsOpened(data.getStructureChestsOpened() + 1);
        data.setStructureChestsXpGained(data.getStructureChestsXpGained() + xp);
    }

    private void handleMinecart(Player player, StorageMinecart minecart) {
        Location location = minecart.getLocation();
        if (!worldAccess.isAllowed(location)) {
            return;
        }
        if (!isStructureAt(location, Structure.MINESHAFT) && !isStructureAt(location, Structure.MINESHAFT_MESA)) {
            return;
        }
        String key = rewardService.key(minecart.getUniqueId());
        if (rewardService.isClaimed(key)) {
            return;
        }
        rewardService.claim(key);
        progressionService.addXp(player.getUniqueId(), XP_MINESHAFT, "structure chest");
        var data = progressionService.getData(player.getUniqueId());
        data.setStructureChestsOpened(data.getStructureChestsOpened() + 1);
        data.setStructureChestsXpGained(data.getStructureChestsXpGained() + XP_MINESHAFT);
    }

    private Location getContainerLocation(InventoryHolder holder) {
        if (holder instanceof DoubleChest doubleChest) {
            Location left = ((Chest) doubleChest.getLeftSide()).getLocation();
            Location right = ((Chest) doubleChest.getRightSide()).getLocation();
            if (left == null) {
                return right;
            }
            if (right == null) {
                return left;
            }
            if (compareLocation(left, right) <= 0) {
                return left;
            }
            return right;
        }
        if (holder instanceof Chest chest) {
            return chest.getLocation();
        }
        if (holder instanceof Barrel barrel) {
            return barrel.getLocation();
        }
        return null;
    }

    private int getStructureXp(Location location) {
        for (StructureReward reward : rewards) {
            if (isStructureAt(location, reward.structure())) {
                return reward.xp();
            }
        }
        return 0;
    }

    private boolean isStructureAt(Location location, Structure structure) {
        return location.getWorld().hasStructureAt(Position.block(location), structure);
    }

    private int compareLocation(Location a, Location b) {
        if (a.getBlockX() != b.getBlockX()) {
            return Integer.compare(a.getBlockX(), b.getBlockX());
        }
        if (a.getBlockY() != b.getBlockY()) {
            return Integer.compare(a.getBlockY(), b.getBlockY());
        }
        return Integer.compare(a.getBlockZ(), b.getBlockZ());
    }

    private void registerRewards() {
        rewards.add(new StructureReward(Structure.ANCIENT_CITY, XP_ANCIENT_CITY));
        rewards.add(new StructureReward(Structure.BASTION_REMNANT, XP_BASTION));
        rewards.add(new StructureReward(Structure.BURIED_TREASURE, XP_BURIED_TREASURE));
        rewards.add(new StructureReward(Structure.DESERT_PYRAMID, XP_DESERT));
        rewards.add(new StructureReward(Structure.END_CITY, XP_END_CITY));
        rewards.add(new StructureReward(Structure.FORTRESS, XP_FORTRESS));
        rewards.add(new StructureReward(Structure.IGLOO, XP_IGLOO));
        rewards.add(new StructureReward(Structure.JUNGLE_PYRAMID, XP_JUNGLE));
        rewards.add(new StructureReward(Structure.MANSION, XP_MANSION));
        rewards.add(new StructureReward(Structure.OCEAN_RUIN_COLD, XP_OCEAN_RUIN));
        rewards.add(new StructureReward(Structure.OCEAN_RUIN_WARM, XP_OCEAN_RUIN));
        rewards.add(new StructureReward(Structure.PILLAGER_OUTPOST, XP_OUTPOST));
        rewards.add(new StructureReward(Structure.RUINED_PORTAL, XP_RUINED_PORTAL));
        rewards.add(new StructureReward(Structure.RUINED_PORTAL_DESERT, XP_RUINED_PORTAL));
        rewards.add(new StructureReward(Structure.RUINED_PORTAL_JUNGLE, XP_RUINED_PORTAL));
        rewards.add(new StructureReward(Structure.RUINED_PORTAL_MOUNTAIN, XP_RUINED_PORTAL));
        rewards.add(new StructureReward(Structure.RUINED_PORTAL_NETHER, XP_RUINED_PORTAL));
        rewards.add(new StructureReward(Structure.RUINED_PORTAL_OCEAN, XP_RUINED_PORTAL));
        rewards.add(new StructureReward(Structure.RUINED_PORTAL_SWAMP, XP_RUINED_PORTAL));
        rewards.add(new StructureReward(Structure.SHIPWRECK, XP_SHIPWRECK));
        rewards.add(new StructureReward(Structure.SHIPWRECK_BEACHED, XP_SHIPWRECK));
        rewards.add(new StructureReward(Structure.STRONGHOLD, XP_STRONGHOLD));
        rewards.add(new StructureReward(Structure.TRIAL_CHAMBERS, XP_TRIAL));
        rewards.add(new StructureReward(Structure.VILLAGE_DESERT, XP_VILLAGE));
        rewards.add(new StructureReward(Structure.VILLAGE_PLAINS, XP_VILLAGE));
        rewards.add(new StructureReward(Structure.VILLAGE_SAVANNA, XP_VILLAGE));
        rewards.add(new StructureReward(Structure.VILLAGE_SNOWY, XP_VILLAGE));
        rewards.add(new StructureReward(Structure.VILLAGE_TAIGA, XP_VILLAGE));
    }

    private record StructureReward(Structure structure, int xp) {
    }
}
