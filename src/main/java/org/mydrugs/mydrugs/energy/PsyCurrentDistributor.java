package org.mydrugs.mydrugs.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class PsyCurrentDistributor {
    private PsyCurrentDistributor() {
    }

    public static List<BlockPos> rebuildTargets(ServerLevel level, BlockPos origin, int radius) {
        List<BlockPos> targets = new ArrayList<>();
        int r = Math.max(0, radius);
        BlockPos.betweenClosedStream(origin.offset(-r, -r, -r), origin.offset(r, r, r))
                .filter(pos -> !pos.equals(origin))
                .forEach(pos -> {
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity != null && MachineEnergyAttachments.hasEnergyStorage(blockEntity)) {
                        targets.add(blockEntity.getBlockPos().immutable());
                    }
                });
        return targets;
    }

    public static int totalReceivable(ServerLevel level, List<BlockPos> targets, PsyCurrentStorage internalStorage) {
        int total = internalStorage.capacity() - internalStorage.stored();
        for (BlockPos pos : targets) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null && MachineEnergyAttachments.hasEnergyStorage(blockEntity)) {
                total += MachineEnergyAttachments.get(blockEntity).storage().receive(Integer.MAX_VALUE, true);
            }
        }
        return total;
    }

    public static boolean distributeStored(ServerLevel level, PsyCurrentStorage storage, List<BlockPos> targets) {
        if (storage.stored() <= 0 || targets.isEmpty()) {
            return false;
        }

        int externalReceivable = totalExternalReceivable(level, targets);
        if (externalReceivable <= 0) {
            return false;
        }

        int amount = Math.min(storage.stored(), externalReceivable);
        int extracted = storage.extract(amount, false);
        int remainder = distributeToExternalTargets(level, extracted, targets);
        if (remainder > 0) {
            storage.receive(remainder, false);
        }
        return remainder != extracted;
    }

    public static int distribute(ServerLevel level, int amount, PsyCurrentStorage internalStorage, List<BlockPos> targets) {
        if (amount <= 0) {
            return 0;
        }

        int remaining = distributeToExternalTargets(level, amount, targets);
        if (remaining > 0) {
            remaining -= internalStorage.receive(remaining, false);
        }
        return remaining;
    }

    private static int totalExternalReceivable(ServerLevel level, List<BlockPos> targets) {
        int total = 0;
        for (BlockPos pos : targets) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null && MachineEnergyAttachments.hasEnergyStorage(blockEntity)) {
                total += MachineEnergyAttachments.get(blockEntity).storage().receive(Integer.MAX_VALUE, true);
            }
        }
        return total;
    }

    private static int distributeToExternalTargets(ServerLevel level, int amount, List<BlockPos> positions) {
        int remaining = amount;
        List<CurrentTarget> targets = new ArrayList<>();
        for (BlockPos pos : positions) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null && MachineEnergyAttachments.hasEnergyStorage(blockEntity)) {
                PsyCurrentStorage storage = MachineEnergyAttachments.get(blockEntity).storage();
                if (storage.receive(1, true) > 0) {
                    targets.add(new CurrentTarget(blockEntity, storage));
                }
            }
        }

        while (remaining > 0 && !targets.isEmpty()) {
            int share = Math.max(1, remaining / targets.size());
            int movedThisPass = 0;
            for (int i = targets.size() - 1; i >= 0 && remaining > 0; i--) {
                CurrentTarget target = targets.get(i);
                int moved = target.storage().receive(Math.min(share, remaining), false);
                remaining -= moved;
                movedThisPass += moved;
                if (moved > 0) {
                    syncTarget(target.blockEntity());
                }
                if (target.storage().receive(1, true) <= 0) {
                    targets.remove(i);
                }
            }
            if (movedThisPass == 0) {
                break;
            }
        }

        return remaining;
    }

    private static void syncTarget(BlockEntity blockEntity) {
        blockEntity.setChanged();
        if (blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide()) {
            BlockState state = blockEntity.getBlockState();
            blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
        }
    }

    private record CurrentTarget(BlockEntity blockEntity, PsyCurrentStorage storage) {
    }
}
