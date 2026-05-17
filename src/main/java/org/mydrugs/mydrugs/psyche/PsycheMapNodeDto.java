package org.mydrugs.mydrugs.psyche;

public record PsycheMapNodeDto(
        String nodeId,
        long unlockedAtGameTime,
        long unlockedDay,
        String trigger,
        String dominantDrugId
) {
}
