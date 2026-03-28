package org.mydrugs.mydrugs.fluids;

import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;

public class DrugTintedFluidType extends FluidType {
    private final boolean drinkable;
    private final @Nullable DrugModel drugModel;
    public DrugTintedFluidType(Properties properties, boolean drinkable, @Nullable DrugId drugId) {
        super(properties);
        this.drinkable = drinkable;
        this.drugModel = DrugRegistry.getDrug(drugId);
    }

    public boolean isDrinkable() {
        return drinkable;
    }

    public @Nullable DrugModel getDrugModel() {
        return drugModel;
    }
}