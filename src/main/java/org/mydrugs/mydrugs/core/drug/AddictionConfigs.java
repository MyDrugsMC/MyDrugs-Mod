package org.mydrugs.mydrugs.core.drug;

import java.util.EnumMap;

public final class AddictionConfigs {
    private static final EnumMap<DrugCategory, AddictionCategoryConfig> CONFIGS = new EnumMap<>(DrugCategory.class);

    static {
        final int MIN = 20 * 60;

        CONFIGS.put(DrugCategory.OPIOID, new AddictionCategoryConfig(
                3.5F,   // addictionRate
                1.00F,  // withdrawalIntensity
                0.045F, // toleranceGainRate
                0.00016F, // toleranceDecayPerSecond
                0.00060F, // addictionRecoveryPerSecond
                45 * 20,   // withdrawalOnsetTicks
                4 * MIN,   // risingTicks
                5 * MIN,   // peakTicks
                12 * MIN,  // recoveryTicks
                22.0F,     // reliefStrength
                0.82F      // relapseWeight
        ));

        CONFIGS.put(DrugCategory.STIMULANT, new AddictionCategoryConfig(
                3.1F,
                0.95F,
                0.040F,
                0.00020F,
                0.00070F,
                MIN,
                3 * MIN,
                4 * MIN,
                10 * MIN,
                20.0F,
                0.76F
        ));

        CONFIGS.put(DrugCategory.CANNABINOID, new AddictionCategoryConfig(
                1.8F,
                0.65F,
                0.025F,
                0.00030F,
                0.00100F,
                2 * MIN,
                4 * MIN,
                3 * MIN,
                8 * MIN,
                18.0F,
                0.40F
        ));

        CONFIGS.put(DrugCategory.PSYCHEDELIC, new AddictionCategoryConfig(
                1.0F,
                0.30F,
                0.018F,
                0.00038F,
                0.00120F,
                2 * MIN,
                3 * MIN,
                2 * MIN,
                6 * MIN,
                12.0F,
                0.18F
        ));

        CONFIGS.put(DrugCategory.DISSOCIATIVE, new AddictionCategoryConfig(
                1.9F,
                0.58F,
                0.026F,
                0.00028F,
                0.00095F,
                90 * 20,
                3 * MIN,
                3 * MIN,
                8 * MIN,
                16.0F,
                0.42F
        ));

        CONFIGS.put(DrugCategory.DEPRESSANT, new AddictionCategoryConfig(
                3.0F,
                0.92F,
                0.036F,
                0.00018F,
                0.00065F,
                90 * 20,
                5 * MIN,
                6 * MIN,
                14 * MIN,
                21.0F,
                0.72F
        ));

        CONFIGS.put(DrugCategory.EMPATHOGEN, new AddictionCategoryConfig(
                1.4F,
                0.45F,
                0.022F,
                0.00030F,
                0.00100F,
                90 * 20,
                2 * MIN,
                3 * MIN,
                7 * MIN,
                14.0F,
                0.28F
        ));

        CONFIGS.put(DrugCategory.DELIRIANT, new AddictionCategoryConfig(
                1.2F,
                0.55F,
                0.020F,
                0.00026F,
                0.00090F,
                2 * MIN,
                3 * MIN,
                4 * MIN,
                9 * MIN,
                13.0F,
                0.25F
        ));

        CONFIGS.put(DrugCategory.INHALANT, new AddictionCategoryConfig(
                1.7F,
                0.52F,
                0.024F,
                0.00027F,
                0.00090F,
                MIN,
                2 * MIN,
                2 * MIN,
                6 * MIN,
                15.0F,
                0.34F
        ));

        CONFIGS.put(DrugCategory.NICOTINIC, new AddictionCategoryConfig(
                2.8F,
                0.72F,
                0.034F,
                0.00022F,
                0.00080F,
                MIN,
                3 * MIN,
                4 * MIN,
                10 * MIN,
                17.0F,
                0.88F
        ));

        CONFIGS.put(DrugCategory.CAFFEINE, new AddictionCategoryConfig(
                1.9F,
                0.50F,
                0.025F,
                0.00028F,
                0.00100F,
                2 * MIN,
                3 * MIN,
                4 * MIN,
                9 * MIN,
                14.0F,
                0.52F
        ));

        CONFIGS.put(DrugCategory.NOOTROPIC, new AddictionCategoryConfig(
                0.9F,
                0.22F,
                0.016F,
                0.00040F,
                0.00130F,
                2 * MIN,
                2 * MIN,
                2 * MIN,
                5 * MIN,
                10.0F,
                0.16F
        ));

        CONFIGS.put(DrugCategory.RESEARCH_CHEMICAL, new AddictionCategoryConfig(
                2.5F,
                0.78F,
                0.032F,
                0.00021F,
                0.00075F,
                MIN,
                4 * MIN,
                4 * MIN,
                11 * MIN,
                18.0F,
                0.60F
        ));

        CONFIGS.put(DrugCategory.SEDATIVE, new AddictionCategoryConfig(
                2.8F,
                0.85F,
                0.032F,
                0.00022F,
                0.00080F,
                2 * MIN,
                5 * MIN,
                5 * MIN,
                12 * MIN,
                22.0F,
                0.66F
        ));

        CONFIGS.put(DrugCategory.MIXED, new AddictionCategoryConfig(
                2.6F,
                0.82F,
                0.033F,
                0.00020F,
                0.00072F,
                MIN,
                4 * MIN,
                5 * MIN,
                12 * MIN,
                18.0F,
                0.64F
        ));

        CONFIGS.put(DrugCategory.OTHER, new AddictionCategoryConfig(
                1.5F,
                0.45F,
                0.022F,
                0.00030F,
                0.00100F,
                2 * MIN,
                3 * MIN,
                3 * MIN,
                8 * MIN,
                14.0F,
                0.30F
        ));
    }

    private AddictionConfigs() {
    }

    public static AddictionCategoryConfig get(DrugCategory category) {
        return CONFIGS.getOrDefault(category, CONFIGS.get(DrugCategory.OTHER));
    }
}