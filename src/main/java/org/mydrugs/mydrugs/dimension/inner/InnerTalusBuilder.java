package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Scatters loose scree/talus at the foot of steep faces so cliffs read as eroding rather than
 * clean-cut (Section 4b). Unlike the glow rework — where surface cubes look like litter — these
 * <em>are</em> meant to be loose fallen rock, so a few region-palette stone blocks resting on the
 * surface at a cliff base is the intended look.
 *
 * <p>Per-column and deterministic: talus appears in the moderate-slope band that flanks steep faces
 * (the base/apron of a cliff), with density tied to {@code cliffStrength}.
 */
final class InnerTalusBuilder {
    private static final long TALUS_SALT = 0x54_414C_5553L;
    private static final double TALUS_MIN_CLIFF = 0.30D;
    private static final double TALUS_MAX_CLIFF = 0.58D;
    private static final int MARGIN = 0;

    private InnerTalusBuilder() {
    }

    static void place(InnerBlockSink sink, InnerChunkSampleCache cache, int minX, int minZ) {
        if (!cache.anyLand() || cache.maxCliffStrength() < TALUS_MIN_CLIFF) {
            return;
        }
        int centerX = InnerTerrain.slotCenter(minX + 8);
        int centerZ = InnerTerrain.slotCenter(minZ + 8);
        long seed = InnerTerrain.seedForSlot(centerX, centerZ);
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                if (!shouldPlaceTalus(sample, seed, worldX, worldZ)) {
                    continue;
                }
                // A loose rock resting on the surface — region deep/scar stone palette.
                BlockState rock = sample.scar() ? sample.profile().scarBlock() : sample.profile().deepBlock();
                sink.setBlock(new BlockPos(worldX, sample.topY() + 1, worldZ), rock, true);
            }
        }
    }

    /** Pure, deterministic talus predicate. Exposed for testing. */
    static boolean shouldPlaceTalus(InnerTerrain.Sample sample, long seed, int worldX, int worldZ) {
        if (!sample.land()
                || sample.lake()
                || sample.hole()
                || sample.path()
                || sample.distanceFromCenter() < InnerDimensionConstants.CORE_RADIUS + 40.0D) {
            return false;
        }
        double cliff = sample.cliffStrength();
        if (cliff < TALUS_MIN_CLIFF || cliff > TALUS_MAX_CLIFF) {
            return false;
        }
        long hash = InnerNoise.mix64(seed + TALUS_SALT
                ^ (long) worldX * 0x1F83_D9ABL
                ^ (long) worldZ * 0x2545_F491L);
        double chance = (cliff - TALUS_MIN_CLIFF) * 0.45D;
        return (hash & 1023L) < chance * 1024.0D;
    }
}
