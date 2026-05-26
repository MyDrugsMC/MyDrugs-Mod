package org.mydrugs.mydrugs.client.effects.render;

import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.client.effects.AddictionClientState;

public final class BadTripSkyTint {
    private static final float TARGET_RED = 0.36F;
    private static final float TARGET_GREEN = 0.015F;
    private static final float TARGET_BLUE = 0.02F;
    private static final float MAX_STRENGTH = 0.92F;

    private static float displayedStrength;

    private BadTripSkyTint() {
    }

    public static void clear() {
        displayedStrength = 0.0F;
    }

    public static void tick() {
        float target = targetStrength();
        float speed = target > displayedStrength ? 0.06F : 0.12F;
        displayedStrength = Mth.lerp(speed, displayedStrength, target);
        if (displayedStrength < 0.002F && target < 0.002F) {
            displayedStrength = 0.0F;
        }
    }

    public static void applyFogColor(ViewportEvent.ComputeFogColor event) {
        if (displayedStrength <= 0.002F) {
            return;
        }

        event.setRed(Mth.lerp(displayedStrength, event.getRed(), TARGET_RED));
        event.setGreen(Mth.lerp(displayedStrength, event.getGreen(), TARGET_GREEN));
        event.setBlue(Mth.lerp(displayedStrength, event.getBlue(), TARGET_BLUE));
    }

    private static float targetStrength() {
        if (!AddictionClientState.badTripActive || !Config.CLIENT.psychedelicShadersEnabled()) {
            return 0.0F;
        }

        float severity = Mth.clamp(
                Math.max(AddictionClientState.badTripSeverity, AddictionClientState.badTripSymptomIntensity),
                0.0F,
                1.0F
        );
        float base = Mth.clamp(0.48F + severity * 0.44F, 0.0F, MAX_STRENGTH);
        float configScale = Config.CLIENT.shaderScale()
                * Config.CLIENT.visualScale()
                * Config.CLIENT.dimensionFogIntensity.get().floatValue();
        if (Config.CLIENT.reducedMotionMode.get()) {
            configScale *= 0.85F;
        }

        return Mth.clamp(base * configScale, 0.0F, MAX_STRENGTH);
    }
}
