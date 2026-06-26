package org.mydrugs.mydrugs.energy.psycurrent;

import net.minecraft.core.BlockPos;

import java.util.List;

/**
 * Server-computed summary of every Psy Current candidate around a Psychotrope Engine. Used by the
 * engine GUI to show target counts, by the area preview to color each block, and by the inspection
 * tool to report network health.
 *
 * <p>{@code valid}, {@code full}, and {@code incompatible} hold immutable, defensively copied
 * snapshots of block positions; callers can ship them straight to the client.
 */
public record PsyCurrentTargetScan(
        List<BlockPos> valid,
        List<BlockPos> full,
        List<BlockPos> incompatible,
        int totalReceivable
) {
    public static final PsyCurrentTargetScan EMPTY =
            new PsyCurrentTargetScan(List.of(), List.of(), List.of(), 0);

    public PsyCurrentTargetScan {
        valid = List.copyOf(valid);
        full = List.copyOf(full);
        incompatible = List.copyOf(incompatible);
        totalReceivable = Math.max(0, totalReceivable);
    }

    public int validCount() {
        return this.valid.size();
    }

    public int fullCount() {
        return this.full.size();
    }

    public int incompatibleCount() {
        return this.incompatible.size();
    }
}
