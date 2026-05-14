package org.mydrugs.mydrugs.effects.addiction.client;

import net.minecraft.util.Mth;

public final class HeartbeatPulse {
    private static int ticksSinceBeat = 9999;

    private HeartbeatPulse() {
    }

    public static void triggerBeat() {
        ticksSinceBeat = 0;
    }

    public static void tick() {
        ticksSinceBeat++;
    }

    public static void clear() {
        ticksSinceBeat = 9999;
    }

    // 0 -> strong kick -> quickly decays
    public static float getEnvelope() {
        float t = ticksSinceBeat / 20.0F; // seconds
        if (t > 0.35F) return 0.0F;

        float attack = Mth.clamp(t / 0.03F, 0.0F, 1.0F);
        float decay = (float) Math.exp(-9.5F * t);

        // one strong squeeze with a tiny rebound feel
        float wave = 0.85F + 0.25F * (float) Math.sin(t * 22.0F);

        return Mth.clamp(attack * decay * wave, 0.0F, 1.0F);
    }

    public static float getFovOffset(float withdrawalStrength) {
        float s = Mth.clamp(withdrawalStrength, 0.0F, 1.0F);
        return getEnvelope() * Mth.lerp(s, 0.35F, 2.4F);
    }

    public static float getShaderPulse(float withdrawalStrength) {
        float s = Mth.clamp(withdrawalStrength, 0.0F, 1.0F);
        return getEnvelope() * Mth.lerp(s, 0.2F, 1.0F);
    }
}
