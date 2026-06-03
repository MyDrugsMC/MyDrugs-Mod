package org.mydrugs.mydrugs.fluids;

/**
 * Physical/chemical category of a fluid. Display/metadata only.
 */
public enum FluidPhase {
    AQUEOUS,
    OIL,
    SLURRY,
    SOLVENT,
    BIOLOGICAL,
    ALCOHOLIC,
    OTHER;

    public String translationKey() {
        return "fluid_phase.mydrugs." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
