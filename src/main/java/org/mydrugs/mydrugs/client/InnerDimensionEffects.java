package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.mydrugs.mydrugs.dimension.inner.InnerAtmosphere;
import org.mydrugs.mydrugs.dimension.inner.InnerTerrain;

/**
 * B3: client special effects for {@code mydrugs:inner}. Drives the fog colour from
 * {@link InnerAtmosphere}, sampled at the local player's position, so crossing between drug
 * regions visibly shifts the mood. End-like sky (matching the dimension's previous effects).
 *
 * <p>The atmosphere sample runs warped terrain noise, so the result is cached per block position
 * on the (single) render thread to avoid recomputing every frame.
 */
public final class InnerDimensionEffects extends DimensionSpecialEffects {
    private static long cachedKey = Long.MIN_VALUE;
    private static Vec3 cachedColor;

    public InnerDimensionEffects() {
        super(SkyType.END, false, true);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        Vec3 mood = currentMoodColor(fogColor);
        // Blend the biome fog toward the region mood, then dim with brightness like the End does.
        Vec3 blended = fogColor.lerp(mood, 0.85D);
        return blended.scale(brightness);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    private static Vec3 currentMoodColor(Vec3 fallback) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return fallback;
        }
        BlockPos pos = player.blockPosition();
        long key = pos.asLong();
        if (key != cachedKey || cachedColor == null) {
            int centerX = InnerTerrain.slotCenter(pos.getX());
            int centerZ = InnerTerrain.slotCenter(pos.getZ());
            InnerAtmosphere.Sample sample = InnerAtmosphere.sample(centerX, centerZ, pos);
            cachedColor = new Vec3(sample.fogRed() / 255.0D, sample.fogGreen() / 255.0D, sample.fogBlue() / 255.0D);
            cachedKey = key;
        }
        return cachedColor;
    }
}
