package org.mydrugs.mydrugs.recovery;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SanctuaryModuleDetectorTest {
    @Test
    void detectsCompleteSanctuaryModuleSet() {
        SanctuaryModuleScan scan = new SanctuaryModuleScan(
                2, 3, 9.0F,
                8, 4, 1, 2, 2,
                2, 1,
                1, 1, 1,
                3, 1,
                0, 0, 0
        );

        Set<SanctuaryModule> modules = SanctuaryModuleDetector.detect(scan, true, RecoveryRoomTier.SANCTUARY);

        assertEquals(7, modules.size());
        assertTrue(modules.contains(SanctuaryModule.REST_MODULE));
        assertTrue(modules.contains(SanctuaryModule.DIARY_DESK));
        assertTrue(modules.contains(SanctuaryModule.MUSIC_CORNER));
        assertTrue(modules.contains(SanctuaryModule.PLANT_BREATHING_CORNER));
        assertTrue(modules.contains(SanctuaryModule.TEA_KITCHEN));
        assertTrue(modules.contains(SanctuaryModule.MEMORY_WALL));
        assertTrue(modules.contains(SanctuaryModule.INTEGRATION_ALCOVE));
    }

    @Test
    void invalidRoomsDoNotExposeModules() {
        SanctuaryModuleScan scan = new SanctuaryModuleScan(
                1, 1, 9.0F,
                3, 2, 1, 1, 1,
                1, 1,
                1, 1, 1,
                2, 1,
                0, 0, 0
        );

        assertTrue(SanctuaryModuleDetector.detect(scan, false, RecoveryRoomTier.SANCTUARY).isEmpty());
        assertTrue(SanctuaryModuleDetector.suggestionKeys(Set.of(), false, RecoveryRoomTier.SAFE_ROOM).isEmpty());
    }

    @Test
    void integrationAlcoveRequiresSanctuaryTier() {
        SanctuaryModuleScan scan = new SanctuaryModuleScan(
                1, 1, 9.0F,
                3, 1, 1, 1, 1,
                0, 0,
                0, 0, 0,
                1, 1,
                0, 0, 0
        );

        assertFalse(SanctuaryModuleDetector.detect(scan, true, RecoveryRoomTier.SAFE_ROOM)
                .contains(SanctuaryModule.INTEGRATION_ALCOVE));
        assertTrue(SanctuaryModuleDetector.detect(scan, true, RecoveryRoomTier.SANCTUARY)
                .contains(SanctuaryModule.INTEGRATION_ALCOVE));
    }

    @Test
    void duplicateContentStillProducesSingleModuleFlag() {
        SanctuaryModuleScan scan = new SanctuaryModuleScan(
                8, 8, 9.0F,
                20, 20, 6, 6, 6,
                6, 6,
                6, 6, 6,
                10, 4,
                0, 0, 0
        );

        Set<SanctuaryModule> modules = SanctuaryModuleDetector.detect(scan, true, RecoveryRoomTier.SANCTUARY);

        assertEquals(SanctuaryModule.values().length, modules.size());
        int flags = SanctuaryModule.flags(modules);
        for (SanctuaryModule module : SanctuaryModule.values()) {
            assertTrue((flags & module.networkBit()) != 0);
        }
    }

    @Test
    void suggestionsAreTierGatedAndLimited() {
        Set<SanctuaryModule> allButIntegration = Set.of(
                SanctuaryModule.REST_MODULE,
                SanctuaryModule.DIARY_DESK,
                SanctuaryModule.MUSIC_CORNER,
                SanctuaryModule.PLANT_BREATHING_CORNER,
                SanctuaryModule.TEA_KITCHEN,
                SanctuaryModule.MEMORY_WALL
        );

        assertFalse(SanctuaryModuleDetector.suggestionKeys(allButIntegration, true, RecoveryRoomTier.SAFE_ROOM)
                .contains(SanctuaryModule.INTEGRATION_ALCOVE.suggestionKey()));
        assertEquals(
                SanctuaryModule.INTEGRATION_ALCOVE.suggestionKey(),
                SanctuaryModuleDetector.suggestionKeys(allButIntegration, true, RecoveryRoomTier.SANCTUARY).getFirst()
        );
    }
}
