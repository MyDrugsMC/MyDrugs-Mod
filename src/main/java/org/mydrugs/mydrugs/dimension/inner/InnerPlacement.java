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
                || ModInnerDimensionBlocks.isSymbolicPlantBlock(state.getBlock());
    }

    private static boolean isGeneratedReplaceable(Block block) {
        return VANILLA_GENERATED_REPLACEABLE.contains(block)
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
