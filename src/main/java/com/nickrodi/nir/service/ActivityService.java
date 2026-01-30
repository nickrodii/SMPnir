package com.nickrodi.nir.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActivityService {
    private final Map<UUID, Long> lastActiveAt = new HashMap<>();

    public void markActive(UUID uuid) {
        lastActiveAt.put(uuid, System.currentTimeMillis());
    }

    public void setLastActiveAt(UUID uuid, long timestamp) {
        lastActiveAt.put(uuid, timestamp);
    }

    public long getLastActiveAt(UUID uuid) {
        return lastActiveAt.getOrDefault(uuid, 0L);
    }

    public boolean isActive(UUID uuid, long now, long timeoutMs) {
        long last = getLastActiveAt(uuid);
        return last > 0L && (now - last) <= timeoutMs;
    }

    public void remove(UUID uuid) {
        lastActiveAt.remove(uuid);
    }
}
