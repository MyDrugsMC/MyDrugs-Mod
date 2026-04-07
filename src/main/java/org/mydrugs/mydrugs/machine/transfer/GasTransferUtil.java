package org.mydrugs.mydrugs.machine.transfer;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.gas.GasStack;
import org.mydrugs.mydrugs.gas.GasTransport;
import org.mydrugs.mydrugs.gas.IGasHandler;
import org.mydrugs.mydrugs.gas.ModGasCapabilities;

public final class GasTransferUtil {
    private GasTransferUtil() {
    }

    public static boolean tryProcessTransferSlot(
            NonNullList<ItemStack> stacks,
            int itemSlot,
            IGasHandler machineTank,
            LockedTransferSlots locks,
            int lockIndex
    ) {
        ItemStack stack = stacks.get(itemSlot);
        if (stack.isEmpty()) {
            locks.reset(lockIndex);
            return false;
        }

        IGasHandler itemTank = stack.getCapability(ModGasCapabilities.ITEM, null);
        if (itemTank == null) {
            locks.reset(lockIndex);
            return false;
        }

        TransferMode mode = locks.get(lockIndex);
        if (mode == TransferMode.NONE) {
            mode = resolveMode(machineTank, itemTank);
            if (mode == TransferMode.NONE) {
                return false;
            }
            locks.set(lockIndex, mode);
        }

        return switch (mode) {
            case DRAIN -> tryDrainItemToMachine(itemTank, machineTank);
            case FILL -> tryFillItemFromMachine(machineTank, itemTank);
            case NONE -> false;
        };
    }

    public static boolean tryFillOutputSlot(
            NonNullList<ItemStack> stacks,
            int itemSlot,
            IGasHandler machineTank
    ) {
        ItemStack stack = stacks.get(itemSlot);
        if (stack.isEmpty()) {
            return false;
        }

        IGasHandler itemTank = stack.getCapability(ModGasCapabilities.ITEM, null);
        if (itemTank == null) {
            return false;
        }

        return tryFillItemFromMachine(machineTank, itemTank);
    }

    private static TransferMode resolveMode(IGasHandler machineTank, IGasHandler itemTank) {
        GasStack itemGas = itemTank.getGasInTank(0);
        if (!itemGas.isEmpty() && machineTank.fill(itemGas, true) > 0) {
            return TransferMode.DRAIN;
        }

        GasStack machineGas = machineTank.getGasInTank(0);
        if (!machineGas.isEmpty() && itemTank.fill(machineGas, true) > 0) {
            return TransferMode.FILL;
        }

        return TransferMode.NONE;
    }

    private static boolean tryDrainItemToMachine(IGasHandler itemTank, IGasHandler machineTank) {
        long freeSpace = machineTank.getTankCapacity(0) - machineTank.getGasInTank(0).amount();
        if (freeSpace <= 0) {
            return false;
        }

        return GasTransport.move(itemTank, machineTank, freeSpace) > 0;
    }

    private static boolean tryFillItemFromMachine(IGasHandler machineTank, IGasHandler itemTank) {
        GasStack machineGas = machineTank.getGasInTank(0);
        if (machineGas.isEmpty()) {
            return false;
        }

        return GasTransport.move(machineTank, itemTank, machineGas.amount()) > 0;
    }
}