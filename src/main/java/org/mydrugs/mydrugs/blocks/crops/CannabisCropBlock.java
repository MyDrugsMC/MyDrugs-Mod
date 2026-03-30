package org.mydrugs.mydrugs.blocks.crops;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import org.mydrugs.mydrugs.items.ModItems;

public class CannabisCropBlock extends CropBlock {
    public CannabisCropBlock(Properties props) {
        super(props);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.CANNABIS_SEEDS.get();
    }
}