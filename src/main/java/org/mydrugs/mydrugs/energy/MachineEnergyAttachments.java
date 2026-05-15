package org.mydrugs.mydrugs.energy;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.mydrugs.mydrugs.blocks.entity.DistillerBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.FluidFiltererBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.SieveBlockEntity;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferAttachments;

public final class MachineEnergyAttachments {
    private MachineEnergyAttachments() {
    }

    public static MachineEnergyAttachment get(BlockEntity blockEntity) {
        return blockEntity.getData(ModAttachments.MACHINE_ENERGY.get());
    }

    public static boolean supportsEnergyUpgrade(BlockEntity blockEntity) {
        return MachineTransferAttachments.isSupported(blockEntity) && !supportsAutomationUpgrade(blockEntity);
    }

    public static boolean supportsAutomationUpgrade(BlockEntity blockEntity) {
        return blockEntity instanceof SieveBlockEntity
                || blockEntity instanceof DistillerBlockEntity
                || blockEntity instanceof FluidFiltererBlockEntity;
    }

    public static boolean hasEnergyStorage(BlockEntity blockEntity) {
        return get(blockEntity).hasAnyEnergyStorageUpgrade();
    }

    public static boolean installEnergyUpgrade(BlockEntity blockEntity) {
        if (!supportsEnergyUpgrade(blockEntity)) {
            return false;
        }
        boolean changed = get(blockEntity).installEnergyUpgrade();
        if (changed) {
            MachineSync.syncAndInvalidateCaps(blockEntity);
        }
        return changed;
    }

    public static boolean installAutomationUpgrade(BlockEntity blockEntity) {
        if (!supportsAutomationUpgrade(blockEntity)) {
            return false;
        }
        boolean changed = get(blockEntity).installAutomationUpgrade();
        if (changed) {
            MachineSync.syncAndInvalidateCaps(blockEntity);
        }
        return changed;
    }
}
