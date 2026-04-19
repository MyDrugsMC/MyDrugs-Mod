package org.mydrugs.mydrugs.effects.addiction.manager.state;

import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.util.AddictionMath;

public final class ResilienceManager {
    private ResilienceManager() {}

    public static void add(PlayerAddictionStats stats, float amount) {
        stats.resilience = AddictionMath.clamp(stats.resilience + amount, 0.0F, 0.50F);
    }

    public static void onTherapy(PlayerAddictionStats stats) {
        add(stats, 0.01F);
    }

    public static void onSuccessfulSleep(PlayerAddictionStats stats) {
        add(stats, 0.003F);
    }

    public static void onSafeZoneRecovery(PlayerAddictionStats stats) {
        add(stats, 0.001F);
    }
}