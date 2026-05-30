package org.mydrugs.mydrugs.dimension.inner.v7;

import net.minecraft.core.BlockPos;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

import java.util.List;

public final class InnerV7RegionMap {
    private static final List<DrugId> ORDER = CuratedDrugChain.ORDER;
    private static final double TWO_PI = Math.PI * 2.0D;

    private InnerV7RegionMap() {
    }

    public static DrugId dominantDrug(int centerX, int centerZ, int worldX, int worldZ) {
        double angle = normalizedAngle(Math.atan2(worldZ - centerZ, worldX - centerX));
        int index = Math.floorMod((int) Math.floor(angle / TWO_PI * ORDER.size()), ORDER.size());
        return ORDER.get(index);
    }

    public static double angleFor(DrugId drugId) {
        int index = ORDER.indexOf(drugId);
        if (index < 0) {
            index = 0;
        }
        return (index + 0.5D) / ORDER.size() * TWO_PI;
    }

    public static BlockPos landmarkFor(int centerX, int centerZ, DrugId drugId) {
        double angle = angleFor(drugId);
        int radius = 520 + 38 * ORDER.indexOf(drugId);
        int x = centerX + (int) Math.round(Math.cos(angle) * radius);
        int z = centerZ + (int) Math.round(Math.sin(angle) * radius);
        return new BlockPos(x, InnerV7Constants.BASE_Y + 22, z);
    }

    public static double angularDistance(double a, double b) {
        double d = Math.abs(normalizedAngle(a) - normalizedAngle(b));
        return Math.min(d, TWO_PI - d);
    }

    private static double normalizedAngle(double angle) {
        double out = angle % TWO_PI;
        return out < 0.0D ? out + TWO_PI : out;
    }
}
