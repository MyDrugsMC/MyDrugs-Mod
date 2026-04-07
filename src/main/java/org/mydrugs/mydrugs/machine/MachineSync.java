package org.mydrugs.mydrugs.machine;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MachineSync {
    private MachineSync() {
    }

    public static void sync(BlockEntity be) {
        be.setChanged();

        if (be.getLevel() != null && !be.getLevel().isClientSide()) {
            be.getLevel().sendBlockUpdated(
                    be.getBlockPos(),
                    be.getBlockState(),
                    be.getBlockState(),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    public static void syncAndInvalidateCaps(BlockEntity be) {
        be.setChanged();

        if (be.getLevel() != null && !be.getLevel().isClientSide()) {
            be.getLevel().sendBlockUpdated(
                    be.getBlockPos(),
                    be.getBlockState(),
                    be.getBlockState(),
                    Block.UPDATE_CLIENTS
            );
            be.getLevel().invalidateCapabilities(be.getBlockPos());
        }
    }
}