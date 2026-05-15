package org.mydrugs.mydrugs.diary;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Server-saved per-player diary state. Holds every entry the player has written
 * (auto or custom), preserved across logout/login and death (copyOnDeath).
 */
public final class PlayerDiaryAttachment implements ValueIOSerializable {
    /** Hard cap on stored entries to prevent unbounded NBT growth. Oldest are pruned first. */
    public static final int MAX_ENTRIES = 2048;
    /** Custom entry char cap (server-side). Anything longer is rejected. */
    public static final int CUSTOM_MAX_LENGTH = 1200;
    /** Soft floor for custom entries; anything shorter after trim/sanitize is rejected. */
    public static final int CUSTOM_MIN_LENGTH = 1;
    /** Writing cooldown, in ticks, between auto/custom diary entries. Mirrors PersonalDiaryItem. */
    public static final int WRITE_COOLDOWN_TICKS = 1200;

    private final List<DiaryEntry> entries = new ArrayList<>();
    /** Last game time at which this player wrote a diary entry (auto OR custom). */
    private long lastWriteGameTime = -100000L;

    /** Returns an unmodifiable view of all entries in insertion (chronological) order. */
    public List<DiaryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /** Append an entry; prune oldest if we exceed {@link #MAX_ENTRIES}. */
    public void append(DiaryEntry entry) {
        if (entry == null) return;
        entries.add(entry);
        while (entries.size() > MAX_ENTRIES) {
            entries.removeFirst();
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

    /** Helper: current Minecraft day from a {@code level.getGameTime()}. */
    public static long currentDay(long gameTime) {
        return gameTime / 24000L + 1L;
    }
}
