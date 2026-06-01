package org.mydrugs.mydrugs.dimension.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SymbolicBushBlock extends SymbolicPlantBlock {
    public static final MapCodec<SymbolicBushBlock> CODEC = simpleCodec(SymbolicBushBlock::new);
    private static final VoxelShape SHAPE = box(2.0D, 0.0D, 2.0D, 14.0D, 11.0D, 14.0D);

    public SymbolicBushBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
