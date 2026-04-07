package org.mydrugs.mydrugs.machine.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.mydrugs.mydrugs.gas.ModGasCapabilities;

public final class MachineItemUtil {
    private MachineItemUtil() {
    }

    public static boolean isFluidContainer(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        return ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM) != null;
    }

    public static boolean isGasContainer(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        return stack.getCapability(ModGasCapabilities.ITEM, null) != null;
    }
}