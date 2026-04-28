package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class MachineTransferConfigProvider {
    private MachineTransferConfigProvider() {
    }

    @Nullable
    public static TransferUpgradeableBlockEntity get(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof TransferUpgradeableBlockEntity upgradeable ? upgradeable : null;
    }

    @Nullable
    public static MachineTransferAttachment getAttachment(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && MachineTransferAttachments.isSupported(blockEntity)
                ? MachineTransferAttachments.get(blockEntity)
                : null;
    }
}
