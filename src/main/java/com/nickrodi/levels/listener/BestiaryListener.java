package com.nickrodi.levels.listener;

import com.nickrodi.levels.service.BestiaryCatalog;
import com.nickrodi.levels.service.BestiaryService;
import com.nickrodi.levels.service.ProgressionService;
import com.nickrodi.levels.service.WorldAccess;
import com.nickrodi.levels.model.PlayerData;
import io.papermc.paper.event.player.PlayerTradeEvent;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Camel;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BestiaryListener implements Listener {
    private static final int BESTIARY_XP = 300;
    private static final Map<EntityType, String> KILL_IDS = new HashMap<>();
    private static final Map<String, String> CUSTOM_NAME_IDS = Map.of(
            "parched", "parched",
            "zombie nautilus", "zombie_nautilus",
            "copper golem", "copper_golem",
            "nautilus", "nautilus",
            "happy ghast", "happy_ghast"
    );
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    static {
        KILL_IDS.put(EntityType.ARMADILLO, "armadillo");
        KILL_IDS.put(EntityType.BAT, "bat");
        KILL_IDS.put(EntityType.CAMEL, "camel");
        KILL_IDS.put(EntityType.CHICKEN, "chicken");
        KILL_IDS.put(EntityType.COW, "cow");
        KILL_IDS.put(EntityType.DONKEY, "donkey");
        KILL_IDS.put(EntityType.GLOW_SQUID, "glow_squid");
        KILL_IDS.put(EntityType.MOOSHROOM, "mooshroom");
        KILL_IDS.put(EntityType.MULE, "mule");
        KILL_IDS.put(EntityType.OCELOT, "ocelot");
        KILL_IDS.put(EntityType.PARROT, "parrot");
        KILL_IDS.put(EntityType.PIG, "pig");
        KILL_IDS.put(EntityType.RABBIT, "rabbit");
        KILL_IDS.put(EntityType.SHEEP, "sheep");
        KILL_IDS.put(EntityType.SKELETON_HORSE, "skeleton_horse");
        KILL_IDS.put(EntityType.SNIFFER, "sniffer");
        KILL_IDS.put(EntityType.SQUID, "squid");
        KILL_IDS.put(EntityType.TURTLE, "turtle");
        KILL_IDS.put(EntityType.WANDERING_TRADER, "wandering_trader");
        KILL_IDS.put(EntityType.ZOMBIE_HORSE, "zombie_horse");
        KILL_IDS.put(EntityType.BEE, "bee");
        KILL_IDS.put(EntityType.CAVE_SPIDER, "cave_spider");
        KILL_IDS.put(EntityType.DOLPHIN, "dolphin");
        KILL_IDS.put(EntityType.DROWNED, "drowned");
        KILL_IDS.put(EntityType.ENDERMAN, "enderman");
        KILL_IDS.put(EntityType.GOAT, "goat");
        KILL_IDS.put(EntityType.IRON_GOLEM, "iron_golem");
        KILL_IDS.put(EntityType.LLAMA, "llama");
        KILL_IDS.put(EntityType.PANDA, "panda");
        KILL_IDS.put(EntityType.POLAR_BEAR, "polar_bear");
        KILL_IDS.put(EntityType.SPIDER, "spider");
        KILL_IDS.put(EntityType.TRADER_LLAMA, "trader_llama");
        KILL_IDS.put(EntityType.ZOMBIFIED_PIGLIN, "zombified_piglin");
        KILL_IDS.put(EntityType.BLAZE, "blaze");
        KILL_IDS.put(EntityType.BOGGED, "bogged");
        KILL_IDS.put(EntityType.BREEZE, "breeze");
        KILL_IDS.put(EntityType.CREAKING, "creaking");
        KILL_IDS.put(EntityType.CREEPER, "creeper");
        KILL_IDS.put(EntityType.ELDER_GUARDIAN, "elder_guardian");
        KILL_IDS.put(EntityType.ENDER_DRAGON, "ender_dragon");
        KILL_IDS.put(EntityType.ENDERMITE, "endermite");
        KILL_IDS.put(EntityType.EVOKER, "evoker");
        KILL_IDS.put(EntityType.GHAST, "ghast");
        KILL_IDS.put(EntityType.GUARDIAN, "guardian");
        KILL_IDS.put(EntityType.HOGLIN, "hoglin");
        KILL_IDS.put(EntityType.HUSK, "husk");
        KILL_IDS.put(EntityType.MAGMA_CUBE, "magma_cube");
        KILL_IDS.put(EntityType.PHANTOM, "phantom");
        KILL_IDS.put(EntityType.PIGLIN_BRUTE, "piglin_brute");
        KILL_IDS.put(EntityType.PILLAGER, "pillager");
        KILL_IDS.put(EntityType.RAVAGER, "ravager");
        KILL_IDS.put(EntityType.SHULKER, "shulker");
        KILL_IDS.put(EntityType.SILVERFISH, "silverfish");
        KILL_IDS.put(EntityType.SKELETON, "skeleton");
        KILL_IDS.put(EntityType.SLIME, "slime");
        KILL_IDS.put(EntityType.STRAY, "stray");
        KILL_IDS.put(EntityType.VEX, "vex");
        KILL_IDS.put(EntityType.VINDICATOR, "vindicator");
        KILL_IDS.put(EntityType.WARDEN, "warden");
        KILL_IDS.put(EntityType.WITCH, "witch");
        KILL_IDS.put(EntityType.WITHER, "wither");
        KILL_IDS.put(EntityType.WITHER_SKELETON, "wither_skeleton");
        KILL_IDS.put(EntityType.ZOGLIN, "zoglin");
        KILL_IDS.put(EntityType.ZOMBIE, "zombie");
        KILL_IDS.put(EntityType.ZOMBIE_VILLAGER, "zombie_villager");
    }

    private final ProgressionService progressionService;
    private final WorldAccess worldAccess;

    public BestiaryListener(ProgressionService progressionService, WorldAccess worldAccess) {
        this.progressionService = progressionService;
        this.worldAccess = worldAccess;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        if (!worldAccess.isAllowed(killer)) {
            return;
        }
        LivingEntity entity = event.getEntity();
        markCustomName(entity, killer);
        if (entity.getType() == EntityType.HUSK && entity.getVehicle() instanceof Camel) {
            mark(killer, "camel_husk");
        }
        if (entity instanceof Cat cat) {
            mark(killer, catId(cat.getCatType()));
            return;
        }
        if (entity instanceof Frog frog) {
            mark(killer, frogId(frog.getVariant()));
            return;
        }
        if (entity instanceof Horse horse) {
            String id = horseId(horse);
            if (id != null) {
                mark(killer, id);
            }
            return;
        }
        if (entity instanceof Fox fox) {
            mark(killer, foxId(fox.getFoxType()));
            return;
        }
        if (entity instanceof Wolf wolf) {
            String id = wolfId(wolf);
            if (id != null) {
                mark(killer, id);
            }
            return;
        }
        if (entity instanceof Allay) {
            return;
        }
        if (entity.getType() == EntityType.AXOLOTL
                || entity.getType() == EntityType.COD
                || entity.getType() == EntityType.SALMON
                || entity.getType() == EntityType.TROPICAL_FISH
                || entity.getType() == EntityType.TADPOLE
                || entity.getType() == EntityType.PUFFERFISH) {
            return;
        }
        if (entity.getType() == EntityType.STRIDER) {
            return;
        }
        if (entity.getType() == EntityType.VILLAGER) {
            return;
        }
        if (entity.getType() == EntityType.PIGLIN) {
            return;
        }

        String id = KILL_IDS.get(entity.getType());
        if (id != null) {
            mark(killer, id);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player player)) {
            return;
        }
        if (!worldAccess.isAllowed(player)) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Cat cat) {
            mark(player, catId(cat.getCatType()));
            return;
        }
        if (entity instanceof Frog frog) {
            mark(player, frogId(frog.getVariant()));
            return;
        }
        if (entity instanceof Horse horse) {
            String id = horseId(horse);
            if (id != null) {
                mark(player, id);
            }
            return;
        }
        if (entity instanceof Fox fox) {
            mark(player, foxId(fox.getFoxType()));
            return;
        }
        if (entity instanceof Wolf wolf) {
            String id = wolfId(wolf);
            if (id != null) {
                mark(player, id);
            }
            return;
        }
        String id = KILL_IDS.get(entity.getType());
        if (id != null) {
            mark(player, id);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTame(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player player)) {
            return;
        }
        if (!worldAccess.isAllowed(player)) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Cat cat) {
            mark(player, catId(cat.getCatType()));
            return;
        }
        if (entity instanceof Fox fox) {
            mark(player, foxId(fox.getFoxType()));
            return;
        }
        if (entity instanceof Wolf wolf) {
            String id = wolfId(wolf);
            if (id != null) {
                mark(player, id);
            }
            return;
        }
        if (entity instanceof Horse horse) {
            String id = horseId(horse);
            if (id != null) {
                mark(player, id);
            }
            return;
        }
        String id = KILL_IDS.get(entity.getType());
        if (id != null) {
            mark(player, id);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucket(PlayerBucketEntityEvent event) {
        if (!worldAccess.isAllowed(event.getPlayer())) {
            return;
        }
        Entity entity = event.getEntity();
        Player player = event.getPlayer();
        if (entity.getType() == EntityType.AXOLOTL) {
            mark(player, "axolotl");
            return;
        }
        if (entity.getType() == EntityType.TADPOLE) {
            mark(player, "tadpole");
            return;
        }
        if (entity.getType() == EntityType.COD) {
            mark(player, "cod");
            return;
        }
        if (entity.getType() == EntityType.SALMON) {
            mark(player, "salmon");
            return;
        }
        if (entity.getType() == EntityType.PUFFERFISH) {
            mark(player, "pufferfish");
            return;
        }
        if (entity instanceof TropicalFish tropicalFish) {
            mark(player, tropicalFishId(tropicalFish, player));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!worldAccess.isAllowed(event.getPlayer())) {
            return;
        }
        Entity entity = event.getRightClicked();
        if (entity.getType() == EntityType.ALLAY) {
            if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) {
                mark(event.getPlayer(), "allay");
            }
            return;
        }
        if (entity.getType() == EntityType.PIGLIN && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.GOLD_INGOT) {
            mark(event.getPlayer(), "piglin_trade");
            return;
        }
        if (entity instanceof Villager villager) {
            if (villager.getProfession() == Villager.Profession.NONE) {
                mark(event.getPlayer(), "villager_unemployed");
            } else if (villager.getProfession() == Villager.Profession.NITWIT) {
                mark(event.getPlayer(), "villager_nitwit");
            }
            return;
        }
        if (entity.getType() == EntityType.ZOMBIE_VILLAGER && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.GOLDEN_APPLE) {
            mark(event.getPlayer(), "zombie_villager");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRide(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player)) {
            return;
        }
        if (!worldAccess.isAllowed(player)) {
            return;
        }
        if (event.getVehicle().getType() == EntityType.STRIDER) {
            mark(player, "strider");
        }
        if (event.getVehicle().getType() == EntityType.GHAST) {
            mark(player, "happy_ghast");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTrade(PlayerTradeEvent event) {
        if (!worldAccess.isAllowed(event.getPlayer())) {
            return;
        }
        if (event.getMerchant() instanceof Villager villager) {
            mark(event.getPlayer(), villagerId(villager.getProfession()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPurchase(PlayerPurchaseEvent event) {
        if (!worldAccess.isAllowed(event.getPlayer())) {
            return;
        }
        if (event instanceof PlayerTradeEvent) {
            return;
        }
        if (event.getMerchant() instanceof Villager villager) {
            mark(event.getPlayer(), villagerId(villager.getProfession()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM) {
            if (event.getEntityType() == EntityType.IRON_GOLEM) {
                if (isNamed(event.getEntity(), "copper golem")) {
                    // handled below as a copper golem
                } else {
                    markNearbyPlayer(event.getEntity(), "iron_golem");
                }
            }
        }
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN) {
            markNearbyPlayer(event.getEntity(), "snow_golem");
        }
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM
                && event.getEntityType() == EntityType.IRON_GOLEM
                && isNamed(event.getEntity(), "copper golem")) {
            markNearbyPlayer(event.getEntity(), "copper_golem");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTransform(EntityTransformEvent event) {
        if (event.getEntityType() != EntityType.ZOMBIE_VILLAGER) {
            return;
        }
        if (event.getTransformReason() != EntityTransformEvent.TransformReason.CURED) {
            return;
        }
        markNearbyPlayer(event.getEntity(), "zombie_villager");
    }

    private void mark(Player player, String id) {
        if (player == null || id == null) {
            return;
        }
        PlayerData data = progressionService.getData(player.getUniqueId());
        if (BestiaryService.markFound(data, id)) {
            progressionService.addXp(player.getUniqueId(), BESTIARY_XP, "\"" + displayNameFor(id) + "\" added to Bestiary collection");
        }
    }

    private String displayNameFor(String id) {
        if (id == null || id.isBlank()) {
            return "Bestiary";
        }
        BestiaryCatalog.Entry entry = BestiaryCatalog.entryById().get(id);
        if (entry != null) {
            return entry.displayName();
        }
        String[] parts = id.toLowerCase(Locale.US).split("_");
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
        return builder.length() > 0 ? builder.toString() : id;
    }

    private void markNearbyPlayer(Entity entity, String id) {
        if (entity == null || id == null) {
            return;
        }
        List<Player> nearby = entity.getWorld().getPlayers();
        for (Player player : nearby) {
            if (!worldAccess.isAllowed(player)) {
                continue;
            }
            if (player.getLocation().distanceSquared(entity.getLocation()) <= 225) {
                mark(player, id);
                return;
            }
        }
    }

    private void markCustomName(LivingEntity entity, Player player) {
        String name = plainName(entity);
        if (name == null || name.isBlank()) {
            return;
        }
        String normalized = normalize(name);
        String id = CUSTOM_NAME_IDS.get(normalized);
        if (id != null) {
            mark(player, id);
        }
    }

    private String normalize(String input) {
        return input.toLowerCase(Locale.US).replaceAll("[^a-z\\s]", "").trim();
    }

    private String catId(Cat.Type type) {
        if (type == null) {
            return null;
        }
        String key = type.getKey().getKey();
        return switch (key) {
            case "black" -> "cat_black";
            case "british_shorthair" -> "cat_british_shorthair";
            case "calico" -> "cat_calico";
            case "jellie" -> "cat_jellie";
            case "persian" -> "cat_persian";
            case "ragdoll" -> "cat_ragdoll";
            case "red" -> "cat_red";
            case "siamese" -> "cat_siamese";
            case "tabby" -> "cat_tabby";
            case "tuxedo" -> "cat_tuxedo";
            case "white" -> "cat_white";
            default -> null;
        };
    }

    private String frogId(Frog.Variant variant) {
        if (variant == null) {
            return null;
        }
        String key = variant.getKey().getKey();
        return switch (key) {
            case "temperate" -> "frog_temperate";
            case "warm" -> "frog_warm";
            case "cold" -> "frog_cold";
            default -> null;
        };
    }

    private String foxId(Fox.Type type) {
        return type == Fox.Type.SNOW ? "fox_snow" : "fox_red";
    }

    private String wolfId(Wolf wolf) {
        String key = wolf.getVariant().getKey().getKey();
        return switch (key) {
            case "woods" -> "wolf_woods";
            case "ashen" -> "wolf_ashen";
            case "black" -> "wolf_black";
            case "chestnut" -> "wolf_chestnut";
            case "rusty" -> "wolf_rusty";
            case "spotted" -> "wolf_spotted";
            case "striped" -> "wolf_striped";
            case "snowy" -> "wolf_snowy";
            default -> null;
        };
    }

    private String horseId(Horse horse) {
        String color = horse.getColor().name().toUpperCase(Locale.US);
        String style = horse.getStyle().name().toUpperCase(Locale.US);
        if (color.equals("WHITE") && style.equals("WHITE")) {
            return "horse_white_stockings";
        }
        if (color.equals("WHITE") && (style.equals("WHITE_FIELD") || style.equals("WHITEFIELD"))) {
            return "horse_white_field";
        }
        if (color.equals("WHITE") && (style.equals("WHITE_DOTS") || style.equals("WHITEDOTS"))) {
            return "horse_white_spotted";
        }
        if (color.equals("BLACK") && (style.equals("BLACK_DOTS") || style.equals("BLACKDOTS"))) {
            return "horse_black_dotted";
        }
        return null;
    }

    private String villagerId(Villager.Profession profession) {
        if (profession == null) {
            return null;
        }
        String key = profession.getKey().getKey();
        return switch (key) {
            case "none" -> "villager_unemployed";
            case "nitwit" -> "villager_nitwit";
            case "armorer" -> "villager_armorer";
            case "butcher" -> "villager_butcher";
            case "cartographer" -> "villager_cartographer";
            case "cleric" -> "villager_cleric";
            case "farmer" -> "villager_farmer";
            case "fisherman" -> "villager_fisherman";
            case "fletcher" -> "villager_fletcher";
            case "leatherworker" -> "villager_leatherworker";
            case "librarian" -> "villager_librarian";
            case "mason" -> "villager_mason";
            case "shepherd" -> "villager_shepherd";
            case "toolsmith" -> "villager_toolsmith";
            case "weaponsmith" -> "villager_weaponsmith";
            default -> null;
        };
    }

    private String tropicalFishId(TropicalFish fish, Player player) {
        TropicalFish.Pattern pattern = fish.getPattern();
        DyeColor body = fish.getBodyColor();
        DyeColor patternColor = fish.getPatternColor();
        String id = tropicalFishId(pattern, body, patternColor);
        if (id != null) {
            return id;
        }
        List<BestiaryCatalog.Entry> entries = BestiaryCatalog.tropicalFish();
        var data = progressionService.getData(player.getUniqueId());
        for (BestiaryCatalog.Entry entry : entries) {
            if (!BestiaryService.isFound(data, entry.id())) {
                return entry.id();
            }
        }
        return entries.isEmpty() ? null : entries.get(0).id();
    }

    private String tropicalFishId(TropicalFish.Pattern pattern, DyeColor body, DyeColor patternColor) {
        String key = pattern.name() + ":" + body.name() + ":" + patternColor.name();
        return switch (key) {
            case "KOB:ORANGE:WHITE" -> "tropical_clownfish";
            case "SUNSTREAK:ORANGE:WHITE" -> "tropical_tomato_clownfish";
            case "SNOOPER:GRAY:ORANGE" -> "tropical_cichlid";
            case "DASHER:CYAN:WHITE" -> "tropical_blue_tang";
            case "BRINELY:LIGHT_BLUE:YELLOW" -> "tropical_butterflyfish";
            case "SPOTTY:GRAY:LIGHT_GRAY" -> "tropical_black_tang";
            case "FLOPPER:BLUE:YELLOW" -> "tropical_yellow_tang";
            case "STRIPEY:ORANGE:BLACK" -> "tropical_goatfish";
            case "GLITTER:MAGENTA:WHITE" -> "tropical_cotton_candy_betta";
            case "BLOCKFISH:PURPLE:YELLOW" -> "tropical_dottyback";
            case "BETTY:WHITE:YELLOW" -> "tropical_moorish_idol";
            case "CLAYFISH:RED:WHITE" -> "tropical_red_snapper";
            case "KOB:YELLOW:BLUE" -> "tropical_yellowtail_parrotfish";
            case "SUNSTREAK:RED:WHITE" -> "tropical_emperor_red_snapper";
            case "SNOOPER:RED:BLACK" -> "tropical_red_lipped_blenny";
            case "DASHER:ORANGE:BLUE" -> "tropical_parrotfish";
            case "BRINELY:BLUE:WHITE" -> "tropical_queen_angelfish";
            case "SPOTTY:RED:WHITE" -> "tropical_red_cichlid";
            case "FLOPPER:WHITE:BLACK" -> "tropical_triggerfish";
            case "STRIPEY:WHITE:BLUE" -> "tropical_threadfin";
            case "GLITTER:MAGENTA:YELLOW" -> "tropical_ornate_butterflyfish";
            case "BLOCKFISH:WHITE:ORANGE" -> "tropical_anemone";
            default -> null;
        };
    }

    private String plainName(LivingEntity entity) {
        Component name = entity.customName();
        return name == null ? null : PLAIN.serialize(name);
    }

    private boolean isNamed(LivingEntity entity, String expected) {
        String name = plainName(entity);
        return name != null && normalize(name).equals(expected);
    }
}
