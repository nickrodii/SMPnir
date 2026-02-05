package com.nickrodi.nir.command;

import com.nickrodi.nir.model.PlayerData;
import com.nickrodi.nir.service.BuildReviewService;
import com.nickrodi.nir.service.BuildReviewSessionService;
import com.nickrodi.nir.service.BuildReviewSessionService.PendingGrade;
import com.nickrodi.nir.service.BuildReviewSessionService.ReviewSession;
import com.nickrodi.nir.service.ProgressionService;
import com.nickrodi.nir.service.StorageService;
import com.nickrodi.nir.service.BuildReviewService.BuildParticipant;
import com.nickrodi.nir.service.BuildReviewService.BuildSubmission;
import com.nickrodi.nir.service.BuildReviewService.ClaimResult;
import com.nickrodi.nir.service.BuildReviewService.ClaimStatus;
import com.nickrodi.nir.service.BuildReviewService.BuildStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BuildCommand implements TabExecutor {
    private static final String PERMISSION_ADMIN = "smpnir.admin";
    private static final String ROLE_GRADER = "grader";
    private static final int BONUS_MIN = 2500;
    private static final int BONUS_MAX = 10000;
    private static final int MAX_ID_LENGTH = 48;

    private final BuildReviewService buildReviewService;
    private final BuildReviewSessionService reviewSessionService;
    private final ProgressionService progressionService;
    private final StorageService storageService;

    public BuildCommand(BuildReviewService buildReviewService, BuildReviewSessionService reviewSessionService, ProgressionService progressionService, StorageService storageService) {
        this.buildReviewService = Objects.requireNonNull(buildReviewService, "buildReviewService");
        this.reviewSessionService = Objects.requireNonNull(reviewSessionService, "reviewSessionService");
        this.progressionService = Objects.requireNonNull(progressionService, "progressionService");
        this.storageService = Objects.requireNonNull(storageService, "storageService");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "submit" -> handleSubmit(sender, args);
            case "groupsubmit" -> handleGroupSubmit(sender, args);
            case "submissions" -> handleSubmissions(sender);
            case "history" -> handleHistory(sender, args);
            case "review" -> handleReview(sender, args);
            case "grade" -> handleGrade(sender, args);
            case "ungrade" -> handleUngrade(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "xp" -> handleXp(sender);
            case "list" -> handleList(sender);
            case "claim" -> handleClaim(sender, args);
            default -> sendHelp(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> subs = new ArrayList<>(List.of("submit", "groupsubmit", "list", "claim", "history", "xp"));
        if (isReviewer(sender)) {
            subs.addAll(List.of("submissions", "review", "grade", "ungrade"));
        }
        if (isAdmin(sender)) {
            subs.add("remove");
        }
        if (args.length == 1) {
            return filter(subs, args[0]);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            switch (sub) {
                case "claim" -> {
                    return filter(gradedBuildIds(), args[1]);
                }
                case "review" -> {
                    if (!isReviewer(sender)) {
                        return List.of();
                    }
                    return filter(pendingBuildIds(), args[1]);
                }
                case "grade" -> {
                    if (!isReviewer(sender)) {
                        return List.of();
                    }
                    return filter(List.of("back"), args[1]);
                }
                case "ungrade" -> {
                    if (!isReviewer(sender)) {
                        return List.of();
                    }
                    return filter(gradedBuildIds(), args[1]);
                }
                case "remove" -> {
                    if (!isAdmin(sender)) {
                        return List.of();
                    }
                    return filter(pendingBuildIds(), args[1]);
                }
                default -> {
                }
            }
        }
        if (args.length == 2 && "history".equalsIgnoreCase(args[0]) && isAdmin(sender)) {
            return filter(onlinePlayerNames(), args[1]);
        }
        if (args.length >= 2 && "groupsubmit".equalsIgnoreCase(args[0])) {
            return filter(onlinePlayerNames(), args[args.length - 1]);
        }
        return List.of();
    }

    private void handleSubmit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can submit builds.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /build submit <id>");
            return;
        }
        String id = args[1].trim();
        if (!isValidId(id)) {
            sender.sendMessage("Build ID must be 1-" + MAX_ID_LENGTH + " characters with no spaces.");
            return;
        }
        List<BuildParticipant> participants = List.of(new BuildParticipant(player.getUniqueId(), player.getName()));
        if (!createSubmission(player, id, participants)) {
            sender.sendMessage("Build ID already exists.");
            return;
        }
        BuildSubmission submission = buildReviewService.get(id);
        notifyAdminsSubmission(submission);
        markBuildersNotified(submission, player.getUniqueId());
        player.sendMessage(Component.text("Build " + id + " submitted!", NamedTextColor.GREEN, TextDecoration.BOLD));
    }

    private void handleGroupSubmit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can submit builds.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("Usage: /build groupsubmit <id> <player> [player...]");
            return;
        }
        String id = args[1].trim();
        if (!isValidId(id)) {
            sender.sendMessage("Build ID must be 1-" + MAX_ID_LENGTH + " characters with no spaces.");
            return;
        }
        Map<UUID, BuildParticipant> participants = new HashMap<>();
        participants.put(player.getUniqueId(), new BuildParticipant(player.getUniqueId(), player.getName()));
        for (int i = 2; i < args.length; i++) {
            for (String token : splitNames(args[i])) {
                OfflinePlayer offline = Bukkit.getOfflinePlayer(token);
                UUID uuid = offline.getUniqueId();
                String name = offline.getName() != null ? offline.getName() : token;
                participants.put(uuid, new BuildParticipant(uuid, name));
            }
        }
        if (!createSubmission(player, id, new ArrayList<>(participants.values()))) {
            sender.sendMessage("Build ID already exists.");
            return;
        }
        BuildSubmission submission = buildReviewService.get(id);
        notifyAdminsSubmission(submission);
        markBuildersNotified(submission, player.getUniqueId());
        player.sendMessage(Component.text("Build " + id + " submitted!", NamedTextColor.GREEN, TextDecoration.BOLD));
    }

    private void handleSubmissions(CommandSender sender) {
        if (!isReviewer(sender)) {
            sender.sendMessage("You do not have permission to use this command.");
            return;
        }
        List<BuildSubmission> pending = buildReviewService.listPending();
        if (pending.isEmpty()) {
            sender.sendMessage("No builds waiting for review.");
            return;
        }
        sender.sendMessage("Build submissions:");
        for (BuildSubmission submission : pending) {
            if (sender instanceof Player player) {
                player.sendMessage(submissionLine(submission));
            } else {
                sender.sendMessage(formatSubmissionLine(submission));
            }
        }
    }

    private void handleHistory(CommandSender sender) {
        handleHistory(sender, new String[0]);
    }

    private void handleHistory(CommandSender sender, String[] args) {
        OfflinePlayer target;
        if (args.length >= 2 && !args[1].isBlank()) {
            if (!isAdmin(sender)) {
                sender.sendMessage("You do not have permission to view other players' build history.");
                return;
            }
            target = Bukkit.getOfflinePlayer(args[1]);
            if (target.getName() == null) {
                sender.sendMessage("Player not found: " + args[1]);
                return;
            }
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Usage: /build history <player>");
                return;
            }
            target = player;
        }
        List<BuildSubmission> builds = buildReviewService.listForPlayer(target.getUniqueId());
        List<BuildSubmission> claimed = new ArrayList<>();
        for (BuildSubmission submission : builds) {
            if (submission.status() == BuildStatus.GRADED && submission.isClaimed(target.getUniqueId())) {
                claimed.add(submission);
            }
        }
        if (claimed.isEmpty()) {
            sender.sendMessage("No claimed builds for " + targetName(target) + ".");
            return;
        }
        sender.sendMessage("Build history for " + targetName(target) + ":");
        for (BuildSubmission submission : claimed) {
            long share = submission.shareFor(target.getUniqueId());
            sender.sendMessage(submission.id() + " (" + share + " xp)");
        }
    }

    private void handleReview(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can review builds.");
            return;
        }
        if (!isReviewer(sender)) {
            sender.sendMessage("You do not have permission to use this command.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /build review <id>");
            return;
        }
        String id = args[1].trim();
        BuildSubmission submission = buildReviewService.get(id);
        if (submission == null) {
            sender.sendMessage("Build not found: " + id);
            return;
        }
        if (submission.status() != BuildStatus.PENDING) {
            sender.sendMessage("Build already graded: " + submission.id());
            return;
        }
        var world = Bukkit.getWorld(submission.world());
        if (world == null) {
            sender.sendMessage("World not found: " + submission.world());
            return;
        }
        if (reviewSessionService.isReviewing(player)) {
            reviewSessionService.endSession(player);
        }
        Location center = new Location(
                world,
                submission.x() + 0.5,
                submission.y() + 1.0,
                submission.z() + 0.5
        );
        reviewSessionService.startSession(player, submission.id(), submission.participantsLabel(), center);
        reviewSessionService.clearPendingGrade(player);
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(center);
        player.sendMessage(Component.text(
                "Reviewing build " + submission.id() + " by " + submission.participantsLabel() + ".",
                NamedTextColor.AQUA
        ));
        player.sendMessage(Component.text(
                "Use /build grade <effort> <aesthetic> <bonusxp> or /build grade back.",
                NamedTextColor.GRAY
        ));
    }

    private void handleGrade(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can grade builds.");
            return;
        }
        if (!isReviewer(sender)) {
            sender.sendMessage("You do not have permission to use this command.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /build grade <effort> <aesthetic> [bonusxp]");
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        switch (action) {
            case "back" -> handleGradeBack(player);
            case "yes" -> handleGradeConfirm(player);
            case "no" -> handleGradeCancel(player);
            default -> handleGradeCalculate(player, args);
        }
    }

    private void handleGradeBack(Player player) {
        if (!reviewSessionService.endSession(player)) {
            player.sendMessage("You are not reviewing a build.");
        }
    }

    private void handleGradeCancel(Player player) {
        reviewSessionService.clearPendingGrade(player);
    }

    private void handleGradeConfirm(Player player) {
        ReviewSession session = reviewSessionService.getSession(player);
        if (session == null) {
            player.sendMessage("You are not reviewing a build.");
            return;
        }
        PendingGrade pending = reviewSessionService.getPendingGrade(player);
        if (pending == null) {
            player.sendMessage("No pending grade to confirm.");
            return;
        }
        BuildSubmission submission = buildReviewService.get(session.buildId());
        if (submission == null) {
            player.sendMessage("Build not found: " + session.buildId());
            reviewSessionService.endSession(player);
            return;
        }
        if (submission.status() != BuildStatus.PENDING) {
            player.sendMessage("Build already graded: " + submission.id());
            reviewSessionService.endSession(player);
            return;
        }
        long xpTotal = pending.xpTotal();
        if (xpTotal <= 0L) {
            player.sendMessage("XP amount must be greater than 0.");
            return;
        }
        UUID graderId = player.getUniqueId();
        String graderName = player.getName();
        if (!buildReviewService.grade(submission.id(), xpTotal, graderId, graderName)) {
            player.sendMessage("Failed to grade build: " + submission.id());
            return;
        }
        BuildSubmission graded = buildReviewService.get(submission.id());
        player.sendMessage("Build " + submission.id() + " graded for " + xpTotal + " XP total.");
        notifyParticipants(graded);
        reviewSessionService.endSession(player);
    }

    private void handleGradeCalculate(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("Usage: /build grade <effort> <aesthetic> [bonusxp]");
            return;
        }
        ReviewSession session = reviewSessionService.getSession(player);
        if (session == null) {
            player.sendMessage("You are not reviewing a build. Use /build review <id> first.");
            return;
        }
        BuildSubmission submission = buildReviewService.get(session.buildId());
        if (submission == null) {
            player.sendMessage("Build not found: " + session.buildId());
            reviewSessionService.endSession(player);
            return;
        }
        if (submission.status() != BuildStatus.PENDING) {
            player.sendMessage("Build already graded: " + submission.id());
            reviewSessionService.endSession(player);
            return;
        }
        Double effort = parseNumericToken(args[1]);
        Double aesthetics = parseNumericToken(args[2]);
        if (effort == null || aesthetics == null) {
            player.sendMessage("Usage: /build grade <effort> <aesthetic> [bonusxp]");
            return;
        }
        if (effort <= 0.0 || aesthetics <= 0.0 || aesthetics > 100.0) {
            player.sendMessage("Usage: /build grade <effort> <aesthetic> [bonusxp]");
            return;
        }
        long bonus = 0L;
        if (args.length >= 4) {
            Integer parsedBonus = parseIntStrict(args[3]);
            if (parsedBonus == null) {
                player.sendMessage("Bonus XP must be a number.");
                return;
            }
            if (parsedBonus < BONUS_MIN || parsedBonus > BONUS_MAX) {
                player.sendMessage("Bonus XP must be between " + BONUS_MIN + " and " + BONUS_MAX + ".");
                return;
            }
            bonus = parsedBonus;
        }
        long base = Math.round(calculateGradeXp(effort, aesthetics));
        long total = Math.max(0L, base + bonus);
        reviewSessionService.setPendingGrade(player, new PendingGrade(total, effort, aesthetics, bonus));

        String builder = session.builderLabel().isBlank() ? "Unknown" : session.builderLabel();
        Component prompt = Component.text()
                .append(Component.text("You are grading this build ", NamedTextColor.GRAY))
                .append(Component.text(session.buildId(), NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text(" by ", NamedTextColor.GRAY))
                .append(Component.text(builder, NamedTextColor.YELLOW))
                .append(Component.text(" for ", NamedTextColor.GRAY))
                .append(Component.text(total + " xp", NamedTextColor.GREEN))
                .append(Component.text(". Are you sure?", NamedTextColor.GRAY))
                .build();
        Component yes = Component.text("[YES]", NamedTextColor.GREEN, TextDecoration.BOLD, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand("/build grade yes"));
        Component no = Component.text("[NO]", NamedTextColor.RED, TextDecoration.BOLD, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand("/build grade no"));
        player.sendMessage(prompt);
        player.sendMessage(Component.text().append(yes).append(Component.text(" ")).append(no).build());
    }

    private void handleUngrade(CommandSender sender, String[] args) {
        if (!isReviewer(sender)) {
            sender.sendMessage("You do not have permission to use this command.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /build ungrade <id>");
            return;
        }
        String id = args[1].trim();
        BuildSubmission submission = buildReviewService.get(id);
        if (submission == null) {
            sender.sendMessage("Build not found: " + id);
            return;
        }
        if (submission.status() != BuildStatus.GRADED) {
            sender.sendMessage("Build is not graded: " + submission.id());
            return;
        }
        long removed = removeClaimedXp(submission);
        if (!buildReviewService.ungrade(id)) {
            sender.sendMessage("Failed to ungrade build: " + id);
            return;
        }
        if (removed > 0L) {
            sender.sendMessage("Build " + id + " returned to pending. Removed " + removed + " XP from claimed players.");
        } else {
            sender.sendMessage("Build " + id + " returned to pending.");
        }
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (!isAdmin(sender)) {
            sender.sendMessage("You do not have permission to use this command.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /build remove <id>");
            return;
        }
        String id = args[1].trim();
        BuildSubmission submission = buildReviewService.get(id);
        if (submission == null) {
            sender.sendMessage("Build not found: " + id);
            return;
        }
        if (submission.status() == BuildStatus.GRADED) {
            sender.sendMessage("Build already graded: " + submission.id());
            return;
        }
        if (!buildReviewService.removePending(id)) {
            sender.sendMessage("Failed to remove build: " + id);
            return;
        }
        sender.sendMessage("Build " + id + " removed.");
    }

    private void handleList(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /build list.");
            return;
        }
        List<BuildSubmission> builds = buildReviewService.listForPlayer(player.getUniqueId());
        if (builds.isEmpty()) {
            sender.sendMessage("You have no builds submitted.");
            return;
        }
        player.sendMessage(Component.text("Your Builds:", NamedTextColor.GOLD, TextDecoration.BOLD));
        List<BuildSubmission> pending = new ArrayList<>();
        List<BuildSubmission> graded = new ArrayList<>();
        for (BuildSubmission submission : builds) {
            if (submission.status() == BuildStatus.PENDING) {
                pending.add(submission);
            } else {
                if (!submission.isClaimed(player.getUniqueId())) {
                    graded.add(submission);
                }
            }
        }
        for (BuildSubmission submission : pending) {
            player.sendMessage(
                    buildIdLine(submission.id())
                            .append(Component.text(" "))
                            .append(Component.text("(PENDING)", NamedTextColor.YELLOW))
            );
        }
        for (BuildSubmission submission : graded) {
            long share = submission.shareFor(player.getUniqueId());
            player.sendMessage(
                    buildIdLine(submission.id())
                            .append(Component.text(" "))
                            .append(xpTag(share))
                            .append(Component.text(" "))
                            .append(claimButton(submission.id()))
            );
        }
    }

    private void handleClaim(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can claim build rewards.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /build claim <id>");
            return;
        }
        String id = args[1].trim();
        ClaimResult result = buildReviewService.claim(id, player.getUniqueId());
        if (result.status() == ClaimStatus.NOT_FOUND) {
            sender.sendMessage("Build not found: " + id);
            return;
        }
        if (result.status() == ClaimStatus.NOT_PARTICIPANT) {
            sender.sendMessage("You are not listed on that build.");
            return;
        }
        if (result.status() == ClaimStatus.NOT_READY) {
            sender.sendMessage("That build has not been graded yet.");
            return;
        }
        if (result.status() == ClaimStatus.ALREADY_CLAIMED) {
            sender.sendMessage("Reward already claimed for build " + id + ".");
            return;
        }
        long share = result.share();
        if (share > 0L) {
            progressionService.addXp(player.getUniqueId(), share, "build");
        }
        var data = progressionService.getData(player.getUniqueId());
        data.setBuildXpGained(data.getBuildXpGained() + share);
        player.sendMessage(Component.text("Claimed " + share + " XP from build " + id + ".", NamedTextColor.AQUA));
    }

    private void handleXp(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /build xp.");
            return;
        }
        long xp = progressionService.getData(player.getUniqueId()).getBuildXpGained();
        player.sendMessage(Component.text("Build XP gained: " + xp, NamedTextColor.AQUA));
    }

    private boolean createSubmission(Player player, String id, List<BuildParticipant> participants) {
        Location location = player.getLocation();
        BuildSubmission submission = BuildSubmission.pending(
                id,
                location.getWorld() == null ? "world" : location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                System.currentTimeMillis(),
                participants
        );
        return buildReviewService.submit(submission);
    }

    private void notifyParticipants(BuildSubmission submission) {
        if (submission == null) {
            return;
        }
        for (BuildParticipant participant : submission.participants()) {
            Player player = Bukkit.getPlayer(participant.uuid());
            if (player == null) {
                continue;
            }
            long share = submission.shareFor(participant.uuid());
            Component headline = gradientText("Your build " + submission.id() + " was graded!", true);
            Component body = Component.text("Use /build list to claim " + share + " XP.", NamedTextColor.GRAY);
            player.sendMessage(headline.append(Component.newline()).append(body));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        }
    }

    private void notifyAdminsSubmission(BuildSubmission submission) {
        if (submission == null) {
            return;
        }
        String message = "New build submitted: " + formatSubmissionLine(submission) + " (use /build submissions)";
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isReviewer(player)) {
                player.sendMessage(message);
            }
        }
    }

    private String formatSubmissionLine(BuildSubmission submission) {
        return submission.id()
                + " @ " + submission.world()
                + " (" + submission.x() + ", " + submission.y() + ", " + submission.z() + ")"
                + " - " + submission.participantsLabel();
    }

    private String formatHistoryLine(BuildSubmission submission) {
        return submission.id()
                + " @ " + submission.world()
                + " (" + submission.x() + ", " + submission.y() + ", " + submission.z() + ")"
                + " - " + submission.xpTotal() + " XP - "
                + submission.participantsLabel();
    }

    private boolean isAdmin(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        return player.hasPermission(PERMISSION_ADMIN);
    }

    private boolean isReviewer(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (player.hasPermission(PERMISSION_ADMIN)) {
            return true;
        }
        return hasRole(player, ROLE_GRADER);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("Build commands:");
        sender.sendMessage("/build submit <id> - submit a build for review");
        sender.sendMessage("/build groupsubmit <id> <player> [player...] - submit with co-builders");
        sender.sendMessage("/build list - view your pending/graded builds");
        sender.sendMessage("/build claim <id> - claim XP for a graded build");
        sender.sendMessage("/build xp - show total XP claimed from builds");
        sender.sendMessage("/build history - view your claimed build history");
        if (isReviewer(sender)) {
            sender.sendMessage("/build submissions - list builds waiting for review (reviewer)");
            sender.sendMessage("/build review <id> - spectate a build for review (reviewer)");
            sender.sendMessage("/build grade <effort> <aesthetic> [bonusxp] - grade current build (reviewer)");
            sender.sendMessage("/build grade back - return from build review (reviewer)");
            sender.sendMessage("/build ungrade <id> - return a graded build to pending (reviewer)");
        }
        if (isAdmin(sender)) {
            sender.sendMessage("/build history <player> - view a player's claimed build history (admin)");
            sender.sendMessage("/build remove <id> - remove a pending build (admin)");
        }
    }

    private Component claimButton(String id) {
        return Component.text("[CLAIM]", NamedTextColor.GREEN, TextDecoration.BOLD, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand("/build claim " + id));
    }

    private Component reviewButton(BuildSubmission submission) {
        String command = "/build review " + submission.id();
        return Component.text("[GRADE]", NamedTextColor.GREEN, TextDecoration.BOLD, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand(command));
    }

    private Component submissionLine(BuildSubmission submission) {
        String submitter = "Unknown";
        List<BuildParticipant> participants = submission.participants();
        if (!participants.isEmpty()) {
            String name = participants.get(0).name();
            submitter = (name == null || name.isBlank())
                    ? participants.get(0).uuid().toString()
                    : name;
        }
        return Component.text(submitter, NamedTextColor.YELLOW)
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text(submission.id(), NamedTextColor.AQUA))
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(reviewButton(submission));
    }

    private void markBuildersNotified(BuildSubmission submission, UUID submitterId) {
        if (submission == null) {
            return;
        }
        for (BuildParticipant participant : submission.participants()) {
            if (participant == null || participant.uuid() == null) {
                continue;
            }
            boolean isSubmitter = submitterId != null && submitterId.equals(participant.uuid());
            Player online = Bukkit.getPlayer(participant.uuid());
            if (!isSubmitter && online != null) {
                online.sendMessage(Component.text(
                        "You were added as a builder on build " + submission.id() + ".",
                        NamedTextColor.AQUA
                ));
            }
            if (isSubmitter || online != null) {
                markBuildNotice(participant.uuid(), submission.id());
            }
        }
    }

    private void markBuildNotice(UUID uuid, String buildId) {
        if (uuid == null || buildId == null || buildId.isBlank()) {
            return;
        }
        PlayerData data = progressionService.getData(uuid);
        boolean online = Bukkit.getPlayer(uuid) != null;
        if (!online) {
            data = storageService.load(uuid);
        }
        List<String> notices = data.getBuildSubmissionNotices();
        if (notices == null) {
            notices = new ArrayList<>();
        }
        String normalized = normalizeKey(buildId);
        for (String entry : notices) {
            if (normalizeKey(entry).equals(normalized)) {
                return;
            }
        }
        notices.add(buildId);
        data.setBuildSubmissionNotices(notices);
        if (online) {
            progressionService.save(uuid);
        } else {
            storageService.save(uuid, data);
        }
    }

    private Component buildIdLine(String id) {
        return Component.text(id, NamedTextColor.AQUA);
    }

    private Component xpTag(long share) {
        return Component.text("(" + share + " xp)", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD);
    }

    private Component gradientText(String text, boolean bold) {
        TextColor start = TextColor.color(0x22C55E);
        TextColor end = TextColor.color(0x86EFAC);
        int len = text.length();
        int startRgb = start.value();
        int endRgb = end.value();
        int startR = (startRgb >> 16) & 0xFF;
        int startG = (startRgb >> 8) & 0xFF;
        int startB = startRgb & 0xFF;
        int endR = (endRgb >> 16) & 0xFF;
        int endG = (endRgb >> 8) & 0xFF;
        int endB = endRgb & 0xFF;
        net.kyori.adventure.text.TextComponent.Builder builder = Component.text();
        for (int i = 0; i < len; i++) {
            double t = len <= 1 ? 0.0 : (double) i / (len - 1);
            int r = (int) Math.round(startR + (endR - startR) * t);
            int g = (int) Math.round(startG + (endG - startG) * t);
            int b = (int) Math.round(startB + (endB - startB) * t);
            builder.append(Component.text(String.valueOf(text.charAt(i)), TextColor.color(r, g, b)));
        }
        if (bold) {
            builder.decorate(TextDecoration.BOLD);
        }
        return builder.build();
    }

    private boolean isValidId(String id) {
        if (id == null) {
            return false;
        }
        String trimmed = id.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_ID_LENGTH) {
            return false;
        }
        return !trimmed.contains(" ");
    }

    private List<String> splitNames(String raw) {
        List<String> names = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return names;
        }
        for (String token : raw.split(",")) {
            String name = token.trim();
            if (!name.isBlank()) {
                names.add(name);
            }
        }
        return names;
    }

    private List<String> pendingBuildIds() {
        List<String> ids = new ArrayList<>();
        for (BuildSubmission submission : buildReviewService.listPending()) {
            ids.add(submission.id());
        }
        return ids;
    }

    private List<String> gradedBuildIds() {
        List<String> ids = new ArrayList<>();
        for (BuildSubmission submission : buildReviewService.listGraded()) {
            ids.add(submission.id());
        }
        return ids;
    }

    private long removeClaimedXp(BuildSubmission submission) {
        if (submission == null || submission.status() != BuildStatus.GRADED) {
            return 0L;
        }
        long removed = 0L;
        for (BuildParticipant participant : submission.participants()) {
            UUID uuid = participant.uuid();
            if (!submission.isClaimed(uuid)) {
                continue;
            }
            long share = submission.shareFor(uuid);
            if (share <= 0L) {
                continue;
            }
            removeXpFromPlayer(uuid, share);
            removed += share;
        }
        return removed;
    }

    private void removeXpFromPlayer(UUID uuid, long amount) {
        if (uuid == null || amount <= 0L) {
            return;
        }
        var data = progressionService.getData(uuid);
        long current = data.getTotalXp();
        long newTotal = Math.max(0L, current - amount);
        progressionService.setTotalXp(uuid, newTotal, "build-ungrade");
        long buildXp = data.getBuildXpGained();
        data.setBuildXpGained(Math.max(0L, buildXp - amount));
        progressionService.save(uuid);
    }

    private List<String> onlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    private String targetName(OfflinePlayer target) {
        String name = target.getName();
        return name != null ? name : target.getUniqueId().toString();
    }

    private List<String> filter(List<String> options, String token) {
        if (token == null || token.isBlank()) {
            return options;
        }
        String lower = token.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                matches.add(option);
            }
        }
        return matches;
    }

    private double calculateGradeXp(double effort, double aesthetics) {
        return 4200.0 * Math.pow(Math.log10(effort + 1.0), 2.2) * (0.9 + 0.002 * aesthetics);
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

    private Integer parseIntStrict(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
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

    private boolean hasRole(Player player, String role) {
        if (player == null || role == null || role.isBlank()) {
            return false;
        }
        var data = progressionService.getData(player.getUniqueId());
        List<String> roles = data.getRoles();
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        String normalized = normalizeRole(role);
        for (String entry : roles) {
            if (normalizeRole(entry).equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeRole(String input) {
        if (input == null) {
            return "";
        }
        return input.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
