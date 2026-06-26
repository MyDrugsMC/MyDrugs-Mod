package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.blocks.entity.StillhouseBurnerBlockEntity;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachments;
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

    public static MachineTransferAccessLevel accessLevel(BlockEntity blockEntity) {
        if (!isSupported(blockEntity)) {
            return MachineTransferAccessLevel.NONE;
        }
        if (hasTransferUpgrade(blockEntity)) {
            return MachineTransferAccessLevel.CONFIGURABLE;
        }
        if (hasInherentFixedTransfer(blockEntity)) {
            return MachineTransferAccessLevel.FIXED_DEFAULTS;
        }
        if (MachineEnergyAttachments.supportsAutomationUpgrade(blockEntity)
                && MachineEnergyAttachments.get(blockEntity).hasAutomationUpgrade()) {
            return MachineTransferAccessLevel.FIXED_DEFAULTS;
        }
        return MachineTransferAccessLevel.NONE;
    }

    public static boolean hasFixedDefaultAccess(BlockEntity blockEntity) {
        return accessLevel(blockEntity) == MachineTransferAccessLevel.FIXED_DEFAULTS;
    }

    public static boolean hasAnyPipeAccess(BlockEntity blockEntity) {
        return accessLevel(blockEntity) != MachineTransferAccessLevel.NONE;
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

    public static boolean remove(BlockEntity blockEntity) {
        if (!isSupported(blockEntity)) {
            return false;
        }
        MachineTransferAttachment attachment = get(blockEntity);
        if (!attachment.installed()) {
            return false;
        }
        attachment.setInstalled(false);
        markChanged(blockEntity);
        return true;
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

        MachineTransferAccessLevel accessLevel = accessLevel(blockEntity);
        if (accessLevel == MachineTransferAccessLevel.NONE) {
            return false;
        }

        MachineLocalSide localSide = MachineOrientation.fromWorld(blockEntity.getBlockState(), worldSide);
        if (accessLevel == MachineTransferAccessLevel.FIXED_DEFAULTS) {
            return port.defaultLocalSides().contains(localSide) && port.supports(rule);
        }

        MachineTransferAttachment attachment = get(blockEntity);
        ensureDefaults(blockEntity, attachment);
        return attachment.config().getRule(port.id(), localSide) == rule;
    }

    private static boolean hasInherentFixedTransfer(BlockEntity blockEntity) {
        return blockEntity instanceof StillhouseBurnerBlockEntity;
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
