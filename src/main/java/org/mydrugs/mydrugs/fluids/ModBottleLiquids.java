package org.mydrugs.mydrugs.fluids;

import net.minecraft.resources.ResourceLocation;

public final class ModBottleLiquids {
    public static final ResourceLocation AMMONIAC = ModFluids.rl("ammoniac");
    public static final ResourceLocation WATER = ResourceLocation.withDefaultNamespace("water");
    public static final ResourceLocation BLOOD = ModFluids.rl("blood");

    private ModBottleLiquids() {
    }

    public static int getArgb(ResourceLocation liquidId, int fallbackRgb) {
        int rgb;
        if (AMMONIAC.equals(liquidId)) {
            rgb = 0xCFE17A;
        } else if (WATER.equals(liquidId)) {
            rgb = 0x3F76E4;
        } else if (BLOOD.equals(liquidId)) {
            rgb = 0x8E1B1B;
        } else {
            rgb = fallbackRgb;
        }

        return 0xFF000000 | (rgb & 0x00FFFFFF);
    }

    public static String getDisplayName(ResourceLocation liquidId) {
        if (AMMONIAC.equals(liquidId)) {
            return "Ammoniac";
        }
        if (WATER.equals(liquidId)) {
            return "Water";
        }
        if (BLOOD.equals(liquidId)) {
            return "Blood";
        }
        return liquidId.toString();
    }
}