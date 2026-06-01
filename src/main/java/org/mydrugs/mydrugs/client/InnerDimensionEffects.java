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
        Vec3 blended = fogColor.lerp(mood, cachedBlend);
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
        return cachedColor;
    }
}
