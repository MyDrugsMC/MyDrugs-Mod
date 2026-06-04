package org.mydrugs.mydrugs.core.drug.integration;

import org.mydrugs.mydrugs.core.drug.DrugId;

import java.util.EnumMap;
import java.util.Map;

public final class IntegrationRequirements {
    private static final Map<DrugId, IntegrationRequirementProfile> PROFILES = new EnumMap<>(DrugId.class);

    static {
        put(new IntegrationRequirementProfile(DrugId.COFFEE, IntegrationRequirementType.ADDICTION_RECOVERY,
                9.0F, 6.0F, 14.0F, 1.0F, 0, true));
        put(new IntegrationRequirementProfile(DrugId.TOBACCO, IntegrationRequirementType.ADDICTION_RECOVERY,
                35.0F, 8.0F, 35.0F, 1.0F, 0, true));
        put(new IntegrationRequirementProfile(DrugId.WEED, IntegrationRequirementType.ADDICTION_RECOVERY,
                45.0F, 8.0F, 35.0F, 1.0F, 0, true));
        put(new IntegrationRequirementProfile(DrugId.HASH, IntegrationRequirementType.ADDICTION_RECOVERY,
                50.0F, 8.0F, 30.0F, 1.0F, 0, true));
        put(new IntegrationRequirementProfile(DrugId.ALCOHOL, IntegrationRequirementType.ADDICTION_RECOVERY,
                40.0F, 8.0F, 30.0F, 1.0F, 0, true));
        put(new IntegrationRequirementProfile(DrugId.COCAINE, IntegrationRequirementType.ADDICTION_RECOVERY,
                60.0F, 8.0F, 20.0F, 1.0F, 0, true));
        put(new IntegrationRequirementProfile(DrugId.LSD, IntegrationRequirementType.CLEAN_PSYCHEDELIC_STREAK,
                0.0F, 0.0F, 0.0F, 0.0F, 5, false,
                1, 1, IntegrationConstants.PSYCHEDELIC_BAD_TRIP_BLOCK_TICKS));
        put(new IntegrationRequirementProfile(DrugId.METH, IntegrationRequirementType.ADDICTION_RECOVERY,
                60.0F, 8.0F, 18.0F, 1.0F, 0, true));
        put(new IntegrationRequirementProfile(DrugId.MUSHROOMS, IntegrationRequirementType.CLEAN_PSYCHEDELIC_STREAK,
                0.0F, 0.0F, 0.0F, 0.0F, 5, false,
                1, 1, IntegrationConstants.PSYCHEDELIC_BAD_TRIP_BLOCK_TICKS));
    }

    private IntegrationRequirements() {
    }

    private static void put(IntegrationRequirementProfile profile) {
        PROFILES.put(profile.drugId(), profile);
    }

    public static IntegrationRequirementProfile profile(DrugId drugId) {
        return PROFILES.get(drugId);
    }

    public static boolean isCurated(DrugId drugId) {
        return PROFILES.containsKey(drugId);
    }

    public static boolean usesCleanDoseStreak(DrugId drugId) {
        IntegrationRequirementProfile profile = profile(drugId);
        return profile != null && profile.usesCleanDoseStreak();
    }
}
