package org.mydrugs.mydrugs.diary;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Server-saved per-player diary state. Holds every entry the player has written
 * (auto or custom), preserved across logout/login and death (copyOnDeath).
 */
public final class PlayerDiaryAttachment implements ValueIOSerializable {
    /** Hard cap on stored entries to prevent unbounded NBT growth. Oldest are pruned first. */
    public static final int MAX_ENTRIES = 2048;
    /** Hard cap on remembered blockers. These are small, deduplicated hints, not a quest log. */
    public static final int MAX_RECENT_BLOCKERS = 16;
    /** Custom entry char cap (server-side). Anything longer is rejected. */
    public static final int CUSTOM_MAX_LENGTH = 1200;
    /** Soft floor for custom entries; anything shorter after trim/sanitize is rejected. */
    public static final int CUSTOM_MIN_LENGTH = 1;
    /** Writing cooldown, in ticks, between auto/custom diary entries. Mirrors PersonalDiaryItem. */
    public static final int WRITE_COOLDOWN_TICKS = 1200;

    private final List<DiaryEntry> entries = new ArrayList<>();
    private final List<RecentBlocker> recentBlockers = new ArrayList<>();
    /** Last game time at which this player wrote a diary entry (auto OR custom). */
    private long lastWriteGameTime = -100000L;

    public record RecentBlocker(String type, long firstSeenGameTime, long lastSeenGameTime, int count) {
        public RecentBlocker {
            type = sanitizeType(type);
            firstSeenGameTime = Math.max(0L, firstSeenGameTime);
            lastSeenGameTime = Math.max(firstSeenGameTime, lastSeenGameTime);
            count = Math.max(1, count);
        }
    }

    /** Returns an unmodifiable view of all entries in insertion (chronological) order. */
    public List<DiaryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public List<RecentBlocker> getRecentBlockers() {
        return Collections.unmodifiableList(recentBlockers);
    }

    /** Append an entry; prune oldest if we exceed {@link #MAX_ENTRIES}. */
    public void append(DiaryEntry entry) {
        if (entry == null) return;
        entries.add(entry);
        while (entries.size() > MAX_ENTRIES) {
            entries.removeFirst();
        }
    }

    public void recordBlocker(String type, long gameTime) {
        String cleanType = sanitizeType(type);
        if (cleanType.isEmpty()) {
            return;
        }

        for (int i = 0; i < recentBlockers.size(); i++) {
            RecentBlocker existing = recentBlockers.get(i);
            if (existing.type().equals(cleanType)) {
                recentBlockers.remove(i);
                recentBlockers.addFirst(new RecentBlocker(
                        cleanType,
                        existing.firstSeenGameTime(),
                        gameTime,
                        existing.count() + 1
                ));
                trimRecentBlockers();
                return;
            }
        }

        recentBlockers.addFirst(new RecentBlocker(cleanType, gameTime, gameTime, 1));
        trimRecentBlockers();
    }

    private void trimRecentBlockers() {
        while (recentBlockers.size() > MAX_RECENT_BLOCKERS) {
            recentBlockers.removeLast();
        }
    }

    public long getLastWriteGameTime() {
        return lastWriteGameTime;
    }

    public void markWritten(long gameTime) {
        this.lastWriteGameTime = gameTime;
    }

    /** Remaining cooldown ticks at {@code currentGameTime}; 0 if writing is allowed now. */
    public int remainingCooldownTicks(long currentGameTime) {
        long ready = lastWriteGameTime + WRITE_COOLDOWN_TICKS;
        long remaining = ready - currentGameTime;
        if (remaining <= 0) return 0;
        return (int) Math.min(Integer.MAX_VALUE, remaining);
    }

    public boolean canWrite(long currentGameTime) {
        return remainingCooldownTicks(currentGameTime) <= 0;
    }

    public boolean hasEntryToday(long currentDay) {
        for (int i = entries.size() - 1; i >= 0; i--) {
            DiaryEntry e = entries.get(i);
            if (e.day() < currentDay) return false;
            if (e.day() == currentDay) return true;
        }
        return false;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putLong("last_write_game_time", lastWriteGameTime);
        ValueOutput.ValueOutputList list = output.childrenList("entries");
        for (DiaryEntry entry : entries) {
            ValueOutput child = list.addChild();
            child.putLong("day", entry.day());
            child.putLong("created", entry.createdGameTime());
            child.putString("type", entry.type().name());
            child.putString("content", entry.content());
            if (!entry.sourceKey().isEmpty()) child.putString("source", entry.sourceKey());
            if (!entry.dominantDrugId().isEmpty()) child.putString("drug", entry.dominantDrugId());
        }
        if (list.isEmpty()) {
            output.discard("entries");
        }
        ValueOutput.ValueOutputList blockers = output.childrenList("recent_blockers");
        for (RecentBlocker blocker : recentBlockers) {
            ValueOutput child = blockers.addChild();
            child.putString("type", blocker.type());
            child.putLong("first_seen", blocker.firstSeenGameTime());
            child.putLong("last_seen", blocker.lastSeenGameTime());
            child.putInt("count", blocker.count());
        }
        if (blockers.isEmpty()) {
            output.discard("recent_blockers");
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        lastWriteGameTime = input.getLongOr("last_write_game_time", -100000L);
        entries.clear();
        for (ValueInput child : input.childrenListOrEmpty("entries")) {
            long day = child.getLongOr("day", 0L);
            long created = child.getLongOr("created", 0L);
            DiaryEntryType type = DiaryEntryType.parse(child.getStringOr("type", "AUTO"));
            String content = child.getStringOr("content", "");
            String source = child.getStringOr("source", "");
            String drug = child.getStringOr("drug", "");
            entries.add(new DiaryEntry(day, created, type, content, source, drug));
        }
        recentBlockers.clear();
        for (ValueInput child : input.childrenListOrEmpty("recent_blockers")) {
            String type = sanitizeType(child.getStringOr("type", ""));
            if (type.isEmpty()) {
                continue;
            }
            recentBlockers.add(new RecentBlocker(
                    type,
                    child.getLongOr("first_seen", 0L),
                    child.getLongOr("last_seen", 0L),
                    child.getIntOr("count", 1)
            ));
            if (recentBlockers.size() >= MAX_RECENT_BLOCKERS) {
                break;
            }
        }
    }

    /**
     * Sanitize a raw custom-entry submission from the client. Strips control characters,
     * trims, and enforces length bounds. Returns {@code null} if the result is invalid.
     */
    public static String sanitizeCustomContent(String raw) {
        if (raw == null) return null;
        StringBuilder sb = new StringBuilder(Math.min(raw.length(), CUSTOM_MAX_LENGTH * 2));
        for (int i = 0; i < raw.length() && sb.length() < CUSTOM_MAX_LENGTH; i++) {
            char c = raw.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t' || (c >= 0x20 && c != 0x7F)) {
                sb.append(c);
            }
        }
        String cleaned = sb.toString().trim();
        if (cleaned.length() < CUSTOM_MIN_LENGTH) return null;
        return cleaned;
    }

    private static String sanitizeType(String raw) {
        if (raw == null) {
            return "";
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder(Math.min(normalized.length(), 48));
        for (int i = 0; i < normalized.length() && builder.length() < 48; i++) {
            char c = normalized.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-') {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /** Helper: current Minecraft day from a {@code level.getGameTime()}. */
    public static long currentDay(long gameTime) {
        return gameTime / 24000L + 1L;
    }
}
