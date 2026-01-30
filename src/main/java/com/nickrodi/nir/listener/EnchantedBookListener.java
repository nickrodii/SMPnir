package com.nickrodi.nir.listener;

import com.nickrodi.nir.model.PlayerData;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.WorldAccess;
import com.nickrodi.nir.util.Keys;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EnchantedBookListener implements Listener {
    private static final int BASE_XP = 200;
    private static final int PER_LEVEL_XP = 50;
    private static final int FINAL_TIER_BONUS = 1000;

    private final ProgressionService progressionService;
    private final WorldAccess worldAccess;

    public EnchantedBookListener(ProgressionService progressionService, WorldAccess worldAccess) {
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
        ItemStack stack = event.getItem().getItemStack();
        handleBook(player, stack);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!worldAccess.isAllowed(player)) {
            return;
        }
        scanInventory(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!worldAccess.isAllowed(event.getPlayer())) {
            return;
        }
        scanInventory(event.getPlayer());
    }

    private void scanInventory(Player player) {
        ItemStack[] items = player.getInventory().getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack stack = items[i];
            if (stack == null || stack.getType() != Material.ENCHANTED_BOOK) {
                continue;
            }
            if (handleBook(player, stack)) {
                player.getInventory().setItem(i, stack);
            }
        }
    }

    private boolean handleBook(Player player, ItemStack stack) {
        if (stack == null || stack.getType() != Material.ENCHANTED_BOOK) {
            return false;
        }
        if (!(stack.getItemMeta() instanceof EnchantmentStorageMeta meta)) {
            return false;
        }
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if (data.has(Keys.BOOK_CLAIMED, PersistentDataType.BYTE)) {
            return false;
        }

        PlayerData playerData = progressionService.getData(player.getUniqueId());
        Set<String> found = playerData.getEnchantmentsFound() == null
                ? new HashSet<>()
                : new HashSet<>(playerData.getEnchantmentsFound());
        Set<String> tiersFound = playerData.getEnchantmentTiersFound() == null
                ? new HashSet<>()
                : new HashSet<>(playerData.getEnchantmentTiersFound());

        long totalXp = 0;
        for (Map.Entry<Enchantment, Integer> entry : meta.getStoredEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
            String key = enchantment.getKey().toString();
            String tierKey = key + ":" + level;
            if (tiersFound.contains(tierKey)) {
                continue;
            }
            long xp = BASE_XP + (PER_LEVEL_XP * Math.max(0, level - 1));
            if (level >= enchantment.getMaxLevel()) {
                xp += FINAL_TIER_BONUS;
            }
            totalXp += xp;
            found.add(key);
            tiersFound.add(tierKey);
            String display = enchantment.getKey().getKey().replace('_', ' ');
            String label = titleCase(display) + " " + roman(level);
            progressionService.addXp(player.getUniqueId(), xp, "\"" + label + "\" added to Enchants collection");
        }

        data.set(Keys.BOOK_CLAIMED, PersistentDataType.BYTE, (byte) 1);
        stack.setItemMeta(meta);

        if (totalXp > 0) {
            playerData.setEnchantsXpGained(playerData.getEnchantsXpGained() + totalXp);
        }

        playerData.setEnchantmentsFound(new ArrayList<>(found));
        playerData.setEnchantmentTiersFound(new ArrayList<>(tiersFound));
        return true;
    }

    private String roman(int value) {
        return switch (value) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(value);
        };
    }

    private String titleCase(String input) {
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
