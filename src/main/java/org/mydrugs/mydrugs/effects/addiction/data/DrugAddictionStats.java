package org.mydrugs.mydrugs.effects.addiction.data;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public final class DrugAddictionStats implements ValueIOSerializable {
    public float addictionValue;
    public float baseWithdrawalMeter;
    public float tolerance;
    public long lastUseTime;

    public float relapseMemory;
    public float peakHistoricalAddiction;

    // --- Dose system ---
    /** What's currently active in the body, drives symptoms/state. */
    public float currentDose;
    /** What's been consumed but not yet absorbed. currentDose lerps toward this. */
    public float targetDose;
    /** Ticks per unit that currentDose rises toward targetDose (set by last consume). */
    public float absorptionRatePerTick;

    @Override
    public void serialize(ValueOutput output) {
        output.putFloat("addiction_value", addictionValue);
        output.putFloat("withdrawal_meter", baseWithdrawalMeter);
        output.putFloat("tolerance", tolerance);
        output.putLong("last_use_time", lastUseTime);
        output.putFloat("relapse_memory", relapseMemory);
        output.putFloat("peak_historical_addiction", peakHistoricalAddiction);

        output.putFloat("current_dose", currentDose);
        output.putFloat("target_dose", targetDose);
        output.putFloat("absorption_rate_per_tick", absorptionRatePerTick);
    }

    @Override
    public void deserialize(ValueInput input) {
        addictionValue = input.getFloatOr("addiction_value", 0.0F);
        baseWithdrawalMeter = input.getFloatOr("withdrawal_meter", 0.0F);
        tolerance = input.getFloatOr("tolerance", 0.0F);
        lastUseTime = input.getLongOr("last_use_time", 0L);
        relapseMemory = input.getFloatOr("relapse_memory", 0.0F);
        peakHistoricalAddiction = input.getFloatOr("peak_historical_addiction", 0.0F);

        currentDose = input.getFloatOr("current_dose", 0.0F);
        targetDose = input.getFloatOr("target_dose", 0.0F);
        absorptionRatePerTick = input.getFloatOr("absorption_rate_per_tick", 0.0F);
    }

    public DrugAddictionStats copy() {
        DrugAddictionStats copy = new DrugAddictionStats();
        copy.addictionValue = addictionValue;
        copy.baseWithdrawalMeter = baseWithdrawalMeter;
        copy.tolerance = tolerance;
        copy.lastUseTime = lastUseTime;
        copy.relapseMemory = relapseMemory;
        copy.peakHistoricalAddiction = peakHistoricalAddiction;
        copy.currentDose = currentDose;
        copy.targetDose = targetDose;
        copy.absorptionRatePerTick = absorptionRatePerTick;
        return copy;
    }

    public float addictionNorm() {
        return addictionValue / 100.0F;
    }

    public float withdrawalNorm() {
        return baseWithdrawalMeter / 100.0F;
    }
}
