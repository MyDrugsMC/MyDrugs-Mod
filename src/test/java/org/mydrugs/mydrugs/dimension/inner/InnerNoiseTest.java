package org.mydrugs.mydrugs.dimension.inner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnerNoiseTest {

    @Test
    void valueNoiseIsDeterministicAndInRange() {
        for (int i = 0; i < 500; i++) {
            int x = i * 37 - 9000;
            int z = i * -53 + 4000;
            double a = InnerNoise.value(1234L, x, z);
            double b = InnerNoise.value(1234L, x, z);
            assertEquals(a, b, "value noise must be deterministic");
            assertTrue(a >= -1.0D && a <= 1.0D, "value noise out of [-1,1]: " + a);
        }
    }

    @Test
    void fbmIsDeterministicAndBounded() {
        for (int i = 0; i < 300; i++) {
            double x = i * 11.5D;
            double z = i * -7.25D;
            double a = InnerNoise.fbm(99L, x, z, 120.0D, 5);
            double b = InnerNoise.fbm(99L, x, z, 120.0D, 5);
            assertEquals(a, b, 0.0D, "fbm must be deterministic");
            assertTrue(a >= -1.0001D && a <= 1.0001D, "fbm out of range: " + a);
        }
    }

    @Test
    void ridgedIsInUnitRange() {
        for (int i = 0; i < 300; i++) {
            double r = InnerNoise.ridged(7L, i * 3.0D, i * 5.0D, 64.0D, 3);
            assertTrue(r >= -0.0001D && r <= 1.0001D, "ridged out of [0,1]: " + r);
        }
    }

    @Test
    void clamp01Clamps() {
        assertEquals(0.0D, InnerNoise.clamp01(-5.0D));
        assertEquals(1.0D, InnerNoise.clamp01(5.0D));
        assertEquals(0.5D, InnerNoise.clamp01(0.5D));
    }

    @Test
    void lerpInterpolates() {
        assertEquals(5.0D, InnerNoise.lerp(0.0D, 10.0D, 0.5D));
        assertEquals(0.0D, InnerNoise.lerp(0.0D, 10.0D, 0.0D));
        assertEquals(10.0D, InnerNoise.lerp(0.0D, 10.0D, 1.0D));
    }
}
