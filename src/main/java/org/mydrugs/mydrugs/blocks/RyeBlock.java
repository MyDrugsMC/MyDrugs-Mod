package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class RyeBlock extends BushBlock {
    public RyeBlock(Properties props) {
        super(props);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT)
                || state.is(ModBlocks.PSYCHEDELIC_GRASS_BLOCK.get());
    }
}