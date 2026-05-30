package org.mydrugs.mydrugs.dimension.inner.v7;

import net.minecraft.server.level.ServerLevel;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;

import java.util.UUID;

public final class InnerDimensionV7 {
    private InnerDimensionV7() {
    }

    public static void ensureOwnerReady(ServerLevel level, InnerDimensionSavedData.IslandState island) {
        if (level == null || island == null || island.owner() == null) {
            return;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        data.markInitialIslandBuilt(island.owner());
        data.markV7Generated(island.owner(), InnerV7Constants.KEY_PREFIX + "owner_ready");
        InnerV7OverlayQueue.enqueueEntryPatch(island);
    }

    public static boolean onIntegration(ServerLevel level, InnerDimensionSavedData.IslandState island, DrugId drugId) {
        if (level == null || island == null || island.owner() == null || drugId == null) {
            return false;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        int previousCount = island.integratedCount();
        int newRadius = InnerDimensionSavedData.radiusAfterIntegration(previousCount, drugId);
        boolean recorded = data.recordIntegration(island.owner(), drugId, Math.max(island.currentRadius(), newRadius));
        if (recorded) {
            data.markV7Generated(island.owner(), InnerV7Constants.KEY_PREFIX + "integration:" + drugId.serializedName());
            InnerV7OverlayQueue.enqueueIntegrationPatch(island, drugId);
        }
        return recorded;
    }

    public static InnerV7RegenerationJob regenerateOwnerDebug(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island
    ) {
        if (level == null || island == null || island.owner() == null) {
            return new InnerV7RegenerationJob(new UUID(0L, 0L), 0, false);
        }
        InnerDimensionSavedData.get(level).clearV7Generated(island.owner());
        return InnerV7OverlayQueue.enqueueFullRegeneration(island);
    }

    public static InnerV7Location locateLandmark(InnerDimensionSavedData.IslandState island, DrugId drugId) {
        return new InnerV7Location(
                drugId,
                InnerV7RegionMap.landmarkFor(island.centerX(), island.centerZ(), drugId),
                "landmark"
        );
    }

    public static boolean cancelRegeneration(UUID owner) {
        return InnerV7OverlayQueue.cancel(owner);
    }

    public static String queueStatus(UUID owner) {
        return InnerV7OverlayQueue.queueStatus(owner);
    }

    public static String lastMetricsFor(UUID owner) {
        return InnerV7OverlayQueue.lastMetricsFor(owner);
    }
}
