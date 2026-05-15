package org.mydrugs.mydrugs.diary;

public record DiaryEntryDto(
        long day,
        long createdGameTime,
        String type,
        String content,
        String sourceKey,
        String dominantDrugId
) {
}
