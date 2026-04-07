package org.mydrugs.mydrugs.machine.fluid;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Objects;
import java.util.function.Predicate;

public final class StoredFluidTank implements FluidTankAccess {
    private final int capacity;
    private final Runnable onChanged;
    private final Predicate<FluidStack> validator;

    private FluidStack stored = FluidStack.EMPTY;

    public StoredFluidTank(int capacity, Runnable onChanged) {
        this(capacity, onChanged, stack -> true);
    }

    public StoredFluidTank(int capacity, Runnable onChanged, Predicate<FluidStack> validator) {
        this.capacity = capacity;
        this.onChanged = onChanged;
        this.validator = validator;
    }

    @Override
    public int capacity() {
        return this.capacity;
    }

    @Override
    public FluidStack getFluid() {
        return this.stored.copy();
    }

    @Override
    public void setFluid(FluidStack stack) {
        if (stack == null || stack.isEmpty() || stack.getAmount() <= 0) {
            this.stored = FluidStack.EMPTY;
        } else {
            this.stored = stack.copyWithAmount(Math.min(this.capacity, stack.getAmount()));
        }
        this.onChanged.run();
    }

    public int encodeFluidSyncId() {
        return this.stored.isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(this.stored.getFluid());
    }

    public void load(ValueInput input, String key) {
        this.stored = input.read(key, FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY);
    }

    public void save(ValueOutput output, String key) {
        output.store(key, FluidStack.OPTIONAL_CODEC, this.stored);
    }

    @Override
    public int insert(FluidStack incoming, boolean simulate) {
        if (incoming.isEmpty() || !validator.test(incoming)) {
            return 0;
        }
        return FluidTankAccess.super.insert(incoming, simulate);
    }

    @Override
    public int getAddableAmount(FluidStack incoming) {
        if (incoming.isEmpty() || !validator.test(incoming)) {
            return 0;
        }
        return FluidTankAccess.super.getAddableAmount(incoming);
    }
}