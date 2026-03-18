package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class MagicMushroomBlock extends MushroomBlock {
    public MagicMushroomBlock(Properties props, ResourceKey<ConfiguredFeature<?, ?>> hugeFeature) {
        super(hugeFeature, props); // if your mappings show the params swapped, swap them
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return super.mayPlaceOn(state, level, pos)
                || state.is(ModBlocks.PSYCHEDELIC_MYCELIUM.get());
    }
}