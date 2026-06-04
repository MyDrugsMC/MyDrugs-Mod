package org.mydrugs.mydrugs.addiction.data;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.mydrugs.mydrugs.core.drug.dose.DoseContribution;
import org.mydrugs.mydrugs.core.drug.dose.DoseState;

import java.util.ArrayList;
import java.util.List;

public final class DrugAddictionStats implements ValueIOSerializable {
    public static final int INTEGRATION_STAGE_NONE = 0;
    public static final int INTEGRATION_STAGE_ELIGIBLE = 1;
    public static final int INTEGRATION_STAGE_INTEGRATED = 2;

    public float addictionValue;
    public float baseWithdrawalMeter;
    public float tolerance;
    public long lastUseTime;

    public float relapseMemory;
    public float peakHistoricalAddiction;
    public float lifetimeDoseConsumed;

    // Integration arc. Eligibility follows reckoning/recovery; it is not the reckoning itself.
    public int integrationStage;
    public float recoveryProgress;
    public long integratedAtGameTime = -1L;
    public int cleanIntegrationDoseStreak;
    public int cleanPsychedelicReflectionCount;
    public int cleanPsychedelicSafeUseCount;
    public long lastPsychedelicReflectionUseTime;
    public long lastBadTripGameTime = -1L;

    public final List<DoseContribution> doseContributions = new ArrayList<>();
    public DoseState lastDoseState = DoseState.NORMAL;

    public float currentDose() {
        float total = 0f;
        for (DoseContribution c : doseContributions) {
            total += c.currentValue();
        }
        return total;
    }

    public boolean isIntegrated() {
        return integrationStage == INTEGRATION_STAGE_INTEGRATED;
    }

    public boolean isEmpty() {
        return addictionValue <= 0.0001F
                && baseWithdrawalMeter <= 0.0001F
                && tolerance <= 0.0001F
                && relapseMemory <= 0.0001F
                && peakHistoricalAddiction <= 0.0001F
                && lifetimeDoseConsumed <= 0.0001F
                && cleanIntegrationDoseStreak <= 0
                && cleanPsychedelicReflectionCount <= 0
                && cleanPsychedelicSafeUseCount <= 0
                && doseContributions.isEmpty();
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putFloat("addiction_value", addictionValue);
        output.putFloat("withdrawal_meter", baseWithdrawalMeter);
        output.putFloat("tolerance", tolerance);
        output.putLong("last_use_time", lastUseTime);
        output.putFloat("relapse_memory", relapseMemory);
        output.putFloat("peak_historical_addiction", peakHistoricalAddiction);
        output.putFloat("lifetime_dose_consumed", lifetimeDoseConsumed);
        output.putInt("integration_stage", integrationStage);
        output.putFloat("recovery_progress", recoveryProgress);
        output.putLong("integrated_at_game_time", integratedAtGameTime);
        output.putInt("clean_integration_dose_streak", cleanIntegrationDoseStreak);
        output.putInt("clean_psychedelic_reflection_count", cleanPsychedelicReflectionCount);
        output.putInt("clean_psychedelic_safe_use_count", cleanPsychedelicSafeUseCount);
        output.putLong("last_psychedelic_reflection_use_time", lastPsychedelicReflectionUseTime);
        output.putLong("last_bad_trip_game_time", lastBadTripGameTime);
        output.putString("last_dose_state", lastDoseState.name());

        ValueOutput contribs = output.child("dose_contributions");
        contribs.putInt("count", doseContributions.size());
        for (int i = 0; i < doseContributions.size(); i++) {
            DoseContribution c = doseContributions.get(i);
            ValueOutput child = contribs.child("c" + i);
            child.putFloat("amount", c.amount);
            child.putInt("ticks_remaining", c.ticksRemaining);
            child.putInt("total_duration", c.totalDuration);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        addictionValue = input.getFloatOr("addiction_value", 0.0F);
        baseWithdrawalMeter = input.getFloatOr("withdrawal_meter", 0.0F);
        tolerance = input.getFloatOr("tolerance", 0.0F);
        lastUseTime = input.getLongOr("last_use_time", 0L);
        relapseMemory = input.getFloatOr("relapse_memory", 0.0F);
        peakHistoricalAddiction = input.getFloatOr("peak_historical_addiction", 0.0F);
        lifetimeDoseConsumed = input.getFloatOr("lifetime_dose_consumed", 0.0F);
        integrationStage = input.getIntOr("integration_stage", 0);
        recoveryProgress = input.getFloatOr("recovery_progress", 0.0F);
        integratedAtGameTime = input.getLongOr("integrated_at_game_time", -1L);
        cleanIntegrationDoseStreak = input.getIntOr("clean_integration_dose_streak", 0);
        cleanPsychedelicReflectionCount = input.getIntOr("clean_psychedelic_reflection_count", 0);
        cleanPsychedelicSafeUseCount = input.getIntOr("clean_psychedelic_safe_use_count", 0);
        lastPsychedelicReflectionUseTime = input.getLongOr("last_psychedelic_reflection_use_time", 0L);
        lastBadTripGameTime = input.getLongOr("last_bad_trip_game_time", -1L);

        String stateName = input.getStringOr("last_dose_state", "NORMAL");
        try {
            lastDoseState = DoseState.valueOf(stateName);
        } catch (IllegalArgumentException e) {
            lastDoseState = DoseState.NORMAL;
        }

        doseContributions.clear();
        ValueInput contribs = input.childOrEmpty("dose_contributions");
        int count = contribs.getIntOr("count", 0);
        for (int i = 0; i < count; i++) {
            ValueInput child = contribs.childOrEmpty("c" + i);
            float amount = child.getFloatOr("amount", 0f);
            int ticksRemaining = child.getIntOr("ticks_remaining", 0);
            int totalDuration = child.getIntOr("total_duration", 1);
            if (amount > 0 && ticksRemaining > 0) {
                doseContributions.add(new DoseContribution(amount, ticksRemaining, totalDuration));
            }
        }
    }

    public DrugAddictionStats copy() {
        DrugAddictionStats copy = new DrugAddictionStats();
        copy.addictionValue = addictionValue;
        copy.baseWithdrawalMeter = baseWithdrawalMeter;
        copy.tolerance = tolerance;
        copy.lastUseTime = lastUseTime;
        copy.relapseMemory = relapseMemory;
        copy.peakHistoricalAddiction = peakHistoricalAddiction;
        copy.lifetimeDoseConsumed = lifetimeDoseConsumed;
        copy.integrationStage = integrationStage;
        copy.recoveryProgress = recoveryProgress;
        copy.integratedAtGameTime = integratedAtGameTime;
        copy.cleanIntegrationDoseStreak = cleanIntegrationDoseStreak;
        copy.cleanPsychedelicReflectionCount = cleanPsychedelicReflectionCount;
        copy.cleanPsychedelicSafeUseCount = cleanPsychedelicSafeUseCount;
        copy.lastPsychedelicReflectionUseTime = lastPsychedelicReflectionUseTime;
        copy.lastBadTripGameTime = lastBadTripGameTime;
        copy.lastDoseState = lastDoseState;
        for (DoseContribution c : doseContributions) {
            copy.doseContributions.add(new DoseContribution(c.amount, c.ticksRemaining, c.totalDuration));
        }
        return copy;
    }

    public float addictionNorm() {
        return addictionValue / 100.0F;
    }

    public float withdrawalNorm() {
        return baseWithdrawalMeter / 100.0F;
    }
}
