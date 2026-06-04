package org.mydrugs.mydrugs.gas;

import org.mydrugs.mydrugs.fluids.Hazard;

/**
 * Static description of a gas. Carries richer metadata (hazard/handling/containment tier) used for
 * display and audits. The legacy four-argument constructor is preserved for existing call sites.
 */
public record GasSpec(
        String path,
        int tint,
        boolean toxic,
        boolean flammable,
        Hazard hazard,
        boolean pipeTransportable,
        boolean tankStorable,
        int containmentTier
) {
    /** Legacy constructor: hazard derived from toxic/flammable; pipe + tank capable, tier 0. */
    public GasSpec(String path, int tint, boolean toxic, boolean flammable) {
        this(path, tint, toxic, flammable,
                toxic ? Hazard.TOXIC : (flammable ? Hazard.FLAMMABLE : Hazard.SAFE),
                true,
                true,
                0);
    }
}
