package org.mydrugs.mydrugs.dimension.inner;

import org.mydrugs.mydrugs.core.drug.DrugId;

import java.util.EnumSet;

/**
 * Per-player mutable trial progress owned by {@link InnerTrialManager}.
 * Tracks announced trials, coffee stillness ticks, tobacco step order,
 * weed placement count, meth/mushroom stabilisation masks, and the cocaine
 * dash start tick.
 *
 * <p>Extracted from {@link InnerTrialManager} to separate state ownership
 * from trial action logic.
 */
final class InnerTrialProgress {
    final EnumSet<DrugId> announced = EnumSet.noneOf(DrugId.class);
    int coffeeTicks;
    int tobaccoStep;
    int weedPlacements;
    int methMask;
    int mushroomMask;
    long cocaineStartTick = -1L;
}
