package org.mydrugs.mydrugs.dimension.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SymbolicPlantBlock extends VegetationBlock {
    public static final MapCodec<SymbolicPlantBlock> CODEC = simpleCodec(SymbolicPlantBlock::new);

    private static final VoxelShape SHAPE = Block.column(10.0D, 0.0D, 13.0D);

    public SymbolicPlantBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.MUD)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.TUFF)
                || state.is(Blocks.STONE_BRICKS)
                || state.is(Blocks.CRACKED_STONE_BRICKS)
                || state.is(Blocks.MOSSY_COBBLESTONE)
                || state.is(Blocks.CALCITE)
                || state.is(Blocks.AMETHYST_BLOCK)
                || state.is(Blocks.SMOOTH_QUARTZ)
                || state.is(Blocks.QUARTZ_BLOCK)
                || state.is(Blocks.WHITE_CONCRETE)
                || state.is(Blocks.REDSTONE_BLOCK)
                || state.is(Blocks.PRISMARINE)
                || state.is(Blocks.SEA_LANTERN)
                || state.is(Blocks.SCULK)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.DEEPSLATE_TILES)
                || state.is(Blocks.CRACKED_DEEPSLATE_TILES)
                || state.is(Blocks.BLACKSTONE)
                || state.is(Blocks.POLISHED_BLACKSTONE)
                || state.is(Blocks.BASALT)
                || state.is(Blocks.SMOOTH_BASALT)
                || state.is(Blocks.SMOOTH_STONE)
                || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.MUSHROOM_STEM);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
