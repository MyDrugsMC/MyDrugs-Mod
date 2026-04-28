package org.mydrugs.mydrugs.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;

/**
 * Selects a pipe side from the exact click position.
 *
 * Behavior:
 * - Clicking near the edge/extremity of the visible face selects that side.
 * - Clicking near the middle selects the actual clicked face.
 *
 * Example on a NORTH/SOUTH face:
 * - left/right area selects WEST/EAST
 * - bottom/top area selects DOWN/UP
 * - center selects NORTH/SOUTH
 */
public final class PipeSideSelector {
    private static final double EDGE_ZONE = 0.30D;

    private PipeSideSelector() {
    }

    public static Direction selectSide(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Vec3 hit = context.getClickLocation();

        double x = clamp01(hit.x - pos.getX());
        double y = clamp01(hit.y - pos.getY());
        double z = clamp01(hit.z - pos.getZ());

        return selectSide(context.getClickedFace(), x, y, z);
    }

    public static Direction selectSide(Direction clickedFace, double x, double y, double z) {
        Candidate best = Candidate.none(clickedFace);

        switch (clickedFace) {
            case NORTH, SOUTH -> {
                best = best.compare(edgeLow(x, Direction.WEST));
                best = best.compare(edgeHigh(x, Direction.EAST));
                best = best.compare(edgeLow(y, Direction.DOWN));
                best = best.compare(edgeHigh(y, Direction.UP));
            }
            case EAST, WEST -> {
                best = best.compare(edgeLow(z, Direction.NORTH));
                best = best.compare(edgeHigh(z, Direction.SOUTH));
                best = best.compare(edgeLow(y, Direction.DOWN));
                best = best.compare(edgeHigh(y, Direction.UP));
            }
            case UP, DOWN -> {
                best = best.compare(edgeLow(x, Direction.WEST));
                best = best.compare(edgeHigh(x, Direction.EAST));
                best = best.compare(edgeLow(z, Direction.NORTH));
                best = best.compare(edgeHigh(z, Direction.SOUTH));
            }
        }

        return best.direction();
    }

    private static Candidate edgeLow(double value, Direction direction) {
        if (value >= EDGE_ZONE) {
            return Candidate.miss();
        }
        return new Candidate(direction, EDGE_ZONE - value);
    }

    private static Candidate edgeHigh(double value, Direction direction) {
        if (value <= 1.0D - EDGE_ZONE) {
            return Candidate.miss();
        }
        return new Candidate(direction, value - (1.0D - EDGE_ZONE));
    }

    private static double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    private record Candidate(Direction direction, double strength) {
        static Candidate none(Direction fallback) {
            return new Candidate(fallback, 0.0D);
        }

        static Candidate miss() {
            return new Candidate(null, -1.0D);
        }

        Candidate compare(Candidate other) {
            if (other.direction == null) {
                return this;
            }
            return other.strength > this.strength ? other : this;
        }
    }
}