package org.mydrugs.mydrugs.effects.addiction.data;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.mydrugs.mydrugs.effects.addiction.dose.DoseContribution;
import org.mydrugs.mydrugs.effects.addiction.dose.DoseState;

import java.util.ArrayList;
import java.util.List;

public final class DrugAddictionStats implements ValueIOSerializable {
    public float addictionValue;
    public float baseWithdrawalMeter;
    public float tolerance;
    public long lastUseTime;

    public float relapseMemory;
    public float peakHistoricalAddiction;

    public final List<DoseContribution> doseContributions = new ArrayList<>();
    public DoseState lastDoseState = DoseState.NORMAL;

    public float currentDose() {
        float total = 0f;
        for (DoseContribution c : doseContributions) {
            total += c.currentValue();
        }
        return total;
    }

    public boolean isEmpty() {
        return addictionValue <= 0.0001F
                && baseWithdrawalMeter <= 0.0001F
                && tolerance <= 0.0001F
                && relapseMemory <= 0.0001F
                && peakHistoricalAddiction <= 0.0001F
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