package org.mydrugs.mydrugs.blocks;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.fluids.ModFluids;

public enum FluidPumpLoggedFluid implements StringRepresentable {
    EMPTY("empty"),
    WATER("water"),
    PETROLEUM("petroleum");

    private final String serializedName;

    FluidPumpLoggedFluid(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public Fluid sourceFluid() {
        return switch (this) {
            case WATER -> Fluids.WATER;
            case PETROLEUM -> ModFluids.PETROLEUM.source().get();
            case EMPTY -> Fluids.EMPTY;
        };
    }

    public static FluidPumpLoggedFluid fromFluid(Fluid fluid) {
        if (fluid == Fluids.WATER) {
            return WATER;
        }
        if (fluid == ModFluids.PETROLEUM.source().get() || fluid == ModFluids.PETROLEUM.flowing().get()) {
            return PETROLEUM;
        }
        return EMPTY;
    }
}
