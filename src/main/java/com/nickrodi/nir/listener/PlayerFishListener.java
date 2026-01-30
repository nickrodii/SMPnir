package com.nickrodi.nir.listener;

import com.nickrodi.nir.model.PlayerData;
import com.nickrodi.nir.service.FishingCollectionCatalog;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.WorldAccess;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class PlayerFishListener implements Listener {
    private static final int XP_JUNK = 10;
    private static final int XP_FISH = 40;
    private static final int XP_TREASURE = 100;

    private final ProgressionService progressionService;
    private final WorldAccess worldAccess;

    public PlayerFishListener(ProgressionService progressionService, WorldAccess worldAccess) {
        this.progressionService = progressionService;
        this.worldAccess = worldAccess;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        Player player = event.getPlayer();
        if (!worldAccess.isAllowed(player)) {
            return;
        }
        if (!(event.getCaught() instanceof Item item)) {
            return;
        }
        ItemStack stack = item.getItemStack();
        var entry = FishingCollectionCatalog.byMaterial(stack.getType());
        if (entry == null) {
            return;
        }
        if (entry.material() == Material.POTION && !isWaterBottle(stack)) {
            return;
        }

        FishingCategory category = categorize(stack);
        if (category != null) {
            int xp = switch (category) {
                case JUNK -> XP_JUNK;
                case FISH -> XP_FISH;
                case TREASURE -> XP_TREASURE;
            };
            progressionService.addXp(player.getUniqueId(), xp, "fishing");
            PlayerData data = progressionService.getData(player.getUniqueId());
            data.setFishingXpGained(data.getFishingXpGained() + xp);

            List<String> found = data.getFishingItemsFound();
            if (found == null) {
                found = new ArrayList<>();
                data.setFishingItemsFound(found);
            }
            if (!found.contains(entry.id())) {
                found.add(entry.id());
                data.setFishingCollectionsFound(found.size());
                data.setFishingXpGained(data.getFishingXpGained() + 300);
                progressionService.addXp(player.getUniqueId(), 300, "\"" + entry.displayName() + "\" added to Fishing collection");
            }
            return;
        }

        PlayerData data = progressionService.getData(player.getUniqueId());
        List<String> found = data.getFishingItemsFound();
        if (found == null) {
            found = new ArrayList<>();
            data.setFishingItemsFound(found);
        }
        if (!found.contains(entry.id())) {
            found.add(entry.id());
            data.setFishingCollectionsFound(found.size());
            data.setFishingXpGained(data.getFishingXpGained() + 300);
            progressionService.addXp(player.getUniqueId(), 300, "\"" + entry.displayName() + "\" added to Fishing collection");
        }
    }


    private FishingCategory categorize(ItemStack stack) {
        Material type = stack.getType();
        if (type == Material.COD || type == Material.SALMON || type == Material.TROPICAL_FISH || type == Material.PUFFERFISH) {
            return FishingCategory.FISH;
        }
        if (type == Material.BOW || type == Material.ENCHANTED_BOOK || type == Material.NAME_TAG
                || type == Material.NAUTILUS_SHELL || type == Material.SADDLE) {
            return FishingCategory.TREASURE;
        }
        if (type == Material.FISHING_ROD) {
            if (!stack.getEnchantments().isEmpty()) {
                return FishingCategory.TREASURE;
            }
            return FishingCategory.JUNK;
        }
        if (type == Material.LILY_PAD || type == Material.BOWL || type == Material.LEATHER
                || type == Material.LEATHER_BOOTS || type == Material.ROTTEN_FLESH || type == Material.STICK
                || type == Material.STRING || type == Material.BONE || type == Material.INK_SAC
                || type == Material.TRIPWIRE_HOOK || type == Material.BAMBOO || type == Material.COCOA_BEANS) {
            return FishingCategory.JUNK;
        }
        if (type == Material.POTION && isWaterBottle(stack)) {
            return FishingCategory.JUNK;
        }
        return null;
    }

    private enum FishingCategory {
        JUNK,
        FISH,
        TREASURE
    }

    private boolean isWaterBottle(ItemStack stack) {
        if (!(stack.getItemMeta() instanceof PotionMeta meta)) {
            return false;
        }
        return meta.getBasePotionType() == PotionType.WATER;
    }
}
