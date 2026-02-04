package com.nickrodi.nir.service;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class BuildReviewService {
    private static final String ROOT = "submissions";

    private final JavaPlugin plugin;
    private final File file;
    private final Map<String, BuildSubmission> submissions = new LinkedHashMap<>();

    public BuildReviewService(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.file = new File(plugin.getDataFolder(), "build-reviews.yml");
    }

    public void load() {
        submissions.clear();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = config.getConfigurationSection(ROOT);
        if (root == null) {
            return;
        }
        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            BuildSubmission submission = BuildSubmission.from(section);
            if (submission == null || submission.id.isBlank()) {
                continue;
            }
            submissions.put(normalize(submission.id), submission);
        }
    }

    public void save() {
        ensureDataFolder();
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection root = config.createSection(ROOT);
        for (BuildSubmission submission : submissions.values()) {
            ConfigurationSection section = root.createSection(normalize(submission.id));
            submission.write(section);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, () -> "Failed to save build reviews: " + e.getMessage());
        }
    }

    public boolean submit(BuildSubmission submission) {
        if (submission == null || submission.id.isBlank()) {
            return false;
        }
        String key = normalize(submission.id);
        if (submissions.containsKey(key)) {
            return false;
        }
        submissions.put(key, submission);
        save();
        return true;
    }

    public BuildSubmission get(String id) {
        return submissions.get(normalize(id));
    }

    public boolean grade(String id, long xpTotal, UUID grader, String graderName) {
        BuildSubmission submission = get(id);
        if (submission == null || submission.status == BuildStatus.GRADED) {
            return false;
        }
        submission.grade(xpTotal, grader, graderName);
        save();
        return true;
    }

    public ClaimResult claim(String id, UUID player) {
        BuildSubmission submission = get(id);
        if (submission == null) {
            return new ClaimResult(ClaimStatus.NOT_FOUND, 0L);
        }
        if (!submission.isParticipant(player)) {
            return new ClaimResult(ClaimStatus.NOT_PARTICIPANT, 0L);
        }
        if (submission.status != BuildStatus.GRADED) {
            return new ClaimResult(ClaimStatus.NOT_READY, 0L);
        }
        if (submission.isClaimed(player)) {
            return new ClaimResult(ClaimStatus.ALREADY_CLAIMED, submission.shareFor(player));
        }
        long share = submission.shareFor(player);
        submission.markClaimed(player);
        save();
        return new ClaimResult(ClaimStatus.OK, share);
    }

    public List<BuildSubmission> listPending() {
        List<BuildSubmission> list = new ArrayList<>();
        for (BuildSubmission submission : submissions.values()) {
            if (submission.status == BuildStatus.PENDING) {
                list.add(submission);
            }
        }
        return sorted(list, Comparator.comparingLong(submission -> submission.submittedAt));
    }

    public List<BuildSubmission> listGraded() {
        List<BuildSubmission> list = new ArrayList<>();
        for (BuildSubmission submission : submissions.values()) {
            if (submission.status == BuildStatus.GRADED) {
                list.add(submission);
            }
        }
        return sorted(list, Comparator.comparingLong(submission -> submission.gradedAt));
    }

    public List<BuildSubmission> listForPlayer(UUID player) {
        List<BuildSubmission> list = new ArrayList<>();
        for (BuildSubmission submission : submissions.values()) {
            if (submission.isParticipant(player)) {
                list.add(submission);
            }
        }
        list.sort(Comparator.comparingLong(submission -> submission.submittedAt));
        return list;
    }

    public boolean hasUnclaimed(UUID player) {
        for (BuildSubmission submission : submissions.values()) {
            if (submission.status == BuildStatus.GRADED
                    && submission.isParticipant(player)
                    && !submission.isClaimed(player)) {
                return true;
            }
        }
        return false;
    }

    private List<BuildSubmission> sorted(List<BuildSubmission> list, Comparator<BuildSubmission> comparator) {
        if (list.isEmpty()) {
            return list;
        }
        list.sort(comparator.reversed());
        return list;
    }

    private void ensureDataFolder() {
        File folder = plugin.getDataFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().log(Level.WARNING, () -> "Failed to create data folder for build reviews.");
        }
    }

    private static String normalize(String id) {
        if (id == null) {
            return "";
        }
        return id.trim().toLowerCase(Locale.ROOT);
    }

    public enum BuildStatus {
        PENDING,
        GRADED
    }

    public enum ClaimStatus {
        OK,
        NOT_FOUND,
        NOT_PARTICIPANT,
        NOT_READY,
        ALREADY_CLAIMED
    }

    public record ClaimResult(ClaimStatus status, long share) {
    }

    public static final class BuildSubmission {
        private final String id;
        private final String world;
        private final int x;
        private final int y;
        private final int z;
        private final long submittedAt;
        private final List<BuildParticipant> participants;
        private BuildStatus status;
        private long gradedAt;
        private long xpTotal;
        private Map<UUID, Long> shares;
        private Set<UUID> claimed;
        private UUID gradedBy;
        private String gradedByName;

        private BuildSubmission(
                String id,
                String world,
                int x,
                int y,
                int z,
                long submittedAt,
                List<BuildParticipant> participants,
                BuildStatus status,
                long gradedAt,
                long xpTotal,
                Map<UUID, Long> shares,
                Set<UUID> claimed,
                UUID gradedBy,
                String gradedByName
        ) {
            this.id = id;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.submittedAt = submittedAt;
            this.participants = participants == null ? new ArrayList<>() : new ArrayList<>(participants);
            this.status = status == null ? BuildStatus.PENDING : status;
            this.gradedAt = gradedAt;
            this.xpTotal = xpTotal;
            this.shares = shares == null ? new HashMap<>() : new HashMap<>(shares);
            this.claimed = claimed == null ? new HashSet<>() : new HashSet<>(claimed);
            this.gradedBy = gradedBy;
            this.gradedByName = gradedByName;
            if (this.status == BuildStatus.GRADED && this.shares.isEmpty() && this.xpTotal > 0L) {
                this.shares = splitShares(this.xpTotal, this.participants);
            }
        }

        public static BuildSubmission pending(
                String id,
                String world,
                int x,
                int y,
                int z,
                long submittedAt,
                List<BuildParticipant> participants
        ) {
            return new BuildSubmission(
                    id,
                    world,
                    x,
                    y,
                    z,
                    submittedAt,
                    participants,
                    BuildStatus.PENDING,
                    0L,
                    0L,
                    new HashMap<>(),
                    new HashSet<>(),
                    null,
                    null
            );
        }

        public String id() {
            return id;
        }

        public String world() {
            return world;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public int z() {
            return z;
        }

        public long submittedAt() {
            return submittedAt;
        }

        public List<BuildParticipant> participants() {
            return new ArrayList<>(participants);
        }

        public BuildStatus status() {
            return status;
        }

        public long gradedAt() {
            return gradedAt;
        }

        public long xpTotal() {
            return xpTotal;
        }

        public String gradedByName() {
            return gradedByName;
        }

        public boolean isParticipant(UUID uuid) {
            if (uuid == null) {
                return false;
            }
            for (BuildParticipant participant : participants) {
                if (uuid.equals(participant.uuid())) {
                    return true;
                }
            }
            return false;
        }

        public long shareFor(UUID uuid) {
            if (uuid == null || shares == null) {
                return 0L;
            }
            return shares.getOrDefault(uuid, 0L);
        }

        public boolean isClaimed(UUID uuid) {
            return uuid != null && claimed != null && claimed.contains(uuid);
        }

        public void markClaimed(UUID uuid) {
            if (uuid == null) {
                return;
            }
            if (claimed == null) {
                claimed = new HashSet<>();
            }
            claimed.add(uuid);
        }

        private void grade(long xpTotal, UUID grader, String graderName) {
            this.status = BuildStatus.GRADED;
            this.gradedAt = System.currentTimeMillis();
            this.xpTotal = Math.max(0L, xpTotal);
            this.gradedBy = grader;
            this.gradedByName = graderName;
            this.shares = splitShares(this.xpTotal, this.participants);
            if (this.claimed == null) {
                this.claimed = new HashSet<>();
            }
        }

        public String participantsLabel() {
            if (participants.isEmpty()) {
                return "Unknown";
            }
            List<String> names = new ArrayList<>();
            for (BuildParticipant participant : participants) {
                String name = participant.name();
                if (name == null || name.isBlank()) {
                    name = participant.uuid().toString();
                }
                names.add(name);
            }
            return String.join(", ", names);
        }

        private void write(ConfigurationSection section) {
            section.set("id", id);
            section.set("world", world);
            section.set("x", x);
            section.set("y", y);
            section.set("z", z);
            section.set("submittedAt", submittedAt);
            section.set("status", status.name().toLowerCase(Locale.ROOT));
            section.set("gradedAt", gradedAt);
            section.set("xpTotal", xpTotal);
            if (gradedBy != null) {
                section.set("gradedBy", gradedBy.toString());
            }
            if (gradedByName != null && !gradedByName.isBlank()) {
                section.set("gradedByName", gradedByName);
            }
            List<Map<String, Object>> storedParticipants = new ArrayList<>();
            for (BuildParticipant participant : participants) {
                Map<String, Object> map = new HashMap<>();
                map.put("uuid", participant.uuid().toString());
                if (participant.name() != null && !participant.name().isBlank()) {
                    map.put("name", participant.name());
                }
                storedParticipants.add(map);
            }
            section.set("participants", storedParticipants);
            if (shares != null && !shares.isEmpty()) {
                Map<String, Object> storedShares = new HashMap<>();
                for (Map.Entry<UUID, Long> entry : shares.entrySet()) {
                    storedShares.put(entry.getKey().toString(), entry.getValue());
                }
                section.createSection("shares", storedShares);
            }
            if (claimed != null && !claimed.isEmpty()) {
                List<String> claimedIds = new ArrayList<>();
                for (UUID uuid : claimed) {
                    claimedIds.add(uuid.toString());
                }
                section.set("claimed", claimedIds);
            }
        }

        private static BuildSubmission from(ConfigurationSection section) {
            String id = section.getString("id", "");
            if (id.isBlank()) {
                return null;
            }
            String world = section.getString("world", "world");
            int x = section.getInt("x", 0);
            int y = section.getInt("y", 0);
            int z = section.getInt("z", 0);
            long submittedAt = section.getLong("submittedAt", 0L);
            String statusRaw = section.getString("status", "pending");
            BuildStatus status = "graded".equalsIgnoreCase(statusRaw) ? BuildStatus.GRADED : BuildStatus.PENDING;
            long gradedAt = section.getLong("gradedAt", 0L);
            long xpTotal = section.getLong("xpTotal", 0L);
            UUID gradedBy = null;
            String gradedByRaw = section.getString("gradedBy", "");
            if (!gradedByRaw.isBlank()) {
                try {
                    gradedBy = UUID.fromString(gradedByRaw);
                } catch (IllegalArgumentException ignored) {
                    gradedBy = null;
                }
            }
            String gradedByName = section.getString("gradedByName", null);
            List<BuildParticipant> participants = new ArrayList<>();
            for (Map<?, ?> entry : section.getMapList("participants")) {
                Object uuidRaw = entry.get("uuid");
                if (uuidRaw == null) {
                    continue;
                }
                try {
                    UUID uuid = UUID.fromString(String.valueOf(uuidRaw));
                    String name = entry.get("name") == null ? null : String.valueOf(entry.get("name"));
                    participants.add(new BuildParticipant(uuid, name));
                } catch (IllegalArgumentException ignored) {
                    // skip invalid uuid
                }
            }
            Map<UUID, Long> shares = new HashMap<>();
            ConfigurationSection sharesSection = section.getConfigurationSection("shares");
            if (sharesSection != null) {
                for (String key : sharesSection.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(key);
                        long share = sharesSection.getLong(key, 0L);
                        shares.put(uuid, share);
                    } catch (IllegalArgumentException ignored) {
                        // skip invalid share key
                    }
                }
            }
            Set<UUID> claimed = new HashSet<>();
            List<String> claimedList = section.getStringList("claimed");
            if (claimedList != null) {
                for (String raw : claimedList) {
                    try {
                        claimed.add(UUID.fromString(raw));
                    } catch (IllegalArgumentException ignored) {
                        // skip invalid uuid
                    }
                }
            }
            return new BuildSubmission(
                    id,
                    world,
                    x,
                    y,
                    z,
                    submittedAt,
                    participants,
                    status,
                    gradedAt,
                    xpTotal,
                    shares,
                    claimed,
                    gradedBy,
                    gradedByName
            );
        }

        private static Map<UUID, Long> splitShares(long total, Collection<BuildParticipant> participants) {
            Map<UUID, Long> result = new HashMap<>();
            if (participants == null || participants.isEmpty()) {
                return result;
            }
            List<BuildParticipant> sorted = new ArrayList<>(participants);
            sorted.sort(Comparator.comparing(participant -> participant.uuid().toString()));
            int count = sorted.size();
            long base = total / count;
            long remainder = total % count;
            for (int i = 0; i < sorted.size(); i++) {
                BuildParticipant participant = sorted.get(i);
                long share = base + (i < remainder ? 1L : 0L);
                result.put(participant.uuid(), share);
            }
            return result;
        }
    }

    public record BuildParticipant(UUID uuid, String name) {
    }
}
