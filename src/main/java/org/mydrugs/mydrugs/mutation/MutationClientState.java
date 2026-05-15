package org.mydrugs.mydrugs.mutation;

import java.util.EnumMap;
import java.util.Map;

public final class MutationClientState {
    private static final Map<MutationStat, Float> VALUES = new EnumMap<>(MutationStat.class);

    private MutationClientState() {
    }

    public static void clear() {
        VALUES.clear();
    }

    public static void apply(Map<MutationStat, Float> incoming) {
        VALUES.clear();
        if (incoming == null || incoming.isEmpty()) {
            return;
        }
        for (Map.Entry<MutationStat, Float> entry : incoming.entrySet()) {
            float clamped = Math.clamp(entry.getValue(), 0.0F, 1.0F);
            if (clamped > 0.0F) {
                VALUES.put(entry.getKey(), clamped);
            }
        }
    }

    public static float get(MutationStat stat) {
        if (stat == null) {
            return 0.0F;
        }
        Float value = VALUES.get(stat);
        return value == null ? 0.0F : value;
    }

    public static float scaleNegative(MutationStat stat, float value) {
        float v = get(stat);
        return value * Math.max(0.0F, 1.0F - v);
    }

    public static float boostPositive(MutationStat stat, float value, float boostScale) {
        float v = get(stat);
        return value * (1.0F + v * boostScale);
    }
}
