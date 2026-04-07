package org.mydrugs.mydrugs.machine.transfer;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.mydrugs.mydrugs.machine.fluid.FluidTankAccess;

public final class FluidTransferUtil {
    private FluidTransferUtil() {
    }

    public static boolean tryProcessTransferSlot(
            Container container,
            int itemSlot,
            FluidTankAccess tank,
            LockedTransferSlots locks,
            int lockIndex
    ) {
        ItemStack stack = container.getItem(itemSlot);
        ItemAccess access = ItemAccess
                .forHandlerIndexStrict(VanillaContainerWrapper.of(container), itemSlot)
                .oneByOne();

        return tryProcessTransferSlot(stack, access, tank, locks, lockIndex);
    }

    public static boolean tryProcessTransferSlot(
            ItemStacksResourceHandler itemHandler,
            NonNullList<ItemStack> stacks,
            int itemSlot,
            FluidTankAccess tank,
            LockedTransferSlots locks,
            int lockIndex
    ) {
        ItemStack stack = stacks.get(itemSlot);
        ItemAccess access = ItemAccess
                .forHandlerIndexStrict(itemHandler, itemSlot)
                .oneByOne();

        return tryProcessTransferSlot(stack, access, tank, locks, lockIndex);
    }

    public static boolean tryFillOutputSlot(
            Container container,
            int itemSlot,
            FluidTankAccess tank
    ) {
        ItemStack stack = container.getItem(itemSlot);
        ItemAccess access = ItemAccess
                .forHandlerIndexStrict(VanillaContainerWrapper.of(container), itemSlot)
                .oneByOne();

        return tryFillContainerFromTank(stack, access, tank);
    }

    public static boolean tryFillOutputSlot(
            ItemStacksResourceHandler itemHandler,
            NonNullList<ItemStack> stacks,
            int itemSlot,
            FluidTankAccess tank
    ) {
        ItemStack stack = stacks.get(itemSlot);
        ItemAccess access = ItemAccess
                .forHandlerIndexStrict(itemHandler, itemSlot)
                .oneByOne();

        return tryFillContainerFromTank(stack, access, tank);
    }

    private static boolean tryProcessTransferSlot(
            ItemStack stack,
            ItemAccess access,
            FluidTankAccess tank,
            LockedTransferSlots locks,
            int lockIndex
    ) {
        if (stack.isEmpty()) {
            locks.reset(lockIndex);
            return false;
        }

        ResourceHandler<FluidResource> handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) {
            locks.reset(lockIndex);
            return false;
        }

        TransferMode mode = locks.get(lockIndex);
        if (mode == TransferMode.NONE) {
            mode = resolveMode(stack, handler, tank);
            if (mode == TransferMode.NONE) {
                return false;
            }
            locks.set(lockIndex, mode);
        }

        return switch (mode) {
            case DRAIN -> tryDrainContainerToTank(stack, handler, tank);
            case FILL -> tryFillContainerFromTank(stack, handler, tank);
            case NONE -> false;
        };
    }

    private static TransferMode resolveMode(
            ItemStack stack,
            ResourceHandler<FluidResource> handler,
            FluidTankAccess tank
    ) {
        FluidResource itemResource = handler.getResource(0);
        int itemAmount = handler.getAmountAsInt(0);

        if (!itemResource.isEmpty() && itemAmount > 0) {
            FluidStack incoming = itemResource.toStack(itemAmount);
            if (tank.getAddableAmount(incoming) > 0) {
                return TransferMode.DRAIN;
            }
        }

        if (getFillRequest(stack, handler, tank.getFluid()) > 0) {
            return TransferMode.FILL;
        }

        return TransferMode.NONE;
    }

    private static boolean tryDrainContainerToTank(
            ItemStack stack,
            ResourceHandler<FluidResource> handler,
            FluidTankAccess tank
    ) {
        FluidResource resource = handler.getResource(0);
        int containedAmount = handler.getAmountAsInt(0);

        if (resource.isEmpty() || containedAmount <= 0) {
            return false;
        }

        FluidStack incoming = resource.toStack(containedAmount);
        int request = getDrainRequest(stack, incoming, tank, containedAmount);
        if (request <= 0) {
            return false;
        }

        try (Transaction tx = Transaction.openRoot()) {
            int extracted = handler.extract(resource, request, tx);
            if (extracted != request) {
                return false;
            }

            tx.commit();
            return tank.insert(incoming.copyWithAmount(extracted), false) == extracted;
        }
    }

    private static boolean tryFillContainerFromTank(
            ItemStack stack,
            ItemAccess access,
            FluidTankAccess tank
    ) {
        ResourceHandler<FluidResource> handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) {
            return false;
        }

        return tryFillContainerFromTank(stack, handler, tank);
    }

    private static boolean tryFillContainerFromTank(
            ItemStack stack,
            ResourceHandler<FluidResource> handler,
            FluidTankAccess tank
    ) {
        FluidStack stored = tank.getFluid();
        if (stored.isEmpty() || stored.getAmount() <= 0) {
            return false;
        }

        int request = getFillRequest(stack, handler, stored);
        if (request <= 0) {
            return false;
        }

        FluidResource resource = FluidResource.of(stored.getFluidHolder());

        try (Transaction tx = Transaction.openRoot()) {
            int inserted = handler.insert(resource, request, tx);
            if (inserted <= 0) {
                return false;
            }

            tx.commit();
            return !tank.extract(inserted, false).isEmpty();
        }
    }

    private static int getDrainRequest(
            ItemStack stack,
            FluidStack incoming,
            FluidTankAccess tank,
            int containedAmount
    ) {
        int addable = tank.getAddableAmount(incoming);
        if (addable <= 0) {
            return 0;
        }

        boolean bucketLike = stack.getItem() instanceof BucketItem || stack.is(Items.BUCKET);
        if (bucketLike) {
            return addable >= FluidType.BUCKET_VOLUME ? FluidType.BUCKET_VOLUME : 0;
        }

        return Math.min(containedAmount, addable);
    }

    private static int getFillRequest(
            ItemStack stack,
            ResourceHandler<FluidResource> handler,
            FluidStack stored
    ) {
        if (stored.isEmpty() || stored.getAmount() <= 0) {
            return 0;
        }

        FluidResource resource = FluidResource.of(stored.getFluidHolder());
        if (resource.isEmpty()) {
            return 0;
        }

        boolean bucketLike = stack.getItem() instanceof BucketItem || stack.is(Items.BUCKET);
        if (bucketLike) {
            return stored.getAmount() >= FluidType.BUCKET_VOLUME
                    ? FluidType.BUCKET_VOLUME
                    : 0;
        }

        int itemAmount = handler.getAmountAsInt(0);
        FluidResource itemResource = handler.getResource(0);

        if (!itemResource.isEmpty()) {
            FluidStack itemFluid = itemResource.toStack(itemAmount);
            if (!FluidStack.isSameFluidSameComponents(stored, itemFluid)) {
                return 0;
            }
        }

        int capacity = handler.getCapacityAsInt(0, resource);
        int remainingSpace = capacity - itemAmount;
        if (remainingSpace <= 0) {
            return 0;
        }

        return Math.min(stored.getAmount(), remainingSpace);
    }
}