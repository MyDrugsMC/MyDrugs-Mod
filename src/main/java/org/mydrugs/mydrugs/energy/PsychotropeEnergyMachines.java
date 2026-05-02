package org.mydrugs.mydrugs.energy;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;

public final class PsychotropeEnergyMachines {
    private PsychotropeEnergyMachines() {
    }

    public static boolean tryUseEnergyTick(BlockEntity blockEntity) {
        MachineEnergyAttachment attachment = MachineEnergyAttachments.get(blockEntity);
        if (!attachment.hasEnergyUpgrade()) {
            return false;
        }

        if (attachment.storage().extract(1, true) <= 0) {
            return false;
        }

        attachment.storage().extract(1, false);
        AdvancementEventHooks.psychotropePoweredMachine(blockEntity);
        sync(blockEntity);
        return true;
    }

    public static void sync(BlockEntity blockEntity) {
        blockEntity.setChanged();
        if (blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide()) {
            BlockState state = blockEntity.getBlockState();
            blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
        }
    }
}
