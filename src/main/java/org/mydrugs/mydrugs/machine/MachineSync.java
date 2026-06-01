package org.mydrugs.mydrugs.machine;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;
import java.util.WeakHashMap;

public final class MachineSync {
    private static final Map<BlockEntity, Long> LAST_BLOCK_UPDATE_TICK = new WeakHashMap<>();

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
            recordBlockUpdate(be);
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
            recordBlockUpdate(be);
            be.getLevel().invalidateCapabilities(be.getBlockPos());
        }
    }

    public static void markChanged(BlockEntity be) {
        be.setChanged();
    }

    public static void syncIfDue(BlockEntity be, int intervalTicks) {
        be.setChanged();

        if (be.getLevel() == null || be.getLevel().isClientSide()) {
            return;
        }
        long now = be.getLevel().getGameTime();
        long previous = LAST_BLOCK_UPDATE_TICK.getOrDefault(be, Long.MIN_VALUE);
        if (now - previous < Math.max(1, intervalTicks)) {
            return;
        }
        LAST_BLOCK_UPDATE_TICK.put(be, now);
        be.getLevel().sendBlockUpdated(
                be.getBlockPos(),
                be.getBlockState(),
                be.getBlockState(),
                Block.UPDATE_CLIENTS
        );
    }

    private static void recordBlockUpdate(BlockEntity be) {
        if (be.getLevel() != null && !be.getLevel().isClientSide()) {
            LAST_BLOCK_UPDATE_TICK.put(be, be.getLevel().getGameTime());
        }
    }
}
