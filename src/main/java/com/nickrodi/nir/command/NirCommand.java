package com.nickrodi.nir.command;

import com.nickrodi.nir.model.PlayerData;
import com.nickrodi.nir.service.BestiaryCatalog;
import com.nickrodi.nir.service.DiscCollectionCatalog;
import com.nickrodi.nir.service.EnchantmentCatalog;
import com.nickrodi.nir.service.FishingCollectionCatalog;
import com.nickrodi.nir.service.LevelCurve;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.QuestService;
import com.nickrodi.nir.service.StorageService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class NirCommand implements TabExecutor {
    private final ProgressionService progressionService;
    private final LevelCurve levelCurve;
    private final StorageService storageService;
    private final QuestService questService;

    public NirCommand(ProgressionService progressionService, LevelCurve levelCurve, StorageService storageService, QuestService questService) {
        this.progressionService = Objects.requireNonNull(progressionService, "progressionService");
        this.levelCurve = Objects.requireNonNull(levelCurve, "levelCurve");
        this.storageService = Objects.requireNonNull(storageService, "storageService");
        this.questService = Objects.requireNonNull(questService, "questService");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !player.hasPermission("smpnir.admin")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        String root = args[0].toLowerCase(Locale.ROOT);
        switch (root) {
            case "debug" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /nir debug <xp|updatenotes>");
                    return true;
                }
                handleDebug(sender, shiftArgs(args, 1));
                return true;
            }
            case "calc" -> {
                handleCalc(sender, shiftArgs(args, 1));
                return true;
            }
            case "user" -> {
                if (args.length < 3) {
                    sender.sendMessage("Usage: /nir user <player> <subcommand> ...");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (target.getName() == null) {
                    sender.sendMessage("Player not found: " + args[1]);
                    return true;
                }
                String sub = args[2].toLowerCase(Locale.ROOT);
                switch (sub) {
                    case "xp" -> handleXp(sender, target, shiftArgs(args, 2));
                    case "level" -> handleLevel(sender, target, shiftArgs(args, 2));
                    case "collections" -> handleCollections(sender, target, shiftArgs(args, 2));
                    case "statistics", "stats" -> handleStatistics(sender, target, shiftArgs(args, 2));
                    case "rule" -> handleRule(sender, target, shiftArgs(args, 2));
                    case "quest" -> handleQuest(sender, target, shiftArgs(args, 2));
                    default -> sendHelp(sender);
                }
                return true;
            }
            default -> {
                sendHelp(sender);
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player player && !player.hasPermission("smpnir.admin")) {
            return List.of();
        }
        List<String> primary = List.of("user", "debug", "calc");
        List<String> userSubs = List.of("xp", "level", "collections", "statistics", "stats", "rule", "quest");
        if (args.length == 1) {
            return filter(primary, args[0]);
        }

        if ("debug".equalsIgnoreCase(args[0])) {
            if (args.length == 2) {
                return filter(List.of("xp", "updatenotes"), args[1]);
            }
            return List.of();
        }
        if ("calc".equalsIgnoreCase(args[0])) {
            if (args.length == 2) {
                return filter(List.of("E=10", "E=50", "E=100", "E=300"), args[1]);
            }
            if (args.length == 3) {
                return filter(List.of("A=20", "A=50", "A=80", "A=95"), args[2]);
            }
            return List.of();
        }

        if (!"user".equalsIgnoreCase(args[0])) {
            return List.of();
        }
        if (args.length == 2) {
            return filter(onlinePlayerNames(), args[1]);
        }
        if (args.length == 3) {
            return filter(userSubs, args[2]);
        }

        String sub = args[2].toLowerCase(Locale.ROOT);
        if (sub.equals("xp")) {
            if (args.length == 4) {
                return filter(List.of("add", "remove", "set", "get"), args[3]);
            }
            return List.of();
        }
        if (sub.equals("level")) {
            if (args.length == 4) {
                return filter(List.of("add", "remove", "set"), args[3]);
            }
            return List.of();
        }
        if (sub.equals("rule")) {
            if (args.length == 4) {
                return filter(List.of("deathchest"), args[3]);
            }
            if (args.length == 5) {
                return filter(List.of("true", "false"), args[4]);
            }
            return List.of();
        }
        if (sub.equals("quest")) {
            if (args.length == 4) {
                return filter(questIds(), args[3]);
            }
            if (args.length == 5) {
                return filter(List.of("grant", "revoke", "get"), args[4]);
            }
            return List.of();
        }
        if (sub.equals("collections")) {
            if (args.length == 4) {
                return filter(collectionCategories(), args[3]);
            }
            String category = args.length > 3 ? args[3] : "";
            if (args.length == 5) {
                return filter(List.of("grant", "revoke", "clear", "add", "remove"), args[4]);
            }
            if (args.length == 6) {
                return filter(collectionIds(category), args[5]);
            }
            if (args.length == 7 && isEnchantCategory(category)) {
                return filter(enchantLevelSuggestions(category, args[5]), args[6]);
            }
            return List.of();
        }
        if (sub.equals("statistics") || sub.equals("stats")) {
            if (args.length == 4) {
                return filter(statSections(), args[3]);
            }
            if (args.length == 5) {
                return filter(List.of("set", "add", "remove"), args[4]);
            }
            if (args.length == 6) {
                String section = args.length > 3 ? args[3] : "";
                return filter(statIds(section), args[5]);
            }
            return List.of();
        }

        return List.of();
    }

    private void handleXp(CommandSender sender, OfflinePlayer target, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /nir user <player> xp <add|remove|set|get> [amount]");
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        if ("get".equals(action)) {
            PlayerData data = loadData(target);
            sender.sendMessage("Total XP: " + data.getTotalXp());
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("Usage: /nir user <player> xp <add|remove|set> <amount>");
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Amount must be a number.");
            return;
        }
        if (amount <= 0) {
            sender.sendMessage("Amount must be greater than 0.");
            return;
        }
        PlayerData data = loadData(target);
        long current = data.getTotalXp();
        switch (action) {
            case "add" -> {
                if (target.isOnline()) {
                    int gained = progressionService.addXp(target.getUniqueId(), amount, "admin");
                    sender.sendMessage("Added " + amount + " XP. Nir gained: " + gained + ".");
                } else {
                    long newTotal = current + amount;
                    data.setTotalXp(newTotal);
                    data.setLevel(levelCurve.getLevelForTotalXp(newTotal));
                    saveData(target, data);
                    sender.sendMessage("Added " + amount + " XP to " + targetName(target) + ".");
                }
            }
            case "remove" -> {
                long newTotal = Math.max(0L, current - amount);
                if (target.isOnline()) {
                    progressionService.setTotalXp(target.getUniqueId(), newTotal, "admin");
                } else {
                    data.setTotalXp(newTotal);
                    data.setLevel(levelCurve.getLevelForTotalXp(newTotal));
                    saveData(target, data);
                }
                sender.sendMessage("Removed " + amount + " XP. Total XP: " + newTotal + ".");
            }
            case "set" -> {
                if (target.isOnline()) {
                    progressionService.setTotalXp(target.getUniqueId(), amount, "admin");
                } else {
                    data.setTotalXp(amount);
                    data.setLevel(levelCurve.getLevelForTotalXp(amount));
                    saveData(target, data);
                }
                sender.sendMessage("Set total XP to " + amount + ".");
            }
            default -> sender.sendMessage("Usage: /nir user <player> xp <add|remove|set|get> [amount]");
        }
    }

    private void handleLevel(CommandSender sender, OfflinePlayer target, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /nir user <player> level <add|remove|set> <amount>");
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Amount must be a number.");
            return;
        }
        if (amount <= 0) {
            sender.sendMessage("Amount must be greater than 0.");
            return;
        }
        PlayerData data = loadData(target);
        int current = data.getLevel();
        int max = levelCurve.getMaxLevel();
        int targetLevel;
        switch (action) {
            case "add" -> targetLevel = current + amount;
            case "remove" -> targetLevel = current - amount;
            case "set" -> targetLevel = amount;
            default -> {
                sender.sendMessage("Usage: /nir user <player> level <add|remove|set> <amount>");
                return;
            }
        }
        if (targetLevel < 1) {
            targetLevel = 1;
        }
        if (targetLevel > max) {
            targetLevel = max;
        }
        long newTotal = levelCurve.getTotalXpForLevel(targetLevel);
        if (target.isOnline()) {
            progressionService.setTotalXp(target.getUniqueId(), newTotal, "admin");
        } else {
            data.setLevel(targetLevel);
            data.setTotalXp(newTotal);
            saveData(target, data);
        }
        sender.sendMessage("Level set to " + targetLevel + ".");
    }

    private void handleDebug(CommandSender sender, String[] args) {
        String sub = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";
        switch (sub) {
            case "xp" -> handleDebugXp(sender);
            case "updatenotes" -> handleDebugUpdateNotes(sender);
            default -> sender.sendMessage("Usage: /nir debug <xp|updatenotes>");
        }
    }

    private void handleDebugXp(CommandSender sender) {
        if (sender instanceof Player player) {
            boolean enabled = progressionService.toggleGlobalXpDebug(player.getUniqueId());
            sender.sendMessage("Global XP debug " + (enabled ? "enabled" : "disabled") + ".");
        } else {
            boolean enabled = progressionService.toggleGlobalXpDebugConsole();
            sender.sendMessage("Global XP debug (console) " + (enabled ? "enabled" : "disabled") + ".");
        }
    }

    private void handleDebugUpdateNotes(CommandSender sender) {
        boolean enabled = progressionService.toggleUpdateNotesDebug();
        sender.sendMessage("Update notes debug " + (enabled ? "enabled" : "disabled") + ".");
    }

    private void handleCalc(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /nir calc <effort> <aesthetics>");
            return;
        }
        Double effort = parseNumericToken(args[0]);
        Double aesthetics = parseNumericToken(args[1]);
        if (effort == null || aesthetics == null) {
            sender.sendMessage("Usage: /nir calc <effort> <aesthetics>");
            return;
        }
        if (effort <= 0.0 || aesthetics <= 0.0 || aesthetics >= 100.0) {
            sender.sendMessage("Usage: /nir calc <effort> <aesthetics>");
            return;
        }
        double value = 4200.0 * Math.pow(Math.log10(effort + 1.0), 2.2) * (0.9 + 0.002 * aesthetics);
        long rounded = Math.round(value);
        sender.sendMessage(String.valueOf(rounded));
    }

    private void handleRule(CommandSender sender, OfflinePlayer target, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /nir user <player> rule deathchest <true|false>");
            return;
        }
        String rule = args[1].toLowerCase(Locale.ROOT);
        String value = args[2].toLowerCase(Locale.ROOT);
        if (!"deathchest".equals(rule)) {
            sender.sendMessage("Unknown rule: " + rule);
            return;
        }
        boolean enabled;
        if ("true".equals(value)) {
            enabled = true;
        } else if ("false".equals(value)) {
            enabled = false;
        } else {
            sender.sendMessage("Usage: /nir user <player> rule deathchest <true|false>");
            return;
        }
        PlayerData data = loadData(target);
        data.setDeathChestEnabled(enabled);
        saveData(target, data);
        sender.sendMessage("Death chest " + (enabled ? "enabled" : "disabled") + " for " + targetName(target) + ".");
    }

    private void handleQuest(CommandSender sender, OfflinePlayer target, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /nir user <player> quest <questId> <grant|revoke|get>");
            return;
        }
        String questRaw = args[1];
        String action = args[2].toLowerCase(Locale.ROOT);
        QuestService.QuestDefinition quest = resolveQuest(questRaw);
        if (quest == null) {
            sender.sendMessage("Unknown quest: " + questRaw);
            return;
        }
        PlayerData data = loadData(target);
        boolean completed = questService.isComplete(data, quest);
        switch (action) {
            case "get" -> sender.sendMessage("Quest " + quest.id() + " completed: " + completed);
            case "grant" -> {
                if (completed) {
                    sender.sendMessage("Quest " + quest.id() + " is already completed for " + targetName(target) + ".");
                    return;
                }
                if (target.isOnline() && target.getPlayer() != null) {
                    boolean done = questService.complete(target.getPlayer(), quest);
                    sender.sendMessage(done
                            ? "Granted quest " + quest.id() + " for " + targetName(target) + "."
                            : "Quest " + quest.id() + " is already completed for " + targetName(target) + ".");
                    return;
                }
                grantQuestOffline(target, data, quest);
                sender.sendMessage("Granted quest " + quest.id() + " for " + targetName(target) + ".");
            }
            case "revoke" -> {
                if (!completed) {
                    sender.sendMessage("Quest " + quest.id() + " is not completed for " + targetName(target) + ".");
                    return;
                }
                revokeQuest(target, data, quest);
                sender.sendMessage("Revoked quest " + quest.id() + " for " + targetName(target) + ".");
            }
            default -> sender.sendMessage("Usage: /nir user <player> quest <questId> <grant|revoke|get>");
        }
    }

    private void handleCollections(CommandSender sender, OfflinePlayer target, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /nir <player> collections <category> <grant|revoke|clear> <id> [level]");
            return;
        }
        String category = args[1].toLowerCase(Locale.ROOT);
        String action = args[2].toLowerCase(Locale.ROOT);
        String id = args.length >= 4 ? args[3] : "";
        int level = args.length >= 5 ? parseInt(args[4], 1) : -1;

        PlayerData data = loadData(target);
        switch (category) {
            case "enchants", "enchant", "enchantments" -> {
                var entry = EnchantmentCatalog.find(id);
                if (entry == null) {
                    sender.sendMessage("Unknown enchant: " + id);
                    return;
                }
                int tier = level <= 0 ? entry.maxLevel() : Math.min(entry.maxLevel(), Math.max(1, level));
                String key = entry.key() + ":" + tier;
                List<String> tiers = data.getEnchantmentTiersFound();
                if (tiers == null) {
                    tiers = new ArrayList<>();
                }
                switch (action) {
                    case "grant", "add" -> {
                        if (!tiers.contains(key)) {
                            tiers.add(key);
                        }
                        data.setEnchantmentTiersFound(tiers);
                        saveData(target, data);
                        sender.sendMessage("Granted enchant tier " + entry.name() + " " + tier + " to " + targetName(target) + ".");
                    }
                    case "revoke", "remove" -> {
                        tiers.remove(key);
                        data.setEnchantmentTiersFound(tiers);
                        saveData(target, data);
                        sender.sendMessage("Removed enchant tier " + entry.name() + " " + tier + " from " + targetName(target) + ".");
                    }
                    case "clear" -> {
                        data.setEnchantmentTiersFound(new ArrayList<>());
                        saveData(target, data);
                        sender.sendMessage("Cleared enchant collection for " + targetName(target) + ".");
                    }
                    default -> sender.sendMessage("Usage: /nir <player> collections enchants <grant|revoke|clear> <enchant> [level]");
                }
            }
            case "biomes", "biome" -> {
                List<String> list = data.getBiomesVisited();
                if (list == null) {
                    list = new ArrayList<>();
                }
                String key = id.contains(":") ? id : "minecraft:" + id;
                switch (action) {
                    case "grant", "add" -> {
                        if (!list.contains(key)) {
                            list.add(key);
                        }
                        data.setBiomesVisited(list);
                        saveData(target, data);
                        sender.sendMessage("Granted biome " + key + " to " + targetName(target) + ".");
                    }
                    case "revoke", "remove" -> {
                        list.remove(key);
                        data.setBiomesVisited(list);
                        saveData(target, data);
                        sender.sendMessage("Removed biome " + key + " from " + targetName(target) + ".");
                    }
                    case "clear" -> {
                        data.setBiomesVisited(new ArrayList<>());
                        saveData(target, data);
                        sender.sendMessage("Cleared biomes collection for " + targetName(target) + ".");
                    }
                    default -> sender.sendMessage("Usage: /nir <player> collections biomes <grant|revoke|clear> <biome>");
                }
            }
            case "fishing" -> {
                List<String> list = data.getFishingItemsFound();
                if (list == null) {
                    list = new ArrayList<>();
                }
                String entryId = resolveFishingId(id);
                if (entryId == null) {
                    sender.sendMessage("Unknown fishing item: " + id);
                    return;
                }
                switch (action) {
                    case "grant", "add" -> {
                        if (!list.contains(entryId)) {
                            list.add(entryId);
                        }
                        data.setFishingItemsFound(list);
                        saveData(target, data);
                        sender.sendMessage("Granted fishing item " + entryId + " to " + targetName(target) + ".");
                    }
                    case "revoke", "remove" -> {
                        list.remove(entryId);
                        data.setFishingItemsFound(list);
                        saveData(target, data);
                        sender.sendMessage("Removed fishing item " + entryId + " from " + targetName(target) + ".");
                    }
                    case "clear" -> {
                        data.setFishingItemsFound(new ArrayList<>());
                        saveData(target, data);
                        sender.sendMessage("Cleared fishing collection for " + targetName(target) + ".");
                    }
                    default -> sender.sendMessage("Usage: /nir <player> collections fishing <grant|revoke|clear> <item>");
                }
            }
            case "discs", "disc", "disk", "disks" -> {
                List<String> list = data.getDiscsFound();
                if (list == null) {
                    list = new ArrayList<>();
                }
                String discId = resolveDiscId(id);
                if (discId == null) {
                    sender.sendMessage("Unknown disc: " + id);
                    return;
                }
                switch (action) {
                    case "grant", "add" -> {
                        if (!list.contains(discId)) {
                            list.add(discId);
                        }
                        data.setDiscsFound(list);
                        saveData(target, data);
                        sender.sendMessage("Granted disc " + discId + " to " + targetName(target) + ".");
                    }
                    case "revoke", "remove" -> {
                        list.remove(discId);
                        data.setDiscsFound(list);
                        saveData(target, data);
                        sender.sendMessage("Removed disc " + discId + " from " + targetName(target) + ".");
                    }
                    case "clear" -> {
                        data.setDiscsFound(new ArrayList<>());
                        saveData(target, data);
                        sender.sendMessage("Cleared disc collection for " + targetName(target) + ".");
                    }
                    default -> sender.sendMessage("Usage: /nir <player> collections discs <grant|revoke|clear> <disc>");
                }
            }
            case "bestiary", "beastiary" -> {
                List<String> list = data.getBestiaryFound();
                if (list == null) {
                    list = new ArrayList<>();
                }
                String entryId = resolveBestiaryId(id);
                if (entryId == null) {
                    sender.sendMessage("Unknown bestiary entry: " + id);
                    return;
                }
                switch (action) {
                    case "grant", "add" -> {
                        if (!list.contains(entryId)) {
                            list.add(entryId);
                        }
                        data.setBestiaryFound(list);
                        saveData(target, data);
                        sender.sendMessage("Granted bestiary entry " + entryId + " to " + targetName(target) + ".");
                    }
                    case "revoke", "remove" -> {
                        list.remove(entryId);
                        data.setBestiaryFound(list);
                        saveData(target, data);
                        sender.sendMessage("Removed bestiary entry " + entryId + " from " + targetName(target) + ".");
                    }
                    case "clear" -> {
                        data.setBestiaryFound(new ArrayList<>());
                        saveData(target, data);
                        sender.sendMessage("Cleared bestiary collection for " + targetName(target) + ".");
                    }
                    default -> sender.sendMessage("Usage: /nir <player> collections bestiary <grant|revoke|clear> <entry>");
                }
            }
            default -> sender.sendMessage("Unknown collection category: " + category);
        }
    }

    private void handleStatistics(CommandSender sender, OfflinePlayer target, String[] args) {
        if (args.length < 5) {
            sender.sendMessage("Usage: /nir <player> statistics <section> <set|add|remove> <stat> <amount>");
            return;
        }
        String section = args[1].toLowerCase(Locale.ROOT);
        String action = args[2].toLowerCase(Locale.ROOT);
        String stat = args[3].toLowerCase(Locale.ROOT);
        long amount;
        try {
            amount = Long.parseLong(args[4]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Amount must be a number.");
            return;
        }
        PlayerData data = loadData(target);
        StatTarget targetStat = resolveStat(section, stat);
        if (targetStat == null) {
            sender.sendMessage("Unknown stat: " + stat + " in " + section + ".");
            return;
        }
        long current = targetStat.get(data);
        long next = switch (action) {
            case "add" -> current + amount;
            case "remove" -> Math.max(0L, current - amount);
            case "set" -> Math.max(0L, amount);
            default -> -1L;
        };
        if (next < 0) {
            sender.sendMessage("Usage: /nir <player> statistics <section> <set|add|remove> <stat> <amount>");
            return;
        }
        targetStat.set(data, next);
        saveData(target, data);
        sender.sendMessage("Updated " + stat + " to " + next + " for " + targetName(target) + ".");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("Usage:");
        sender.sendMessage("/nir debug <xp|updatenotes>");
        sender.sendMessage("/nir calc <effort> <aesthetics>");
        sender.sendMessage("/nir user <player> xp <add|remove|set|get> [amount]");
        sender.sendMessage("/nir user <player> level <add|remove|set> <amount>");
        sender.sendMessage("/nir user <player> collections <category> <grant|revoke|clear> <id> [level]");
        sender.sendMessage("/nir user <player> statistics <section> <set|add|remove> <stat> <amount>");
        sender.sendMessage("/nir user <player> rule deathchest <true|false>");
        sender.sendMessage("/nir user <player> quest <questId> <grant|revoke|get>");
    }

    private List<String> filter(List<String> options, String token) {
        if (token == null || token.isBlank()) {
            return options;
        }
        String lower = token.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(lower)) {
                matches.add(option);
            }
        }
        return matches;
    }

    private PlayerData loadData(OfflinePlayer target) {
        if (target.isOnline()) {
            return progressionService.getData(target.getUniqueId());
        }
        return storageService.load(target.getUniqueId());
    }

    private void saveData(OfflinePlayer target, PlayerData data) {
        storageService.save(target.getUniqueId(), data);
    }

    private String[] shiftArgs(String[] args, int startIndex) {
        if (startIndex <= 0) {
            return args;
        }
        String[] shifted = new String[args.length - startIndex];
        System.arraycopy(args, startIndex, shifted, 0, shifted.length);
        return shifted;
    }

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private Double parseNumericToken(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String cleaned = raw.trim().replaceAll("[^0-9.+-]", "");
        if (cleaned.isBlank() || cleaned.equals(".") || cleaned.equals("+") || cleaned.equals("-")) {
            return null;
        }
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizeKey(String input) {
        if (input == null) {
            return "";
        }
        return input.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private List<String> onlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    private List<String> collectionCategories() {
        return List.of("enchants", "biomes", "fishing", "discs", "bestiary");
    }

    private boolean isEnchantCategory(String category) {
        String normalized = category == null ? "" : category.toLowerCase(Locale.ROOT);
        return normalized.equals("enchants")
                || normalized.equals("enchant")
                || normalized.equals("enchantments");
    }

    private List<String> collectionIds(String category) {
        String normalized = category == null ? "" : category.toLowerCase(Locale.ROOT);
        List<String> ids = new ArrayList<>();
        switch (normalized) {
            case "enchants", "enchant", "enchantments" -> {
                for (EnchantmentCatalog.EnchantCategory cat : EnchantmentCatalog.categories()) {
                    for (EnchantmentCatalog.EnchantEntry entry : cat.entries()) {
                        ids.add(entry.commandId());
                        ids.add(entry.key());
                    }
                }
            }
            case "biomes", "biome" -> {
                for (org.bukkit.block.Biome biome : org.bukkit.block.Biome.values()) {
                    ids.add(biome.getKey().getKey());
                }
            }
            case "fishing" -> {
                for (FishingCollectionCatalog.FishingEntry entry : FishingCollectionCatalog.entries()) {
                    ids.add(entry.id());
                }
            }
            case "discs", "disc", "disk", "disks" -> {
                for (DiscCollectionCatalog.DiscEntry entry : DiscCollectionCatalog.entries()) {
                    ids.add(entry.id());
                }
            }
            case "bestiary", "beastiary" -> {
                for (BestiaryCatalog.Entry entry : BestiaryCatalog.allEntries()) {
                    ids.add(entry.id());
                }
            }
            default -> {
            }
        }
        return ids;
    }

    private List<String> enchantLevelSuggestions(String category, String enchantId) {
        if (!isEnchantCategory(category)) {
            return List.of();
        }
        EnchantmentCatalog.EnchantEntry entry = EnchantmentCatalog.find(enchantId);
        if (entry == null) {
            return List.of("1", "2", "3", "4", "5");
        }
        List<String> nir = new ArrayList<>();
        for (int i = 1; i <= entry.maxLevel(); i++) {
            nir.add(String.valueOf(i));
        }
        return nir;
    }

    private List<String> statSections() {
        return List.of("combat", "mobs", "mining", "exploration", "extra", "playtime");
    }

    private List<String> statIds(String section) {
        String normalized = section == null ? "" : section.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "combat" -> List.of("players_killed", "dragon", "wither", "warden", "mobs", "monsters");
            case "mobs" -> List.of("mobs", "monsters", "bred", "tamed");
            case "mining" -> List.of("ores", "crops");
            case "exploration" -> List.of("chests");
            case "extra" -> List.of("trades", "xporbs", "advancements", "quests");
            case "playtime" -> List.of("minutes");
            default -> List.of();
        };
    }

    private List<String> questIds() {
        List<String> ids = new ArrayList<>();
        for (QuestService.QuestDefinition quest : questService.quests()) {
            ids.add(quest.id());
        }
        return ids;
    }

    private String targetName(OfflinePlayer target) {
        String name = target.getName();
        return name != null ? name : target.getUniqueId().toString();
    }

    private QuestService.QuestDefinition resolveQuest(String input) {
        if (input == null) {
            return null;
        }
        String normalized = normalizeKey(input);
        QuestService.QuestDefinition fallback = null;
        for (QuestService.QuestDefinition quest : questService.quests()) {
            String title = normalizeKey(quest.title());
            if (title.equals(normalized)) {
                return quest;
            }
        }
        for (QuestService.QuestDefinition quest : questService.quests()) {
            String questId = normalizeKey(quest.id());
            if (questId.equals(normalized)) {
                return quest;
            }
            if (!normalized.isBlank() && questId.contains(normalized)) {
                fallback = fallback == null ? quest : null;
            }
        }
        return fallback;
    }

    private void grantQuestOffline(OfflinePlayer target, PlayerData data, QuestService.QuestDefinition quest) {
        List<String> completed = data.getQuestsCompleted();
        if (completed == null) {
            completed = new ArrayList<>();
        }
        completed.add(quest.id());
        data.setQuestsCompleted(completed);
        data.setQuestsDone(data.getQuestsDone() + 1);
        data.setQuestsXpGained(data.getQuestsXpGained() + quest.xp());
        long newTotal = data.getTotalXp() + quest.xp();
        data.setTotalXp(newTotal);
        data.setLevel(levelCurve.getLevelForTotalXp(newTotal));
        saveData(target, data);
    }

    private void revokeQuest(OfflinePlayer target, PlayerData data, QuestService.QuestDefinition quest) {
        List<String> completed = data.getQuestsCompleted();
        if (completed == null || completed.isEmpty()) {
            return;
        }
        completed.remove(quest.id());
        data.setQuestsCompleted(completed);
        data.setQuestsDone(Math.max(0L, data.getQuestsDone() - 1));
        data.setQuestsXpGained(Math.max(0L, data.getQuestsXpGained() - quest.xp()));
        long newTotal = Math.max(0L, data.getTotalXp() - quest.xp());
        if (target.isOnline()) {
            progressionService.setTotalXp(target.getUniqueId(), newTotal, "admin");
        } else {
            data.setTotalXp(newTotal);
            data.setLevel(levelCurve.getLevelForTotalXp(newTotal));
            saveData(target, data);
        }
    }

    private String resolveFishingId(String input) {
        if (input == null) {
            return null;
        }
        String normalized = normalizeKey(input);
        for (FishingCollectionCatalog.FishingEntry entry : FishingCollectionCatalog.entries()) {
            if (normalizeKey(entry.id()).equals(normalized) || normalizeKey(entry.displayName()).equals(normalized)) {
                return entry.id();
            }
        }
        return null;
    }

    private String resolveDiscId(String input) {
        if (input == null) {
            return null;
        }
        String normalized = normalizeKey(input);
        for (DiscCollectionCatalog.DiscEntry entry : DiscCollectionCatalog.entries()) {
            if (normalizeKey(entry.id()).equals(normalized) || normalizeKey(entry.displayName()).equals(normalized)) {
                return entry.id();
            }
        }
        return null;
    }

    private String resolveBestiaryId(String input) {
        if (input == null) {
            return null;
        }
        String normalized = normalizeKey(input);
        for (BestiaryCatalog.Entry entry : BestiaryCatalog.allEntries()) {
            if (normalizeKey(entry.id()).equals(normalized) || normalizeKey(entry.displayName()).equals(normalized)) {
                return entry.id();
            }
        }
        return null;
    }

    private StatTarget resolveStat(String section, String stat) {
        String normalized = normalizeKey(stat);
        return switch (section.toLowerCase(Locale.ROOT)) {
            case "combat" -> switch (normalized) {
                case "playerskilled", "playerkills", "players" -> new StatTarget(PlayerData::getPlayerKills, PlayerData::setPlayerKills);
                case "dragon" -> new StatTarget(PlayerData::getDragonKills, PlayerData::setDragonKills);
                case "wither" -> new StatTarget(PlayerData::getWitherKills, PlayerData::setWitherKills);
                case "warden" -> new StatTarget(PlayerData::getWardenKills, PlayerData::setWardenKills);
                case "mobs", "mobskilled" -> new StatTarget(PlayerData::getMobsKilled, PlayerData::setMobsKilled);
                case "monsters", "monsterskilled" -> new StatTarget(PlayerData::getMonstersKilled, PlayerData::setMonstersKilled);
                default -> null;
            };
            case "mobs" -> switch (normalized) {
                case "mobs", "mobskilled" -> new StatTarget(PlayerData::getMobsKilled, PlayerData::setMobsKilled);
                case "monsters", "monsterskilled" -> new StatTarget(PlayerData::getMonstersKilled, PlayerData::setMonstersKilled);
                case "bred", "mobsbred" -> new StatTarget(PlayerData::getMobsBred, PlayerData::setMobsBred);
                case "tamed", "mobstamed" -> new StatTarget(PlayerData::getMobsTamed, PlayerData::setMobsTamed);
                default -> null;
            };
            case "mining" -> switch (normalized) {
                case "ores", "oresmined" -> new StatTarget(PlayerData::getOresMined, PlayerData::setOresMined);
                case "crops", "cropsharvested" -> new StatTarget(PlayerData::getCropsHarvested, PlayerData::setCropsHarvested);
                default -> null;
            };
            case "exploration" -> switch (normalized) {
                case "chests", "structurechests" -> new StatTarget(PlayerData::getStructureChestsOpened, PlayerData::setStructureChestsOpened);
                default -> null;
            };
            case "extra" -> switch (normalized) {
                case "trades", "villagertrades" -> new StatTarget(PlayerData::getVillagerTrades, PlayerData::setVillagerTrades);
                case "xporbs", "xporbsused", "xp" -> new StatTarget(PlayerData::getVanillaXpSpent, PlayerData::setVanillaXpSpent);
                case "advancements", "advancementsdone" -> new StatTarget(PlayerData::getAdvancementsDone, PlayerData::setAdvancementsDone);
                case "quests", "questsdone" -> new StatTarget(PlayerData::getQuestsDone, PlayerData::setQuestsDone);
                default -> null;
            };
            case "playtime" -> switch (normalized) {
                case "minutes", "playtimeminutes" -> new StatTarget(PlayerData::getActivePlaytimeMinutes, PlayerData::setActivePlaytimeMinutes);
                default -> null;
            };
            default -> null;
        };
    }

    private record StatTarget(java.util.function.ToLongFunction<PlayerData> getter,
                              java.util.function.ObjLongConsumer<PlayerData> setter) {
        long get(PlayerData data) {
            return getter.applyAsLong(data);
        }

        void set(PlayerData data, long value) {
            setter.accept(data, value);
        }
    }
}
