package org.mydrugs.mydrugs.worldgen;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.Config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldgenConfigTest {

    @Test
    void defaultPsychedelicBiomeGenerationIsSeparateAndRare() {
        assertFalse(Config.Worldgen.DEFAULT_REPLACE_MUSHROOM_FIELDS);
        assertTrue(Config.Worldgen.DEFAULT_ADD_PSYCHEDELIC_BIOME_SEPARATELY);
        assertEquals(1, Config.Worldgen.DEFAULT_PSYCHEDELIC_BIOME_WEIGHT);
        assertEquals("mushroom_only", Config.Worldgen.DEFAULT_PSYCHEDELIC_BIOME_CLIMATE_BANDS);

        assertTrue(WorldgenConfig.terraBlenderOverworldEnabled(
                true,
                true,
                Config.Worldgen.DEFAULT_PSYCHEDELIC_BIOME_WEIGHT,
                Config.Worldgen.DEFAULT_REPLACE_MUSHROOM_FIELDS,
                Config.Worldgen.DEFAULT_ADD_PSYCHEDELIC_BIOME_SEPARATELY
        ));
    }

    @Test
    void invalidClimateBandFallsBackToMushroomOnly() {
        assertEquals("mushroom_only", WorldgenConfig.normalizePsychedelicClimateBands("not_a_band"));
        assertEquals("mushroom_only", WorldgenConfig.normalizePsychedelicClimateBands(null));
        assertEquals("warm_wet", WorldgenConfig.normalizePsychedelicClimateBands(" WARM_WET "));
        assertEquals("broad_wet", WorldgenConfig.normalizePsychedelicClimateBands("broad_wet"));
    }

    @Test
    void warningPredicatesDetectConflictingOrWeightlessInjection() {
        assertTrue(WorldgenConfig.hasConflictingPsychedelicBiomeInjection(true, true));
        assertFalse(WorldgenConfig.hasConflictingPsychedelicBiomeInjection(false, true));
        assertFalse(WorldgenConfig.hasConflictingPsychedelicBiomeInjection(true, false));

        assertTrue(WorldgenConfig.hasEnabledPsychedelicBiomeInjectionWithoutWeight(0, false, true));
        assertTrue(WorldgenConfig.hasEnabledPsychedelicBiomeInjectionWithoutWeight(0, true, false));
        assertFalse(WorldgenConfig.hasEnabledPsychedelicBiomeInjectionWithoutWeight(1, false, true));
        assertFalse(WorldgenConfig.hasEnabledPsychedelicBiomeInjectionWithoutWeight(0, false, false));
    }
}
