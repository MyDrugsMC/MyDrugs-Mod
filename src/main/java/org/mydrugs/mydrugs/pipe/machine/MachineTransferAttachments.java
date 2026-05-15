package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.pipe.blockentity.PipeBlockEntity;
import org.mydrugs.mydrugs.pipe.network.PipeNetworkDirtyReason;
import org.mydrugs.mydrugs.pipe.network.PipeNetworkManager;

import java.util.List;

public final class MachineTransferAttachments {
    private MachineTransferAttachments() {
    }

    public static MachineTransferAttachment get(BlockEntity blockEntity) {
        return blockEntity.getData(ModAttachments.MACHINE_TRANSFER.get());
    }

    public static boolean isSupported(BlockEntity blockEntity) {
        return !spec(blockEntity).ports().isEmpty();
    }

    public static boolean hasTransferUpgrade(BlockEntity blockEntity) {
        return isSupported(blockEntity) && get(blockEntity).installed();
    }

    public static boolean install(BlockEntity blockEntity) {
        if (!isSupported(blockEntity)) {
            return false;
        }

        MachineTransferAttachment attachment = get(blockEntity);
        boolean changed = !attachment.installed();
        attachment.setInstalled(true);
        changed |= ensureDefaults(blockEntity, attachment);
        markChanged(blockEntity);
        return changed;
    }

    public static MachineTransferConfig config(BlockEntity blockEntity) {
        MachineTransferAttachment attachment = get(blockEntity);
        if (ensureDefaults(blockEntity, attachment)) {
            markChanged(blockEntity);
        }
        return attachment.config();
    }

    public static MachineTransferSpec spec(BlockEntity blockEntity) {
        return MachineTransferSpecs.get(blockEntity);
    }

    public static List<MachineTransferPortSpec> ports(BlockEntity blockEntity) {
        return spec(blockEntity).ports();
    }

    public static boolean allows(BlockEntity blockEntity, MachineTransferPortSpec port, Direction worldSide, MachineTransferSideRule rule) {
        if (!isSupported(blockEntity)) {
            return false;
        }

        MachineTransferAttachment attachment = get(blockEntity);
        if (!attachment.installed()) {
            return false;
        }

        ensureDefaults(blockEntity, attachment);
        MachineLocalSide localSide = MachineOrientation.fromWorld(blockEntity.getBlockState(), worldSide);
        return attachment.config().getRule(port.id(), localSide) == rule;
    }

    private static void seedDefaults(BlockEntity blockEntity, MachineTransferConfig config) {
        config.seedDefaults(spec(blockEntity));
    }

    private static boolean ensureDefaults(BlockEntity blockEntity, MachineTransferAttachment attachment) {
        boolean changed = false;
        if (!attachment.defaultsSeeded()) {
            seedDefaults(blockEntity, attachment.config());
            attachment.setDefaultsSeeded(true);
            changed = true;
        }
        return attachment.config().sanitizeAgainst(spec(blockEntity)) || changed;
    }

    public static void markCapabilityChanged(BlockEntity blockEntity) {
        MachineSync.syncAndInvalidateCaps(blockEntity);
        markAdjacentPipeNetworksDirty(blockEntity);
    }

    private static void markChanged(BlockEntity blockEntity) {
        markCapabilityChanged(blockEntity);
    }

    private static void markAdjacentPipeNetworksDirty(BlockEntity blockEntity) {
        if (blockEntity.getLevel() == null || blockEntity.getLevel().isClientSide()) {
            return;
        }

        BlockPos pos = blockEntity.getBlockPos();
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (blockEntity.getLevel().getBlockEntity(neighborPos) instanceof PipeBlockEntity pipe) {
                PipeNetworkManager.markDirty(
                        blockEntity.getLevel(),
                        neighborPos,
                        pipe.kind(),
                        PipeNetworkDirtyReason.CAPABILITY_INVALIDATED
                );
            }
        }
    }
}
