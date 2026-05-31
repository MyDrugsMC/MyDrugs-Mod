package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

import java.util.Set;

final class InnerPlacement {
    private static final Set<Block> VANILLA_GENERATED_REPLACEABLE = Set.of(
            Blocks.AIR,
            Blocks.CAVE_AIR,
            Blocks.VOID_AIR,
            Blocks.GRASS_BLOCK,
            Blocks.DIRT,
            Blocks.DIRT_PATH,
            Blocks.ROOTED_DIRT,
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
            Blocks.LANTERN,
            Blocks.IRON_BARS,
            Blocks.BOOKSHELF
    );

    private InnerPlacement() {
    }

    static BlockPos surfaceTop(ServerLevel level, int x, int z) {
        InnerTerrain.Sample sample = InnerTerrain.sample(x, z);
        int surfaceY = sample.land() ? sample.topY() : InnerDimensionConstants.BASE_Y;
        return new BlockPos(x, Math.max(level.getMinY(), surfaceY), z);
    }

    static boolean safeSet(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            boolean allowTerrainReplace,
            MutablePlacementCount count
    ) {
        if (count.attempted++ >= InnerDimensionConstants.MAX_OVERLAY_BLOCKS_PER_CHUNK
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
        boolean replaceable = current.isAir() || current.canBeReplaced() || !current.getFluidState().isEmpty();
        if (!replaceable && (!allowTerrainReplace || !isGeneratedReplaceable(current.getBlock()))) {
            count.skipped++;
            return false;
        }
        level.setBlock(pos, state, InnerDimensionConstants.UPDATE_FLAGS);
        count.placed++;
        return true;
    }

    static void clearSpawnColumn(ServerLevel level, BlockPos feet, MutablePlacementCount count) {
        safeSet(level, feet, Blocks.AIR.defaultBlockState(), true, count);
        safeSet(level, feet.above(), Blocks.AIR.defaultBlockState(), true, count);
    }

    private static boolean requiresSupport(BlockState state) {
        return state.is(Blocks.CANDLE)
                || state.is(Blocks.REDSTONE_TORCH)
                || state.is(Blocks.LANTERN)
                || state.is(Blocks.SOUL_LANTERN)
                || state.is(Blocks.AMETHYST_CLUSTER)
                || state.is(ModInnerDimensionBlocks.BREATH_GRASS.get())
                || state.is(ModInnerDimensionBlocks.CALMING_FERN.get())
                || state.is(ModInnerDimensionBlocks.MEMORY_REEDS.get())
                || state.is(ModInnerDimensionBlocks.REDLINE_THORN.get())
                || state.is(ModInnerDimensionBlocks.MYCELIAL_ROOT.get());
    }

    private static boolean isGeneratedReplaceable(Block block) {
        return VANILLA_GENERATED_REPLACEABLE.contains(block)
                || block == ModInnerDimensionBlocks.LUCID_ECHO_NODE.get()
                || block == ModInnerDimensionBlocks.BITTER_ECHO_NODE.get()
                || block == ModInnerDimensionBlocks.CALMING_ECHO_NODE.get()
                || block == ModInnerDimensionBlocks.PRESSED_CALM_NODE.get()
                || block == ModInnerDimensionBlocks.FERMENTED_MEMORY_NODE.get()
                || block == ModInnerDimensionBlocks.REDLINE_CRYSTAL_NODE.get()
                || block == ModInnerDimensionBlocks.DREAM_RESIDUE_GEODE.get()
                || block == ModInnerDimensionBlocks.OVERDRIVE_SLAG.get()
                || block == ModInnerDimensionBlocks.MYCELIAL_INSIGHT_NODE.get()
                || block == ModInnerDimensionBlocks.BREATH_GRASS.get()
                || block == ModInnerDimensionBlocks.CALMING_FERN.get()
                || block == ModInnerDimensionBlocks.MEMORY_REEDS.get()
                || block == ModInnerDimensionBlocks.REDLINE_THORN.get()
                || block == ModInnerDimensionBlocks.MYCELIAL_ROOT.get();
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
    }

    record PlacementCount(int placed, int skipped) {
    }
}
