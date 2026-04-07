package org.mydrugs.mydrugs.machine.handler;

import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;

public class SyncedFluidStacksHandler extends FluidStacksResourceHandler {
    private final Runnable onChanged;

    public SyncedFluidStacksHandler(int tankCount, int capacity, Runnable onChanged) {
        super(tankCount, capacity);
        this.onChanged = onChanged;
    }

    public NonNullList<FluidStack> list() {
        return this.stacks;
    }

    @Override
    protected void onContentsChanged(int index, FluidStack previousStack) {
        if (this.onChanged != null) {
            this.onChanged.run();
        }
    }
}