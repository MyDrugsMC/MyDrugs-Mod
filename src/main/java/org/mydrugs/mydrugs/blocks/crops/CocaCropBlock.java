package org.mydrugs.mydrugs.blocks.crops;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import org.mydrugs.mydrugs.items.ModItems;

public class CocaCropBlock extends CropBlock {
    public static final MapCodec<CocaCropBlock> CODEC = simpleCodec(CocaCropBlock::new);

    public CocaCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<CocaCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModCrops.COCA_SEEDS.get();
    }
}
