package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnerTerrainTest {

    private static final int CENTER_X = 0;
    private static final int CENTER_Z = 0;

    @Test
    void sampleIsDeterministic() {
        for (int i = 0; i < 400; i++) {
            int x = i * 13 - 1500;
            int z = i * -29 + 900;
            InnerTerrain.Sample a = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
            InnerTerrain.Sample b = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
            assertEquals(a, b, "same inputs must yield an identical sample at " + x + "," + z);
        }
    }

    @Test
    void passCacheDoesNotChangeResults() {
        int x = 321;
        int z = -654;
        InnerTerrain.Sample uncached = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
        InnerTerrain.beginCachePass();
        try {
            InnerTerrain.Sample first = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
            InnerTerrain.Sample second = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
            assertEquals(uncached, first, "cached sample must equal the uncached one");
            assertEquals(first, second, "repeated cached lookups must be identical");
        } finally {
            InnerTerrain.endCachePass();
        }
    }

    @Test
    void farOutsideIslandIsNotLand() {
        InnerTerrain.Sample sample = InnerTerrain.sample(CENTER_X, CENTER_Z, 100_000, 100_000);
        assertFalse(sample.land(), "a column far beyond the island footprint must be void");
    }

    @Test
    void centerSampleIsLandAndSafe() {
        InnerTerrain.Sample sample = InnerTerrain.sample(CENTER_X, CENTER_Z, CENTER_X, CENTER_Z);
        assertTrue(sample.land(), "sanctuary center must stay on land");
        assertTrue(sample.path(), "sanctuary center should be treated as a safe path area");
        assertFalse(sample.hole(), "holes must not enter the sanctuary center");
        assertFalse(sample.lake(), "lakes must not enter the sanctuary center");
        assertTrue(sample.topY() >= InnerDimensionConstants.BASE_Y, "center top should be at or above the base shelf");
    }

    @Test
    void islandBodyIsLand() {
        // Sweep a mid-radius ring well inside the island; the main land body should dominate.
        int land = 0;
        int total = 0;
        for (int deg = 0; deg < 360; deg += 5) {
            double rad = Math.toRadians(deg);
            int x = (int) Math.round(Math.cos(rad) * 360.0D);
            int z = (int) Math.round(Math.sin(rad) * 360.0D);
            total++;
            if (InnerTerrain.sample(CENTER_X, CENTER_Z, x, z).land()) {
                land++;
            }
        }
        assertTrue(land >= total * 0.9, "expected the mid-radius ring to be almost entirely land, was " + land + "/" + total);
    }

    @Test
    void scarFlagMatchesStrengthThreshold() {
        for (int i = 0; i < 1500; i++) {
            int x = i * 7 - 1200;
            int z = i * 17 - 1200;
            InnerTerrain.Sample sample = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
            assertEquals(sample.scarStrength() > 0.42D, sample.scar(),
                    "scar flag must follow the strength threshold at " + x + "," + z);
        }
    }

    @Test
    void surfaceDepthStaysInExpectedBand() {
        for (int i = 0; i < 1000; i++) {
            int x = i * 19 - 800;
            int z = i * -23 + 600;
            InnerTerrain.Sample sample = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
            assertTrue(sample.surfaceDepth() >= 2 && sample.surfaceDepth() <= 6,
                    "surface depth out of band: " + sample.surfaceDepth());
        }
    }

    @Test
    void holesAndLakesStayOutOfSanctuaryCore() {
        for (int z = -InnerDimensionConstants.CORE_RADIUS; z <= InnerDimensionConstants.CORE_RADIUS; z += 8) {
            for (int x = -InnerDimensionConstants.CORE_RADIUS; x <= InnerDimensionConstants.CORE_RADIUS; x += 8) {
                if (x * x + z * z > InnerDimensionConstants.CORE_RADIUS * InnerDimensionConstants.CORE_RADIUS) {
                    continue;
                }
                InnerTerrain.Sample sample = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
                assertFalse(sample.hole(), "hole appeared in sanctuary core at " + x + "," + z);
                assertFalse(sample.lake(), "lake appeared in sanctuary core at " + x + "," + z);
            }
        }
    }

    @Test
    void chunkLandCheckKeepsCenterAndRejectsFarChunks() {
        assertTrue(InnerTerrain.chunkMayHaveLand(0, 0), "center chunk should be considered possible land");
        assertFalse(InnerTerrain.chunkMayHaveLand(100_000, 100_000), "far chunk should be rejected quickly");
    }

    @Test
    void pathStrengthIsZeroOutsideMaximumSpokeWander() {
        long seed = InnerTerrain.seedForSlot(CENTER_X, CENTER_Z);
        double distance = 640.0D;
        for (DrugId drug : CuratedDrugChain.ORDER) {
            double angle = InnerRegionMap.angleFor(drug) + InnerTerrain.spokeExclusionEnvelopeForTest() + 0.001D;
            assertEquals(0.0D, InnerTerrain.pathStrengthForTest(seed, distance, angle, drug), 0.0D,
                    "path strength must skip columns outside the spoke envelope for " + drug);
        }
    }

    @Test
    void featureSamplerIsDeterministic() {
        long seed = InnerTerrain.seedForSlot(CENTER_X, CENTER_Z);
        InnerFeatureSample a = InnerFeatureSampler.sample(
                seed, CENTER_X, CENTER_Z, 384, -512, DrugId.WEED, 640.0D, 0.05D, 800.0D, 70, 0.35D, 0.42D, false
        );
        InnerFeatureSample b = InnerFeatureSampler.sample(
                seed, CENTER_X, CENTER_Z, 384, -512, DrugId.WEED, 640.0D, 0.05D, 800.0D, 70, 0.35D, 0.42D, false
        );
        assertEquals(a, b, "feature sampling must be deterministic for the same column");
    }

    @Test
    void groveSamplerIsDeterministic() {
        InnerTerrain.Sample terrain = InnerTerrain.sample(CENTER_X, CENTER_Z, 384, -256);
        long seed = InnerTerrain.seedForSlot(CENTER_X, CENTER_Z);
        InnerGroveSample a = InnerGroveSampler.sample(seed, CENTER_X, CENTER_Z, 384, -256, terrain);
        InnerGroveSample b = InnerGroveSampler.sample(seed, CENTER_X, CENTER_Z, 384, -256, terrain);
        assertEquals(a, b, "grove sampling must be deterministic for the same column");
    }

    @Test
    void sceneSamplerIsDeterministic() {
        long seed = InnerTerrain.seedForSlot(CENTER_X, CENTER_Z);
        for (int i = 0; i < 240; i++) {
            int x = i * 17 - 1300;
            int z = i * -31 + 950;
            InnerTerrain.Sample terrain = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
            InnerGroveSample grove = InnerGroveSampler.sample(seed, CENTER_X, CENTER_Z, x, z, terrain);
            InnerSceneSample a = InnerSceneSampler.sample(seed, CENTER_X, CENTER_Z, x, z, terrain, grove);
            InnerSceneSample b = InnerSceneSampler.sample(seed, CENTER_X, CENTER_Z, x, z, terrain, grove);
            assertEquals(a, b, "scene sampling must be deterministic for " + x + "," + z);
        }
    }

    @Test
    void everySceneTypeHasMeaningfulBehavior() {
        assertEquals(InnerSceneType.values().length, InnerSceneSampler.sceneTypesWithBehaviorForTest().size());
        assertEquals(InnerSceneType.values().length, InnerSceneSampler.meaningfulBehaviorCountForTest());
    }

    @Test
    void sceneBehaviorChangesDensityAndDecorationBiases() {
        InnerSceneSample quiet = InnerSceneSampler.behaviorForTest(InnerSceneType.QUIET_FIELD, DrugId.WEED);
        InnerSceneSample dense = InnerSceneSampler.behaviorForTest(InnerSceneType.DENSE_GROVE, DrugId.WEED);
        InnerSceneSample vista = InnerSceneSampler.behaviorForTest(InnerSceneType.PATH_VISTA, DrugId.COFFEE);
        InnerSceneSample lake = InnerSceneSampler.behaviorForTest(InnerSceneType.PRISM_BASIN, DrugId.LSD);
        assertTrue(quiet.plantMultiplier() < dense.plantMultiplier(), "quiet fields should thin plants relative to dense groves");
        assertTrue(quiet.canopyMultiplier() < dense.canopyMultiplier(), "quiet fields should thin canopy relative to dense groves");
        assertTrue(vista.pathDetailMultiplier() > quiet.pathDetailMultiplier(), "vista scenes should bias path decoration");
        assertTrue(lake.lakeDetailMultiplier() > quiet.lakeDetailMultiplier(), "lake scenes should bias lake decoration");
        assertTrue(vista.preserveOpenView(), "vista scenes must preserve sight lines");
    }

    @Test
    void everyCuratedDrugProfileIsComplete() {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            InnerTerrainProfile profile = InnerTerrainProfile.forDrug(drug);
            assertEquals(drug, profile.drugId());
            assertNotNull(profile.surface(), drug + " missing surface supplier");
            assertNotNull(profile.subsurface(), drug + " missing subsurface supplier");
            assertNotNull(profile.deep(), drug + " missing deep supplier");
            assertNotNull(profile.accent(), drug + " missing accent supplier");
            assertNotNull(profile.path(), drug + " missing path supplier");
            assertNotNull(profile.scar(), drug + " missing scar supplier");
            assertNotNull(profile.nodeBlock(), drug + " missing node supplier");
            assertFalse(profile.plantBlocks().isEmpty(), drug + " should define ambient plants or document an intentional empty list");
            assertNotNull(profile.flora(), drug + " missing flora palette");
            assertTrue(profile.flora().hasAny(), drug + " should define a flora palette");
        }
    }

    @Test
    void everyCuratedDrugHasTreeAndFloraIdentity() {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            assertTrue(InnerTreeBuilder.hasArchetypeFor(drug), drug + " missing tree archetype");
            assertTrue(InnerHeroFeatureBuilder.hasHeroForDrug(drug), drug + " missing hero feature");
            assertTrue(InnerPlantBuilder.hasFloraIdentityFor(drug), drug + " missing flora identity");
            assertTrue(InnerGlowBuilder.hasGlowLanguageFor(drug), drug + " missing glow language");
            assertTrue(InnerLandmarkApproachBuilder.hasApproachStyleFor(drug), drug + " missing landmark approach style");
            assertTrue(InnerPathSceneBuilder.hasDecorationFor(drug), drug + " missing path decoration language");
            assertTrue(InnerPathSceneBuilder.hasArchFor(drug), drug + " missing path arch language");
        }
    }

    @Test
    void pathVistaAndArchRulesAreDeterministicAndRejectSanctuary() {
        assertTrue(InnerPathSceneBuilder.vistaDeterministicForTest());
        assertTrue(InnerPathSceneBuilder.rejectsSanctuaryForTest(), "sanctuary core must not get random path arches or vistas");
        assertTrue(InnerPathSceneBuilder.allRegionDecorationsDefinedForTest());
    }

    @Test
    void symbolicFloraRegistryHasExpandedPalette() {
        assertTrue(InnerSymbolicFloraCatalog.idsForTest().size() >= 17,
                "symbolic flora should include the original plants plus at least twelve new plants");
        assertTrue(InnerSymbolicFloraCatalog.idsForTest().contains("prism_lotus"));
        assertTrue(InnerSymbolicFloraCatalog.idsForTest().contains("redline_bramble"));
        assertTrue(InnerSymbolicFloraCatalog.idsForTest().contains("mycelial_threads"));
    }

    @Test
    void lakeSamplesExposeShoreAndWetlandStrength() {
        long seed = InnerTerrain.seedForSlot(CENTER_X, CENTER_Z);
        boolean found = false;
        for (int z = -900; z <= 900 && !found; z += 32) {
            for (int x = -900; x <= 900; x += 32) {
                InnerFeatureSample sample = InnerFeatureSampler.sample(
                        seed, CENTER_X, CENTER_Z, x, z, DrugId.ALCOHOL, Math.hypot(x, z), 0.02D, 700.0D, 70, 0.18D, 0.25D, false
                );
                if (sample.shoreStrength() > 0.08D && sample.wetlandStrength() > 0.08D) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found, "lake sampling should emit shore and wetland strength around broad lakes");
    }

    @Test
    void sanctuaryRejectsHeroFeatures() {
        assertTrue(InnerHeroFeatureBuilder.rejectsSanctuaryForTest(),
                "sanctuary core must reject hero trees and other major unsafe features");
        assertTrue(InnerSceneSampler.rejectsSanctuaryForTest(),
                "sanctuary core should resolve to calm negative space");
        assertTrue(InnerLakeSceneBuilder.rejectsSanctuaryForTest(),
                "sanctuary core must reject lake scene focal objects");
        assertTrue(InnerCoastDramaBuilder.rejectsSanctuaryForTest(),
                "sanctuary core must reject coast debris");
    }

    @Test
    void transitionSamplesExposeMixedFeatureIdentity() {
        InnerTerrain.Sample transition = null;
        for (int deg = 0; deg < 360 && transition == null; deg++) {
            double rad = Math.toRadians(deg);
            int x = (int) Math.round(Math.cos(rad) * 560.0D);
            int z = (int) Math.round(Math.sin(rad) * 560.0D);
            InnerTerrain.Sample sample = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
            if (sample.transitionZone() && sample.secondaryWeight() > 0.12D && sample.primaryDrug() != sample.secondaryDrug()) {
                transition = sample;
            }
        }
        assertNotNull(transition, "expected at least one sector-boundary transition sample");
        assertTrue(InnerTransitionPalette.hasExplicitHybrid(transition.primaryDrug(), transition.secondaryDrug()),
                "transition pair should have an explicit hybrid palette");
        assertEquals(transition.secondaryDrug(), transition.chooseFeatureDrug(0L),
                "low hash should select the secondary feature identity in a transition band");
        assertEquals(transition.primaryDrug(), transition.chooseFeatureDrug(-1L),
                "high hash should select the primary feature identity in a transition band");
    }

    @Test
    void transitionPaletteHasAtLeastSixExplicitHybrids() {
        assertTrue(InnerTransitionPalette.explicitHybridCountForTest() >= 9,
                "transition palette should define the nine adjacent hybrid pairs");
    }

    @Test
    void lakeScenePassHasSeveralComposedTypes() {
        assertTrue(InnerLakeSceneBuilder.lakeSceneTypeCountForTest() >= 6,
                "lake scenes should include several rare composed scene types");
        assertNotNull(InnerLakeSceneBuilder.typeForTest(DrugId.ALCOHOL, InnerLakeType.MEMORY));
        assertNotNull(InnerLakeSceneBuilder.typeForTest(DrugId.COCAINE, InnerLakeType.MUD));
    }

    @Test
    void destructiveRebuildUsesSameVisualFeatureBuildersAsInitialGeneration() {
        List<String> order = InnerVisualFeatureBuilders.builderOrderForTest();
        assertEquals(List.of(
                "lake_details",
                "lake_scenes",
                "rivers",
                "coast_drama",
                "spikes",
                "talus",
                "mega_forms",
                "hero_features",
                "grove_trees",
                "path_scenes",
                "landmark_approaches",
                "transition_scenes",
                "vaults",
                "flora",
                "glow_details",
                "sky_shards"
        ), order);
    }

    @Test
    void rebuildOverlayAndBurstUseTheSharedVisualFeaturePass() throws IOException {
        String rebuilder = Files.readString(Path.of("src/main/java/org/mydrugs/mydrugs/dimension/inner/InnerChunkRebuilder.java"));
        String overlay = Files.readString(Path.of("src/main/java/org/mydrugs/mydrugs/dimension/inner/InnerOverlayQueue.java"));
        String burst = Files.readString(Path.of("src/main/java/org/mydrugs/mydrugs/dimension/inner/InnerBurstRegenerator.java"));
        assertTrue(rebuilder.contains("InnerVisualFeatureBuilders.placeOverlayFeatures"));
        assertTrue(overlay.contains("InnerVisualFeatureBuilders.placeOverlayFeatures"));
        // Burst recreate is two-phase: rebuild all terrain, then decorate. The terrain pass must
        // run before the decoration pass so the multi-chunk sanctuary disc is never half-cleared.
        assertTrue(burst.contains("rebuildChunkTerrainNow"));
        assertTrue(burst.contains("decorateChunkNow"));
        assertTrue(burst.indexOf("rebuildChunkTerrainNow") < burst.indexOf("decorateChunkNow"),
                "burst must rebuild terrain before decorating");
    }

    @Test
    void destructiveRecreateSeparatesTerrainRebuildFromDecoration() throws IOException {
        // Regression for the recreate "semicircle": the sanctuary disc spans several chunks. If
        // decoration runs inline with the chunk-by-chunk terrain clear, the half written into a
        // not-yet-rebuilt neighbour is wiped when that neighbour is later cleared. Both recreate
        // paths must therefore decorate only after ALL terrain is final.
        String overlay = Files.readString(Path.of("src/main/java/org/mydrugs/mydrugs/dimension/inner/InnerOverlayQueue.java"));
        String rebuilder = Files.readString(Path.of("src/main/java/org/mydrugs/mydrugs/dimension/inner/InnerChunkRebuilder.java"));
        // Structural decoration (sanctuary/landmarks/vaults/paths) lives in its own method so it
        // can run as a distinct phase, decoupled from the terrain rebuild.
        assertTrue(overlay.contains("private static void decorateChunk("));
        assertTrue(overlay.contains("InnerSanctuaryBuilder.placeCenterSanctuary"));
        // The terrain rebuild must NOT place the sanctuary; that is what made the recreate cut it
        // in half. The sanctuary belongs only to the separate decoration phase.
        assertTrue(!rebuilder.contains("placeCenterSanctuary"),
                "terrain rebuild must not place the sanctuary; decoration is a separate phase");
        // The queued FULL_RECREATE path runs the same two phases (terrain, then decorate).
        assertTrue(overlay.contains("inDecoratePhase"));
        assertTrue(overlay.contains("recordForDecorate"));
    }

    @Test
    void innerClientParticlesAndFogAreDimensionGuarded() throws IOException {
        String particles = Files.readString(Path.of("src/main/java/org/mydrugs/mydrugs/client/InnerAmbientParticleController.java"));
        String fog = Files.readString(Path.of("src/main/java/org/mydrugs/mydrugs/client/InnerDimensionEffects.java"));
        assertTrue(particles.contains("InnerDimensions.INNER_LEVEL"));
        assertTrue(particles.contains("particleIntensity()"));
        assertTrue(fog.contains("InnerDimensions.INNER_LEVEL"));
        assertTrue(fog.contains("fogDensity()"));
    }

    @Test
    void everyLandmarkSitsInsideTheIslandRadius() {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            BlockPos landmark = InnerRegionMap.landmarkFor(CENTER_X, CENTER_Z, drug);
            double distance = Math.hypot(landmark.getX() - CENTER_X, landmark.getZ() - CENTER_Z);
            assertTrue(distance <= InnerDimensionConstants.ISLAND_RADIUS,
                    drug + " landmark sits outside the island radius (" + distance + ")");
        }
    }

    @Test
    void everyRegionLandmarkIsReachableFromTheSanctuary() {
        // Walk the straight line from the centre to each landmark; the island body should keep it
        // almost entirely on land, so the shrine is reachable from the central sanctuary.
        for (DrugId drug : CuratedDrugChain.ORDER) {
            BlockPos landmark = InnerRegionMap.landmarkFor(CENTER_X, CENTER_Z, drug);
            int steps = 120;
            int land = 0;
            for (int i = 0; i <= steps; i++) {
                double t = i / (double) steps;
                int x = (int) Math.round(CENTER_X + (landmark.getX() - CENTER_X) * t);
                int z = (int) Math.round(CENTER_Z + (landmark.getZ() - CENTER_Z) * t);
                if (InnerTerrain.sample(CENTER_X, CENTER_Z, x, z).land()) {
                    land++;
                }
            }
            assertTrue(land >= (steps + 1) * 0.9,
                    drug + " landmark is not reachable: only " + land + "/" + (steps + 1) + " steps are land");
        }
    }

    @Test
    void slotCenterSnapsToSpacingGrid() {
        int spacing = InnerDimensionConstants.SLOT_SPACING;
        assertEquals(0, InnerTerrain.slotCenter(10));
        assertEquals(spacing, InnerTerrain.slotCenter(spacing - 10));
        assertEquals(spacing, InnerTerrain.slotCenter(spacing + 10));
        for (int i = -5; i <= 5; i++) {
            int snapped = InnerTerrain.slotCenter(i * spacing + 3);
            assertEquals(0, snapped % spacing, "slot centre must land on the spacing grid");
        }
    }

    @Test
    void seedForSlotIsDeterministicAndDistinct() {
        long a = InnerTerrain.seedForSlot(0, 0);
        long b = InnerTerrain.seedForSlot(0, 0);
        long c = InnerTerrain.seedForSlot(InnerDimensionConstants.SLOT_SPACING, 0);
        assertEquals(a, b, "seed must be deterministic for a slot");
        assertTrue(a != c, "distinct slots should generally produce distinct seeds");
    }

    @Test
    void innerDimensionSourcesDoNotCarryVersionedVisualResidue() throws IOException {
        Pattern residue = Pattern.compile("inner_v\\d+|\\bV\\d+\\b|Part [A-Z]");
        List<Path> roots = List.of(
                Path.of("src/main/java/org/mydrugs/mydrugs/dimension"),
                Path.of("src/main/java/org/mydrugs/mydrugs/commands"),
                Path.of("src/test/java/org/mydrugs/mydrugs/dimension")
        );
        for (Path root : roots) {
            try (var stream = Files.walk(root)) {
                List<Path> offenders = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".java"))
                        .filter(path -> {
                            try {
                                return residue.matcher(Files.readString(path)).find();
                            } catch (IOException e) {
                                throw new IllegalStateException(e);
                            }
                        })
                        .toList();
                assertTrue(offenders.isEmpty(), "versioned visual residue remains in " + offenders);
            }
        }
    }
}
