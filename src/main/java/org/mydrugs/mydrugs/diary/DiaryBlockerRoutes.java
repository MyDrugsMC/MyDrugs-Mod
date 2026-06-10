package org.mydrugs.mydrugs.diary;

import java.util.List;

/**
 * Maps authoritative machine and Resonator statuses to diary-safe blocker ids
 * and player-facing routing metadata.
 */
public final class DiaryBlockerRoutes {
    public static final String MISSING_CATALYST = "machine_missing_catalyst";
    public static final String MISSING_CONTAINER = "machine_missing_container";
    public static final String MISSING_DIARY_CONTEXT = "machine_missing_diary_context";
    public static final String MISSING_INPUT_FLUID = "machine_missing_input_fluid";
    public static final String MISSING_INPUT_GAS = "machine_missing_input_gas";
    public static final String MISSING_INPUT_ITEM = "machine_missing_input_item";
    public static final String MISSING_RECOVERY_CONTEXT = "machine_missing_recovery_context";
    public static final String MISSING_DREAM_RESIDUE = "resonator_missing_dream_residue";
    public static final String MISSING_DRUG_KNOWLEDGE = "resonator_missing_drug_knowledge";

    private static final List<Route> ROUTES = List.of(
            route(MISSING_CATALYST, "machine_status.mydrugs.missing_catalyst",
                    "Machine Handbook - Chemical Machine Era", "mydrugs:catalyst_bed"),
            route(MISSING_CONTAINER, "machine_status.mydrugs.missing_container",
                    "Machine Handbook - Early Processing", "minecraft:bucket"),
            route(MISSING_DIARY_CONTEXT, "machine_status.mydrugs.missing_diary_context",
                    "The Diary", "mydrugs:personal_diary"),
            route(MISSING_INPUT_FLUID, "machine_status.mydrugs.missing_input_fluid",
                    "Machine Handbook", "minecraft:bucket"),
            route(MISSING_INPUT_GAS, "machine_status.mydrugs.missing_input_gas",
                    "Lab Chemistry", "mydrugs:gas_tank"),
            route(MISSING_INPUT_ITEM, "machine_status.mydrugs.missing_input_item",
                    "Machine Handbook", ""),
            route(MISSING_RECOVERY_CONTEXT, "machine_status.mydrugs.missing_recovery_context",
                    "Recovery Sanctuary", "mydrugs:recovery_anchor"),
            route(MISSING_DREAM_RESIDUE, "message.mydrugs.resonator.missing_dream_residue",
                    "Psychotrope Resonator", "mydrugs:dream_residue"),
            route(MISSING_DRUG_KNOWLEDGE, "message.mydrugs.resonator.missing_drug_knowledge",
                    "Knowledge Spine", "")
    );

    private DiaryBlockerRoutes() {
    }

    public static Route fromSourceKey(String sourceKey) {
        if (sourceKey == null || sourceKey.isBlank()) {
            return null;
        }
        for (Route route : ROUTES) {
            if (route.sourceKey().equals(sourceKey)) {
                return route;
            }
        }
        return null;
    }

    public static Route fromBlockerType(String blockerType) {
        if (blockerType == null || blockerType.isBlank()) {
            return null;
        }
        for (Route route : ROUTES) {
            if (route.blockerType().equals(blockerType)) {
                return route;
            }
        }
        return null;
    }

    public static boolean isRoutedType(String blockerType) {
        return fromBlockerType(blockerType) != null;
    }

    public static List<Route> all() {
        return ROUTES;
    }

    private static Route route(String blockerType, String sourceKey, String guidePage, String iconItemId) {
        return new Route(
                blockerType,
                sourceKey,
                "diary.mydrugs.blocker.route." + blockerType,
                guidePage,
                iconItemId
        );
    }

    public record Route(
            String blockerType,
            String sourceKey,
            String routeTextKey,
            String guidePage,
            String iconItemId
    ) {
    }
}
