package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.blocks.entity.GasTankBlockEntity;
import org.mydrugs.mydrugs.gas.GasTankContents;
import org.mydrugs.mydrugs.gas.GasType;
import org.mydrugs.mydrugs.gas.ModGases;
import org.mydrugs.mydrugs.registry.ModDataComponents;

import javax.annotation.Nullable;

public class GasTankBlock extends Block implements EntityBlock {
    public GasTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GasTankBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide()) return;

        if (level.getBlockEntity(pos) instanceof GasTankBlockEntity tankBe) {
            GasTankContents contents = stack.getOrDefault(
                    ModDataComponents.GAS_TANK_CONTENTS.get(),
                    GasTankContents.EMPTY
            );

            GasType gas = ModGases.getNullable(contents.gasId());
            tankBe.getTank().loadStored(gas, contents.amount());
            tankBe.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}