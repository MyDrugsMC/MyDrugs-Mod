package org.mydrugs.mydrugs.fluids;

public final class ModBottleLiquids {
    public static final String AMMONIAC = "mydrugs:ammoniac";
    public static final String WATER = "minecraft:water";
    public static final String BLOOD = "mydrugs:blood";

    private ModBottleLiquids() {}

    public static int getArgb(String liquidId, int fallbackRgb) {
        int rgb = switch (liquidId) {
            case AMMONIAC -> 0xCFE17A;
            case WATER -> 0x3F76E4;
            case BLOOD -> 0x8E1B1B;
            default -> fallbackRgb;
        };

        return 0xFF000000 | (rgb & 0x00FFFFFF);
    }

    public static String getDisplayName(String liquidId) {
        return switch (liquidId) {
            case AMMONIAC -> "Ammoniac";
            case WATER -> "Water";
            case BLOOD -> "Blood";
            default -> liquidId;
        };
    }
}