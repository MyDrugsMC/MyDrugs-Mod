package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

import java.util.Set;

final class InnerPlacement {
    /**
     * Controls update flags and replaceability rules during block placement.
     *
     * <ul>
     *   <li>{@link #LIVE_OVERLAY} — {@link Block#UPDATE_ALL} flags, safe for player-visible
     *       incremental overlays and entry setup.</li>
     *   <li>{@link #RECREATE} — {@link Block#UPDATE_CLIENTS} | {@link Block#UPDATE_KNOWN_SHAPE}
     *       | {@link Block#UPDATE_SUPPRESS_DROPS} flags, cheaper for bulk terrain/decoration
     *       rebuilds where every block in the column is being rewritten.</li>
     * </ul>
     */
    enum PlacementMode {
        LIVE_OVERLAY(Block.UPDATE_ALL),
        RECREATE(InnerDimensionConstants.RECREATE_UPDATE_FLAGS);

        final int updateFlags;

        PlacementMode(int updateFlags) {
            this.updateFlags = updateFlags;
        }
    }

    private static final class GeneratedReplaceableBlocks {
        private static final Set<Block> VANILLA = Set.of(
            Blocks.AIR,
            Blocks.CAVE_AIR,
            Blocks.VOID_AIR,
            Blocks.WATER,
            Blocks.LAVA,
            Blocks.GRASS_BLOCK,
            Blocks.DIRT,
            Blocks.DIRT_PATH,
            Blocks.ROOTED_DIRT,
            Blocks.CLAY,
            Blocks.MOSS_BLOCK,
            Blocks.MYCELIUM,
            Blocks.TUFF,
            Blocks.STONE,
            Blocks.STONE_BRICKS,
            Blocks.CRACKED_STONE_BRICKS,
            Blocks.MOSSY_COBBLESTONE,
            Blocks.CALCITE,
            Blocks.AMETHYST_BLOCK,
            Blocks.SMOOTH_BASALT,
            Blocks.DEEPSLATE,
            Blocks.DEEPSLATE_TILES,
            Blocks.CRACKED_DEEPSLATE_TILES,
            Blocks.MUD,
            Blocks.SMOOTH_QUARTZ,
            Blocks.QUARTZ_BLOCK,
            Blocks.WHITE_CONCRETE,
            Blocks.REDSTONE_BLOCK,
            Blocks.PRISMARINE,
            Blocks.TINTED_GLASS,
            Blocks.SCULK,
            Blocks.BLACKSTONE,
            Blocks.POLISHED_BLACKSTONE,
            Blocks.BASALT,
            Blocks.MAGMA_BLOCK,
            Blocks.MUSHROOM_STEM,
            Blocks.RED_MUSHROOM_BLOCK,
            Blocks.BROWN_MUSHROOM_BLOCK,
            Blocks.SMOOTH_STONE,
            Blocks.SEA_LANTERN,
            Blocks.GLOWSTONE,
            Blocks.SHROOMLIGHT,
            Blocks.LANTERN,
            Blocks.SOUL_LANTERN,
            Blocks.REDSTONE_TORCH,
            Blocks.OAK_LOG,
            Blocks.OAK_LEAVES,
            Blocks.DARK_OAK_LEAVES,
            Blocks.JUNGLE_LOG,
            Blocks.JUNGLE_LEAVES,
            Blocks.AZALEA_LEAVES,
            Blocks.DARK_OAK_LOG,
            Blocks.STRIPPED_DARK_OAK_LOG,
            Blocks.SPRUCE_LOG,
            Blocks.STRIPPED_SPRUCE_LOG,
            Blocks.IRON_BARS,
            Blocks.BOOKSHELF,
            Blocks.OBSIDIAN,
            Blocks.CRYING_OBSIDIAN,
            Blocks.LIGHTNING_ROD,
            Blocks.AMETHYST_CLUSTER
        );
    }

    private InnerPlacement() {
    }

    static BlockPos surfaceTop(ServerLevel level, int x, int z) {
        InnerTerrain.Sample sample = InnerTerrain.sample(x, z);
        int surfaceY = sample.land() ? sample.topY() : InnerDimensionConstants.BASE_Y;
        return new BlockPos(x, Math.max(level.getMinY(), surfaceY), z);
    }

    /**
     * Places a block using {@link PlacementMode#LIVE_OVERLAY} update flags.
     * Safe for player-visible incremental overlays and entry setup.
     */
    static boolean safeSet(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            boolean allowTerrainReplace,
            MutablePlacementCount count
    ) {
        return safeSet(level, pos, state, allowTerrainReplace, count, PlacementMode.LIVE_OVERLAY, true);
    }

