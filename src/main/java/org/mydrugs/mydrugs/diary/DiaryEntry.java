package org.mydrugs.mydrugs.diary;

/**
 * One saved diary entry.
 *
 * @param day            Minecraft day number ({@code level.getGameTime() / 24000L + 1}) at write time.
 * @param createdGameTime Raw game time when the entry was written.
 * @param type           AUTO (generated from state) or CUSTOM (written by player).
 * @param content        Sanitized text content.
 * @param sourceKey      Optional state key for debugging/future filtering, e.g. "bad_trip", "calm_day".
 * @param dominantDrugId Optional dominant drug id string at the time of writing, e.g. "weed".
 */
public record DiaryEntry(
        long day,
        long createdGameTime,
        DiaryEntryType type,
        String content,
        String sourceKey,
        String dominantDrugId
) {
    public DiaryEntry {
        if (content == null) content = "";
        if (sourceKey == null) sourceKey = "";
        if (dominantDrugId == null) dominantDrugId = "";
        if (type == null) type = DiaryEntryType.AUTO;
    }
}
