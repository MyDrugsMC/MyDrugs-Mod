package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;

import java.util.Locale;
import java.util.UUID;

public final class InnerDimensionSystem {
    private InnerDimensionSystem() {
    }

    public static void ensureOwnerReady(ServerLevel level, InnerDimensionSavedData.IslandState island) {
        if (level == null || island == null || island.owner() == null) {
            return;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        data.markInitialIslandBuilt(island.owner());
        data.markStructurePlaced(island.owner(), InnerDimensionConstants.MARKER_OWNER_READY);
        InnerOverlayQueue.enqueueEntryRefresh(island);
    }

    public static boolean onIntegration(ServerLevel level, InnerDimensionSavedData.IslandState island, DrugId drugId) {
        if (level == null || island == null || island.owner() == null || drugId == null) {
            return false;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        boolean recorded = data.recordIntegration(island.owner(), drugId);
        if (recorded) {
            data.markStructurePlaced(island.owner(), InnerDimensionConstants.pathMarker(drugId));
            InnerOverlayQueue.enqueueIntegrationAwakening(island, drugId);
        }
        return recorded;
    }

    public static InnerRefreshJob refreshOwnerOverlayDebug(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island
    ) {
        if (level == null || island == null || island.owner() == null) {
            return InnerRefreshJob.empty();
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        data.clearOverlayMarkers(island.owner());
        restoreSemanticMarkers(data, island);
        return InnerOverlayQueue.enqueueOwnerOverlayRefresh(island);
    }

    public static InnerRefreshJob recreateOwnerDebug(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island
    ) {
        if (level == null || island == null || island.owner() == null) {
            return InnerRefreshJob.empty();
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        data.clearOverlayMarkers(island.owner());
        restoreSemanticMarkers(data, island);
        return InnerOverlayQueue.enqueueOwnerFullRecreate(island);
    }

    private static void restoreSemanticMarkers(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island
    ) {
        data.markStructurePlaced(island.owner(), InnerDimensionConstants.MARKER_OWNER_READY);
        for (DrugId drugId : island.integratedDrugs()) {
            data.markStructurePlaced(island.owner(), InnerDimensionConstants.pathMarker(drugId));
        }
    }

    public static InnerLocation locateLandmark(InnerDimensionSavedData.IslandState island, DrugId drugId) {
        return new InnerLocation(
                drugId,
                InnerRegionMap.landmarkFor(island.centerX(), island.centerZ(), drugId),
                "landmark"
        );
    }

    public static boolean cancelRefresh(UUID owner) {
        return InnerOverlayQueue.cancel(owner);
    }

    public static String queueStatus(UUID owner) {
        return InnerOverlayQueue.queueStatus(owner);
    }

    public static String lastMetricsFor(UUID owner) {
        return InnerOverlayQueue.lastMetricsFor(owner);
    }

    public static BlockPos safeSpawnPos(ServerLevel level, InnerDimensionSavedData.IslandState island) {
        int y = InnerTerrain.safeSpawnY(island.centerX(), island.centerZ());
        BlockPos feet = new BlockPos(island.centerX(), y, island.centerZ());
        level.getChunkAt(feet);
        InnerPlacement.MutablePlacementCount count = new InnerPlacement.MutablePlacementCount();
        InnerPlacement.safeSet(level, feet.below(), InnerTerrainProfile.forDrug(DrugId.COFFEE).pathBlock(), true, count);
        InnerPlacement.clearSpawnColumn(level, feet, count);
        return feet;
    }

    public static String debugStatus(ServerLevel level, InnerDimensionSavedData.IslandState island, BlockPos pos) {
        InnerTerrain.Sample sample = InnerTerrain.sample(island.centerX(), island.centerZ(), pos.getX(), pos.getZ());
        InnerAtmosphere.Sample atmosphere = InnerAtmosphere.sample(island.centerX(), island.centerZ(), pos);
        return String.format(
                Locale.ROOT,
                "Inner Dimension owner=%s center=%d,%d queue_remaining=%d last_metrics=[%s] drug=%s topY=%d path=%.2f scar=%.2f satellite=%s fog=%d,%d,%d calm=%.2f danger=%.2f saved_metrics=[%s]",
                island.owner(),
                island.centerX(),
                island.centerZ(),
                InnerOverlayQueue.remainingFor(island.owner()),
                InnerOverlayQueue.lastMetricsFor(island.owner()),
                sample.drugId().serializedName(),
                sample.topY(),
                sample.pathStrength(),
                sample.scarStrength(),
                sample.satellite(),
                atmosphere.fogRed(),
                atmosphere.fogGreen(),
                atmosphere.fogBlue(),
                atmosphere.calm(),
                atmosphere.danger(),
                island.lastOverlayMetricsSummary()
        );
    }
}
