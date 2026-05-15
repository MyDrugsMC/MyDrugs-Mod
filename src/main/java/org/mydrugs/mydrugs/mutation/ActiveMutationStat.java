package org.mydrugs.mydrugs.mutation;

import org.mydrugs.mydrugs.items.data.MutationStatValue;

import java.util.List;

public final class ActiveMutationStat {
    private final String statId;
    private float currentValue;
    private float targetValue;
    private float assimilationProgress;
    private float assimilationDifficulty;
    private List<String> sourceUuids;
    private List<String> sourceNames;
    private boolean justCompleted;

    public ActiveMutationStat(
            String statId,
            float currentValue,
            float targetValue,
            float assimilationProgress,
            float assimilationDifficulty,
            List<String> sourceUuids,
            List<String> sourceNames
    ) {
        this.statId = statId;
        this.currentValue = clamp(currentValue);
        this.targetValue = clamp(targetValue);
        this.assimilationProgress = Math.clamp(assimilationProgress, 0.0F, 1.0F);
        this.assimilationDifficulty = Math.clamp(assimilationDifficulty, 0.20F, 1.50F);
        this.sourceUuids = List.copyOf(sourceUuids);
        this.sourceNames = List.copyOf(sourceNames);
    }

    public String statId() {
        return this.statId;
    }

    public float currentValue() {
        return this.currentValue;
    }

    public float targetValue() {
        return this.targetValue;
    }

    public float assimilationProgress() {
        return this.assimilationProgress;
    }

    public float assimilationDifficulty() {
        return this.assimilationDifficulty;
    }

    public List<String> sourceUuids() {
        return this.sourceUuids;
    }

    public List<String> sourceNames() {
        return this.sourceNames;
    }

    public MutationStatValue asCurrentValue() {
        return new MutationStatValue(this.statId, this.currentValue, this.assimilationProgress);
    }

    public void mergeTarget(float incomingValue, float incomingDifficulty, float geneticStability, List<String> incomingUuids, List<String> incomingNames) {
        float oldValue = this.targetValue;
        float difference = incomingValue - oldValue;
        float stability = Math.clamp(geneticStability, 0.0F, 1.0F);
        float averaged = oldValue + difference / 2.0F + Math.abs(difference) / 2.0F * stability;
        this.targetValue = roundToPercent(averaged);
        this.assimilationDifficulty = Math.clamp(Math.max(this.assimilationDifficulty, incomingDifficulty), 0.20F, 1.50F);
        this.sourceUuids = mergeStrings(this.sourceUuids, incomingUuids);
        this.sourceNames = mergeStrings(this.sourceNames, incomingNames);
    }

    public boolean tickAssimilation(float infectionSlowdown) {
        return tickAssimilation(infectionSlowdown, 1.0F);
    }

    public boolean tickAssimilation(float infectionSlowdown, float speedMultiplier) {
        if (this.currentValue >= this.targetValue) {
            this.currentValue = this.targetValue;
            this.assimilationProgress = 1.0F;
            return false;
        }

        float previous = this.currentValue;
        float boost = Math.max(0.10F, speedMultiplier);
        float speed = 0.0005F / Math.max(0.20F, this.assimilationDifficulty) * infectionSlowdown * boost;
        this.currentValue += (this.targetValue - this.currentValue) * speed;
        if (this.targetValue - this.currentValue < 0.0005F) {
            this.currentValue = this.targetValue;
        }
        this.assimilationProgress = this.targetValue <= 0.0F ? 1.0F : Math.clamp(this.currentValue / this.targetValue, 0.0F, 1.0F);
        boolean completedNow = previous < this.targetValue && this.currentValue >= this.targetValue;
        if (completedNow) {
            this.justCompleted = true;
        }
        return Math.abs(previous - this.currentValue) > 0.000001F;
    }

    public boolean consumeJustCompleted() {
        if (this.justCompleted) {
            this.justCompleted = false;
            return true;
        }
        return false;
    }

    public boolean decay(float amount) {
        float previousCurrent = this.currentValue;
        float previousTarget = this.targetValue;
        this.targetValue = Math.max(0.0F, this.targetValue - amount);
        this.currentValue = Math.min(this.currentValue, this.targetValue);
        this.assimilationProgress = this.targetValue <= 0.0F ? 0.0F : Math.clamp(this.currentValue / this.targetValue, 0.0F, 1.0F);
        return previousCurrent != this.currentValue || previousTarget != this.targetValue;
    }

    private static float clamp(float value) {
        return Math.clamp(value, 0.0F, 1.0F);
    }

    private static float roundToPercent(float value) {
        return clamp(Math.round(clamp(value) * 100.0F) / 100.0F);
    }

    private static List<String> mergeStrings(List<String> first, List<String> second) {
        java.util.LinkedHashSet<String> merged = new java.util.LinkedHashSet<>(first);
        merged.addAll(second);
        return List.copyOf(merged);
    }
}
