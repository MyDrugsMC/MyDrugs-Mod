package org.mydrugs.mydrugs.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class ThirdEyePetalBlock extends FlowerBlock {
    public static final MapCodec<ThirdEyePetalBlock> CODEC = simpleCodec(ThirdEyePetalBlock::new);

    public ThirdEyePetalBlock(Properties props) {
        super(MobEffects.NIGHT_VISION, 5.0F, props);
    }

    @Override
    public MapCodec<? extends FlowerBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.STONE)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.POWDER_SNOW);
    }
}
