package org.mydrugs.mydrugs.fluids;

import org.mydrugs.mydrugs.core.drug.DrugId;

/**
 * Static description of a fluid. Carries richer metadata (role/hazard/phase/handling) used for
 * display and audits. The legacy four-argument constructor is preserved so existing registration
 * call sites continue to compile with sensible defaults.
 */
public record FluidSpec(
        String name,
        int tint,
        boolean drinkable,
        DrugId drugId,
        FluidRole role,
        Hazard hazard,
        FluidPhase phase,
        boolean bottlable,
        boolean bucketable
) {
    /** Legacy constructor: neutral metadata defaults, bottlable + bucketable. */
    public FluidSpec(String name, int tint, boolean drinkable, DrugId drugId) {
        this(name, tint, drinkable, drugId,
                FluidRole.INTERMEDIATE,
                Hazard.SAFE,
                FluidPhase.AQUEOUS,
                true,
                true);
    }

    public FluidSpec withRole(FluidRole role) {
        return new FluidSpec(name, tint, drinkable, drugId, role, hazard, phase, bottlable, bucketable);
    }

    public FluidSpec withHazard(Hazard hazard) {
        return new FluidSpec(name, tint, drinkable, drugId, role, hazard, phase, bottlable, bucketable);
    }

    public FluidSpec withPhase(FluidPhase phase) {
        return new FluidSpec(name, tint, drinkable, drugId, role, hazard, phase, bottlable, bucketable);
    }

    public FluidSpec withHandling(boolean bottlable, boolean bucketable) {
        return new FluidSpec(name, tint, drinkable, drugId, role, hazard, phase, bottlable, bucketable);
    }
}
