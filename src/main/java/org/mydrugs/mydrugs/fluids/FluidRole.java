package org.mydrugs.mydrugs.fluids;

/**
 * Production role of a fluid in the processing chains. Display/metadata only.
 */
public enum FluidRole {
    RAW_INPUT,
    INTERMEDIATE,
    PRODUCT,
    BYPRODUCT,
    WASTE;

    public String translationKey() {
        return "fluid_role.mydrugs." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
