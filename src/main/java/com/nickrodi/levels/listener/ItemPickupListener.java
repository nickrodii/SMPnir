package com.nickrodi.levels.listener;

import com.nickrodi.levels.service.ProgressionService;
import com.nickrodi.levels.service.WorldAccess;
import com.nickrodi.levels.service.DiscCollectionCatalog;
import com.nickrodi.levels.util.Keys;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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
        PersistentDataContainer itemData = event.getItem().getPersistentDataContainer();
        PersistentDataContainer metaData = null;
        ItemMeta itemMeta = event.getItem().getItemStack().getItemMeta();
        if (itemMeta != null) {
            metaData = itemMeta.getPersistentDataContainer();
        }

        boolean entityHas = itemData.has(Keys.CROP_XP_VALUE, PersistentDataType.INTEGER);
        boolean metaHas = metaData != null && metaData.has(Keys.CROP_XP_VALUE, PersistentDataType.INTEGER);
        byte awardedEntity = itemData.getOrDefault(Keys.CROP_XP_AWARDED, PersistentDataType.BYTE, (byte) 0);
        byte awardedMeta = metaData != null ? metaData.getOrDefault(Keys.CROP_XP_AWARDED, PersistentDataType.BYTE, (byte) 0) : 0;
        boolean alreadyAwarded = awardedEntity != 0 || awardedMeta != 0;

        if ((entityHas || metaHas) && !alreadyAwarded) {
            int xp = entityHas
                    ? itemData.getOrDefault(Keys.CROP_XP_VALUE, PersistentDataType.INTEGER, 0)
                    : metaData.getOrDefault(Keys.CROP_XP_VALUE, PersistentDataType.INTEGER, 0);
            int count = entityHas
                    ? itemData.getOrDefault(Keys.CROP_XP_COUNT, PersistentDataType.INTEGER, 1)
                    : metaData.getOrDefault(Keys.CROP_XP_COUNT, PersistentDataType.INTEGER, 1);
            if (xp > 0) {
                progressionService.addXp(player.getUniqueId(), xp, "harvest");
                var playerData = progressionService.getData(player.getUniqueId());
                playerData.setCropsHarvested(playerData.getCropsHarvested() + count);
                playerData.setCropsXpGained(playerData.getCropsXpGained() + xp);
            }
            itemData.set(Keys.CROP_XP_AWARDED, PersistentDataType.BYTE, (byte) 1);
            if (itemMeta != null) {
                itemMeta.getPersistentDataContainer().set(Keys.CROP_XP_AWARDED, PersistentDataType.BYTE, (byte) 1);
                var stack = event.getItem().getItemStack();
                stack.setItemMeta(itemMeta);
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
