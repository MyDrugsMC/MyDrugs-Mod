package org.mydrugs.mydrugs.dimension.inner;

record InnerFeatureSample(
        boolean lake,
        double lakeStrength,
        double lakeCoreStrength,
        double shoreStrength,
        double wetlandStrength,
        InnerLakeType lakeType,
        int lakeSurfaceY,
        int lakeFloorY,
        boolean lakeIsland,
        boolean lakeCenterpiece,
        boolean hole,
        double holeStrength,
        InnerHoleType holeType,
        boolean spikeField,
        double spikeStrength,
        boolean treeZone,
        double treeDensity,
        boolean plantPatch,
        double plantDensity,
        double cliffStrength,
        double slopeHint
) {
}
