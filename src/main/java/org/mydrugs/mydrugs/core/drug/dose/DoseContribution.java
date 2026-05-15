package org.mydrugs.mydrugs.core.drug.dose;

/**
 * Represents a single drug consumption's contribution to the total dose.
 * The value linearly decreases from {@code amount} to 0 over {@code totalDuration} ticks,
 * producing a smooth fade-out instead of an abrupt drop.
 */
public final class DoseContribution {
    public final float amount;
    public int ticksRemaining;
    public final int totalDuration;

    public DoseContribution(float amount, int totalDuration) {
        this.amount = amount;
        this.ticksRemaining = totalDuration;
        this.totalDuration = totalDuration;
    }

    /** Constructor used during deserialization. */
    public DoseContribution(float amount, int ticksRemaining, int totalDuration) {
        this.amount = amount;
        this.ticksRemaining = ticksRemaining;
        this.totalDuration = totalDuration;
    }

    /**
     * Current dose value — linearly decreases from {@code amount} to 0
     * as {@code ticksRemaining} counts down to 0.
     */
    public float currentValue() {
        if (totalDuration <= 0) return 0f;
        return amount * (ticksRemaining / (float) totalDuration);
    }

    public boolean isExpired() {
        return ticksRemaining <= 0;
    }
}
