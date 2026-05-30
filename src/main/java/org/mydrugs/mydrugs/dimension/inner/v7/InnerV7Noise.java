package org.mydrugs.mydrugs.dimension.inner.v7;

public final class InnerV7Noise {
    private InnerV7Noise() {
    }

    public static long mix64(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return value;
    }

    public static double value(long seed, int x, int z) {
        long mixed = mix64(seed + x * 0x9E3779B97F4A7C15L + z * 0xC2B2AE3D27D4EB4FL);
        return ((mixed >>> 11) * 0x1.0p-53) * 2.0D - 1.0D;
    }

    public static double smoothValue(long seed, double x, double z, double scale) {
        double sx = x / scale;
        double sz = z / scale;
        int x0 = fastFloor(sx);
        int z0 = fastFloor(sz);
        double tx = fade(sx - x0);
        double tz = fade(sz - z0);
        double a = lerp(value(seed, x0, z0), value(seed, x0 + 1, z0), tx);
        double b = lerp(value(seed, x0, z0 + 1), value(seed, x0 + 1, z0 + 1), tx);
        return lerp(a, b, tz);
    }

    public static double fbm(long seed, double x, double z, double scale, int octaves) {
        double sum = 0.0D;
        double amp = 0.5D;
        double total = 0.0D;
        double currentScale = scale;
        for (int i = 0; i < octaves; i++) {
            sum += smoothValue(seed + i * 341873128712L, x, z, currentScale) * amp;
            total += amp;
            amp *= 0.5D;
            currentScale *= 0.5D;
        }
        return total == 0.0D ? 0.0D : sum / total;
    }

    public static double ridged(long seed, double x, double z, double scale, int octaves) {
        double n = fbm(seed, x, z, scale, octaves);
        return 1.0D - Math.abs(n);
    }

    public static int fastFloor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6.0D - 15.0D) + 10.0D);
    }
}
