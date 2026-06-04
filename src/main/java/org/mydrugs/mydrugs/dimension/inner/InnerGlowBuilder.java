package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

/**
 * Places light so it reads as part of the terrain rather than player-dropped litter. Light is
 * chosen by the <em>context</em> of the column it lands on (see {@link Mode}):
 *
 * <ul>
 *   <li><b>ROCK / CLIFF / SCAR</b> — embed an emissive crystal/echo node <em>flush into the
 *       surface</em> ({@code topY}, never {@code topY+1}); reads as exposed mineralisation.</li>
 *   <li><b>GROVE / PLANT / WETLAND</b> — a glowing <em>plant</em> on the surface; plants never read
 *       as litter.</li>
 *   <li><b>LAKE</b> — make the lake <em>floor</em> emissive (a bioluminescent bed) at
 *       {@code lakeFloorY}; nothing is placed on the shore.</li>
 *   <li><b>AMBIENT</b> — open, flat, featureless dark ground gets an invisible {@link Blocks#LIGHT}
 *       fill a couple of blocks up (soft air-glow, fitting a dream/psyche dimension) or, if
 *       {@link #AMBIENT_LIGHT_FILL} is disabled, nothing at all. Never a visible cube.</li>
 * </ul>
 *
 * <p>Placement is deterministic and seeded exactly as before, and the same logic runs for both the
 * initial ({@link #placeInitialGlow}) and overlay ({@link #placeOverlayGlow}) passes, so generated
 * and overlaid chunks agree. The old freestanding-vanilla-cube mapping is gone.
 */
final class InnerGlowBuilder {
    /** When true, featureless dark ground gets a soft invisible {@link Blocks#LIGHT} fill. */
    static final boolean AMBIENT_LIGHT_FILL = true;
    /** Light level of the ambient fill block (0-15). Tunable. */
    static final int AMBIENT_LIGHT_LEVEL = 8;
    /** Height above the surface for the invisible ambient light source. */
    private static final int AMBIENT_LIGHT_HEIGHT = 2;

    private static final double ROCK_CLIFF_MIN = 0.42D;
    private static final double ROCK_SLOPE_MIN = 0.55D;
    private static final double GROVE_WET_MIN = 0.30D;
    private static final double LAKE_GLOW_MIN = 0.22D;

    private InnerGlowBuilder() {
    }

    enum Mode {
        LAKE_BED,
        EMBEDDED_ROCK,
        GLOW_PLANT,
        AMBIENT_FILL,
        NONE
    }

