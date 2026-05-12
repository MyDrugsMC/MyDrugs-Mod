package org.mydrugs.mydrugs.effects.addiction.diary;

/**
 * Current player-state summary for page 1 ("How I feel today").
 * Bitmask of recovery supports uses the same flag layout as AddictionClientState (RECOVERY_*).
 */
public record DiaryPlayerStateDto(
        float stress,
        float globalSeverity,
        String dominantDrugId,
        String dominantCategory,
        String doseState,
        boolean badTripActive,
        float badTripSeverity,
        int overdoseTimerTicks,
        int symptomFlags,
        int recoveryFlags,
        boolean sleepBlocked
) {
}
