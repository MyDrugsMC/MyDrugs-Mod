package org.mydrugs.mydrugs.recipes.polish;

public enum MachineLane {
    EARLY_PROCESSING,
    CRUDE_FLUIDS,
    CHEMICAL_GAS_FLUID,
    INDUSTRIAL_PETROLEUM,
    PSY_RITUAL,
    BIOTECH,
    RECOVERY;

    public String translationKey() {
        return "recipe_polish.mydrugs.lane." + name().toLowerCase();
    }
}
