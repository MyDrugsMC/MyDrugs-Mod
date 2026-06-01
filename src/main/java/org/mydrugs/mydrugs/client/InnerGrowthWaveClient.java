package org.mydrugs.mydrugs.client;

import org.mydrugs.mydrugs.client.shaders.InnerAtmosphereShader;
import org.mydrugs.mydrugs.sounds.ModSounds;

/**
 * Phase 8 client flourish: fired when {@link org.mydrugs.mydrugs.network.InnerGrowthWavePayload}
 * arrives because a drug was integrated while the player stands in the dimension. Pairs the
 * server-paced outward chunk reveal with a one-time post bloom (Phase 2) and a resonant sound swell
 * (Phase 5). The reveal itself, the new constellation, and the new region's fog/sky hue are handled
 * by the server pacing and the {@code InnerSkyStatePayload} that follows.
 */
public final class InnerGrowthWaveClient {
    private InnerGrowthWaveClient() {
    }

    public static void trigger(int drugNetworkId) {
        // One-time bloom that rides up and decays over ~0.8s.
        InnerAtmosphereShader.INSTANCE.addCrossingPulse(0.9F);
        // Resonant swell + a soft breath, scaled by the master ambient volume inside playOneShot.
        InnerSoundscapeController.playOneShot(ModSounds.INNER_LANDMARK_TONE.get(), 0.85F, 0.95F);
        InnerSoundscapeController.playOneShot(ModSounds.INNER_BREATH.get(), 0.6F, 1.05F);
    }
}
