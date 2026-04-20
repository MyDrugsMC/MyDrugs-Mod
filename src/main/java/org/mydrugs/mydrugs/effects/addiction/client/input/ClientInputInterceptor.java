package org.mydrugs.mydrugs.effects.addiction.client.input;

import org.mydrugs.mydrugs.effects.addiction.client.AddictionClientState;
import org.mydrugs.mydrugs.effects.addiction.config.SymptomFlags;

public final class ClientInputInterceptor {
    private static final java.util.Random RANDOM = new java.util.Random();

    private ClientInputInterceptor() {
    }

    public static boolean shouldFailInput() {
        if (!AddictionClientState.has(SymptomFlags.CONFUSION)) return false;

        float severity = AddictionClientState.globalSeverity;
        float chance = 0.08F + severity * 0.12F;
        return RANDOM.nextFloat() < chance;
    }

    public static float getMovementMultiplier() {
        if (!AddictionClientState.has(SymptomFlags.FATIGUE)) return 1.0F;
        return 1.0F - Math.min(0.20F, AddictionClientState.globalSeverity * 0.20F);
    }
}