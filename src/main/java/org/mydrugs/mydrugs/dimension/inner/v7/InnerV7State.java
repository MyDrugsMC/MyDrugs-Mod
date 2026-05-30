package org.mydrugs.mydrugs.dimension.inner.v7;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record InnerV7State(
        String version,
        int slotX,
        int slotZ,
        List<String> generatedKeys,
        List<String> structureMarkers,
        String metricsSummary,
        boolean migratedFromLegacy,
        int legacyMarkerCount
) {
    public static final InnerV7State EMPTY =
            new InnerV7State("", 0, 0, List.of(), List.of(), "", false, 0);

    public static final Codec<InnerV7State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("version", "").forGetter(InnerV7State::version),
            Codec.INT.optionalFieldOf("slot_x", 0).forGetter(InnerV7State::slotX),
            Codec.INT.optionalFieldOf("slot_z", 0).forGetter(InnerV7State::slotZ),
            Codec.STRING.listOf().optionalFieldOf("generated_keys", List.of()).forGetter(InnerV7State::generatedKeys),
            Codec.STRING.listOf().optionalFieldOf("structure_markers", List.of()).forGetter(InnerV7State::structureMarkers),
            Codec.STRING.optionalFieldOf("metrics_summary", "").forGetter(InnerV7State::metricsSummary),
            Codec.BOOL.optionalFieldOf("migrated_from_legacy", false).forGetter(InnerV7State::migratedFromLegacy),
            Codec.INT.optionalFieldOf("legacy_marker_count", 0).forGetter(InnerV7State::legacyMarkerCount)
    ).apply(instance, InnerV7State::new));

    public InnerV7State {
        version = version == null ? "" : version;
        generatedKeys = List.copyOf(generatedKeys);
        structureMarkers = List.copyOf(structureMarkers);
        metricsSummary = metricsSummary == null ? "" : metricsSummary;
        legacyMarkerCount = Math.max(0, legacyMarkerCount);
    }

    public boolean hasData() {
        return !version.isBlank()
                || !generatedKeys.isEmpty()
                || !structureMarkers.isEmpty()
                || migratedFromLegacy
                || legacyMarkerCount > 0;
    }
}
