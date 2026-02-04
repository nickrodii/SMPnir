package com.nickrodi.nir.model;

import java.util.List;
import java.util.Map;

public class PlayerData {
    private int level;
    private long totalXp;
    private List<String> cosmeticsUnlocked;
    private Map<String, String> cosmeticsEquipped;
    private long lastDeathAt;
    private List<String> biomesVisited;
    private List<String> enchantmentsFound;
    private List<String> enchantmentTiersFound;
    private long lastWardenKillDay;
    private int wardenKillsToday;
    private long activePlaytimeMinutes;
    private long playtimeXpGained;
    private long survivalBonusXp;
    private long lastDeathPlaytimeMinutes;
    private long oresMined;
    private long miningXpGained;
    private long cropsHarvested;
    private long cropsXpGained;
    private long mobsBred;
    private long mobsBredXpGained;
    private long mobsTamed;
    private long mobsTamedXpGained;
    private long mobsKilled;
    private long mobsXpGained;
    private long monstersKilled;
    private long monstersXpGained;
    private long dragonKills;
    private long dragonXpGained;
    private long witherKills;
    private long witherXpGained;
    private long wardenKills;
    private long wardenXpGained;
    private long playerKills;
    private long playerKillsXpGained;
    private long villagerTrades;
    private long tradeXpGained;
    private long vanillaXpSpent;
    private long vanillaXpGained;
    private long vanillaXpDay;
    private long vanillaXpDaily;
    private long advancementsDone;
    private long advancementsXpGained;
    private long questsDone;
    private long questsXpGained;
    private List<String> questsCompleted;
    private long structureChestsOpened;
    private List<String> fishingItemsFound;
    private long fishingCollectionsFound;
    private long fishingXpGained;
    private long nightEventsFound;
    private long nightEventsXpGained;
    private long buildXpGained;
    private String lastUpdateMessageVersion;
    private long biomesXpGained;
    private long structureChestsXpGained;
    private long enchantsXpGained;
    private List<String> discsFound;
    private List<String> bestiaryFound;
    private boolean deathChestEnabled;
    private String statDisplayType;

    public PlayerData() {
        this.level = 1;
        this.totalXp = 0L;
        this.lastDeathAt = 0L;
        this.lastWardenKillDay = 0L;
        this.wardenKillsToday = 0;
        this.activePlaytimeMinutes = 0L;
        this.playtimeXpGained = 0L;
        this.survivalBonusXp = 0L;
        this.lastDeathPlaytimeMinutes = 0L;
        this.oresMined = 0L;
        this.miningXpGained = 0L;
        this.cropsHarvested = 0L;
        this.cropsXpGained = 0L;
        this.mobsBred = 0L;
        this.mobsBredXpGained = 0L;
        this.mobsTamed = 0L;
        this.mobsTamedXpGained = 0L;
        this.mobsKilled = 0L;
        this.mobsXpGained = 0L;
        this.monstersKilled = 0L;
        this.monstersXpGained = 0L;
        this.dragonKills = 0L;
        this.dragonXpGained = 0L;
        this.witherKills = 0L;
        this.witherXpGained = 0L;
        this.wardenKills = 0L;
        this.wardenXpGained = 0L;
        this.playerKills = 0L;
        this.playerKillsXpGained = 0L;
        this.villagerTrades = 0L;
        this.tradeXpGained = 0L;
        this.vanillaXpSpent = 0L;
        this.vanillaXpGained = 0L;
        this.vanillaXpDay = 0L;
        this.vanillaXpDaily = 0L;
        this.advancementsDone = 0L;
        this.advancementsXpGained = 0L;
        this.questsDone = 0L;
        this.questsXpGained = 0L;
        this.questsCompleted = null;
        this.structureChestsOpened = 0L;
        this.fishingItemsFound = null;
        this.nightEventsFound = 0L;
        this.nightEventsXpGained = 0L;
        this.buildXpGained = 0L;
        this.lastUpdateMessageVersion = null;
        this.biomesXpGained = 0L;
        this.structureChestsXpGained = 0L;
        this.enchantsXpGained = 0L;
        this.discsFound = null;
        this.bestiaryFound = null;
        this.deathChestEnabled = true;
        this.statDisplayType = null;
    }

