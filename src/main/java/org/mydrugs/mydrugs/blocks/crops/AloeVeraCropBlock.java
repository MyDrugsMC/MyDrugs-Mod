package org.mydrugs.mydrugs.blocks.crops;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.items.ModItems;

public class AloeVeraCropBlock extends CropBlock {
    public static final MapCodec<AloeVeraCropBlock> CODEC = simpleCodec(AloeVeraCropBlock::new);

    public AloeVeraCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<AloeVeraCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.PODZOL);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.ALOE_VERA.get();
    }
}