    /**
     * Places a block using the given {@link PlacementMode} update flags.
     */
    static boolean safeSet(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            boolean allowTerrainReplace,
            MutablePlacementCount count,
            PlacementMode mode
    ) {
        return safeSet(level, pos, state, allowTerrainReplace, count, mode, true);
    }

    /**
     * Same replacement rules as {@link #safeSet}, but without charging the small per-chunk
     * overlay attempt budget. Use only for deterministic structures that intentionally span
     * multiple chunks, such as the center sanctuary disc. Otherwise, a large structure can be
     * clipped halfway through its own loop and appear as a semicircle.
     */
    static boolean safeSetStructural(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            boolean allowTerrainReplace,
            MutablePlacementCount count
    ) {
        return safeSet(level, pos, state, allowTerrainReplace, count, PlacementMode.LIVE_OVERLAY, false);
    }

    /**
     * Same as {@link #safeSetStructural} but with explicit {@link PlacementMode}.
     */
    static boolean safeSetStructural(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            boolean allowTerrainReplace,
            MutablePlacementCount count,
            PlacementMode mode
    ) {
        return safeSet(level, pos, state, allowTerrainReplace, count, mode, false);
    }

    private static boolean safeSet(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            boolean allowTerrainReplace,
            MutablePlacementCount count,
            PlacementMode mode,
            boolean enforceAttemptBudget
    ) {
        if ((enforceAttemptBudget && count.attempted++ >= InnerDimensionConstants.MAX_OVERLAY_BLOCKS_PER_CHUNK)
                || pos.getY() < level.getMinY()
                || pos.getY() >= level.getMaxY()) {
            count.skipped++;
            return false;
        }
        if (level.getBlockEntity(pos) != null) {
            count.skipped++;
            return false;
        }
        if (requiresSupport(state) && !state.canSurvive(level, pos)) {
            count.skipped++;
            return false;
        }
        BlockState current = level.getBlockState(pos);
        if (current.equals(state)) {
            return true;
        }
        if (!canReplaceCurrent(current, allowTerrainReplace)) {
            count.skipped++;
            return false;
        }
        level.setBlock(pos, state, mode.updateFlags);
        count.placed++;
        return true;
    }

    private static boolean canReplaceCurrent(BlockState current, boolean allowTerrainReplace) {
        return canReplaceCurrent(
                current.isAir(),
                current.canBeReplaced(),
                !current.getFluidState().isEmpty(),
                allowTerrainReplace,
                isGeneratedReplaceable(current.getBlock())
        );
    }

    private static boolean canReplaceCurrent(
            boolean air,
            boolean replaceable,
            boolean fluid,
            boolean allowTerrainReplace,
            boolean generatedReplaceable
    ) {
        return air || replaceable || fluid || allowTerrainReplace && generatedReplaceable;
    }

    static boolean canReplaceCurrentForTest(
            boolean air,
            boolean replaceable,
            boolean fluid,
            boolean allowTerrainReplace,
            boolean generatedReplaceable,
            PlacementMode mode
    ) {
        // PlacementMode affects update flags only; safety/replaceability rules are shared.
        return canReplaceCurrent(air, replaceable, fluid, allowTerrainReplace, generatedReplaceable);
    }

    static void clearSpawnColumn(ServerLevel level, BlockPos feet, MutablePlacementCount count) {
        clearSpawnColumn(level, feet, count, PlacementMode.LIVE_OVERLAY);
    }

    static void clearSpawnColumn(
            ServerLevel level,
            BlockPos feet,
            MutablePlacementCount count,
            PlacementMode mode
    ) {
        safeSet(level, feet, Blocks.AIR.defaultBlockState(), true, count, mode);
        safeSet(level, feet.above(), Blocks.AIR.defaultBlockState(), true, count, mode);
    }

    private static boolean requiresSupport(BlockState state) {
        return state.is(Blocks.CANDLE)
                || state.is(Blocks.REDSTONE_TORCH)
                || state.is(Blocks.LANTERN)
                || state.is(Blocks.SOUL_LANTERN)
                || state.is(Blocks.AMETHYST_CLUSTER)
                || ModInnerDimensionBlocks.isSymbolicPlantBlock(state.getBlock());
    }

    private static boolean isGeneratedReplaceable(Block block) {
        return GeneratedReplaceableBlocks.VANILLA.contains(block)
                || ModInnerDimensionBlocks.isGeneratedInnerBlock(block);
    }

    static final class MutablePlacementCount {
        private int attempted;
        private int placed;
        private int skipped;

        PlacementCount freeze() {
            return new PlacementCount(placed, skipped);
        }

        void recordPlaced() {
            placed++;
        }

        void recordSkipped() {
            skipped++;
        }
    }

    record PlacementCount(int placed, int skipped) {
    }
}
