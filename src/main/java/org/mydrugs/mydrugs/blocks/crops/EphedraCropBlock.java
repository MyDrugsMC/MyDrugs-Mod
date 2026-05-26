package org.mydrugs.mydrugs.blocks.crops;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class EphedraCropBlock extends CropBlock {
    public static final MapCodec<EphedraCropBlock> CODEC = simpleCodec(EphedraCropBlock::new);
    private static final int SLOW_GROWTH_DIVISOR = 3;

    public EphedraCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<EphedraCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModCrops.EPHEDRA_CUTTINGS.get();
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(SLOW_GROWTH_DIVISOR) != 0) {
            return;
        }
        super.randomTick(state, level, pos, random);
    }
}
