package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.gas.GasStack;
import org.mydrugs.mydrugs.gas.IGasHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public final class MachineTransferResourceHandlers {
    private MachineTransferResourceHandlers() {
    }

    @Nullable
    public static ResourceHandler<ItemResource> itemContainer(BlockEntity blockEntity, Container container, @Nullable Direction side) {
        if (side == null || !MachineTransferAttachments.hasTransferUpgrade(blockEntity)) {
            return null;
        }

        SlotAccess access = accessFor(blockEntity, MachineTransferResourceKind.ITEM, side);
        if (access.isEmpty()) {
            return null;
        }

        return new ContainerItemResourceHandler(container, blockEntity, access.insertSlots(), access.extractSlots());
    }

    @Nullable
    public static <T extends Resource> ResourceHandler<T> restricted(
            BlockEntity blockEntity,
            MachineTransferResourceKind kind,
            @Nullable Direction side,
            @Nullable ResourceHandler<T> delegate
    ) {
        if (side == null || delegate == null || !MachineTransferAttachments.hasTransferUpgrade(blockEntity)) {
            return null;
        }

        SlotAccess access = accessFor(blockEntity, kind, side);
        if (access.isEmpty()) {
            return null;
        }

        return new FilteredResourceHandler<>(delegate, access.insertSlots(), access.extractSlots());
    }

    @Nullable
    public static IGasHandler restrictedGas(BlockEntity blockEntity, @Nullable Direction side, @Nullable IGasHandler delegate) {
        if (side == null || delegate == null || !MachineTransferAttachments.hasTransferUpgrade(blockEntity)) {
            return null;
        }

        SlotAccess access = accessFor(blockEntity, MachineTransferResourceKind.GAS, side);
        if (access.isEmpty()) {
            return null;
        }

        return new FilteredGasHandler(delegate, access.insertSlots(), access.extractSlots());
    }

    private static SlotAccess accessFor(BlockEntity blockEntity, MachineTransferResourceKind kind, Direction worldSide) {
        MachineTransferConfig config = MachineTransferAttachments.config(blockEntity);
        MachineLocalSide localSide = MachineOrientation.fromWorld(blockEntity.getBlockState(), worldSide);
        Set<Integer> insertSlots = new HashSet<>();
        Set<Integer> extractSlots = new HashSet<>();

        for (MachineTransferPortSpec port : MachineTransferAttachments.ports(blockEntity)) {
            if (port.kind() != kind) {
                continue;
            }

            MachineTransferSideRule rule = config.getRule(port.id(), localSide);
            if (!port.supports(rule)) {
                continue;
            }

            Set<Integer> targets = targetSlots(port);
            if (rule == MachineTransferSideRule.INPUT) {
                insertSlots.addAll(targets);
            } else if (rule == MachineTransferSideRule.OUTPUT) {
                extractSlots.addAll(targets);
            }
        }

        return new SlotAccess(insertSlots, extractSlots);
    }

    private static Set<Integer> targetSlots(MachineTransferPortSpec port) {
        return switch (port.kind()) {
            case ITEM -> Set.copyOf(port.itemSlots());
            case FLUID -> Set.copyOf(port.fluidTanks());
            case GAS -> Set.copyOf(port.gasTanks());
        };
    }

    private record SlotAccess(Set<Integer> insertSlots, Set<Integer> extractSlots) {
        boolean isEmpty() {
            return this.insertSlots.isEmpty() && this.extractSlots.isEmpty();
        }
    }

    private record FilteredResourceHandler<T extends Resource>(
            ResourceHandler<T> delegate,
            Set<Integer> insertSlots,
            Set<Integer> extractSlots
    ) implements ResourceHandler<T> {
        private FilteredResourceHandler {
            insertSlots = Set.copyOf(insertSlots);
            extractSlots = Set.copyOf(extractSlots);
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public T getResource(int slot) {
            return this.delegate.getResource(slot);
        }

        @Override
        public long getAmountAsLong(int slot) {
            return this.delegate.getAmountAsLong(slot);
        }

        @Override
        public long getCapacityAsLong(int slot, T resource) {
            return this.insertSlots.contains(slot) || this.extractSlots.contains(slot)
                    ? this.delegate.getCapacityAsLong(slot, resource)
                    : 0;
        }

        @Override
        public boolean isValid(int slot, T resource) {
            return this.insertSlots.contains(slot) && this.delegate.isValid(slot, resource);
        }

        @Override
        public int insert(int slot, T resource, int amount, TransactionContext transaction) {
            return this.insertSlots.contains(slot) ? this.delegate.insert(slot, resource, amount, transaction) : 0;
        }

        @Override
        public int extract(int slot, T resource, int amount, TransactionContext transaction) {
            return this.extractSlots.contains(slot) ? this.delegate.extract(slot, resource, amount, transaction) : 0;
        }
    }

    private record FilteredGasHandler(IGasHandler delegate, Set<Integer> fillTanks, Set<Integer> drainTanks) implements IGasHandler {
        private FilteredGasHandler {
            fillTanks = Set.copyOf(fillTanks);
            drainTanks = Set.copyOf(drainTanks);
        }

        @Override
        public int getTanks() {
            return this.delegate.getTanks();
        }

        @Override
        public GasStack getGasInTank(int tank) {
            return this.fillTanks.contains(tank) || this.drainTanks.contains(tank)
                    ? this.delegate.getGasInTank(tank)
                    : GasStack.EMPTY;
        }

        @Override
        public long getTankCapacity(int tank) {
            return this.fillTanks.contains(tank) || this.drainTanks.contains(tank) ? this.delegate.getTankCapacity(tank) : 0;
        }

        @Override
        public boolean isGasValid(int tank, GasStack stack) {
            return this.fillTanks.contains(tank) && this.delegate.isGasValid(tank, stack);
        }

        @Override
        public long fill(GasStack stack, boolean simulate) {
            if (stack == null || stack.isEmpty()) {
                return 0;
            }

            long remaining = stack.amount();
            for (int tank : new TreeSet<>(this.fillTanks)) {
                if (remaining <= 0) {
                    break;
                }

                long filled = this.delegate.fill(tank, stack.withAmount(remaining), simulate);
                remaining -= filled;
            }
            return stack.amount() - remaining;
        }

        @Override
        public GasStack drain(long amount, boolean simulate) {
            if (amount <= 0) {
                return GasStack.EMPTY;
            }

            for (int tank : new TreeSet<>(this.drainTanks)) {
                GasStack drained = this.delegate.drain(tank, amount, simulate);
                if (!drained.isEmpty()) {
                    return drained;
                }
            }
            return GasStack.EMPTY;
        }

        @Override
        public long fill(int tank, GasStack stack, boolean simulate) {
            return this.fillTanks.contains(tank) ? this.delegate.fill(tank, stack, simulate) : 0;
        }

        @Override
        public GasStack drain(int tank, long amount, boolean simulate) {
            return this.drainTanks.contains(tank) ? this.delegate.drain(tank, amount, simulate) : GasStack.EMPTY;
        }
    }
}
