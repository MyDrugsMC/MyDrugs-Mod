package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import org.mydrugs.mydrugs.core.drug.DrugId;

import java.util.EnumSet;
import java.util.Set;

final class InnerSceneSampler {
    private static final long SCENE_SALT = 0x5343_454E_455L;
    private static final int COARSE_CELL = 96;

    private InnerSceneSampler() {
    }

    static InnerSceneSample sample(
            long seed,
            int islandCenterX,
            int islandCenterZ,
            int worldX,
            int worldZ,
            InnerTerrain.Sample terrain,
            InnerGroveSample grove
    ) {
        InnerSceneType type = chooseType(seed, islandCenterX, islandCenterZ, worldX, worldZ, terrain, grove);
        return behavior(type, terrain.drugId());
    }

    static boolean shouldPreserveOpenView(InnerSceneSample scene) {
        return scene.preserveOpenView();
    }

    static boolean isVistaPoint(long seed, int islandCenterX, int islandCenterZ, int worldX, int worldZ, InnerTerrain.Sample terrain) {
        if (!terrain.land()
                || terrain.distanceFromCenter() < InnerDimensionConstants.CORE_RADIUS + 44.0D
                || terrain.pathStrength() < 0.58D
                || terrain.hole()) {
            return false;
        }
        double segmentLength = 96.0D + (terrain.drugId().networkId() % 4) * 18.0D;
        int segment = Math.max(1, (int) Math.floor(terrain.distanceFromCenter() / segmentLength));
        long hash = InnerNoise.mix64(seed
                + SCENE_SALT
                + (long) segment * 0x6D2B_79F5L
                + (long) terrain.drugId().networkId() * 0x1F12_3BB5L);
        double local = InnerNoise.smoothValue(seed + SCENE_SALT + 91L, worldX, worldZ, 42.0D);
        return (hash & 3L) == 0L && local > -0.28D;
    }

    static Set<InnerSceneType> sceneTypesWithBehaviorForTest() {
        return EnumSet.allOf(InnerSceneType.class);
    }

    static int meaningfulBehaviorCountForTest() {
        int count = 0;
        for (InnerSceneType type : InnerSceneType.values()) {
            InnerSceneSample sample = behavior(type, DrugId.COFFEE);
            if (sample.plantMultiplier() != 1.0D
                    || sample.canopyMultiplier() != 1.0D
                    || sample.heroFeatureMultiplier() != 1.0D
                    || sample.glowMultiplier() != 1.0D
                    || sample.pathDetailMultiplier() != 1.0D
                    || sample.lakeDetailMultiplier() != 1.0D
                    || sample.preserveOpenView()
                    || sample.vista()
                    || sample.landmarkApproach()
                    || sample.satelliteDrama()) {
                count++;
            }
        }
        return count;
    }

    static InnerSceneSample behaviorForTest(InnerSceneType type, DrugId drugId) {
        return behavior(type, drugId);
    }

    static boolean rejectsSanctuaryForTest() {
        long seed = InnerTerrain.seedForSlot(0, 0);
        InnerTerrain.Sample terrain = InnerTerrain.sample(0, 0, 0, 0);
        InnerGroveSample grove = InnerGroveSampler.sample(seed, 0, 0, 0, 0, terrain);
        InnerSceneSample scene = sample(seed, 0, 0, 0, 0, terrain, grove);
        return scene.type() == InnerSceneType.QUIET_FIELD && scene.preserveOpenView();
    }

