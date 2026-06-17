package org.mydrugs.mydrugs.diary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerDiaryMilestoneTest {
    @Test
    void automaticMilestonesAppendOncePerSourceAndDrug() {
        PlayerDiaryAttachment diary = new PlayerDiaryAttachment();
        DiaryEntry coffee = entry("dimension.trial_completed", "coffee");
        DiaryEntry coffeeAgain = entry("dimension.trial_completed", "coffee");
        DiaryEntry tobacco = entry("dimension.trial_completed", "tobacco");

        assertTrue(diary.appendIfAbsent(coffee));
        assertFalse(diary.appendIfAbsent(coffeeAgain));
        assertTrue(diary.appendIfAbsent(tobacco));
        assertEquals(2, diary.getEntries().size());
    }

    @Test
    void firstMilestonesDeduplicateAcrossDrugContext() {
        PlayerDiaryAttachment diary = new PlayerDiaryAttachment();

        assertTrue(diary.appendIfSourceAbsent(entry("dimension.first_region", "coffee")));
        assertFalse(diary.appendIfSourceAbsent(entry("dimension.first_region", "lsd")));
        assertEquals(1, diary.getEntries().size());
    }

    private static DiaryEntry entry(String source, String drug) {
        return new DiaryEntry(1L, 20L, DiaryEntryType.AUTO, "Test milestone", source, drug);
    }
}
