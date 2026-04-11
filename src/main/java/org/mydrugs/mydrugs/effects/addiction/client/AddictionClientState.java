package org.mydrugs.mydrugs.effects.addiction.client;

import org.mydrugs.mydrugs.effects.addiction.config.SymptomFlags;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionClientSnapshotPayload;

public final class AddictionClientState {
    public static float globalSeverity;
    public static float stressLevel;
    public static String dominantCategory = "CANNABINOID";
    public static int symptomFlags;
    public static int insomniaTicksRemaining;

    private AddictionClientState() {}

    public static void apply(AddictionClientSnapshotPayload payload) {
        globalSeverity = payload.globalSeverity();
        stressLevel = payload.stressLevel();
        dominantCategory = payload.dominantCategory();
        symptomFlags = payload.symptomFlags();
        insomniaTicksRemaining = payload.insomniaTicksRemaining();
    }

    public static boolean has(int flag) {
        return SymptomFlags.has(symptomFlags, flag);
    }

    public static void tick() {
        if (insomniaTicksRemaining > 0) {
            insomniaTicksRemaining--;
        }
    }
}