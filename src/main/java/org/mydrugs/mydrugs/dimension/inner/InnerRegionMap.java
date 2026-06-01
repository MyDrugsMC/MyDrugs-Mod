package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

import java.util.List;

public final class InnerRegionMap {
    private static final List<DrugId> ORDER = CuratedDrugChain.ORDER;
    private static final double TWO_PI = Math.PI * 2.0D;

    // Angular half-width of the transition band on either side of a sector boundary.
    private static final double BLEND_BAND = 0.22D;

    private InnerRegionMap() {
    }

    /**
     * The two regions nearest an angle and how strongly the secondary bleeds into the primary.
     * Outside the transition band {@code secondaryWeight} is 0 (pure primary); at a sector
     * boundary it reaches 0.5 (equal mingling). {@code primaryWeight + secondaryWeight == 1}.
     */
    public record RegionBlend(DrugId primary, DrugId secondary, double secondaryWeight) {
        public double primaryWeight() {
            return 1.0D - secondaryWeight;
        }
    }

    public static RegionBlend regionBlend(double angle) {
        double normalized = normalizedAngle(angle);
        int n = ORDER.size();
        double sectorSize = TWO_PI / n;
        int i = Math.floorMod((int) Math.floor(normalized / sectorSize), n);
        double center = (i + 0.5D) * sectorSize;
        double offset = normalized - center;
        if (offset > Math.PI) {
            offset -= TWO_PI;
        } else if (offset < -Math.PI) {
            offset += TWO_PI;
        }
        int neighbor = Math.floorMod(offset >= 0.0D ? i + 1 : i - 1, n);
        double distToBoundary = sectorSize / 2.0D - Math.abs(offset);
        double secondaryWeight = 0.0D;
        if (distToBoundary < BLEND_BAND) {
            double t = InnerNoise.clamp01(distToBoundary / BLEND_BAND);
            secondaryWeight = 0.5D * (1.0D - smoothstep(t));
        }
        return new RegionBlend(ORDER.get(i), ORDER.get(neighbor), secondaryWeight);
    }

    private static double smoothstep(double t) {
        return t * t * (3.0D - 2.0D * t);
    }

    public static List<DrugId> regionOrder() {
        return ORDER;
    }

    public static DrugId dominantDrug(int centerX, int centerZ, int worldX, int worldZ) {
        return dominantDrugForAngle(Math.atan2(worldZ - centerZ, worldX - centerX));
    }

    /**
     * Dominant region for an already-computed (possibly domain-warped) angle.
     * Single source of the sector-to-drug mapping; {@link #dominantDrug} delegates here.
     */
    public static DrugId dominantDrugForAngle(double angle) {
        double normalized = normalizedAngle(angle);
        int index = Math.floorMod((int) Math.floor(normalized / TWO_PI * ORDER.size()), ORDER.size());
        return ORDER.get(index);
    }

    public static boolean hasAngle(DrugId drugId) {
        return ORDER.contains(drugId);
    }

    public static double angleFor(DrugId drugId) {
        int index = ORDER.indexOf(drugId);
        if (index < 0) {
            index = 0;
        }
        return (index + 0.5D) / ORDER.size() * TWO_PI;
    }

    public static BlockPos landmarkFor(int centerX, int centerZ, DrugId drugId) {
        int index = Math.max(0, ORDER.indexOf(drugId));
        double angle = angleFor(drugId);
        int radius = InnerDimensionConstants.LANDMARK_BASE_RADIUS
                + InnerDimensionConstants.LANDMARK_RADIUS_STEP * index;
        int x = centerX + (int) Math.round(Math.cos(angle) * radius);
        int z = centerZ + (int) Math.round(Math.sin(angle) * radius);
        return new BlockPos(x, InnerDimensionConstants.BASE_Y + 24, z);
    }

    public static double angularDistance(double a, double b) {
        double d = Math.abs(normalizedAngle(a) - normalizedAngle(b));
        return Math.min(d, TWO_PI - d);
    }

    public static double normalizedAngle(double angle) {
        double out = angle % TWO_PI;
        return out < 0.0D ? out + TWO_PI : out;
    }
}