    public PlayerData(int level, long totalXp) {
        this.level = level;
        this.totalXp = totalXp;
        this.lastDeathAt = 0L;
        this.lastWardenKillDay = 0L;
        this.wardenKillsToday = 0;
        this.activePlaytimeMinutes = 0L;
        this.playtimeXpGained = 0L;
        this.survivalBonusXp = 0L;
        this.lastDeathPlaytimeMinutes = 0L;
        this.oresMined = 0L;
        this.miningXpGained = 0L;
        this.cropsHarvested = 0L;
        this.cropsXpGained = 0L;
        this.mobsBred = 0L;
        this.mobsBredXpGained = 0L;
        this.mobsTamed = 0L;
        this.mobsTamedXpGained = 0L;
        this.mobsKilled = 0L;
        this.mobsXpGained = 0L;
        this.monstersKilled = 0L;
        this.monstersXpGained = 0L;
        this.dragonKills = 0L;
        this.dragonXpGained = 0L;
        this.witherKills = 0L;
        this.witherXpGained = 0L;
        this.wardenKills = 0L;
        this.wardenXpGained = 0L;
        this.playerKills = 0L;
        this.playerKillsXpGained = 0L;
        this.villagerTrades = 0L;
        this.tradeXpGained = 0L;
        this.vanillaXpSpent = 0L;
        this.vanillaXpGained = 0L;
        this.vanillaXpDay = 0L;
        this.vanillaXpDaily = 0L;
        this.advancementsDone = 0L;
        this.advancementsXpGained = 0L;
        this.questsDone = 0L;
        this.questsXpGained = 0L;
        this.questsCompleted = null;
        this.structureChestsOpened = 0L;
        this.fishingItemsFound = null;
        this.nightEventsFound = 0L;
        this.nightEventsXpGained = 0L;
        this.buildXpGained = 0L;
        this.lastUpdateMessageVersion = null;
        this.biomesXpGained = 0L;
        this.structureChestsXpGained = 0L;
        this.enchantsXpGained = 0L;
        this.discsFound = null;
        this.bestiaryFound = null;
        this.deathChestEnabled = true;
        this.statDisplayType = null;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(long totalXp) {
        this.totalXp = totalXp;
    }

    public List<String> getCosmeticsUnlocked() {
        return cosmeticsUnlocked;
    }

    public void setCosmeticsUnlocked(List<String> cosmeticsUnlocked) {
        this.cosmeticsUnlocked = cosmeticsUnlocked;
    }

    public Map<String, String> getCosmeticsEquipped() {
        return cosmeticsEquipped;
    }

    public void setCosmeticsEquipped(Map<String, String> cosmeticsEquipped) {
        this.cosmeticsEquipped = cosmeticsEquipped;
    }

    public long getLastDeathAt() {
        return lastDeathAt;
    }

    public void setLastDeathAt(long lastDeathAt) {
        this.lastDeathAt = lastDeathAt;
    }

    public List<String> getBiomesVisited() {
        return biomesVisited;
    }

    public void setBiomesVisited(List<String> biomesVisited) {
        this.biomesVisited = biomesVisited;
    }

    public List<String> getEnchantmentsFound() {
        return enchantmentsFound;
    }

    public void setEnchantmentsFound(List<String> enchantmentsFound) {
        this.enchantmentsFound = enchantmentsFound;
    }

    public List<String> getEnchantmentTiersFound() {
        return enchantmentTiersFound;
    }

    public void setEnchantmentTiersFound(List<String> enchantmentTiersFound) {
        this.enchantmentTiersFound = enchantmentTiersFound;
    }

    public long getLastWardenKillDay() {
        return lastWardenKillDay;
    }

    public void setLastWardenKillDay(long lastWardenKillDay) {
        this.lastWardenKillDay = lastWardenKillDay;
    }

    public int getWardenKillsToday() {
        return wardenKillsToday;
    }

    public void setWardenKillsToday(int wardenKillsToday) {
        this.wardenKillsToday = wardenKillsToday;
    }

    public long getActivePlaytimeMinutes() {
        return activePlaytimeMinutes;
    }

    public void setActivePlaytimeMinutes(long activePlaytimeMinutes) {
        this.activePlaytimeMinutes = activePlaytimeMinutes;
    }

    public long getPlaytimeXpGained() {
        return playtimeXpGained;
    }

    public void setPlaytimeXpGained(long playtimeXpGained) {
        this.playtimeXpGained = playtimeXpGained;
    }

    public long getSurvivalBonusXp() {
        return survivalBonusXp;
    }

    public void setSurvivalBonusXp(long survivalBonusXp) {
        this.survivalBonusXp = survivalBonusXp;
    }

    public long getLastDeathPlaytimeMinutes() {
        return lastDeathPlaytimeMinutes;
    }

    public void setLastDeathPlaytimeMinutes(long lastDeathPlaytimeMinutes) {
        this.lastDeathPlaytimeMinutes = lastDeathPlaytimeMinutes;
    }

    public long getOresMined() {
        return oresMined;
    }

    public void setOresMined(long oresMined) {
        this.oresMined = oresMined;
    }

    public long getMiningXpGained() {
        return miningXpGained;
    }

    public void setMiningXpGained(long miningXpGained) {
        this.miningXpGained = miningXpGained;
    }

    public long getCropsHarvested() {
        return cropsHarvested;
    }

    public void setCropsHarvested(long cropsHarvested) {
        this.cropsHarvested = cropsHarvested;
    }

    public long getCropsXpGained() {
        return cropsXpGained;
    }

    public void setCropsXpGained(long cropsXpGained) {
        this.cropsXpGained = cropsXpGained;
    }

    public long getMobsBred() {
        return mobsBred;
    }

    public void setMobsBred(long mobsBred) {
        this.mobsBred = mobsBred;
    }

    public long getMobsBredXpGained() {
        return mobsBredXpGained;
    }

    public void setMobsBredXpGained(long mobsBredXpGained) {
        this.mobsBredXpGained = mobsBredXpGained;
    }

    public long getMobsTamed() {
        return mobsTamed;
    }

    public void setMobsTamed(long mobsTamed) {
        this.mobsTamed = mobsTamed;
    }

    public long getMobsTamedXpGained() {
        return mobsTamedXpGained;
    }

    public void setMobsTamedXpGained(long mobsTamedXpGained) {
        this.mobsTamedXpGained = mobsTamedXpGained;
    }

    public long getMobsKilled() {
        return mobsKilled;
    }

    public void setMobsKilled(long mobsKilled) {
        this.mobsKilled = mobsKilled;
    }

    public long getMobsXpGained() {
        return mobsXpGained;
    }

    public void setMobsXpGained(long mobsXpGained) {
        this.mobsXpGained = mobsXpGained;
    }

    public long getMonstersKilled() {
        return monstersKilled;
    }

    public void setMonstersKilled(long monstersKilled) {
        this.monstersKilled = monstersKilled;
    }

    public long getMonstersXpGained() {
        return monstersXpGained;
    }

    public void setMonstersXpGained(long monstersXpGained) {
        this.monstersXpGained = monstersXpGained;
    }

    public long getDragonKills() {
        return dragonKills;
    }

    public void setDragonKills(long dragonKills) {
        this.dragonKills = dragonKills;
    }

    public long getDragonXpGained() {
        return dragonXpGained;
    }

    public void setDragonXpGained(long dragonXpGained) {
        this.dragonXpGained = dragonXpGained;
    }

    public long getWitherKills() {
        return witherKills;
    }

    public void setWitherKills(long witherKills) {
        this.witherKills = witherKills;
    }

    public long getWitherXpGained() {
        return witherXpGained;
    }

    public void setWitherXpGained(long witherXpGained) {
        this.witherXpGained = witherXpGained;
    }

    public long getWardenKills() {
        return wardenKills;
    }

    public void setWardenKills(long wardenKills) {
        this.wardenKills = wardenKills;
    }

    public long getWardenXpGained() {
        return wardenXpGained;
    }

    public void setWardenXpGained(long wardenXpGained) {
        this.wardenXpGained = wardenXpGained;
    }

    public long getPlayerKills() {
        return playerKills;
    }

    public void setPlayerKills(long playerKills) {
        this.playerKills = playerKills;
    }

    public long getPlayerKillsXpGained() {
        return playerKillsXpGained;
    }

    public void setPlayerKillsXpGained(long playerKillsXpGained) {
        this.playerKillsXpGained = playerKillsXpGained;
    }

    public long getVillagerTrades() {
        return villagerTrades;
    }

    public void setVillagerTrades(long villagerTrades) {
        this.villagerTrades = villagerTrades;
    }

    public long getTradeXpGained() {
        return tradeXpGained;
    }

    public void setTradeXpGained(long tradeXpGained) {
        this.tradeXpGained = tradeXpGained;
    }

    public long getVanillaXpSpent() {
        return vanillaXpSpent;
    }

    public void setVanillaXpSpent(long vanillaXpSpent) {
        this.vanillaXpSpent = vanillaXpSpent;
    }

    public long getVanillaXpGained() {
        return vanillaXpGained;
    }

    public void setVanillaXpGained(long vanillaXpGained) {
        this.vanillaXpGained = vanillaXpGained;
    }

    public long getVanillaXpDay() {
        return vanillaXpDay;
    }

    public void setVanillaXpDay(long vanillaXpDay) {
        this.vanillaXpDay = vanillaXpDay;
    }

    public long getVanillaXpDaily() {
        return vanillaXpDaily;
    }

    public void setVanillaXpDaily(long vanillaXpDaily) {
        this.vanillaXpDaily = vanillaXpDaily;
    }

    public long getAdvancementsDone() {
        return advancementsDone;
    }

    public void setAdvancementsDone(long advancementsDone) {
        this.advancementsDone = advancementsDone;
    }

    public long getAdvancementsXpGained() {
        return advancementsXpGained;
    }

    public void setAdvancementsXpGained(long advancementsXpGained) {
        this.advancementsXpGained = advancementsXpGained;
    }

    public long getQuestsDone() {
        return questsDone;
    }

    public void setQuestsDone(long questsDone) {
        this.questsDone = questsDone;
    }

    public long getQuestsXpGained() {
        return questsXpGained;
    }

    public void setQuestsXpGained(long questsXpGained) {
        this.questsXpGained = questsXpGained;
    }

    public List<String> getQuestsCompleted() {
        return questsCompleted;
    }

    public void setQuestsCompleted(List<String> questsCompleted) {
        this.questsCompleted = questsCompleted;
    }

    public long getStructureChestsOpened() {
        return structureChestsOpened;
    }

    public void setStructureChestsOpened(long structureChestsOpened) {
        this.structureChestsOpened = structureChestsOpened;
    }

    public long getFishingCollectionsFound() {
        return fishingCollectionsFound;
    }

    public List<String> getFishingItemsFound() {
        return fishingItemsFound;
    }

    public void setFishingItemsFound(List<String> fishingItemsFound) {
        this.fishingItemsFound = fishingItemsFound;
    }

    public void setFishingCollectionsFound(long fishingCollectionsFound) {
        this.fishingCollectionsFound = fishingCollectionsFound;
    }

    public long getFishingXpGained() {
        return fishingXpGained;
    }

    public void setFishingXpGained(long fishingXpGained) {
        this.fishingXpGained = fishingXpGained;
    }

    public long getNightEventsFound() {
        return nightEventsFound;
    }

    public void setNightEventsFound(long nightEventsFound) {
        this.nightEventsFound = nightEventsFound;
    }

    public long getNightEventsXpGained() {
        return nightEventsXpGained;
    }

    public void setNightEventsXpGained(long nightEventsXpGained) {
        this.nightEventsXpGained = nightEventsXpGained;
    }

    public long getBuildXpGained() {
        return buildXpGained;
    }

    public void setBuildXpGained(long buildXpGained) {
        this.buildXpGained = buildXpGained;
    }

    public String getLastUpdateMessageVersion() {
        return lastUpdateMessageVersion;
    }

    public void setLastUpdateMessageVersion(String lastUpdateMessageVersion) {
        this.lastUpdateMessageVersion = lastUpdateMessageVersion;
    }

    public long getBiomesXpGained() {
        return biomesXpGained;
    }

    public void setBiomesXpGained(long biomesXpGained) {
        this.biomesXpGained = biomesXpGained;
    }

    public long getStructureChestsXpGained() {
        return structureChestsXpGained;
    }

    public void setStructureChestsXpGained(long structureChestsXpGained) {
        this.structureChestsXpGained = structureChestsXpGained;
    }

    public long getEnchantsXpGained() {
        return enchantsXpGained;
    }

    public void setEnchantsXpGained(long enchantsXpGained) {
        this.enchantsXpGained = enchantsXpGained;
    }

    public List<String> getDiscsFound() {
        return discsFound;
    }

    public void setDiscsFound(List<String> discsFound) {
        this.discsFound = discsFound;
    }

    public List<String> getBestiaryFound() {
        return bestiaryFound;
    }

    public void setBestiaryFound(List<String> bestiaryFound) {
        this.bestiaryFound = bestiaryFound;
    }

    public boolean isDeathChestEnabled() {
        return deathChestEnabled;
    }

    public void setDeathChestEnabled(boolean deathChestEnabled) {
        this.deathChestEnabled = deathChestEnabled;
    }

    public String getStatDisplayType() {
        return statDisplayType;
    }

    public void setStatDisplayType(String statDisplayType) {
        this.statDisplayType = statDisplayType;
    }
}
