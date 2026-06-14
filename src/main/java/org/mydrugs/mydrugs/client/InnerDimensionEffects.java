package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.mydrugs.mydrugs.dimension.InnerDimensions;
import org.mydrugs.mydrugs.dimension.inner.InnerAtmosphere;
import org.mydrugs.mydrugs.dimension.inner.InnerTerrain;

/**
 * Client special effects for {@code mydrugs:inner}. Fog colour is sampled from
 * {@link InnerAtmosphere}, so paths, lakes, scars, transitions, and region scenes shift the mood.
 *
 * <p>The atmosphere sample runs terrain noise, so the result is cached per block position
 * on the (single) render thread to avoid recomputing every frame.
 */
public final class InnerDimensionEffects extends DimensionSpecialEffects {
    private static long cachedKey = Long.MIN_VALUE;
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
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null || !mc.level.dimension().equals(InnerDimensions.INNER_LEVEL)) {
            easedColor = null;
            lastEaseNanos = 0L;
            return fallback;
        }
        BlockPos pos = player.blockPosition();
        long key = pos.asLong();
        if (key != cachedKey || cachedColor == null) {
            int centerX = InnerTerrain.slotCenter(pos.getX());
            int centerZ = InnerTerrain.slotCenter(pos.getZ());
            InnerAtmosphere.Sample sample = InnerAtmosphere.sample(centerX, centerZ, pos);
            cachedColor = new Vec3(sample.fogRed() / 255.0D, sample.fogGreen() / 255.0D, sample.fogBlue() / 255.0D);
            cachedBlend = 0.68D + Math.min(0.27D, sample.fogDensity() * 0.27D + sample.glowBias() * 0.05D);
            cachedKey = key;
        }
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

    private static boolean reducedMotion() {
        try {
            return org.mydrugs.mydrugs.Config.CLIENT.reducedMotionMode.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }
}
