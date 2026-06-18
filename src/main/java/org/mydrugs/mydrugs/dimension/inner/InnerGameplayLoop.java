package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.InnerDimensionService;

import java.util.UUID;

public final class InnerGameplayLoop {
    private static final double TRIAL_ATTEMPT_RADIUS_SQR = 24.0D * 24.0D;

    private InnerGameplayLoop() {
    }

    public enum Phase {
        UNAVAILABLE,
        ENTER_READY,
        SEEK_TRIAL,
        ATTEMPT_TRIAL,
        RETURN_TO_ANCHOR,
        REFLECT_UNLOCK,
        SEEK_NEXT,
        WAIT_FOR_INTEGRATION,
        OPEN_SPIRAL_COURT,
        COMPLETE_SPIRAL_COURT,
        COMPLETE;

        public boolean directsTrialSearch() {
            return this == SEEK_TRIAL || this == ATTEMPT_TRIAL || this == SEEK_NEXT;
        }
    }

    public record State(Phase phase, @Nullable DrugId nearestTrial) {
    }

    public static Phase outside(InnerDimensionService.OpenStatus status) {
        return status == InnerDimensionService.OpenStatus.READY ? Phase.ENTER_READY : Phase.UNAVAILABLE;
    }

    public static State inside(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            UUID owner,
            BlockPos origin
    ) {
        if (data == null || island == null || owner == null) {
            return new State(Phase.UNAVAILABLE, null);
        }
        if (data.hasPendingTrialReturn(owner)) {
            return new State(Phase.RETURN_TO_ANCHOR, null);
        }
        if (island.allInnerTrialsCompleted()) {
            if (!data.isSpiralCourtPlaced(owner)) {
                return new State(Phase.OPEN_SPIRAL_COURT, null);
            }
            if (!data.hasProgressMarker(owner, InnerDimensionConstants.MARKER_SPIRAL_COMPLETED)) {
                return new State(Phase.COMPLETE_SPIRAL_COURT, null);
            }
            return new State(Phase.COMPLETE, null);
        }
        if (needsReflectionUnlock(data, island, owner)) {
            return new State(Phase.REFLECT_UNLOCK, null);
        }

        DrugId nearest = InnerTrialManager.nearestIncompleteTrial(island, origin);
        if (nearest != null) {
            return new State(trialSearchPhase(island, origin, nearest), nearest);
        }
        if (island.allIntegratedTrialsCompleted()) {
            return new State(Phase.WAIT_FOR_INTEGRATION, null);
        }
        return new State(Phase.SEEK_TRIAL, null);
    }

    public static boolean needsReflectionUnlock(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            UUID owner
    ) {
        return data != null
                && island != null
                && owner != null
                && !island.allInnerTrialsCompleted()
                && island.completedInnerTrialCount() > 0
                && (!data.hasProgressMarker(owner, InnerDimensionConstants.MARKER_FIRST_VAULT_OPENED)
                || island.restoredScarMarkers().isEmpty());
    }

    private static Phase trialSearchPhase(
            InnerDimensionSavedData.IslandState island,
            BlockPos origin,
            DrugId nearest
    ) {
        if (origin != null) {
            BlockPos landmark = InnerRegionMap.landmarkFor(island.centerX(), island.centerZ(), nearest);
            double dx = landmark.getX() - origin.getX();
            double dz = landmark.getZ() - origin.getZ();
            if (dx * dx + dz * dz <= TRIAL_ATTEMPT_RADIUS_SQR) {
                return Phase.ATTEMPT_TRIAL;
            }
        }
        return island.completedInnerTrialCount() > 0 ? Phase.SEEK_NEXT : Phase.SEEK_TRIAL;
    }
}
