package org.mydrugs.mydrugs.dimension.inner.v7;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record InnerLegacyInnerDimensionState(
        String version,
        boolean enabled,
        boolean migratedFromPrevious,
        List<String> generatedKeys,
        List<String> structureMarkers,
        String metricsSummary
) {
    public static final InnerLegacyInnerDimensionState EMPTY =
            new InnerLegacyInnerDimensionState("", false, false, List.of(), List.of(), "");

    public static final Codec<InnerLegacyInnerDimensionState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("version", "").forGetter(InnerLegacyInnerDimensionState::version),
            Codec.BOOL.optionalFieldOf("enabled", false).forGetter(InnerLegacyInnerDimensionState::enabled),
            Codec.BOOL.optionalFieldOf("migrated_from_v5", false).forGetter(InnerLegacyInnerDimensionState::migratedFromPrevious),
            Codec.STRING.listOf().optionalFieldOf("generated_keys", List.of()).forGetter(InnerLegacyInnerDimensionState::generatedKeys),
            Codec.STRING.listOf().optionalFieldOf("structure_markers", List.of()).forGetter(InnerLegacyInnerDimensionState::structureMarkers),
            Codec.STRING.optionalFieldOf("metrics_summary", "").forGetter(InnerLegacyInnerDimensionState::metricsSummary)
    ).apply(instance, InnerLegacyInnerDimensionState::new));

    public InnerLegacyInnerDimensionState {
        version = version == null ? "" : version;
        generatedKeys = List.copyOf(generatedKeys);
        structureMarkers = List.copyOf(structureMarkers);
        metricsSummary = metricsSummary == null ? "" : metricsSummary;
    }

    public int markerCount() {
        return generatedKeys.size() + structureMarkers.size() + (version.isBlank() ? 0 : 1);
    }
}
