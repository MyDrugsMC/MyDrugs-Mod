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

        return tryUseEnergyTick(blockEntity, attachment);
    }

    public static boolean tryUseAutomationEnergyTick(BlockEntity blockEntity) {
        MachineEnergyAttachment attachment = MachineEnergyAttachments.get(blockEntity);
        if (!attachment.hasAutomationUpgrade()) {
            return false;
        }

        return tryUseEnergyTick(blockEntity, attachment);
    }

    private static boolean tryUseEnergyTick(BlockEntity blockEntity, MachineEnergyAttachment attachment) {
        int amount = PsychotropeEnergyConstants.DEFAULT_MACHINE_ENERGY_PER_TICK;
        if (attachment.storage().extract(amount, true) < amount) {
            return false;
        }

        attachment.storage().extract(amount, false);
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
