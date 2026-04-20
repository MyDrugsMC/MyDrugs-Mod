package org.mydrugs.mydrugs.blocks.crops;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

public class OpiumPoppyCropBlock extends CropBlock {
    public static final MapCodec<OpiumPoppyCropBlock> CODEC = simpleCodec(OpiumPoppyCropBlock::new);

    public OpiumPoppyCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<OpiumPoppyCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModCrops.OPIUM_POPPY_SEEDS.get();
    }
}
