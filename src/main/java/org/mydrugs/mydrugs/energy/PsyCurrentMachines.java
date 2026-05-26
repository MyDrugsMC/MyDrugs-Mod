package org.mydrugs.mydrugs.energy;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;

/** Helper for machines drawing Psy Current from their upgrade buffer each tick. */
public final class PsyCurrentMachines {
    private PsyCurrentMachines() {
    }

    public static boolean tryUseCurrentTick(BlockEntity blockEntity) {
        MachineEnergyAttachment attachment = MachineEnergyAttachments.get(blockEntity);
        if (!attachment.hasEnergyUpgrade()) {
            return false;
        }
        return tryUseCurrentTick(blockEntity, attachment);
    }

    public static boolean tryUseAutomationCurrentTick(BlockEntity blockEntity) {
        MachineEnergyAttachment attachment = MachineEnergyAttachments.get(blockEntity);
        if (!attachment.hasAutomationUpgrade()) {
            return false;
        }
        return tryUseCurrentTick(blockEntity, attachment);
    }

    private static boolean tryUseCurrentTick(BlockEntity blockEntity, MachineEnergyAttachment attachment) {
        int amount = PsyCurrentConstants.DEFAULT_MACHINE_CURRENT_PER_TICK;
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