    private static InnerSceneType chooseType(
            long seed,
            int islandCenterX,
            int islandCenterZ,
            int worldX,
            int worldZ,
            InnerTerrain.Sample terrain,
            InnerGroveSample grove
    ) {
        if (!terrain.land()) {
            return InnerSceneType.FLOATING_DEBRIS_COAST;
        }
        if (terrain.distanceFromCenter() < InnerDimensionConstants.CORE_RADIUS + 34.0D) {
            return InnerSceneType.QUIET_FIELD;
        }
        if (isLandmarkApproach(islandCenterX, islandCenterZ, worldX, worldZ, terrain)) {
            return InnerSceneType.LANDMARK_APPROACH;
        }
        if (isVistaPoint(seed, islandCenterX, islandCenterZ, worldX, worldZ, terrain)) {
            return InnerSceneType.PATH_VISTA;
        }
        if (terrain.satellite()) {
            return InnerSceneType.SATELLITE_CROWN;
        }
        if (terrain.lake() || terrain.wetlandStrength() > 0.26D || terrain.shoreStrength() > 0.24D) {
            return lakeSceneFor(terrain);
        }
        if (terrain.transitionZone() && terrain.transitionStrength() > 0.30D) {
            return InnerSceneType.TRANSITION_GARDEN;
        }
        if (terrain.scarStrength() > 0.55D || terrain.holeStrength() > 0.34D) {
            return terrain.drugId() == DrugId.METH ? InnerSceneType.EMBER_PIT : InnerSceneType.SCAR_FIELD;
        }
        if (isCoast(terrain)) {
            return InnerSceneType.FLOATING_DEBRIS_COAST;
        }
        if (grove.heroCandidate()) {
            return InnerSceneType.HERO_TREE_GROVE;
        }

        double openNoise = InnerNoise.smoothValue(seed + SCENE_SALT, worldX, worldZ, COARSE_CELL);
        double denseNoise = InnerNoise.smoothValue(seed + SCENE_SALT + 41L, worldX, worldZ, COARSE_CELL * 0.72D);
        if (grove.inGrove() && denseNoise > -0.34D) {
            return InnerSceneType.DENSE_GROVE;
        }
        if (openNoise < -0.40D) {
            return negativeSpaceFor(terrain.drugId());
        }
        if (denseNoise > 0.52D && (terrain.treeZone() || terrain.plantPatch())) {
            return InnerSceneType.DENSE_GROVE;
        }
        return terrain.drugId() == DrugId.HASH || terrain.drugId() == DrugId.LSD
                ? InnerSceneType.CRYSTAL_CLEARING
                : InnerSceneType.QUIET_FIELD;
    }

    private static boolean isLandmarkApproach(
            int islandCenterX,
            int islandCenterZ,
            int worldX,
            int worldZ,
            InnerTerrain.Sample terrain
    ) {
        if (terrain.distanceFromCenter() < InnerDimensionConstants.CORE_RADIUS + 48.0D || !InnerRegionMap.hasAngle(terrain.primaryDrug())) {
            return false;
        }
        BlockPos landmark = InnerRegionMap.landmarkFor(islandCenterX, islandCenterZ, terrain.primaryDrug());
        double distance = Math.hypot(worldX - landmark.getX(), worldZ - landmark.getZ());
        if (distance < 132.0D) {
            return true;
        }
        if (!terrain.transitionZone() || !InnerRegionMap.hasAngle(terrain.secondaryDrug())) {
            return false;
        }
        BlockPos secondary = InnerRegionMap.landmarkFor(islandCenterX, islandCenterZ, terrain.secondaryDrug());
        return Math.hypot(worldX - secondary.getX(), worldZ - secondary.getZ()) < 112.0D;
    }

    private static boolean isCoast(InnerTerrain.Sample terrain) {
        return terrain.distanceFromCenter() > InnerDimensionConstants.ISLAND_RADIUS - 190.0D
                || (terrain.density() > 0.0D && terrain.density() < 86.0D);
    }

    private static InnerSceneType lakeSceneFor(InnerTerrain.Sample terrain) {
        return switch (terrain.lakeType()) {
            case PRISM -> InnerSceneType.PRISM_BASIN;
            case MAGMA -> InnerSceneType.EMBER_PIT;
            case MEMORY -> InnerSceneType.MEMORY_MARSH;
            case MUD -> terrain.drugId() == DrugId.MUSHROOMS ? InnerSceneType.ROOTED_WETLAND : InnerSceneType.MEMORY_MARSH;
            case SCULK_VOID -> InnerSceneType.SCAR_FIELD;
            case WATER, NONE -> switch (terrain.drugId()) {
                case ALCOHOL -> InnerSceneType.MEMORY_MARSH;
                case HASH, LSD -> InnerSceneType.PRISM_BASIN;
                case METH -> InnerSceneType.EMBER_PIT;
                case MUSHROOMS -> InnerSceneType.ROOTED_WETLAND;
                default -> InnerSceneType.MIRROR_LAKE;
            };
        };
    }

    private static InnerSceneType negativeSpaceFor(DrugId drugId) {
        return switch (drugId) {
            case TOBACCO -> InnerSceneType.ASH_FLAT;
            case HASH, LSD, COCAINE -> InnerSceneType.CRYSTAL_CLEARING;
            case METH -> InnerSceneType.SCAR_FIELD;
            default -> InnerSceneType.QUIET_FIELD;
        };
    }

