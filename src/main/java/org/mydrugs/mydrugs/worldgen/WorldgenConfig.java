package org.mydrugs.mydrugs.worldgen;

import java.util.Locale;

import org.mydrugs.mydrugs.Config;

public final class WorldgenConfig {
    public static final String SALT = "salt";
    public static final String SULFUR_ORE = "sulfur_ore";
    public static final String PLATINUM_ORE = "platinum_ore";
    public static final String ALUMINIUM_ORE = "aluminium_ore";
    public static final String PHOSPHATE_ORE = "phosphate_ore";
    public static final String PETROLEUM_LAKE = "petroleum_lake";
    public static final String ALOE_VERA = "aloe_vera";
    public static final String LAVENDER = "lavender";
    public static final String VALERIAN = "valerian";
    public static final String BITTER_NUT_BUSH = "bitter_nut_bush";
    public static final String THIRD_EYE_PETAL = "third_eye_petal";
    public static final String EPHEDRA = "ephedra";
    public static final String PSYCHEDELIC_MUSHROOMS = "psychedelic_mushrooms";

    private WorldgenConfig() {
    }

    public static boolean terraBlenderOverworldEnabled() {
        return Config.WORLDGEN.enableWorldgen.get()
                && Config.WORLDGEN.enableOverworldBiomes.get()
                && Config.WORLDGEN.psychedelicBiomeWeight.get() > 0
                && (Config.WORLDGEN.replaceMushroomFields.get()
                || Config.WORLDGEN.addPsychedelicBiomeSeparately.get());
    }

    public static int psychedelicBiomeWeight() {
        return Math.max(0, Config.WORLDGEN.psychedelicBiomeWeight.get());
    }

    public static String psychedelicClimateBands() {
        String configured = Config.WORLDGEN.psychedelicBiomeClimateBands.get();
        String normalized = configured == null ? "" : configured.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "mushroom_only", "warm_wet", "broad_wet" -> normalized;
            default -> "mushroom_only";
        };
    }

    public static boolean surfaceRulesEnabled() {
        return Config.WORLDGEN.enableWorldgen.get()
                && Config.WORLDGEN.enableOverworldBiomes.get()
                && Config.WORLDGEN.enableCustomSurfaceRules.get();
    }

    public static boolean featureEnabled(String key) {
        if (!Config.WORLDGEN.enableWorldgen.get()) {
            return false;
        }
        return switch (key) {
            case SALT -> true;
            case SULFUR_ORE -> Config.WORLDGEN.enableSulfurOre.get();
            case PLATINUM_ORE -> Config.WORLDGEN.enablePlatinumOre.get();
            case ALUMINIUM_ORE -> Config.WORLDGEN.enableAluminiumOre.get();
            case PHOSPHATE_ORE -> Config.WORLDGEN.enablePhosphateOre.get();
            case PETROLEUM_LAKE -> Config.WORLDGEN.enablePetroleumLakes.get();
            case ALOE_VERA -> Config.WORLDGEN.enableAloeVera.get();
            case LAVENDER -> Config.WORLDGEN.enableLavender.get();
            case VALERIAN -> Config.WORLDGEN.enableValerian.get();
            case BITTER_NUT_BUSH -> Config.WORLDGEN.enableBitterNutBush.get();
            case THIRD_EYE_PETAL -> Config.WORLDGEN.enableThirdEyePetal.get();
            case EPHEDRA -> Config.WORLDGEN.enableEphedra.get();
            case PSYCHEDELIC_MUSHROOMS -> Config.WORLDGEN.enableOverworldBiomes.get()
                    && Config.WORLDGEN.enablePsychedelicMushrooms.get();
            default -> true;
        };
    }

    public static int orderedMinHeight(int minHeight, int maxHeight) {
        return Math.min(minHeight, maxHeight);
    }

    public static int orderedMaxHeight(int minHeight, int maxHeight) {
        return Math.max(minHeight, maxHeight);
    }
}
