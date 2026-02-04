package com.nickrodi.nir.service;

import com.nickrodi.nir.model.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

public class StorageService {
    private final JavaPlugin plugin;
    private final File playersDir;

    public StorageService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playersDir = new File(plugin.getDataFolder(), "players");
    }

    public PlayerData load(UUID uuid) {
        ensurePlayersDir();
        File file = getPlayerFile(uuid);
        if (!file.exists()) {
            return new PlayerData();
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        int level = config.getInt("level", 1);
        long totalXp = config.getLong("totalXp", 0L);
        long lastDeathAt = config.getLong("lastDeathAt", 0L);
        long lastWardenKillDay = config.getLong("lastWardenKillDay", 0L);
        int wardenKillsToday = config.getInt("wardenKillsToday", 0);
        long activePlaytimeMinutes = config.getLong("activePlaytimeMinutes", 0L);
        long playtimeXpGained = config.getLong("playtimeXpGained", 0L);
        long survivalBonusXp = config.getLong("survivalBonusXp", 0L);
        long lastDeathPlaytimeMinutes = config.getLong("lastDeathPlaytimeMinutes", -1L);
        long oresMined = config.getLong("oresMined", 0L);
        long miningXpGained = config.getLong("miningXpGained", 0L);
        long cropsHarvested = config.getLong("cropsHarvested", 0L);
        long cropsXpGained = config.getLong("cropsXpGained", 0L);
        long mobsBred = config.getLong("mobsBred", 0L);
        long mobsBredXpGained = config.getLong("mobsBredXpGained", 0L);
        long mobsTamed = config.getLong("mobsTamed", 0L);
        long mobsTamedXpGained = config.getLong("mobsTamedXpGained", 0L);
        long mobsKilled = config.getLong("mobsKilled", 0L);
        long mobsXpGained = config.getLong("mobsXpGained", 0L);
        long monstersKilled = config.getLong("monstersKilled", 0L);
        long monstersXpGained = config.getLong("monstersXpGained", 0L);
        long dragonKills = config.getLong("dragonKills", 0L);
        long dragonXpGained = config.getLong("dragonXpGained", 0L);
        long witherKills = config.getLong("witherKills", 0L);
        long witherXpGained = config.getLong("witherXpGained", 0L);
        long wardenKills = config.getLong("wardenKills", 0L);
        long wardenXpGained = config.getLong("wardenXpGained", 0L);
        long playerKills = config.getLong("playerKills", 0L);
        long playerKillsXpGained = config.getLong("playerKillsXpGained", 0L);
        long villagerTrades = config.getLong("villagerTrades", 0L);
        long tradeXpGained = config.getLong("tradeXpGained", 0L);
        long vanillaXpSpent = config.getLong("vanillaXpSpent", 0L);
        long vanillaXpGained = config.getLong("vanillaXpGained", 0L);
        long vanillaXpDay = config.getLong("vanillaXpDay", 0L);
        long vanillaXpDaily = config.getLong("vanillaXpDaily", 0L);
        long advancementsDone = config.getLong("advancementsDone", 0L);
        long advancementsXpGained = config.getLong("advancementsXpGained", 0L);
        long questsDone = config.getLong("questsDone", 0L);
        long questsXpGained = config.getLong("questsXpGained", 0L);
        long structureChestsOpened = config.getLong("structureChestsOpened", 0L);
        long fishingCollectionsFound = config.getLong("fishingCollectionsFound", 0L);
        long fishingXpGained = config.getLong("fishingXpGained", 0L);
        java.util.List<String> fishingItemsFound = config.getStringList("fishingItemsFound");
        long nightEventsFound = config.getLong("nightEventsFound", 0L);
        long nightEventsXpGained = config.getLong("nightEventsXpGained", 0L);
        long buildXpGained = config.getLong("buildXpGained", 0L);
        String lastUpdateMessageVersion = config.getString("lastUpdateMessageVersion", null);
        long biomesXpGained = config.getLong("biomesXpGained", 0L);
        long structureChestsXpGained = config.getLong("structureChestsXpGained", 0L);
        long enchantsXpGained = config.getLong("enchantsXpGained", 0L);
        boolean deathChestEnabled = config.getBoolean("deathChestEnabled", true);
        String statDisplayType = config.getString("statDisplayType", null);
        List<String> discsFound = config.getStringList("discsFound");
        List<String> bestiaryFound = config.getStringList("bestiaryFound");

        PlayerData data = new PlayerData(level, totalXp);
        data.setLastDeathAt(lastDeathAt);
        data.setLastWardenKillDay(lastWardenKillDay);
        data.setWardenKillsToday(wardenKillsToday);
        data.setActivePlaytimeMinutes(activePlaytimeMinutes);
        data.setPlaytimeXpGained(playtimeXpGained);
        data.setSurvivalBonusXp(survivalBonusXp);
        if (lastDeathPlaytimeMinutes < 0L) {
            data.setLastDeathPlaytimeMinutes(activePlaytimeMinutes);
        } else {
            data.setLastDeathPlaytimeMinutes(lastDeathPlaytimeMinutes);
        }
        data.setOresMined(oresMined);
        data.setMiningXpGained(miningXpGained);
        data.setCropsHarvested(cropsHarvested);
        data.setCropsXpGained(cropsXpGained);
        data.setMobsBred(mobsBred);
        data.setMobsBredXpGained(mobsBredXpGained);
        data.setMobsTamed(mobsTamed);
        data.setMobsTamedXpGained(mobsTamedXpGained);
        data.setMobsKilled(mobsKilled);
        data.setMobsXpGained(mobsXpGained);
        data.setMonstersKilled(monstersKilled);
        data.setMonstersXpGained(monstersXpGained);
        data.setDragonKills(dragonKills);
        data.setDragonXpGained(dragonXpGained);
        data.setWitherKills(witherKills);
        data.setWitherXpGained(witherXpGained);
        data.setWardenKills(wardenKills);
        data.setWardenXpGained(wardenXpGained);
        data.setPlayerKills(playerKills);
        data.setPlayerKillsXpGained(playerKillsXpGained);
        data.setVillagerTrades(villagerTrades);
        data.setTradeXpGained(tradeXpGained);
        data.setVanillaXpSpent(vanillaXpSpent);
        data.setVanillaXpGained(vanillaXpGained);
        data.setVanillaXpDay(vanillaXpDay);
        data.setVanillaXpDaily(vanillaXpDaily);
        data.setAdvancementsDone(advancementsDone);
        data.setAdvancementsXpGained(advancementsXpGained);
        data.setQuestsDone(questsDone);
        data.setQuestsXpGained(questsXpGained);
        data.setStructureChestsOpened(structureChestsOpened);
        data.setFishingCollectionsFound(fishingCollectionsFound);
        data.setFishingXpGained(fishingXpGained);
        data.setFishingItemsFound(fishingItemsFound);
        data.setNightEventsFound(nightEventsFound);
        data.setNightEventsXpGained(nightEventsXpGained);
        data.setBuildXpGained(buildXpGained);
        data.setLastUpdateMessageVersion(lastUpdateMessageVersion);
        data.setBiomesXpGained(biomesXpGained);
        data.setStructureChestsXpGained(structureChestsXpGained);
        data.setEnchantsXpGained(enchantsXpGained);
        data.setDeathChestEnabled(deathChestEnabled);
        data.setStatDisplayType(statDisplayType);
        if (discsFound != null && !discsFound.isEmpty()) {
            data.setDiscsFound(discsFound);
        }
        if (bestiaryFound != null && !bestiaryFound.isEmpty()) {
            data.setBestiaryFound(bestiaryFound);
        }

        if (config.contains("cosmeticsUnlocked")) {
            List<String> cosmeticsUnlocked = config.getStringList("cosmeticsUnlocked");
            data.setCosmeticsUnlocked(cosmeticsUnlocked);
        }

        if (config.contains("cosmeticsEquipped")) {
            ConfigurationSection section = config.getConfigurationSection("cosmeticsEquipped");
            if (section != null) {
                Map<String, String> equipped = new HashMap<>();
                for (String key : section.getKeys(false)) {
                    Object value = section.get(key);
                    if (value != null) {
                        equipped.put(key, String.valueOf(value));
                    }
                }
                if (!equipped.isEmpty()) {
                    data.setCosmeticsEquipped(equipped);
                }
            }
        }

        if (config.contains("biomesVisited")) {
            List<String> biomesVisited = config.getStringList("biomesVisited");
            data.setBiomesVisited(biomesVisited);
        }

        if (config.contains("enchantmentsFound")) {
            List<String> enchantmentsFound = config.getStringList("enchantmentsFound");
            data.setEnchantmentsFound(enchantmentsFound);
        }

        if (config.contains("enchantmentTiersFound")) {
            List<String> enchantmentTiersFound = config.getStringList("enchantmentTiersFound");
            data.setEnchantmentTiersFound(enchantmentTiersFound);
        }

        if (config.contains("questsCompleted")) {
            List<String> questsCompleted = config.getStringList("questsCompleted");
            data.setQuestsCompleted(questsCompleted);
        }

        return data;
    }

    public void save(UUID uuid, PlayerData data) {
        ensurePlayersDir();
        File file = getPlayerFile(uuid);
        YamlConfiguration config = new YamlConfiguration();
        config.set("level", data.getLevel());
        config.set("totalXp", data.getTotalXp());
        config.set("lastDeathAt", data.getLastDeathAt());
        config.set("lastWardenKillDay", data.getLastWardenKillDay());
        config.set("wardenKillsToday", data.getWardenKillsToday());
        config.set("activePlaytimeMinutes", data.getActivePlaytimeMinutes());
        config.set("playtimeXpGained", data.getPlaytimeXpGained());
        config.set("survivalBonusXp", data.getSurvivalBonusXp());
        config.set("lastDeathPlaytimeMinutes", data.getLastDeathPlaytimeMinutes());
        config.set("oresMined", data.getOresMined());
        config.set("miningXpGained", data.getMiningXpGained());
        config.set("cropsHarvested", data.getCropsHarvested());
        config.set("cropsXpGained", data.getCropsXpGained());
        config.set("mobsBred", data.getMobsBred());
        config.set("mobsBredXpGained", data.getMobsBredXpGained());
        config.set("mobsTamed", data.getMobsTamed());
        config.set("mobsTamedXpGained", data.getMobsTamedXpGained());
        config.set("mobsKilled", data.getMobsKilled());
        config.set("mobsXpGained", data.getMobsXpGained());
        config.set("monstersKilled", data.getMonstersKilled());
        config.set("monstersXpGained", data.getMonstersXpGained());
        config.set("dragonKills", data.getDragonKills());
        config.set("dragonXpGained", data.getDragonXpGained());
        config.set("witherKills", data.getWitherKills());
        config.set("witherXpGained", data.getWitherXpGained());
        config.set("wardenKills", data.getWardenKills());
        config.set("wardenXpGained", data.getWardenXpGained());
        config.set("playerKills", data.getPlayerKills());
        config.set("playerKillsXpGained", data.getPlayerKillsXpGained());
        config.set("villagerTrades", data.getVillagerTrades());
        config.set("tradeXpGained", data.getTradeXpGained());
        config.set("vanillaXpSpent", data.getVanillaXpSpent());
        config.set("vanillaXpGained", data.getVanillaXpGained());
        config.set("vanillaXpDay", data.getVanillaXpDay());
        config.set("vanillaXpDaily", data.getVanillaXpDaily());
        config.set("advancementsDone", data.getAdvancementsDone());
        config.set("advancementsXpGained", data.getAdvancementsXpGained());
        config.set("questsDone", data.getQuestsDone());
        config.set("questsXpGained", data.getQuestsXpGained());
        config.set("structureChestsOpened", data.getStructureChestsOpened());
        config.set("fishingCollectionsFound", data.getFishingCollectionsFound());
        config.set("fishingXpGained", data.getFishingXpGained());
        config.set("fishingItemsFound", data.getFishingItemsFound());
        config.set("nightEventsFound", data.getNightEventsFound());
        config.set("nightEventsXpGained", data.getNightEventsXpGained());
        config.set("buildXpGained", data.getBuildXpGained());
        if (data.getLastUpdateMessageVersion() != null && !data.getLastUpdateMessageVersion().isBlank()) {
            config.set("lastUpdateMessageVersion", data.getLastUpdateMessageVersion());
        }
        config.set("biomesXpGained", data.getBiomesXpGained());
        config.set("structureChestsXpGained", data.getStructureChestsXpGained());
        config.set("enchantsXpGained", data.getEnchantsXpGained());
        config.set("deathChestEnabled", data.isDeathChestEnabled());
        if (data.getStatDisplayType() != null && !data.getStatDisplayType().isBlank()) {
            config.set("statDisplayType", data.getStatDisplayType());
        }

        List<String> cosmeticsUnlocked = data.getCosmeticsUnlocked();
        if (cosmeticsUnlocked != null && !cosmeticsUnlocked.isEmpty()) {
            config.set("cosmeticsUnlocked", cosmeticsUnlocked);
        }

        Map<String, String> cosmeticsEquipped = data.getCosmeticsEquipped();
        if (cosmeticsEquipped != null && !cosmeticsEquipped.isEmpty()) {
            config.createSection("cosmeticsEquipped", new HashMap<>(cosmeticsEquipped));
        }

        List<String> biomesVisited = data.getBiomesVisited();
        if (biomesVisited != null && !biomesVisited.isEmpty()) {
            config.set("biomesVisited", biomesVisited);
        }

        List<String> enchantmentsFound = data.getEnchantmentsFound();
        if (enchantmentsFound != null && !enchantmentsFound.isEmpty()) {
            config.set("enchantmentsFound", enchantmentsFound);
        }

        List<String> enchantmentTiersFound = data.getEnchantmentTiersFound();
        if (enchantmentTiersFound != null && !enchantmentTiersFound.isEmpty()) {
            config.set("enchantmentTiersFound", enchantmentTiersFound);
        }

        List<String> questsCompleted = data.getQuestsCompleted();
        if (questsCompleted != null && !questsCompleted.isEmpty()) {
            config.set("questsCompleted", questsCompleted);
        }

        List<String> discsFound = data.getDiscsFound();
        if (discsFound != null && !discsFound.isEmpty()) {
            config.set("discsFound", discsFound);
        }

        List<String> bestiaryFound = data.getBestiaryFound();
        if (bestiaryFound != null && !bestiaryFound.isEmpty()) {
            config.set("bestiaryFound", bestiaryFound);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(
                    Level.WARNING,
                    () -> "Failed to save player data for " + uuid + ": " + e.getMessage()
            );
        }
    }

    private void ensurePlayersDir() {
        if (!playersDir.exists() && !playersDir.mkdirs()) {
            plugin.getLogger().log(
                    Level.WARNING,
                    () -> "Failed to create players data directory: " + playersDir.getAbsolutePath()
            );
        }
    }

    public List<UUID> listPlayerUuids() {
        ensurePlayersDir();
        File[] files = playersDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            return List.of();
        }
        List<UUID> uuids = new ArrayList<>(files.length);
        for (File file : files) {
            String name = file.getName();
            String raw = name.substring(0, name.length() - 4);
            try {
                uuids.add(UUID.fromString(raw));
            } catch (IllegalArgumentException ignored) {
                // skip invalid filenames
            }
        }
        return uuids;
    }

    private File getPlayerFile(UUID uuid) {
        return new File(playersDir, uuid + ".yml");
    }
}