    static void placeInitialGlow(ChunkAccess chunk, InnerChunkSampleCache cache) {
        ChunkPos chunkPos = chunk.getPos();
        placeGlow(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), (pos, state) -> {
            if (pos.getY() < chunk.getMinY() || pos.getY() >= chunk.getMinY() + chunk.getHeight()) {
                return;
            }
            if (!chunkPos.equals(new ChunkPos(pos))) {
                return;
            }
            chunk.setBlockState(pos, state, 2);
        });
    }

    static void placeOverlayGlow(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeGlow(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(),
                (pos, state) -> InnerPlacement.safeSet(level, pos, state, true, count));
    }

    static boolean hasGlowLanguageFor(DrugId drugId) {
        return switch (drugId) {
            case COFFEE, TOBACCO, WEED, HASH, ALCOHOL, COCAINE, LSD, METH, MUSHROOMS -> true;
            default -> false;
        };
    }

    /**
     * Region-flavoured emissive block for set-piece accents (used by coast/lake/path builders).
     * Repurposed away from raw vanilla cubes to the region's own crystal/echo node so set-pieces
     * read as mineralisation rather than placed light blocks.
     */
    static BlockState glowStateFor(DrugId drugId) {
        return InnerTerrainProfile.forDrug(drugId).nodeState();
    }

    private static void placeGlow(InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
        int islandCenterX = InnerTerrain.slotCenter(minX + 8);
        int islandCenterZ = InnerTerrain.slotCenter(minZ + 8);
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        int placed = 0;
        for (int localZ = 1; localZ < 16 && placed < 7; localZ += 3) {
            for (int localX = 1; localX < 16 && placed < 7; localX += 3) {
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                if (!canHostGlow(sample)) {
                    continue;
                }
                InnerGroveSample grove = InnerGroveSampler.sample(seed, islandCenterX, islandCenterZ, worldX, worldZ, sample);
                InnerSceneSample scene = InnerSceneSampler.sample(seed, islandCenterX, islandCenterZ, worldX, worldZ, sample, grove);
                long hash = InnerNoise.mix64(seed
                        ^ (long) worldX * 0x1A36_5C89L
                        ^ (long) worldZ * 0x0F5E_2D49L);
                if (!shouldPlaceGlow(sample, scene, hash)) {
                    continue;
                }
                if (placeGlowAt(worldX, worldZ, sample, scene, hash, setter)) {
                    placed++;
                }
            }
        }
    }

    private static boolean canHostGlow(InnerTerrain.Sample sample) {
        return sample.land()
                && !sample.hole()
                && sample.distanceFromCenter() > InnerDimensionConstants.CORE_RADIUS + 18.0D;
    }

    private static boolean shouldPlaceGlow(InnerTerrain.Sample sample, InnerSceneSample scene, long hash) {
        double chance = 0.010D
                + scene.glowMultiplier() * 0.026D
                + sample.scarStrength() * 0.030D
                + sample.lakeCoreStrength() * 0.026D
                + (sample.transitionZone() ? sample.transitionStrength() * 0.018D : 0.0D);
        if (scene.type() == InnerSceneType.PATH_VISTA || scene.type() == InnerSceneType.LANDMARK_APPROACH) {
            chance += 0.050D;
        }
        if (scene.type() == InnerSceneType.QUIET_FIELD || scene.type() == InnerSceneType.ASH_FLAT) {
            chance *= 0.54D;
        }
        return (hash & 1023L) < chance * 1024.0D;
    }

    /** Pure, deterministic decision of <em>how</em> a column should be lit. Exposed for testing. */
    static Mode modeFor(InnerTerrain.Sample sample) {
        if (sample.lake() && sample.lakeCoreStrength() > LAKE_GLOW_MIN) {
            return Mode.LAKE_BED;
        }
        if (sample.scar()
                || sample.cliffStrength() > ROCK_CLIFF_MIN
                || sample.slopeHint() > ROCK_SLOPE_MIN) {
            return Mode.EMBEDDED_ROCK;
        }
        if (sample.treeZone()
                || sample.plantPatch()
                || sample.wetlandStrength() > GROVE_WET_MIN
                || sample.shoreStrength() > GROVE_WET_MIN) {
            return Mode.GLOW_PLANT;
        }
        return AMBIENT_LIGHT_FILL ? Mode.AMBIENT_FILL : Mode.NONE;
    }

    /** Y at which a given mode places its block, relative to the column's terrain. Pure. */
    static int glowYFor(Mode mode, InnerTerrain.Sample sample) {
        return switch (mode) {
            case LAKE_BED -> sample.lakeFloorY();
            case EMBEDDED_ROCK -> sample.topY();           // flush with the surface
            case GLOW_PLANT -> sample.topY() + 1;          // a plant sits on the surface
            case AMBIENT_FILL -> sample.topY() + AMBIENT_LIGHT_HEIGHT;
            case NONE -> sample.topY();
        };
    }

    /**
     * Whether a mode places a solid (full-cube) block. The invariant the whole rework enforces:
     * a solid cube is only ever placed flush ({@code topY}) or below — never proud at {@code topY+1}.
     */
    static boolean placesSolidCube(Mode mode) {
        return mode == Mode.LAKE_BED || mode == Mode.EMBEDDED_ROCK;
    }

    private static boolean placeGlowAt(
            int worldX,
            int worldZ,
            InnerTerrain.Sample sample,
            InnerSceneSample scene,
            long hash,
            BlockSetter setter
    ) {
        DrugId drugId = sample.chooseFeatureDrug(hash);
        Mode mode = modeFor(sample);
        int y = glowYFor(mode, sample);
        switch (mode) {
            case LAKE_BED -> {
                // Bioluminescent lake floor — emissive bed beneath the water, nothing on the shore.
                setter.set(new BlockPos(worldX, y, worldZ), lakeBedGlowFor(drugId));
                return true;
            }
            case EMBEDDED_ROCK -> {
                // Exposed mineralisation flush with the surface (topY), never proud of the ground.
                BlockState node = sample.scar()
                        ? ModInnerDimensionBlocks.REDLINE_CRYSTAL_NODE.get().defaultBlockState()
                        : glowStateFor(drugId);
                setter.set(new BlockPos(worldX, y, worldZ), node);
                return true;
            }
            case GLOW_PLANT -> {
                // Biological glow that sits on the surface as a plant (not litter).
                setter.set(new BlockPos(worldX, y, worldZ), glowPlantFor(sample, drugId));
                return true;
            }
            case AMBIENT_FILL -> {
                // Invisible soft fill: the air glows, fitting a dream dimension. No visible block.
                BlockState light = Blocks.LIGHT.defaultBlockState()
                        .setValue(LightBlock.LEVEL, AMBIENT_LIGHT_LEVEL);
                setter.set(new BlockPos(worldX, y, worldZ), light);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static BlockState lakeBedGlowFor(DrugId drugId) {
        return switch (drugId) {
            case MUSHROOMS, WEED, HASH -> ModInnerDimensionBlocks.MYCELIAL_INSIGHT_NODE.get().defaultBlockState();
            case LSD -> ModInnerDimensionBlocks.LUCID_ECHO_NODE.get().defaultBlockState();
            case COCAINE, METH, CRACK -> ModInnerDimensionBlocks.REDLINE_CRYSTAL_NODE.get().defaultBlockState();
            default -> ModInnerDimensionBlocks.DREAM_RESIDUE_GEODE.get().defaultBlockState();
        };
    }

    private static BlockState glowPlantFor(InnerTerrain.Sample sample, DrugId drugId) {
        boolean wet = sample.wetlandStrength() > GROVE_WET_MIN
                || sample.shoreStrength() > GROVE_WET_MIN;
        if (wet) {
            return drugId == DrugId.LSD
                    ? ModInnerDimensionBlocks.PRISM_LOTUS.get().defaultBlockState()
                    : ModInnerDimensionBlocks.MEMORY_LOTUS.get().defaultBlockState();
        }
        return switch (drugId) {
            case WEED, HASH, MUSHROOMS -> ModInnerDimensionBlocks.SPORE_BLOOM.get().defaultBlockState();
            case LSD -> ModInnerDimensionBlocks.PRISM_LOTUS.get().defaultBlockState();
            case COCAINE, METH, CRACK -> ModInnerDimensionBlocks.REDLINE_SPARK_BLOOM.get().defaultBlockState();
            default -> ModInnerDimensionBlocks.DREAM_ORCHID.get().defaultBlockState();
        };
    }

    private interface BlockSetter {
        void set(BlockPos pos, BlockState state);
    }
}