    private static InnerSceneSample behavior(InnerSceneType type, DrugId drugId) {
        double dangerGlow = drugId == DrugId.METH || drugId == DrugId.COCAINE ? 0.18D : 0.0D;
        double calmPlant = drugId == DrugId.WEED || drugId == DrugId.MUSHROOMS ? 0.12D : 0.0D;
        return switch (type) {
            case QUIET_FIELD -> sample(type, 0.84D, 0.34D + calmPlant, 0.18D, 0.35D, 0.58D, 0.72D, 0.38D, true, false, false, false, 0.22D);
            case DENSE_GROVE -> sample(type, 0.20D, 1.24D + calmPlant, 1.34D, 1.06D, 0.88D, 0.64D, 0.64D, false, false, false, false, 0.55D);
            case HERO_TREE_GROVE -> sample(type, 0.34D, 0.96D, 1.08D, 1.85D, 1.24D, 0.72D, 0.72D, false, false, false, false, 0.72D);
            case MIRROR_LAKE -> sample(type, 0.90D, 0.34D, 0.14D, 0.20D, 1.35D, 0.82D, 1.55D, true, false, false, false, 0.44D);
            case MEMORY_MARSH -> sample(type, 0.52D, 1.05D, 0.45D, 0.42D, 1.42D, 0.86D, 1.48D, false, false, false, false, 0.60D);
            case PRISM_BASIN -> sample(type, 0.72D, 0.62D, 0.24D, 0.52D, 1.82D, 0.98D, 1.76D, true, false, false, false, 0.72D);
            case ROOTED_WETLAND -> sample(type, 0.38D, 1.26D, 0.84D, 0.74D, 1.28D, 0.76D, 1.44D, false, false, false, false, 0.58D);
            case EMBER_PIT -> sample(type, 0.66D, 0.20D, 0.14D, 0.36D, 1.95D, 0.82D, 1.35D, true, false, false, false, 0.82D);
            case SCAR_FIELD -> sample(type, 0.76D, 0.26D, 0.12D, 0.26D, 1.45D + dangerGlow, 0.66D, 0.72D, true, false, false, false, 0.76D);
            case PATH_VISTA -> sample(type, 0.94D, 0.22D, 0.10D, 0.28D, 1.62D, 2.12D, 0.74D, true, true, false, false, 0.50D);
            case LANDMARK_APPROACH -> sample(type, 0.78D, 0.52D, 0.24D, 0.38D, 1.48D, 1.86D, 0.84D, true, true, true, false, 0.58D);
            case TRANSITION_GARDEN -> sample(type, 0.48D, 1.10D, 0.72D, 0.82D, 1.56D, 1.24D, 1.18D, false, false, false, false, 0.66D);
            case ASH_FLAT -> sample(type, 0.88D, 0.18D, 0.08D, 0.18D, 0.94D, 0.58D, 0.36D, true, false, false, false, 0.44D);
            case CRYSTAL_CLEARING -> sample(type, 0.82D, 0.36D, 0.12D, 0.36D, 1.72D, 0.82D, 0.88D, true, false, false, false, 0.62D);
            case FLOATING_DEBRIS_COAST -> sample(type, 0.62D, 0.42D, 0.22D, 0.30D, 1.24D, 0.70D, 0.52D, true, false, false, true, 0.58D);
            case SATELLITE_CROWN -> sample(type, 0.48D, 0.70D, 0.46D, 0.62D, 1.88D, 0.82D, 0.78D, false, false, false, true, 0.78D);
        };
    }

    private static InnerSceneSample sample(
            InnerSceneType type,
            double openness,
            double plantMultiplier,
            double canopyMultiplier,
            double heroFeatureMultiplier,
            double glowMultiplier,
            double pathDetailMultiplier,
            double lakeDetailMultiplier,
            boolean preserveOpenView,
            boolean vista,
            boolean landmarkApproach,
            boolean satelliteDrama,
            double intensity
    ) {
        return new InnerSceneSample(
                type,
                openness,
                Math.max(0.0D, plantMultiplier),
                Math.max(0.0D, canopyMultiplier),
                Math.max(0.0D, heroFeatureMultiplier),
                Math.max(0.0D, glowMultiplier),
                Math.max(0.0D, pathDetailMultiplier),
                Math.max(0.0D, lakeDetailMultiplier),
                preserveOpenView,
                vista,
                landmarkApproach,
                satelliteDrama,
                InnerNoise.clamp01(intensity)
        );
    }
}
