package com.nickrodi.nir.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.nickrodi.nir.util.Keys;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Manages player-death loot chests (tagged via PDC). Normal chests are ignored.
 *
 * Spec:
 * - Place one DOUBLE chest with all drops.
 * - If touching lava, show a hologram "Chest burns in MM:SS" and count down.
 * - Timer pauses when not touching lava; resumes if lava touches again.
 * - Lava countdown never despawns the chest.
 * - When emptied, the chest is destroyed (no drops).
 * - When broken, contents drop but chest item does not.
 */
public class DeathChestService {
    private static final long DEFAULT_BURN_MINUTES = 30L;
    private static final long UPDATE_INTERVAL_TICKS = 20L;
    private static final double DISPLAY_Y_OFFSET = 1.2;

    private static final int SEARCH_RADIUS = 2;
    private static final int DOWN_SEARCH_BLOCKS = 32;
    private static final int[] DY_SEARCH = new int[]{0, 1, 2, 3, 4};

    private static final BlockFace[] LAVA_FACES = new BlockFace[]{
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.UP,
            BlockFace.DOWN
    };

    private static final BlockFace[] DOUBLE_CHEST_FACES = new BlockFace[]{
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST
    };

    private final JavaPlugin plugin;
    private final WorldAccess worldAccess;
    private final long burnDurationMs;

    private final Map<String, DeathChestEntry> byBlock = new HashMap<>();
    private final Set<DeathChestEntry> active = new HashSet<>();
    private BukkitTask task;

    public DeathChestService(JavaPlugin plugin, WorldAccess worldAccess) {
        this.plugin = plugin;
        this.worldAccess = worldAccess;
        long minutes = plugin.getConfig().getLong("death-chest.burn-minutes", DEFAULT_BURN_MINUTES);
        if (minutes < 0L) {
            minutes = 0L;
        }
        this.burnDurationMs = minutes * 60L * 1000L;
    }

