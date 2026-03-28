package org.mydrugs.mydrugs.fluids;

import org.mydrugs.mydrugs.core.drug.DrugId;

public record FluidEntryDef(
        String name,
        int tint,
        boolean drinkable,
        DrugId drugId
) {}