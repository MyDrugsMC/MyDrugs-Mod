package org.mydrugs.mydrugs.mutation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfectionStateTest {
    @Test
    void treatmentReducesSeverityAndProgression() {
        InfectionState infection = new InfectionState();
        infection.load(true, 20 * 60 * 6, 0.70F, 3);

        infection.treat(0.20F, 20 * 60 * 3);

        assertTrue(infection.active());
        assertEquals(0.50F, infection.severity(), 1.0E-6F);
        assertEquals(20 * 60 * 3, infection.ticks());
        assertEquals(2, infection.stage());
    }

    @Test
    void strongTreatmentClearsFreshInfection() {
        InfectionState infection = new InfectionState();
        infection.start(0.45F);

        infection.treat(0.55F, 20 * 60 * 3);

        assertFalse(infection.active());
        assertEquals(0, infection.ticks());
        assertEquals(0.0F, infection.severity());
    }

    @Test
    void cureRemainsAFullCure() {
        InfectionState infection = new InfectionState();
        infection.load(true, 20 * 60 * 8, 1.0F, 3);

        infection.cure(0.01F);

        assertFalse(infection.active());
        assertEquals(0, infection.ticks());
    }
}
