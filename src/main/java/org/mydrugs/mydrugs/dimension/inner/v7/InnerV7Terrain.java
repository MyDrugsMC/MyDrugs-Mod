package org.mydrugs.mydrugs.dimension.inner.v7;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;

public final class InnerV7Terrain {
    private InnerV7Terrain() {
    }

    public static Sample sample(int worldX, int worldZ) {
        int centerX = slotCenter(worldX);
        int centerZ = slotCenter(worldZ);
        double dx = worldX - centerX;
        double dz = worldZ - centerZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        double angle = Math.atan2(dz, dx);
        long seed = InnerV7Constants.BASE_SEED
                ^ (long) centerX * 341873128712L
                ^ (long) centerZ * 132897987541L;

        DrugId drugId = InnerV7RegionMap.dominantDrug(centerX, centerZ, worldX, worldZ);
        double coastWarp = InnerV7Noise.fbm(seed, worldX, worldZ, 220.0D, 4) * 92.0D;
        double cliffBreak = InnerV7Noise.ridged(seed + 13L, worldX, worldZ, 90.0D, 3) * 34.0D;
        double density = InnerV7Constants.ISLAND_RADIUS - distance + coastWarp - cliffBreak;

        double scar = scarStrength(seed, dx, dz, angle, distance);
        density -= scar * 96.0D;

        Satellite satellite = satellite(seed, centerX, centerZ, worldX, worldZ);
        boolean satelliteLand = satellite.density() > 0.0D;
        boolean mainLand = density > 0.0D;
        boolean path = pathStrength(distance, angle, drugId) > 0.0D && density > -42.0D;
        boolean land = mainLand || satelliteLand || path;

        double heightNoise = InnerV7Noise.fbm(seed + 29L, worldX, worldZ, 150.0D, 5);
        double ridge = InnerV7Noise.ridged(seed + 31L, worldX, worldZ, 72.0D, 3);
        int topY = InnerV7Constants.BASE_Y
                + (int) Math.round(heightNoise * 22.0D + ridge * 14.0D)
                + heightBias(drugId);
        if (path) {
            topY = InnerV7Constants.BASE_Y + 3 + (int) Math.round(heightNoise * 3.0D);
        }
        if (satelliteLand && !mainLand) {
            topY = satellite.topY();
        }
        if (distance < InnerV7Constants.CORE_RADIUS) {
            topY = InnerV7Constants.BASE_Y + 4;
        }

        int thickness = 18 + (int) Math.round(InnerV7Noise.ridged(seed + 41L, worldX, worldZ, 65.0D, 3) * 24.0D);
        if (path) {
            thickness = Math.max(8, thickness / 2);
        }
        int bottomY = Math.max(InnerV7Constants.MIN_Y + 4, topY - thickness);
        return new Sample(land, topY, bottomY, drugId, path, scar > 0.45D, satelliteLand && !mainLand, density);
    }

    public static boolean chunkMayHaveLand(int chunkMinX, int chunkMinZ) {
        int centerX = slotCenter(chunkMinX + 8);
        int centerZ = slotCenter(chunkMinZ + 8);
        int farthestX = Math.max(Math.abs(chunkMinX - centerX), Math.abs(chunkMinX + 15 - centerX));
        int farthestZ = Math.max(Math.abs(chunkMinZ - centerZ), Math.abs(chunkMinZ + 15 - centerZ));
        double farthest = Math.sqrt((double) farthestX * farthestX + (double) farthestZ * farthestZ);
        return farthest <= InnerV7Constants.ISLAND_RADIUS + InnerV7Constants.SATELLITE_REACH + 128;
    }

    public static BlockState stateFor(Sample sample, int y) {
        if (sample.path() && y == sample.topY()) {
            return sample.drugId() == DrugId.MUSHROOMS
                    ? Blocks.ROOTED_DIRT.defaultBlockState()
                    : Blocks.DIRT_PATH.defaultBlockState();
        }
        if (sample.scar() && y >= sample.topY() - 2) {
            return switch (sample.drugId()) {
                case COCAINE -> Blocks.REDSTONE_BLOCK.defaultBlockState();
                case METH -> Blocks.MAGMA_BLOCK.defaultBlockState();
                case LSD -> Blocks.TINTED_GLASS.defaultBlockState();
                default -> Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState();
            };
        }
        if (y == sample.topY()) {
            return surface(sample.drugId());
        }
        if (y >= sample.topY() - 3) {
            return subsurface(sample.drugId());
        }
        return deep(sample.drugId());
    }

    public static boolean caveAir(Sample sample, int worldX, int y, int worldZ) {
        if (sample.path() || y > sample.topY() - 5 || y < sample.bottomY() + 4) {
            return false;
        }
        long seed = InnerV7Constants.BASE_SEED + sample.drugId().ordinal() * 101L;
        double cave = InnerV7Noise.ridged(seed, worldX, worldZ + y * 5.0D, 46.0D, 3);
        return cave > 0.82D;
    }

    private static int slotCenter(int coordinate) {
        return (int) Math.round((double) coordinate / InnerV7Constants.SLOT_SPACING) * InnerV7Constants.SLOT_SPACING;
    }

