package com.nickrodi.nir.service;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldAccess {
    private final String overworldName;
    private final String netherName;
    private final String endName;

    public WorldAccess(JavaPlugin plugin) {
        String base = plugin.getConfig().getString("worlds.base", "world");
        this.overworldName = base;
        this.netherName = base + "_nether";
        this.endName = base + "_the_end";
    }

    public boolean isAllowed(World world) {
        if (world == null) {
            return false;
        }
        String name = world.getName();
        return name.equals(overworldName) || name.equals(netherName) || name.equals(endName);
    }

    public boolean isAllowed(Entity entity) {
        return entity != null && isAllowed(entity.getWorld());
    }

    public boolean isAllowed(Location location) {
        return location != null && isAllowed(location.getWorld());
    }

    public boolean isAllowed(Player player) {
        return player != null && isAllowed(player.getWorld());
    }

    public boolean isNetherOrEnd(World world) {
        if (world == null) {
            return false;
        }
        String name = world.getName();
        return name.equals(netherName) || name.equals(endName);
    }
}
