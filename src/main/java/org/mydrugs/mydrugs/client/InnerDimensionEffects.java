package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.mydrugs.mydrugs.dimension.inner.InnerAtmosphere;

/**
 * Client special effects for {@code mydrugs:inner}. Fog colour is derived from the shared
 * {@link InnerAtmosphereClient} cache, so paths, lakes, scars, transitions, and region scenes
 * shift the mood without duplicating the terrain-noise sampling.
 *
 * <p>Atmosphere sampling ownership lives in {@link InnerAtmosphereClient}; this class only
 * derives presentation values (colour Vec3, blend weight) and applies time-based easing so
 * region/scene boundaries crossfade the fog mood over a couple of seconds.
 */
public final class InnerDimensionEffects extends DimensionSpecialEffects {
    private static Vec3 cachedColor;
    private static double cachedBlend = 0.85D;

    // Time-eased presentation values so crossing a region/scene boundary shifts the fog mood
    // over a couple of seconds instead of snapping per block step. Render-thread only.
    private static Vec3 easedColor;
    private static double easedBlend = 0.85D;
    private static long lastEaseNanos;

    public InnerDimensionEffects() {
        // SkyType.NONE: the borrowed End sky is gone. The level clears to the mood fog colour
        // (the horizon band), and InnerSkyRenderer paints the gradient, constellations, and core
        // beacon on top in RenderLevelStageEvent.AfterSky so sky and fog read as one volume.
        super(SkyType.NONE, false, true);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        Vec3 mood = currentMoodColor(fogColor);
        // Blend the biome fog toward the region mood, then dim with brightness like the End does.
        Vec3 blended = fogColor.lerp(mood, easedBlend);
        return blended.scale(brightness);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    private static Vec3 currentMoodColor(Vec3 fallback) {
        InnerAtmosphere.Sample sample = InnerAtmosphereClient.current(Minecraft.getInstance());
        if (sample == null) {
            easedColor = null;
            lastEaseNanos = 0L;
            return fallback;
        }
        cachedColor = new Vec3(sample.fogRed() / 255.0D, sample.fogGreen() / 255.0D, sample.fogBlue() / 255.0D);
        cachedBlend = 0.68D + Math.min(0.27D, sample.fogDensity() * 0.27D + sample.glowBias() * 0.05D);
        return easeToward(cachedColor);
    }

    /**
     * Exponential time-based ease toward the target mood (frame-rate independent). Reduced motion
     * eases more slowly — boundaries become a gradual mood drift rather than a visible swing.
     */
    private static Vec3 easeToward(Vec3 target) {
        long now = System.nanoTime();
        if (easedColor == null || lastEaseNanos == 0L) {
            easedColor = target;
            easedBlend = cachedBlend;
            lastEaseNanos = now;
            return easedColor;
        }
        double dtSeconds = Math.min(0.25D, (now - lastEaseNanos) / 1_000_000_000.0D);
        lastEaseNanos = now;
        double tau = reducedMotion() ? 2.4D : 0.9D; // seconds to ~63% of the shift
        double t = 1.0D - Math.exp(-dtSeconds / tau);
        easedColor = easedColor.lerp(target, t);
        easedBlend = easedBlend + (cachedBlend - easedBlend) * t;
        return easedColor;
    }

    /** Resets all cached and eased fog state, safe to call outside the Inner Dimension. */
    public static void clear() {
        cachedColor = null;
        cachedBlend = 0.85D;
        easedColor = null;
        easedBlend = 0.85D;
        lastEaseNanos = 0L;
    }

    private static boolean reducedMotion() {
        try {
            return org.mydrugs.mydrugs.Config.CLIENT.reducedMotionMode.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }
}
