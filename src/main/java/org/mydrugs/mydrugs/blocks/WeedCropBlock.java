package org.mydrugs.mydrugs.blocks;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import org.mydrugs.mydrugs.items.ModItems;

public class WeedCropBlock extends CropBlock {
    public WeedCropBlock(Properties props) {
        super(props);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.WEED_SEEDS.get();
    }
}