package org.mydrugs.mydrugs.dimension.inner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Lifecycle and bound guarantees for the static {@link InnerTerrain} satellite-layout cache. The
 * cache must clear to empty, never exceed its capacity, and recompute identical layouts after a
 * clear (it is a pure-function memo, so eviction can never change terrain output).
 */
class InnerTerrainCacheTest {

    @BeforeEach
    @AfterEach
    void clearCaches() {
        InnerTerrain.clearAllSatelliteCenters();
    }

    @Test
    void clearEmptiesTheSatelliteCache() {
        int spacing = InnerDimensionConstants.SLOT_SPACING;
        InnerTerrain.satelliteCenterCountForTest(0, 0);
        InnerTerrain.satelliteCenterCountForTest(spacing, 0);
        assertTrue(InnerTerrain.satelliteCenterCacheSize() >= 2,
                "warming two distinct slots should leave at least two cache entries");

        InnerTerrain.clearAllSatelliteCenters();
        assertEquals(0, InnerTerrain.satelliteCenterCacheSize(),
                "clearAllSatelliteCenters must empty the cache");
    }

    @Test
    void clearForSingleSlotRemovesOnlyThatSlot() {
        int spacing = InnerDimensionConstants.SLOT_SPACING;
        InnerTerrain.satelliteCenterCountForTest(0, 0);
        InnerTerrain.satelliteCenterCountForTest(spacing, 0);
        int before = InnerTerrain.satelliteCenterCacheSize();

        InnerTerrain.clearSatelliteCenters(0, 0);
        assertEquals(before - 1, InnerTerrain.satelliteCenterCacheSize(),
                "clearing one slot should drop exactly one cache entry");
    }

    @Test
    void satelliteLayoutIsDeterministicAcrossAClear() {
        int cx = InnerTerrain.slotCenter(50_000);
        int cz = InnerTerrain.slotCenter(-50_000);

        int before = InnerTerrain.satelliteCenterCountForTest(cx, cz);
        InnerTerrain.clearAllSatelliteCenters();
        int after = InnerTerrain.satelliteCenterCountForTest(cx, cz);

        assertEquals(before, after, "satellite layout must recompute identically after a clear");
    }

    @Test
    void satelliteCacheNeverExceedsCapacity() {
        int cap = InnerTerrain.satelliteCenterCacheCapacity();
        int spacing = InnerDimensionConstants.SLOT_SPACING;

        // Warm more distinct slots than the cache can hold and assert it stays bounded throughout.
        for (int i = 0; i < cap * 2; i++) {
            InnerTerrain.satelliteCenterCountForTest(i * spacing, 0);
            assertTrue(InnerTerrain.satelliteCenterCacheSize() <= cap,
                    "satellite cache exceeded its capacity at iteration " + i);
        }
        assertEquals(cap, InnerTerrain.satelliteCenterCacheSize(),
                "after overfilling, the cache should sit exactly at capacity");
    }
}
