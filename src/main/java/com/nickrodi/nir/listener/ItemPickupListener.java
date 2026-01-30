package com.nickrodi.nir.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.nickrodi.nir.service.DiscCollectionCatalog;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.WorldAccess;
import com.nickrodi.nir.util.Keys;

/**
 * Handles item pickup-related rewards.
 *
 * NOTE: Harvest XP is no longer awarded on pickup. It is awarded directly on crop break
 * in BlockListener (fully grown + not player-placed).
 */
public class ItemPickupListener implements Listener {
    private final ProgressionService progressionService;
    private final WorldAccess worldAccess;

    public ItemPickupListener(ProgressionService progressionService, WorldAccess worldAccess) {
        this.progressionService = progressionService;
        this.worldAccess = worldAccess;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!worldAccess.isAllowed(player)) {
            return;
        }

        Material type = event.getItem().getItemStack().getType();

        // Disc collection XP
        if (type.isRecord()) {
            ItemMeta meta = event.getItem().getItemStack().getItemMeta();
            if (meta != null) {
                PersistentDataContainer metaData = meta.getPersistentDataContainer();
                if (metaData.has(Keys.DISC_COLLECTED, PersistentDataType.BYTE)) {
                    return;
                }
            }

            var data = progressionService.getData(player.getUniqueId());
            var discs = data.getDiscsFound();
            if (discs == null) {
                discs = new java.util.ArrayList<>();
            }

            String id = type.getKey().toString();
            if (!discs.contains(id)) {
                discs.add(id);
                data.setDiscsFound(discs);

                String name = type.getKey().getKey().replace('_', ' ');
                DiscCollectionCatalog.DiscEntry entry = DiscCollectionCatalog.byMaterial(type);
                if (entry != null) {
                    name = entry.displayName();
                }
                String label = toTitleCase(name);
                progressionService.addXp(player.getUniqueId(), 600, "\"" + label + "\" added to Discs collection");
            }

            if (meta != null) {
                meta.getPersistentDataContainer().set(Keys.DISC_COLLECTED, PersistentDataType.BYTE, (byte) 1);
                var stack = event.getItem().getItemStack();
                stack.setItemMeta(meta);
                event.getItem().setItemStack(stack);
            }
        }
    }

    private String toTitleCase(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String[] parts = input.trim().toLowerCase(java.util.Locale.US).split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }
}
