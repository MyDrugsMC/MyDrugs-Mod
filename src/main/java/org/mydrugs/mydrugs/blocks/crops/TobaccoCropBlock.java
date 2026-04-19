package org.mydrugs.mydrugs.blocks.crops;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import org.mydrugs.mydrugs.items.ModItems;

public class TobaccoCropBlock extends CropBlock {
    public static final MapCodec<TobaccoCropBlock> CODEC = simpleCodec(TobaccoCropBlock::new);

    public TobaccoCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<TobaccoCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModCrops.TOBACCO_SEEDS.get();
    }
}