    public void start() {
        if (task != null) {
            return;
        }
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, UPDATE_INTERVAL_TICKS, UPDATE_INTERVAL_TICKS);
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (var chest : new ArrayList<>(active)) {
            removeDisplay(chest);
        }
        active.clear();
        byBlock.clear();
    }

    /**
     * Spawns one double chest and stores all drops. Returns leftovers (if any).
     */
    public SpawnResult spawnDeathChest(Player player, List<ItemStack> drops, Location origin) {
    if (player == null || origin == null || drops == null || drops.isEmpty()) {
        return new SpawnResult(false, drops == null ? List.of() : new ArrayList<>(drops));
    }
    if (!worldAccess.isAllowed(origin)) {
        return new SpawnResult(false, new ArrayList<>(drops));
    }
    World world = origin.getWorld();
    if (world == null) {
        return new SpawnResult(false, new ArrayList<>(drops));
    }

    // Clean + clone items
    List<ItemStack> items = new ArrayList<>();
    for (ItemStack it : drops) {
        if (it == null || it.getType().isAir() || it.getAmount() <= 0) {
            continue;
        }
        items.add(it.clone());
    }
    if (items.isEmpty()) {
        return new SpawnResult(false, List.of());
    }

    // IMPORTANT: Never place below death Y. Only search at death Y and above.
    ChestPair pair = findDoubleChestPairAtOrAbove(origin);
    if (pair == null) {
        return new SpawnResult(false, items); // placement failed; caller should allow vanilla drops
    }

    if (!placeDoubleChest(pair)) {
        return new SpawnResult(false, items);
    }

    // Tag both halves
    long createdAt = System.currentTimeMillis();
    tagChestAt(pair.primary, player.getUniqueId(), createdAt);
    tagChestAt(pair.secondary, player.getUniqueId(), createdAt);

    Inventory inv = combinedInventory(pair.primary);
    if (inv == null) {
        // Cleanup blocks we placed
        safeSetAir(pair.primary);
        safeSetAir(pair.secondary);
        return new SpawnResult(false, items);
    }

    Map<Integer, ItemStack> leftover = inv.addItem(items.toArray(new ItemStack[0]));
    List<ItemStack> remaining = new ArrayList<>(leftover.values());

    // Track for lava timer + cleanup checks
    DeathChestEntry entry = new DeathChestEntry(pair.primary, pair.secondary, burnDurationMs);
    active.add(entry);
    byBlock.put(key(pair.primary), entry);
    byBlock.put(key(pair.secondary), entry);

    // If it starts touching lava, start ticking immediately.
    if (burnDurationMs > 0L && isTouchingLava(entry) == LavaTouch.TOUCHING) {
        entry.burning = true;
        entry.lastTickMs = createdAt;
        updateDisplay(entry, entry.remainingMs);
    }

    return new SpawnResult(true, remaining);
}

    public void scheduleCleanupCheck(Inventory inventory) {
        if (inventory == null) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> tryCleanupInventory(inventory));
    }

    public void tryCleanupInventory(Inventory inventory) {
        if (inventory == null) {
            return;
        }
        if (!isInventoryEmpty(inventory)) {
            return;
        }

        ChestBlocks blocks = chestBlocksFromInventory(inventory);
        if (blocks == null) {
            return;
        }
        if (!isDeathChestBlock(blocks.primary.getBlock())) {
            return;
        }
        destroyDeathChest(blocks.primary, blocks.secondary);
    }

    public boolean isDeathChestBlock(Block block) {
        if (block == null || block.getType() != Material.CHEST) {
            return false;
        }
        BlockState state = block.getState();
        if (!(state instanceof Chest chest)) {
            return false;
        }
        PersistentDataContainer pdc = chest.getPersistentDataContainer();
        Byte flag = pdc.get(Keys.DEATH_CHEST, PersistentDataType.BYTE);
        return flag != null && flag == (byte) 1;
    }

    /**
     * Break handling: drops CONTENTS but never drops the chest item.
     */
    public void breakDeathChest(Block brokenBlock) {
        if (brokenBlock == null || brokenBlock.getType() != Material.CHEST) {
            return;
        }
        if (!isDeathChestBlock(brokenBlock)) {
            return;
        }
        Location primary = brokenBlock.getLocation();
        Location secondary = connectedChestLocation(brokenBlock);

        Inventory inv = combinedInventory(primary);
        if (inv == null) {
            // fallback single chest inventory
            BlockState st = primary.getBlock().getState();
            if (st instanceof Chest chest) {
                inv = chest.getBlockInventory();
            }
        }

        if (inv != null) {
            World world = primary.getWorld();
            if (world != null) {
                Location dropLoc = primary.clone().add(0.5, 0.5, 0.5);
                for (ItemStack item : inv.getContents()) {
                    if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
                        continue;
                    }
                    world.dropItemNaturally(dropLoc, item);
                }
            }
            inv.clear();
        }

        destroyDeathChest(primary, secondary);
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (var chest : new ArrayList<>(active)) {
            if (!refreshBlocks(chest)) {
                cleanupChest(chest);
                continue;
            }

            if (burnDurationMs <= 0L) {
                if (chest.burning) {
                    chest.burning = false;
                    removeDisplay(chest);
                }
                continue;
            }

            LavaTouch touch = isTouchingLava(chest);
            if (touch == LavaTouch.UNKNOWN) {
                continue;
            }

            if (touch == LavaTouch.TOUCHING) {
                if (!chest.burning) {
                    chest.burning = true;
                    chest.lastTickMs = now;
                } else {
                    long delta = Math.max(0L, now - chest.lastTickMs);
                    chest.remainingMs -= delta;
                    chest.lastTickMs = now;
                }

                if (chest.remainingMs <= 0L) {
                    chest.remainingMs = 0L;
                    chest.burning = false;
                    removeDisplay(chest);
                    continue;
                }

                updateDisplay(chest, chest.remainingMs);
            } else {
                // Not touching lava: pause + hide hologram.
                if (chest.burning) {
                    chest.burning = false;
                    chest.lastTickMs = now;
                    removeDisplay(chest);
                }
            }
        }
    }

    private boolean refreshBlocks(DeathChestEntry chest) {
        boolean hasAny = false;
        for (Location loc : chest.blocks()) {
            World world = loc.getWorld();
            if (world == null) {
                continue;
            }
            int cx = loc.getBlockX() >> 4;
            int cz = loc.getBlockZ() >> 4;
            if (!world.isChunkLoaded(cx, cz)) {
                hasAny = true;
                continue;
            }
            if (loc.getBlock().getType() == Material.CHEST) {
                hasAny = true;
            }
        }
        return hasAny;
    }

    private void cleanupChest(DeathChestEntry chest) {
        removeDisplay(chest);
        active.remove(chest);
        byBlock.remove(key(chest.primary));
        byBlock.remove(key(chest.secondary));
    }

    private void despawnChest(DeathChestEntry chest) {
        safeSetAir(chest.primary);
        safeSetAir(chest.secondary);
        removeDisplay(chest);
        active.remove(chest);
        byBlock.remove(key(chest.primary));
        byBlock.remove(key(chest.secondary));
    }

    private void destroyDeathChest(Location primary, Location secondary) {
        DeathChestEntry tracked = byBlock.get(key(primary));
        if (tracked == null && secondary != null) {
            tracked = byBlock.get(key(secondary));
        }
        if (tracked != null) {
            removeDisplay(tracked);
            active.remove(tracked);
        }
        safeSetAir(primary);
        if (secondary != null) {
            safeSetAir(secondary);
        }
        byBlock.remove(key(primary));
        if (secondary != null) {
            byBlock.remove(key(secondary));
        }
    }

    private void safeSetAir(Location loc) {
        if (loc == null) {
            return;
        }
        Block b = loc.getBlock();
        if (b.getType() == Material.CHEST) {
            BlockState state = b.getState();
            if (state instanceof Chest chest) {
                chest.getBlockInventory().clear();
            }
        }
        b.setType(Material.AIR, false);
    }

    private void updateDisplay(DeathChestEntry chest, long remainingMs) {
        if (chest.display == null || chest.display.isDead()) {
            chest.display = spawnDisplay(chest.primary);
        }
        if (chest.display == null) {
            return;
        }
        chest.display.customName(Component.text("Chest burns in " + formatTime(remainingMs), NamedTextColor.RED));
        chest.display.setCustomNameVisible(true);
    }

    private ArmorStand spawnDisplay(Location anchor) {
        World world = anchor.getWorld();
        if (world == null) {
            return null;
        }
        Location loc = anchor.clone().add(0.5, DISPLAY_Y_OFFSET, 0.5);
        var entity = world.spawnEntity(loc, EntityType.ARMOR_STAND);
        if (!(entity instanceof ArmorStand stand)) {
            return null;
        }
        stand.setVisible(false);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setPersistent(false);
        stand.setCustomNameVisible(true);
        return stand;
    }

    private void removeDisplay(DeathChestEntry chest) {
        if (chest.display != null) {
            chest.display.remove();
            chest.display = null;
        }
    }

    private LavaTouch isTouchingLava(DeathChestEntry chest) {
        boolean checked = false;
        for (Location loc : chest.blocks()) {
            World world = loc.getWorld();
            if (world == null) {
                continue;
            }
            int cx = loc.getBlockX() >> 4;
            int cz = loc.getBlockZ() >> 4;
            if (!world.isChunkLoaded(cx, cz)) {
                continue;
            }
            checked = true;
            Block block = loc.getBlock();
            if (block.getType() != Material.CHEST) {
                continue;
            }
            for (BlockFace face : LAVA_FACES) {
                Material type = block.getRelative(face).getType();
                if (type == Material.LAVA || type == Material.LAVA_CAULDRON) {
                    return LavaTouch.TOUCHING;
                }
            }
        }
        return checked ? LavaTouch.NOT_TOUCHING : LavaTouch.UNKNOWN;
    }

    private boolean isInventoryEmpty(Inventory inventory) {
        for (ItemStack it : inventory.getContents()) {
            if (it != null && !it.getType().isAir() && it.getAmount() > 0) {
                return false;
            }
        }
        return true;
    }

    private ChestBlocks chestBlocksFromInventory(Inventory inventory) {
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof DoubleChest doubleChest) {
            InventoryHolder leftHolder = doubleChest.getLeftSide();
            InventoryHolder rightHolder = doubleChest.getRightSide();
            if (!(leftHolder instanceof Chest left) || !(rightHolder instanceof Chest right)) {
                return null;
            }
            return new ChestBlocks(left.getLocation(), right.getLocation());
        }
        if (holder instanceof Chest chest) {
            Location primary = chest.getLocation();
            Location secondary = connectedChestLocation(primary.getBlock());
            return new ChestBlocks(primary, secondary);
        }
        return null;
    }

    private void tagChestAt(Location loc, UUID owner, long createdAt) {
        BlockState state = loc.getBlock().getState();
        if (!(state instanceof Chest chest)) {
            return;
        }
        PersistentDataContainer container = chest.getPersistentDataContainer();
        container.set(Keys.DEATH_CHEST, PersistentDataType.BYTE, (byte) 1);
        container.set(Keys.DEATH_CHEST_OWNER, PersistentDataType.STRING, owner.toString());
        container.set(Keys.DEATH_CHEST_CREATED, PersistentDataType.LONG, createdAt);
        chest.update(true);
    }

    private Inventory combinedInventory(Location primary) {
        BlockState state = primary.getBlock().getState();
        if (!(state instanceof Chest chest)) {
            return null;
        }
        return chest.getInventory(); // combined if double chest
    }

    private ChestPair findDoubleChestPairAtOrAbove(Location origin) {
        World world = origin.getWorld();
        if (world == null) {
            return null;
        }
    
        final int baseX = origin.getBlockX();
        final int baseY = origin.getBlockY();
        final int baseZ = origin.getBlockZ();
    
        final int minY = world.getMinHeight();
        final int maxY = world.getMaxHeight() - 1;
    
        // Search order matters:
        // - prefer exact death block first
        // - then nearby at same Y
        // - then up to +12 blocks above (never below)
        for (int radius = 0; radius <= SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                
                    int x = baseX + dx;
                    int z = baseZ + dz;
                
                    for (int dy = 0; dy <= 12; dy++) {
                        int y = baseY + dy;
                        if (y < minY || y > maxY) {
                            continue;
                        }
                    
                        Location primary = new Location(world, x, y, z);
                        if (!isValidChestSpot(primary)) {
                            continue;
                        }
                    
                        for (BlockFace face : DOUBLE_CHEST_FACES) {
                            Location secondary = primary.clone().add(face.getModX(), 0, face.getModZ());
                            if (!isValidChestSpot(secondary)) {
                                continue;
                            }
                            return new ChestPair(primary, secondary);
                        }
                    }
                }
            }
        }
    
        return null;
    }


    private boolean isValidChestSpot(Location location) {
        Block block = location.getBlock();
        Material type = block.getType();

        // Never place inside lava
        if (type == Material.LAVA) {
            return false;
        }

        // Already empty -> OK
        if (block.isEmpty()) {
            return true;
        }

        // Allow replaceable / passable non-solid blocks (tall grass, flowers, snow layer, water, etc.)
        // Disallow solid blocks (stone, dirt, logs, etc.)
        if (!type.isSolid() && block.isPassable()) {
            return true;
        }

        return false;
    }


    private boolean placeDoubleChest(ChestPair pair) {
        Block a = pair.primary.getBlock();
        Block b = pair.secondary.getBlock();
        
        // Never place inside lava
        if (a.getType() == Material.LAVA || b.getType() == Material.LAVA) {
            return false;
        }
    
        // If blocks are solid, do not overwrite terrain
        if (a.getType().isSolid() || b.getType().isSolid()) {
            return false;
        }
    
        // Clear replaceables/passables (grass, flowers, snow layer, water, etc.)
        if (!a.isEmpty()) a.setType(Material.AIR, false);
        if (!b.isEmpty()) b.setType(Material.AIR, false);
    
        // Place chests
        a.setType(Material.CHEST, false);
        b.setType(Material.CHEST, false);
    
        BlockFace connection = faceFromTo(pair.primary, pair.secondary);
        if (connection == null) {
            return false;
        }
    
        // Any consistent facing works, this keeps it stable.
        BlockFace facing = (connection == BlockFace.EAST || connection == BlockFace.WEST)
                ? BlockFace.NORTH
                : BlockFace.EAST;
    
        org.bukkit.block.data.type.Chest.Type typeA;
        org.bukkit.block.data.type.Chest.Type typeB;
    
        BlockFace right = rightOf(facing);
        boolean secondaryIsRight = (connection == right);
    
        typeA = secondaryIsRight
                ? org.bukkit.block.data.type.Chest.Type.LEFT
                : org.bukkit.block.data.type.Chest.Type.RIGHT;
    
        typeB = secondaryIsRight
                ? org.bukkit.block.data.type.Chest.Type.RIGHT
                : org.bukkit.block.data.type.Chest.Type.LEFT;
    
        applyChestData(a, facing, typeA);
        applyChestData(b, facing, typeB);
    
        return true;
    }


    private void applyChestData(Block block, BlockFace facing, org.bukkit.block.data.type.Chest.Type type) {
        BlockData data = block.getBlockData();
        if (!(data instanceof org.bukkit.block.data.type.Chest chestData)) {
            return;
        }
        chestData.setFacing(facing);
        chestData.setType(type);
        if (chestData.isWaterlogged()) {
            chestData.setWaterlogged(false);
        }
        block.setBlockData(chestData, false);
    }

    private BlockFace faceFromTo(Location from, Location to) {
        int dx = to.getBlockX() - from.getBlockX();
        int dz = to.getBlockZ() - from.getBlockZ();
        if (dx == 1 && dz == 0) return BlockFace.EAST;
        if (dx == -1 && dz == 0) return BlockFace.WEST;
        if (dx == 0 && dz == 1) return BlockFace.SOUTH;
        if (dx == 0 && dz == -1) return BlockFace.NORTH;
        return null;
    }

    private BlockFace rightOf(BlockFace facing) {
        return switch (facing) {
            case NORTH -> BlockFace.EAST;
            case SOUTH -> BlockFace.WEST;
            case EAST -> BlockFace.SOUTH;
            case WEST -> BlockFace.NORTH;
            default -> BlockFace.EAST;
        };
    }

    private BlockFace leftOf(BlockFace facing) {
        return switch (facing) {
            case NORTH -> BlockFace.WEST;
            case SOUTH -> BlockFace.EAST;
            case EAST -> BlockFace.NORTH;
            case WEST -> BlockFace.SOUTH;
            default -> BlockFace.WEST;
        };
    }

    private Location connectedChestLocation(Block chestBlock) {
        if (chestBlock == null || chestBlock.getType() != Material.CHEST) {
            return null;
        }
        BlockData data = chestBlock.getBlockData();
        if (!(data instanceof org.bukkit.block.data.type.Chest chestData)) {
            return null;
        }
        if (chestData.getType() == org.bukkit.block.data.type.Chest.Type.SINGLE) {
            return null;
        }
        BlockFace facing = chestData.getFacing();
        BlockFace offset = (chestData.getType() == org.bukkit.block.data.type.Chest.Type.LEFT) ? leftOf(facing) : rightOf(facing);
        Block other = chestBlock.getRelative(offset);
        if (other.getType() != Material.CHEST) {
            return null;
        }
        return other.getLocation();
    }

    private String key(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return "";
        }
        return world.getUID() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }

    private String formatTime(long remainingMs) {
        long totalSeconds = Math.max(0L, remainingMs / 1000L);
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private enum LavaTouch {
        TOUCHING,
        NOT_TOUCHING,
        UNKNOWN
    }

    private record ChestPair(Location primary, Location secondary) {
    }

    private record ChestBlocks(Location primary, Location secondary) {
    }

    private static final class DeathChestEntry {
        private final Location primary;
        private final Location secondary;
        private long remainingMs;
        private boolean burning;
        private long lastTickMs;
        private ArmorStand display;

        private DeathChestEntry(Location primary, Location secondary, long durationMs) {
            this.primary = primary;
            this.secondary = secondary;
            this.remainingMs = Math.max(0L, durationMs);
            this.burning = false;
            this.lastTickMs = System.currentTimeMillis();
        }

        private List<Location> blocks() {
            return List.of(primary, secondary);
        }
    }

    public record SpawnResult(boolean placed, List<ItemStack> leftover) {}
}
