package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.blocks.entity.GasTankBlockEntity;

public class GasTankBlock extends Block implements EntityBlock {
    public GasTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GasTankBlockEntity(pos, state);
    }
}