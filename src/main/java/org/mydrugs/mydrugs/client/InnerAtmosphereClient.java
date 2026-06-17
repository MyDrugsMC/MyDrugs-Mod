package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.dimension.InnerDimensions;
import org.mydrugs.mydrugs.dimension.inner.InnerAtmosphere;
import org.mydrugs.mydrugs.dimension.inner.InnerTerrain;

/**
 * Single shared client-side cache of the {@link InnerAtmosphere.Sample} for the local player's
 * position. Sky (Phase 1), post-processing (Phase 2), region-crossing stings (Phase 3), particles
 * (Phase 4), and the soundscape (Phase 5) all read from here so the expensive terrain noise behind
 * {@link InnerAtmosphere#sample} runs at most once per changed block position rather than once per
 * consumer per frame.
 *
 * <p>Mirrors the per-block-position cache that {@link InnerDimensionEffects} already used for fog,
 * but hoisted so every effect shares one result. Always no-ops cleanly outside
 * {@link InnerDimensions#INNER_LEVEL}.
 */
public final class InnerAtmosphereClient {
    private static long cachedKey = Long.MIN_VALUE;
    @Nullable
    private static InnerAtmosphere.Sample cached;
    private static int cachedCenterX;
    private static int cachedCenterZ;

    private InnerAtmosphereClient() {
    }

    /** True only when the local player is inside the Inner Dimension with a live level. */
    public static boolean inInnerDimension(@Nullable Minecraft mc) {
        return mc != null && mc.level != null && mc.player != null
                && mc.level.dimension().equals(InnerDimensions.INNER_LEVEL);
    }

    /**
     * Returns the cached atmosphere sample for the player's current block position, recomputing only
     * when the player has moved to a new block. Returns {@code null} outside the Inner Dimension.
     */
    @Nullable
    public static InnerAtmosphere.Sample current(@Nullable Minecraft mc) {
        if (!inInnerDimension(mc)) {
            cached = null;
            cachedKey = Long.MIN_VALUE;
            return null;
        }
        Player player = mc.player;
        BlockPos pos = player.blockPosition();
        long key = pos.asLong();
        if (key != cachedKey || cached == null) {
            cachedCenterX = InnerTerrain.slotCenter(pos.getX());
            cachedCenterZ = InnerTerrain.slotCenter(pos.getZ());
            cached = InnerAtmosphere.sample(cachedCenterX, cachedCenterZ, pos);
            cachedKey = key;
        }
        return cached;
    }

    /** World X of the centre of the slot the player currently stands in (the sanctuary centre). */
    public static int slotCenterX() {
        return cachedCenterX;
    }

    /** World Z of the centre of the slot the player currently stands in (the sanctuary centre). */
    public static int slotCenterZ() {
        return cachedCenterZ;
    }

    public static void clear() {
        cached = null;
        cachedKey = Long.MIN_VALUE;
        cachedCenterX = 0;
        cachedCenterZ = 0;
    }
}
