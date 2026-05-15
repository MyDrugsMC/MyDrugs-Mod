package org.mydrugs.mydrugs.blocks.crops;

import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public record CropSpec<T extends CropBlock>(
        String cropId,
        String seedId,
        Function<BlockBehaviour.Properties, T> factory
) {
}
