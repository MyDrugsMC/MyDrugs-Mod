package org.mydrugs.mydrugs.fluids;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugModel;

public final class FluidTypesEx {
    private FluidTypesEx() {
    }

    public static boolean isDrinkable(Fluid fluid) {
        FluidType type = fluid.getFluidType();
        return type instanceof DrugTintedFluidType drugType && drugType.isDrinkable();
    }

    public static @Nullable DrugModel getDrugModel(Fluid fluid) {
        FluidType type = fluid.getFluidType();
        return type instanceof DrugTintedFluidType drugType ? drugType.getDrugModel() : null;
    }
}