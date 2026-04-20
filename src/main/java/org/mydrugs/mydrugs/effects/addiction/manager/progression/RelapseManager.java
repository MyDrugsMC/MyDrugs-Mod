package org.mydrugs.mydrugs.effects.addiction.manager.progression;

import org.mydrugs.mydrugs.core.drug.AddictionConfigs;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.effects.addiction.data.DrugAddictionStats;

public final class RelapseManager {
    private RelapseManager() {
    }

    public static void onUse(DrugCategory category, DrugAddictionStats stats) {
        float weight = AddictionConfigs.get(category).relapseWeight();
        stats.relapseMemory = Math.max(stats.relapseMemory, stats.addictionNorm() * weight);
        stats.peakHistoricalAddiction = Math.max(stats.peakHistoricalAddiction, stats.addictionValue);
    }

    public static void decay(DrugAddictionStats stats) {
        stats.relapseMemory = Math.max(0.0F, stats.relapseMemory - 0.00005F);
    }

    public static float applyRelapseMultiplier(float baseGain, DrugAddictionStats stats) {
        return baseGain * (1.0F + stats.relapseMemory * 0.50F);
    }
}