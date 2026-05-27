package org.mydrugs.mydrugs.recovery;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class SanctuaryModuleDetector {
    private static final int MAX_SUGGESTIONS = 3;

    private SanctuaryModuleDetector() {
    }

    public static Set<SanctuaryModule> detect(SanctuaryModuleScan scan, boolean valid, RecoveryRoomTier tier) {
        if (scan == null || !valid || tier == null || !tier.isValidRoom()) {
            return Set.of();
        }

        EnumSet<SanctuaryModule> modules = EnumSet.noneOf(SanctuaryModule.class);

        if (scan.beds() > 0 && scan.hasSafeLight() && scan.dangerFree()) {
            modules.add(SanctuaryModule.REST_MODULE);
        }
        if ((scan.lecterns() > 0 || scan.bookshelves() >= 2)
                && scan.bookshelves() + scan.lecterns() >= 2
                && scan.seats() + scan.tableLikeBlocks() > 0) {
            modules.add(SanctuaryModule.DIARY_DESK);
        }
        if (scan.musicBlocks() > 0) {
            modules.add(SanctuaryModule.MUSIC_CORNER);
        }
        if (scan.plants() >= 3) {
            modules.add(SanctuaryModule.PLANT_BREATHING_CORNER);
        }
        if ((scan.cauldrons() > 0 && scan.teaHeatSources() > 0)
                || (scan.teaHeatSources() > 0 && scan.teaStorageBlocks() > 0 && scan.plants() > 0)) {
            modules.add(SanctuaryModule.TEA_KITCHEN);
        }
        if (scan.memoryDisplays() >= 2 || (scan.memoryDisplays() >= 1 && scan.bookshelves() > 0)) {
            modules.add(SanctuaryModule.MEMORY_WALL);
        }
        if (tier == RecoveryRoomTier.SANCTUARY
                && scan.dangerFree()
                && scan.integrationMarkers() > 0
                && scan.lecterns() + scan.bookshelves() + scan.memoryDisplays() > 0) {
            modules.add(SanctuaryModule.INTEGRATION_ALCOVE);
        }

        return modules.isEmpty() ? Set.of() : EnumSet.copyOf(modules);
    }

    public static List<String> suggestionKeys(Set<SanctuaryModule> modules, boolean valid, RecoveryRoomTier tier) {
        if (!valid || tier == null || !tier.isValidRoom()) {
            return List.of();
        }

        List<String> suggestions = new ArrayList<>();
        addMissing(suggestions, modules, SanctuaryModule.REST_MODULE);
        if (tier.ordinal() >= RecoveryRoomTier.RESTING_ROOM.ordinal()) {
            addMissing(suggestions, modules, SanctuaryModule.DIARY_DESK);
            addMissing(suggestions, modules, SanctuaryModule.PLANT_BREATHING_CORNER);
        }
        if (tier.ordinal() >= RecoveryRoomTier.SAFE_ROOM.ordinal()) {
            addMissing(suggestions, modules, SanctuaryModule.MUSIC_CORNER);
            addMissing(suggestions, modules, SanctuaryModule.TEA_KITCHEN);
        }
        if (tier == RecoveryRoomTier.SANCTUARY) {
            addMissing(suggestions, modules, SanctuaryModule.MEMORY_WALL);
            addMissing(suggestions, modules, SanctuaryModule.INTEGRATION_ALCOVE);
        }

        return suggestions.size() <= MAX_SUGGESTIONS
                ? List.copyOf(suggestions)
                : List.copyOf(suggestions.subList(0, MAX_SUGGESTIONS));
    }

    private static void addMissing(List<String> suggestions, Set<SanctuaryModule> modules, SanctuaryModule module) {
        if (modules == null || !modules.contains(module)) {
            suggestions.add(module.suggestionKey());
        }
    }
}
