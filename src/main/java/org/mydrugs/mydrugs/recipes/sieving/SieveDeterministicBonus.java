package org.mydrugs.mydrugs.recipes.sieving;

public final class SieveDeterministicBonus {
    private static final float EPSILON = 0.0001F;

    private SieveDeterministicBonus() {
    }

    public static Result apply(float currentAccumulator, float bonusChance) {
        if (!Float.isFinite(currentAccumulator)) {
            currentAccumulator = 0.0F;
        }
        if (!Float.isFinite(bonusChance) || bonusChance <= 0.0F) {
            return new Result(Math.max(0.0F, currentAccumulator), 0);
        }

        float next = Math.max(0.0F, currentAccumulator) + bonusChance;
        int count = 0;
        while (next + EPSILON >= 1.0F) {
            count++;
            next -= 1.0F;
        }
        if (next < EPSILON) {
            next = 0.0F;
        }
        return new Result(next, count);
    }

    public record Result(float accumulator, int bonusCount) {
    }
}
