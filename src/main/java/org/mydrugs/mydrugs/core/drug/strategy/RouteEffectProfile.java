package org.mydrugs.mydrugs.core.drug.strategy;

public record RouteEffectProfile(
        int onsetTicks,
        int peakTicks,
        int comedownTicks,
        float intensityMultiplier,
        float durationMultiplier,
        float doseMultiplier,
        float riskMultiplier
) {
    public static final RouteEffectProfile IMMEDIATE = new RouteEffectProfile(0, 0, 0, 1.0F, 1.0F, 1.0F, 1.0F);

    public RouteEffectProfile {
        onsetTicks = Math.max(0, onsetTicks);
        peakTicks = Math.max(0, peakTicks);
        comedownTicks = Math.max(0, comedownTicks);
        intensityMultiplier = Math.max(0.0F, intensityMultiplier);
        durationMultiplier = Math.max(0.05F, durationMultiplier);
        doseMultiplier = Math.max(0.0F, doseMultiplier);
        riskMultiplier = Math.max(0.0F, riskMultiplier);
    }

    public static RouteEffectProfile immediate() {
        return IMMEDIATE;
    }

    public boolean hasPhaseCurve() {
        return onsetTicks > 0 || peakTicks > 0 || comedownTicks > 0;
    }
}
