package org.mydrugs.mydrugs.dimension.inner.v7;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class InnerV7Migration {
    private InnerV7Migration() {
    }

    public static InnerV7State migrateIfNeeded(
            int slotX,
            int slotZ,
            List<String> legacyGeneratedStructures,
            InnerLegacyInnerDimensionState legacyState,
            InnerV7State decodedV7
    ) {
        InnerV7State safeV7 = decodedV7 == null ? InnerV7State.EMPTY : decodedV7;
        if (safeV7.hasData()) {
            return safeV7;
        }

        InnerLegacyInnerDimensionState safeLegacy =
                legacyState == null ? InnerLegacyInnerDimensionState.EMPTY : legacyState;
        int legacyCount = safeLegacy.markerCount() + legacyGeneratedStructures.size();
        if (legacyCount == 0) {
            return new InnerV7State(
                    InnerV7Constants.VERSION,
                    slotX,
                    slotZ,
                    List.of(),
                    List.of(),
                    "",
                    false,
                    0
            );
        }

        Set<String> keys = new LinkedHashSet<>();
        keys.add(InnerV7Constants.MIGRATION_KEY);
        for (String key : safeLegacy.generatedKeys()) {
            if (key != null && !key.isBlank()) {
                keys.add("legacy:" + key);
            }
        }
        for (String key : legacyGeneratedStructures) {
            if (key != null && !key.isBlank()) {
                keys.add("legacy_structure:" + key);
            }
        }

        return new InnerV7State(
                InnerV7Constants.VERSION,
                slotX,
                slotZ,
                List.copyOf(keys),
                safeLegacy.structureMarkers(),
                safeLegacy.metricsSummary(),
                true,
                legacyCount
        );
    }
}
