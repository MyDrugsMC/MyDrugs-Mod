package org.mydrugs.mydrugs.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The persistent island state (Phase G.3).
 *
 * Owns the source of truth for: how many drugs the player base has integrated, which structures
 * have already been placed (so re-entry / expansion never regenerates them), and the current
 * island radius. Stored on the inner {@link ServerLevel} via {@link #get}.
 */
public final class InnerDimensionSavedData extends SavedData {
    private static final String DATA_ID = "mydrugs_inner_dimension";

    /** Starting radius of the barren rock (Phase G acceptance: ~3-radius blob at zero integrations). */
    public static final int INITIAL_RADIUS = 3;

    public static final Codec<InnerDimensionSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("integrated_count", 0)
                    .forGetter(d -> d.integratedCount),
            Codec.STRING.listOf().optionalFieldOf("integrated_drugs", List.of())
                    .forGetter(d -> d.integratedDrugs.stream().map(DrugId::serializedName).toList()),
            Codec.STRING.listOf().optionalFieldOf("generated_structures", List.of())
                    .forGetter(d -> List.copyOf(d.generatedStructures)),
            Codec.INT.optionalFieldOf("current_radius", INITIAL_RADIUS)
                    .forGetter(d -> d.currentRadius),
            Codec.BOOL.optionalFieldOf("initial_island_built", false)
                    .forGetter(d -> d.initialIslandBuilt)
    ).apply(instance, InnerDimensionSavedData::new));

    public static final SavedDataType<InnerDimensionSavedData> TYPE =
            new SavedDataType<>(DATA_ID, InnerDimensionSavedData::new, CODEC);

    private int integratedCount;
    private final Set<DrugId> integratedDrugs;
    private final Set<String> generatedStructures;
    private int currentRadius;
    private boolean initialIslandBuilt;

    public InnerDimensionSavedData() {
        this(0, List.of(), List.of(), INITIAL_RADIUS, false);
    }

    private InnerDimensionSavedData(
            int integratedCount,
            List<String> integratedDrugs,
            List<String> generatedStructures,
            int currentRadius,
            boolean initialIslandBuilt
    ) {
        this.integratedCount = integratedCount;
        this.integratedDrugs = integratedDrugs.stream()
                .map(DrugId::bySerializedNameOrNull)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        this.generatedStructures = new HashSet<>(generatedStructures);
        this.currentRadius = Math.max(INITIAL_RADIUS, currentRadius);
        this.initialIslandBuilt = initialIslandBuilt;
    }

    public static InnerDimensionSavedData get(ServerLevel innerLevel) {
        return innerLevel.getDataStorage().computeIfAbsent(TYPE);
    }

    public int integratedCount() {
        return this.integratedCount;
    }

    public Set<DrugId> integratedDrugs() {
        return Set.copyOf(this.integratedDrugs);
    }

    public int currentRadius() {
        return this.currentRadius;
    }

    public boolean initialIslandBuilt() {
        return this.initialIslandBuilt;
    }

    public boolean isStructureGenerated(String structureId) {
        return structureId != null && this.generatedStructures.contains(structureId);
    }

    public void markInitialIslandBuilt() {
        if (!this.initialIslandBuilt) {
            this.initialIslandBuilt = true;
            setDirty();
        }
    }

    /**
     * Records a new integration. Returns true if this drug was not already integrated for the
     * dimension — the caller uses that to decide whether to expand the island.
     */
    public boolean recordIntegration(DrugId drugId, int newRadius) {
        if (drugId == null || !this.integratedDrugs.add(drugId)) {
            return false;
        }
        this.integratedCount = this.integratedDrugs.size();
        this.currentRadius = Math.max(this.currentRadius, newRadius);
        setDirty();
        return true;
    }

    /**
     * Records a structure as generated. Returns true if it was newly recorded (caller should place
     * blocks); false if it was already present and must be skipped.
     */
    public boolean markStructureGenerated(String structureId) {
        if (structureId == null || !this.generatedStructures.add(structureId)) {
            return false;
        }
        setDirty();
        return true;
    }

    /**
     * Phase G chain order radius adds: COFFEE,TOBACCO,WEED,HASH +6; ALCOHOL +6; COCAINE,LSD,METH +8;
     * MUSHROOMS +10. See §3.3 of the master plan.
     */
    public static int radiusAfterIntegration(int beforeIntegrationsCount, DrugId drugId) {
        int delta = switch (drugId) {
            case COFFEE, TOBACCO, WEED, HASH, ALCOHOL -> 6;
            case COCAINE, LSD, METH -> 8;
            case MUSHROOMS -> 10;
            default -> 0;
        };
        return radiusBefore(beforeIntegrationsCount) + delta;
    }

    public static int radiusBefore(int integrationsCount) {
        int r = INITIAL_RADIUS;
        for (int i = 0; i < integrationsCount && i < CuratedDrugChain.ORDER.size(); i++) {
            DrugId d = CuratedDrugChain.ORDER.get(i);
            r += switch (d) {
                case COFFEE, TOBACCO, WEED, HASH, ALCOHOL -> 6;
                case COCAINE, LSD, METH -> 8;
                case MUSHROOMS -> 10;
                default -> 0;
            };
        }
        return r;
    }
}
