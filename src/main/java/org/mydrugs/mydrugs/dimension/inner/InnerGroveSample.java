package org.mydrugs.mydrugs.dimension.inner;

import org.mydrugs.mydrugs.core.drug.DrugId;

record InnerGroveSample(
        boolean inGrove,
        boolean groveCore,
        boolean groveEdge,
        double strength,
        double canopyDensity,
        double understoryDensity,
        double groundDensity,
        boolean heroCandidate,
        DrugId primaryDrug,
        DrugId secondaryDrug,
        double secondaryWeight,
        InnerTreeArchetype archetype
) {
}
