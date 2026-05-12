package org.mydrugs.mydrugs.effects.addiction.diary;

public record DiaryDrugStatDto(
        String drugId,
        float lifetimeDose,
        float addictionValue,
        float withdrawalMeter,
        float tolerance,
        float peakHistoricalAddiction,
        float currentDose
) {
}
