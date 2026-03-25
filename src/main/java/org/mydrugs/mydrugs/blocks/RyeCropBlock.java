package org.mydrugs.mydrugs.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import org.mydrugs.mydrugs.items.ModItems;

public class RyeCropBlock extends CropBlock {
    public static final MapCodec<RyeCropBlock> CODEC = simpleCodec(RyeCropBlock::new);

    public RyeCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<RyeCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.RYE_SEEDS.get();
    }
}
