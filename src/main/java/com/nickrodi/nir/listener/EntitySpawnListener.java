package com.nickrodi.nir.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.nickrodi.nir.service.WorldAccess;
import com.nickrodi.nir.util.Keys;

public class EntitySpawnListener implements Listener {
    private final WorldAccess worldAccess;

    public EntitySpawnListener(WorldAccess worldAccess) {
        this.worldAccess = worldAccess;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!worldAccess.isAllowed(event.getLocation())) {
            return;
        }
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER
                || reason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
            PersistentDataContainer data = event.getEntity().getPersistentDataContainer();
            data.set(Keys.SPAWNER_TAG, PersistentDataType.BYTE, (byte) 1);
        }
    }
}
