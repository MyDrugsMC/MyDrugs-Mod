package org.mydrugs.mydrugs.energy.psycurrent;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.energy.PsyCurrentConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StrainRiskTest {

    @Test
    void mapsRawStrainToNamedBuckets() {
        assertEquals(StrainRisk.STABLE, StrainRisk.forStrain(0));
        assertEquals(StrainRisk.STABLE, StrainRisk.forStrain(39));
        assertEquals(StrainRisk.TENSE, StrainRisk.forStrain(40));
        assertEquals(StrainRisk.TENSE, StrainRisk.forStrain(69));
        assertEquals(StrainRisk.DANGEROUS, StrainRisk.forStrain(70));
        assertEquals(StrainRisk.DANGEROUS, StrainRisk.forStrain(89));
        assertEquals(StrainRisk.CRITICAL, StrainRisk.forStrain(90));
        assertEquals(StrainRisk.CRITICAL, StrainRisk.forStrain(99));
        assertEquals(StrainRisk.OVERLOADED, StrainRisk.forStrain(PsyCurrentConstants.ENGINE_MAX_STRAIN));
        assertEquals(StrainRisk.OVERLOADED, StrainRisk.forStrain(500));
    }

    @Test
    void clampsNegativeStrainToStable() {
        assertEquals(StrainRisk.STABLE, StrainRisk.forStrain(-100));
    }

    @Test
    void nullFuelMirrorsCurrentStrain() {
        assertEquals(StrainRisk.STABLE, StrainRisk.forecast(10, null, false));
        assertEquals(StrainRisk.DANGEROUS, StrainRisk.forecast(75, null, false));
    }

    @Test
    void forecastSumsStartAndPerSecondAcrossBurn() {
        // 25-second burn that adds 2 strain/s lifts a near-zero engine to Dangerous: 0+10+50.
        DistillateFuel fuel = new DistillateFuel(
                null,
                DistillateFuelType.REDLINE,
                10_000,
                25 * 20,
                10,
                2,
                false);
        StrainRisk forecast = StrainRisk.forecast(20, fuel, false);
        assertEquals(StrainRisk.DANGEROUS, forecast);
    }

    @Test
    void currentRegulatorReducesProjectedStrain() {
        DistillateFuel fuel = new DistillateFuel(
                null,
                DistillateFuelType.OVERDRIVE,
                10_000,
                30 * 20,
                5,
                3,
                false);
        StrainRisk without = StrainRisk.forecast(10, fuel, false);
        StrainRisk with = StrainRisk.forecast(10, fuel, true);
        // The regulator subtracts ENGINE_CURRENT_REGULATOR_STRAIN_REDUCTION per second; over 30s
        // that's enough to bring an overloaded forecast back to merely dangerous.
        assertEquals(StrainRisk.OVERLOADED, without);
        assertEquals(StrainRisk.DANGEROUS, with);
    }

    @Test
    void negativeStrainFuelIsClampedToStable() {
        DistillateFuel calming = new DistillateFuel(
                null,
                DistillateFuelType.CALMING,
                5_000,
                25 * 20,
                -20,
                -1,
                false);
        StrainRisk forecast = StrainRisk.forecast(15, calming, false);
        // Strong cooling fuel projects to stable regardless of starting strain.
        assertEquals(StrainRisk.STABLE, forecast);
    }
}
