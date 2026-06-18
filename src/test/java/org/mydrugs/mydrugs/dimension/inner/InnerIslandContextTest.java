package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnerIslandContextTest {
    @Test
    void resolvesIslandByPhysicalSlot() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID firstOwner = UUID.fromString("00000000-0000-0000-0000-000000000041");
        UUID secondOwner = UUID.fromString("00000000-0000-0000-0000-000000000042");
        data.getOrCreateIsland(firstOwner);
        InnerDimensionSavedData.IslandState secondIsland = data.getOrCreateIsland(secondOwner);
        BlockPos playerPos = new BlockPos(
                secondIsland.centerX() + 10,
                InnerDimensionConstants.BASE_Y,
                secondIsland.centerZ() - 10
        );

        InnerIslandContext context = InnerIslandContext.resolveForInnerPosition(data, secondOwner, playerPos);

        assertNotNull(context);
        assertSame(secondIsland, context.island());
        assertEquals(secondOwner, context.owner());
        assertEquals(
                new BlockPos(secondIsland.centerX(), InnerDimensionConstants.BASE_Y, secondIsland.centerZ()),
                context.centerPosition()
        );
        assertTrue(context.playerIsOwner());
        assertTrue(context.playerInsideOwnIsland());
        assertTrue(context.contains(playerPos));
    }

    @Test
    void visitorInAnotherIslandSlotDoesNotCreatePhantomIsland() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000043");
        UUID visitor = UUID.fromString("00000000-0000-0000-0000-000000000044");
        InnerDimensionSavedData.IslandState ownerIsland = data.getOrCreateIsland(owner);
        BlockPos visitorPos = new BlockPos(
                ownerIsland.centerX(),
                InnerDimensionConstants.BASE_Y,
                ownerIsland.centerZ()
        );

        InnerIslandContext context = InnerIslandContext.resolveForInnerPosition(data, visitor, visitorPos);

        assertNotNull(context);
        assertSame(ownerIsland, context.island());
        assertEquals(owner, context.owner());
        assertFalse(context.playerIsOwner());
        assertFalse(context.playerInsideOwnIsland());
        assertNull(data.island(visitor));
    }

    @Test
    void ownerInteractionGuardsProtectTrialAndScarSystems() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000045");
        UUID visitor = UUID.fromString("00000000-0000-0000-0000-000000000046");
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        BlockPos ownerPos = new BlockPos(island.centerX(), InnerDimensionConstants.BASE_Y, island.centerZ());
        BlockPos outsideSlot = ownerPos.offset(InnerDimensionConstants.SLOT_SPACING, 0, 0);

        assertTrue(InnerTrialManager.canOwnerInteractAtForTest(data, owner, ownerPos, ownerPos));
        assertTrue(InnerScarHealer.canOwnerRestoreAtForTest(data, owner, ownerPos, ownerPos));

        assertFalse(InnerTrialManager.canOwnerInteractAtForTest(data, visitor, ownerPos, ownerPos));
        assertFalse(InnerScarHealer.canOwnerRestoreAtForTest(data, visitor, ownerPos, ownerPos));

        assertFalse(InnerTrialManager.canOwnerInteractAtForTest(data, owner, ownerPos, outsideSlot));
        assertFalse(InnerScarHealer.canOwnerRestoreAtForTest(data, owner, ownerPos, outsideSlot));
        assertNull(data.island(visitor));
    }
}
