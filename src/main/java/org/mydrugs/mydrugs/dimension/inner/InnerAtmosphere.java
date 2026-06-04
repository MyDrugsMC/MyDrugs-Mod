package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import org.mydrugs.mydrugs.core.drug.DrugId;

public final class InnerAtmosphere {
    private InnerAtmosphere() {
    }

    public static Sample sample(int centerX, int centerZ, BlockPos pos) {
        InnerTerrain.Sample terrain = InnerTerrain.sample(centerX, centerZ, pos.getX(), pos.getZ());
        long seed = InnerTerrain.seedForSlot(centerX, centerZ);
        InnerGroveSample grove = InnerGroveSampler.sample(seed, centerX, centerZ, pos.getX(), pos.getZ(), terrain);
        InnerSceneSample scene = InnerSceneSampler.sample(seed, centerX, centerZ, pos.getX(), pos.getZ(), terrain, grove);
        double sanctuary = InnerNoise.clamp01(1.0D - terrain.distanceFromCenter() / 180.0D);
        double lakeMist = terrain.lakeStrength() * 0.34D + terrain.wetlandStrength() * 0.24D;
        double scarHaze = terrain.scarStrength() * 0.48D + terrain.holeStrength() * 0.22D;
        double pathClarity = InnerNoise.clamp01(terrain.pathStrength() * 0.42D + scene.openness() * 0.22D);
        double regionHaze = regionHaze(terrain.drugId());
        double danger = InnerNoise.clamp01(terrain.scarStrength()
                + scene.intensity() * 0.22D
                + (terrain.drugId() == DrugId.METH ? 0.18D : 0.0D)
                + (terrain.drugId() == DrugId.COCAINE ? 0.14D : 0.0D));
        double calm = InnerNoise.clamp01(sanctuary + (terrain.drugId() == DrugId.WEED ? 0.35D : 0.0D)
                + (terrain.drugId() == DrugId.COFFEE ? 0.18D : 0.0D)
                + (terrain.drugId() == DrugId.MUSHROOMS ? 0.16D : 0.0D)
                + scene.openness() * 0.16D);
        ColorHint color = colorFor(terrain.drugId(), sanctuary, danger, scene, lakeMist);
        double fogDensity = InnerNoise.clamp01(0.18D + regionHaze + lakeMist + scarHaze - sanctuary * 0.34D - pathClarity * 0.22D);
        double particleIntensity = InnerNoise.clamp01(0.05D
                + scene.intensity() * 0.22D
                + grove.groundDensity() * 0.12D
                + lakeMist * 0.22D
                + scarHaze * 0.20D
                - sanctuary * 0.12D);
        double glowBias = InnerNoise.clamp01(scene.glowMultiplier() / 2.0D + terrain.lakeCoreStrength() * 0.18D);
        return new Sample(
                terrain.drugId(),
                color.red(),
                color.green(),
                color.blue(),
                0.25D + danger * 0.55D,
                fogDensity,
                particleIntensity,
                glowBias,
                calm,
                danger,
                scene.type(),
                terrain.primaryDrug(),
                terrain.secondaryDrug(),
                terrain.transitionStrength()
        );
    }

    private static double regionHaze(DrugId drugId) {
        return switch (drugId) {
            case TOBACCO -> 0.12D;
            case ALCOHOL -> 0.18D;
            case LSD -> 0.10D;
            case METH -> 0.16D;
            case MUSHROOMS -> 0.20D;
            case COCAINE -> -0.03D;
            default -> 0.04D;
        };
    }

    private static ColorHint colorFor(DrugId drugId, double sanctuary, double danger, InnerSceneSample scene, double lakeMist) {
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
        if (scene.type() == InnerSceneType.PRISM_BASIN || scene.type() == InnerSceneType.CRYSTAL_CLEARING) {
            red = blend(red, 150, 0.18D + scene.glowMultiplier() * 0.06D);
            green = blend(green, 205, 0.16D);
            blue = blend(blue, 235, 0.22D);
        }
        if (lakeMist > 0.12D) {
            red = blend(red, 120, lakeMist * 0.30D);
            green = blend(green, 150, lakeMist * 0.24D);
            blue = blend(blue, 190, lakeMist * 0.34D);
        }
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
            double fogDensity,
            double particleIntensity,
            double glowBias,
            double calm,
            double danger,
            InnerSceneType sceneType,
            DrugId primaryDrug,
            DrugId secondaryDrug,
            double transitionStrength
    ) {
    }
}
