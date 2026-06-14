package org.mydrugs.mydrugs.dimension.inner;

import org.mydrugs.mydrugs.core.drug.DrugId;

/**
 * Per-column slice of a floating sky-shard islet (the y 150-215 band above the island).
 * {@code strength} is the radial falloff inside the islet (1 at its centre, 0 at the rim);
 * {@code drug} is the region identity of the whole islet (sampled at its cell centre so one
 * islet never mixes palettes).
 */
record InnerSkyShardSample(
        boolean land,
        int topY,
        int bottomY,
        double strength,
        DrugId drug,
        boolean crystalline
) {
    static final InnerSkyShardSample NONE =
            new InnerSkyShardSample(false, 0, 0, 0.0D, DrugId.COFFEE, false);
}
