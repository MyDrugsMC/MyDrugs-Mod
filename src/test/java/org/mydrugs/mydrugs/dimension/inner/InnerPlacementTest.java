package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.world.level.block.Block;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for InnerPlacement safety, replaceability rules, and update flag control.
 */
class InnerPlacementTest {

    @Test
    void liveOverlayModeUsesUpdateAllFlags() {
        assertEquals(Block.UPDATE_ALL, InnerPlacement.PlacementMode.LIVE_OVERLAY.updateFlags,
                "LIVE_OVERLAY must use UPDATE_ALL for player-visible safety");
    }

    @Test
    void recreateModeUsesCheaperUpdateFlags() {
        int expected = Block.UPDATE_CLIENTS
                | Block.UPDATE_KNOWN_SHAPE
                | Block.UPDATE_SUPPRESS_DROPS;
        assertEquals(expected, InnerPlacement.PlacementMode.RECREATE.updateFlags,
                "RECREATE must use the cheaper flag set for bulk rebuilds");
    }

    @Test
    void recreateModeOmitsExpensiveNeighborAndInvisibleFlags() {
        // RECREATE omits UPDATE_NEIGHBORS and UPDATE_INVISIBLE — the heavy
        // connectivity/lighting passes — which makes it cheaper for bulk rebuilds.
        int recreate = InnerPlacement.PlacementMode.RECREATE.updateFlags;
        assertEquals(0, recreate & Block.UPDATE_NEIGHBORS,
                "RECREATE must omit UPDATE_NEIGHBORS for bulk performance");
        assertEquals(0, recreate & Block.UPDATE_INVISIBLE,
                "RECREATE must omit UPDATE_INVISIBLE for bulk performance");
    }

    @Test
    void recreateUpdateFlagsMatchConstants() {
        assertEquals(InnerDimensionConstants.RECREATE_UPDATE_FLAGS,
                InnerPlacement.PlacementMode.RECREATE.updateFlags,
                "RECREATE mode flags must match InnerDimensionConstants.RECREATE_UPDATE_FLAGS");
    }

    @Test
    void recreateUpdateFlagsSuppressDrops() {
        // UPDATE_SUPPRESS_DROPS must be set so bulk recreate doesn't spawn items.
        assertTrue((InnerPlacement.PlacementMode.RECREATE.updateFlags & Block.UPDATE_SUPPRESS_DROPS) != 0,
                "RECREATE flags must include UPDATE_SUPPRESS_DROPS");
    }

    @Test
    void liveOverlayFlagsIncludeNeighborUpdates() {
        // LIVE_OVERLAY must include UPDATE_NEIGHBORS so redstone/power/connectivity stays correct.
        assertTrue((InnerPlacement.PlacementMode.LIVE_OVERLAY.updateFlags & Block.UPDATE_NEIGHBORS) != 0,
                "LIVE_OVERLAY flags must include UPDATE_NEIGHBORS");
    }

    @Test
    void replaceabilityRulesDoNotDependOnPlacementMode() {
        assertEquals(
                InnerPlacement.canReplaceCurrentForTest(
                        false,
                        false,
                        false,
                        true,
                        true,
                        InnerPlacement.PlacementMode.LIVE_OVERLAY
                ),
                InnerPlacement.canReplaceCurrentForTest(
                        false,
                        false,
                        false,
                        true,
                        true,
                        InnerPlacement.PlacementMode.RECREATE
                )
        );
        assertEquals(
                InnerPlacement.canReplaceCurrentForTest(
                        false,
                        false,
                        false,
                        true,
                        false,
                        InnerPlacement.PlacementMode.LIVE_OVERLAY
                ),
                InnerPlacement.canReplaceCurrentForTest(
                        false,
                        false,
                        false,
                        true,
                        false,
                        InnerPlacement.PlacementMode.RECREATE
                )
        );
    }

    @Test
    void generatedTerrainCanBeReplacedOnlyWhenAllowed() {
        assertFalse(InnerPlacement.canReplaceCurrentForTest(
                false,
                false,
                false,
                false,
                true,
                InnerPlacement.PlacementMode.RECREATE
        ));
        assertTrue(InnerPlacement.canReplaceCurrentForTest(
                false,
                false,
                false,
                true,
                true,
                InnerPlacement.PlacementMode.RECREATE
        ));
    }

    @Test
    void unknownSolidBlocksAreProtectedByDefaultEvenInRecreateMode() {
        assertFalse(InnerPlacement.canReplaceCurrentForTest(
                false,
                false,
                false,
                true,
                false,
                InnerPlacement.PlacementMode.RECREATE
        ));
    }

    @Test
    void airAndFluidsRemainReplaceable() {
        assertTrue(InnerPlacement.canReplaceCurrentForTest(
                true,
                false,
                false,
                false,
                false,
                InnerPlacement.PlacementMode.LIVE_OVERLAY
        ));
        assertTrue(InnerPlacement.canReplaceCurrentForTest(
                false,
                false,
                true,
                false,
                false,
                InnerPlacement.PlacementMode.RECREATE
        ));
    }

    @Test
    void safeSetChecksBlockEntitiesBeforeReplacing() throws Exception {
        // The pure unit-test JVM does not provide a ServerLevel instance, so this guards the
        // production behavior at source level: block entities must be rejected before setBlock.
        String source = Files.readString(Path.of(
                "src/main/java/org/mydrugs/mydrugs/dimension/inner/InnerPlacement.java"
        ));
        int blockEntityGuard = source.indexOf("level.getBlockEntity(pos) != null");
        int setBlock = source.indexOf("level.setBlock(pos, state, mode.updateFlags)");

        assertTrue(blockEntityGuard >= 0, "safeSet must guard existing block entities");
        assertTrue(setBlock > blockEntityGuard, "block entity guard must run before setBlock");
    }
}
