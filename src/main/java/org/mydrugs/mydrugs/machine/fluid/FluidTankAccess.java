package org.mydrugs.mydrugs.machine.fluid;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface FluidTankAccess {
    static FluidTankAccess of(int capacity, Supplier<FluidStack> getter, Consumer<FluidStack> setter) {
        Objects.requireNonNull(getter);
        Objects.requireNonNull(setter);

        return new FluidTankAccess() {
            @Override
            public int capacity() {
                return capacity;
            }

            @Override
            public FluidStack getFluid() {
                return getter.get();
            }

            @Override
            public void setFluid(FluidStack stack) {
                setter.accept(stack == null ? FluidStack.EMPTY : stack.copy());
            }
        };
    }

    int capacity();

    FluidStack getFluid();

    void setFluid(FluidStack stack);

    default boolean isEmpty() {
        FluidStack stored = this.getFluid();
        return stored.isEmpty() || stored.getAmount() <= 0;
    }

    default int getAmount() {
        FluidStack stored = this.getFluid();
        return stored.isEmpty() ? 0 : stored.getAmount();
    }

    default int getFreeSpace() {
        return Math.max(0, this.capacity() - this.getAmount());
    }

    default int getAddableAmount(FluidStack incoming) {
        if (incoming == null || incoming.isEmpty() || incoming.getAmount() <= 0) {
            return 0;
        }

        FluidStack stored = this.getFluid();
        if (!stored.isEmpty() && !FluidStack.isSameFluidSameComponents(stored, incoming)) {
            return 0;
        }

        return Math.min(this.getFreeSpace(), incoming.getAmount());
    }

    default int insert(FluidStack incoming, boolean simulate) {
        int addable = this.getAddableAmount(incoming);
        if (simulate || addable <= 0) {
            return addable;
        }

        FluidStack stored = this.getFluid();
        if (stored.isEmpty()) {
            this.setFluid(incoming.copyWithAmount(addable));
        } else {
            this.setFluid(stored.copyWithAmount(stored.getAmount() + addable));
        }

        return addable;
    }

    default FluidStack extract(int amount, boolean simulate) {
        FluidStack stored = this.getFluid();
        if (stored.isEmpty() || amount <= 0) {
            return FluidStack.EMPTY;
        }

        int extracted = Math.min(amount, stored.getAmount());
        FluidStack drained = stored.copyWithAmount(extracted);

        if (!simulate) {
            int remaining = stored.getAmount() - extracted;
            this.setFluid(remaining <= 0 ? FluidStack.EMPTY : stored.copyWithAmount(remaining));
        }

        return drained;
    }

    default ResourceLocation getFluidId() {
        FluidStack stored = getFluid();
        if (stored.isEmpty()) return null;

        Fluid fluid = stored.getFluid();
        if (fluid == Fluids.EMPTY) return null;

        return BuiltInRegistries.FLUID.getKey(fluid);
    }

    default boolean has(ResourceLocation resourceLocation) {
        return getFluidId().equals(resourceLocation);
    }

    default boolean dump() {
        if (isEmpty()) return false;
        setFluid(FluidStack.EMPTY);
        return true;
    }
}