package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import org.mydrugs.mydrugs.core.drug.DrugId;

public final class InnerAtmosphere {
    private InnerAtmosphere() {
    }

    public static Sample sample(int centerX, int centerZ, BlockPos pos) {
        InnerTerrain.Sample terrain = InnerTerrain.sample(centerX, centerZ, pos.getX(), pos.getZ());
        double sanctuary = InnerNoise.clamp01(1.0D - terrain.distanceFromCenter() / 180.0D);
        double danger = InnerNoise.clamp01(terrain.scarStrength() + (terrain.drugId() == DrugId.METH ? 0.18D : 0.0D)
                + (terrain.drugId() == DrugId.COCAINE ? 0.14D : 0.0D));
        double calm = InnerNoise.clamp01(sanctuary + (terrain.drugId() == DrugId.WEED ? 0.35D : 0.0D)
                + (terrain.drugId() == DrugId.COFFEE ? 0.18D : 0.0D)
                + (terrain.drugId() == DrugId.MUSHROOMS ? 0.16D : 0.0D));
        ColorHint color = colorFor(terrain.drugId(), sanctuary, danger);
        return new Sample(terrain.drugId(), color.red(), color.green(), color.blue(), 0.25D + danger * 0.55D, calm, danger);
    }

    private static ColorHint colorFor(DrugId drugId, double sanctuary, double danger) {
        int red = switch (drugId) {
            case COFFEE -> 202;
            case TOBACCO -> 150;
            case WEED -> 118;
            case HASH -> 190;
            case ALCOHOL -> 105;
            case COCAINE -> 228;
            case LSD -> 120;
            case METH -> 82;
            case MUSHROOMS -> 130;
            default -> 160;
        };
        int green = switch (drugId) {
            case COFFEE -> 170;
            case TOBACCO -> 135;
            case WEED -> 190;
            case HASH -> 180;
            case ALCOHOL -> 120;
            case COCAINE -> 78;
            case LSD -> 200;
            case METH -> 82;
            case MUSHROOMS -> 165;
            default -> 150;
        };
        int blue = switch (drugId) {
            case COFFEE -> 118;
            case TOBACCO -> 105;
            case WEED -> 130;
            case HASH -> 215;
            case ALCOHOL -> 145;
            case COCAINE -> 82;
            case LSD -> 230;
            case METH -> 105;
            case MUSHROOMS -> 160;
            default -> 150;
        };
        red = blend(red, 210, sanctuary);
        green = blend(green, 220, sanctuary);
        blue = blend(blue, 210, sanctuary);
        red = blend(red, 170, danger * 0.5D);
        green = blend(green, 60, danger * 0.5D);
        blue = blend(blue, 65, danger * 0.5D);
        return new ColorHint(red, green, blue);
    }

    private static int blend(int from, int to, double amount) {
        double clamped = InnerNoise.clamp01(amount);
        return (int) Math.round(from + (to - from) * clamped);
    }

    private record ColorHint(int red, int green, int blue) {
    }

    public record Sample(
            DrugId dominantDrug,
            int fogRed,
            int fogGreen,
            int fogBlue,
            double intensity,
            double calm,
            double danger
    ) {
    }
}
