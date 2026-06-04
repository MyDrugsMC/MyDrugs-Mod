package org.mydrugs.mydrugs.energy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.energy.psycurrent.DistillateFuel;
import org.mydrugs.mydrugs.energy.psycurrent.DistillateFuelType;

class PsyCurrentPlanTest {

    @Test
    void storageClampsReceiveAndExtract() {
        PsyCurrentStorage storage = new PsyCurrentStorage(100);

        assertEquals(75, storage.receive(75, false));
        assertEquals(75, storage.stored());
        assertEquals(25, storage.receive(50, false));
        assertEquals(100, storage.stored());
        assertEquals(40, storage.extract(40, false));
        assertEquals(60, storage.stored());
        assertEquals(60, storage.extract(100, false));
        assertEquals(0, storage.stored());
    }

    @Test
    void storageSimulationDoesNotMutate() {
        PsyCurrentStorage storage = new PsyCurrentStorage(100);

        assertEquals(80, storage.receive(80, true));
        assertEquals(0, storage.stored());
        storage.receive(60, false);
        assertEquals(40, storage.receive(80, true));
        assertEquals(60, storage.stored());
        assertEquals(50, storage.extract(50, true));
        assertEquals(60, storage.stored());
    }

    @Test
    void nonPulsingFuelOutputsExactAverageCurrent() {
        DistillateFuel fuel = new DistillateFuel(null, DistillateFuelType.LUCID, 2_000, 200, 0, 0, false);

        assertEquals(10, fuel.outputForTick(0));
        assertEquals(10, fuel.outputForTick(199));
    }

    @Test
    void unstableFuelUsesDeterministicRepeatingPulse() {
        DistillateFuel fuel = new DistillateFuel(null, DistillateFuelType.UNSTABLE, 4_000, 160, 20, 0, true);

        int[] firstCycle = new int[8];
        for (int i = 0; i < firstCycle.length; i++) {
            firstCycle[i] = fuel.outputForTick(i * 10);
        }

        assertEquals(firstCycle[0], fuel.outputForTick(80));
        assertEquals(firstCycle[1], fuel.outputForTick(90));
        assertEquals(firstCycle[2], fuel.outputForTick(100));
        assertEquals(firstCycle[3], fuel.outputForTick(110));
        assertEquals(firstCycle[4], fuel.outputForTick(120));
        assertEquals(firstCycle[5], fuel.outputForTick(130));
        assertEquals(firstCycle[6], fuel.outputForTick(140));
        assertEquals(firstCycle[7], fuel.outputForTick(150));
    }
}
