package org.mydrugs.mydrugs.core.drug;

public final class DrugDurationScale {
    public static final int MINECRAFT_DAY_TICKS = 24000;

    private DrugDurationScale() {
    }

    public static int fromRealHours(float realHours, float referenceDayHours) {
        if (realHours <= 0.0F || referenceDayHours <= 0.0F) {
            return 0;
        }
        return Math.round((realHours / referenceDayHours) * MINECRAFT_DAY_TICKS);
    }

    public static int seconds(int seconds) {
        return Math.max(0, seconds * 20);
    }
}