    private static double scarStrength(long seed, double dx, double dz, double angle, double distance) {
        double strength = 0.0D;
        for (int i = 0; i < 5; i++) {
            double scarAngle = i * Math.PI * 0.4D + InnerV7Noise.value(seed + 71L, i, 0) * 0.25D;
            double angular = InnerV7RegionMap.angularDistance(angle, scarAngle);
            double radialBand = Math.sin(distance / (150.0D + i * 17.0D));
            double line = Math.max(0.0D, 1.0D - angular / 0.035D) * Math.max(0.0D, 0.65D + radialBand);
            strength = Math.max(strength, line);
        }
        double cross = Math.abs(Math.sin((dx * 0.006D) + InnerV7Noise.smoothValue(seed + 73L, dx, dz, 240.0D)));
        return Math.max(strength, cross > 0.985D && distance > 360.0D ? 0.7D : 0.0D);
    }

    private static double pathStrength(double distance, double angle, DrugId drugId) {
        if (distance < 92.0D) {
            return 1.0D;
        }
        if (Math.abs(distance - 270.0D) < 7.0D || Math.abs(distance - 640.0D) < 6.0D) {
            return 1.0D;
        }
        double regionAngle = InnerV7RegionMap.angleFor(drugId);
        return InnerV7RegionMap.angularDistance(angle, regionAngle) < 0.018D
                && distance > 80.0D
                && distance < 1160.0D ? 1.0D : 0.0D;
    }

    private static Satellite satellite(long seed, int centerX, int centerZ, int worldX, int worldZ) {
        double bestDensity = -1000.0D;
        int bestTop = InnerV7Constants.BASE_Y + 18;
        for (int i = 0; i < 7; i++) {
            double angle = (i + 0.35D) / 7.0D * Math.PI * 2.0D;
            double radius = 1320.0D + InnerV7Noise.value(seed + 91L, i, 0) * 120.0D;
            int sx = centerX + (int) Math.round(Math.cos(angle) * radius);
            int sz = centerZ + (int) Math.round(Math.sin(angle) * radius);
            double dx = worldX - sx;
            double dz = worldZ - sz;
            double distance = Math.sqrt(dx * dx + dz * dz);
            double size = 86.0D + InnerV7Noise.value(seed + 93L, i, 0) * 28.0D;
            double density = size - distance + InnerV7Noise.fbm(seed + i * 19L, worldX, worldZ, 55.0D, 3) * 26.0D;
            if (density > bestDensity) {
                bestDensity = density;
                bestTop = InnerV7Constants.BASE_Y + 22 + (int) Math.round(InnerV7Noise.value(seed + 97L, i, 0) * 12.0D);
            }
        }
        return new Satellite(bestDensity, bestTop);
    }

    private static int heightBias(DrugId drugId) {
        return switch (drugId) {
            case COFFEE -> -4;
            case TOBACCO -> -1;
            case WEED -> -2;
            case HASH -> 6;
            case ALCOHOL -> -8;
            case COCAINE -> 4;
            case LSD -> 16;
            case METH -> 2;
            case MUSHROOMS -> 0;
            default -> 0;
        };
    }

    private static BlockState surface(DrugId drugId) {
        return switch (drugId) {
            case COFFEE -> Blocks.GRASS_BLOCK.defaultBlockState();
            case TOBACCO -> Blocks.TUFF.defaultBlockState();
            case WEED -> Blocks.MOSS_BLOCK.defaultBlockState();
            case HASH -> Blocks.CALCITE.defaultBlockState();
            case ALCOHOL -> Blocks.MUD.defaultBlockState();
            case COCAINE -> Blocks.SMOOTH_QUARTZ.defaultBlockState();
            case LSD -> Blocks.PRISMARINE.defaultBlockState();
            case METH -> Blocks.BLACKSTONE.defaultBlockState();
            case MUSHROOMS -> Blocks.MYCELIUM.defaultBlockState();
            default -> Blocks.STONE.defaultBlockState();
        };
    }

    private static BlockState subsurface(DrugId drugId) {
        return switch (drugId) {
            case COFFEE -> Blocks.DIRT.defaultBlockState();
            case TOBACCO -> Blocks.STONE_BRICKS.defaultBlockState();
            case WEED -> Blocks.ROOTED_DIRT.defaultBlockState();
            case HASH -> Blocks.AMETHYST_BLOCK.defaultBlockState();
            case ALCOHOL -> Blocks.DEEPSLATE.defaultBlockState();
            case COCAINE -> Blocks.WHITE_CONCRETE.defaultBlockState();
            case LSD -> Blocks.CALCITE.defaultBlockState();
            case METH -> Blocks.BASALT.defaultBlockState();
            case MUSHROOMS -> Blocks.MUSHROOM_STEM.defaultBlockState();
            default -> Blocks.STONE.defaultBlockState();
        };
    }

    private static BlockState deep(DrugId drugId) {
        return switch (drugId) {
            case TOBACCO -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            case HASH -> Blocks.SMOOTH_BASALT.defaultBlockState();
            case ALCOHOL -> Blocks.DEEPSLATE_TILES.defaultBlockState();
            case COCAINE -> Blocks.QUARTZ_BLOCK.defaultBlockState();
            case LSD -> Blocks.SCULK.defaultBlockState();
            case METH -> Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState();
            case MUSHROOMS -> Blocks.ROOTED_DIRT.defaultBlockState();
            default -> Blocks.STONE.defaultBlockState();
        };
    }

    private record Satellite(double density, int topY) {
    }

    public record Sample(
            boolean land,
            int topY,
            int bottomY,
            DrugId drugId,
            boolean path,
            boolean scar,
            boolean satellite,
            double density
    ) {
    }
}
