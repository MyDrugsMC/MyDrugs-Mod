package org.mydrugs.mydrugs.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.inner.v7.InnerLegacyInnerDimensionState;
import org.mydrugs.mydrugs.dimension.inner.v7.InnerV7Constants;
import org.mydrugs.mydrugs.dimension.inner.v7.InnerV7Migration;
import org.mydrugs.mydrugs.dimension.inner.v7.InnerV7State;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class InnerDimensionSavedData extends SavedData {
    public static final int INITIAL_RADIUS = 8;

    public static final Codec<IslandState> ISLAND_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("owner").forGetter(IslandState::ownerString),
            Codec.INT.fieldOf("center_x").forGetter(IslandState::centerX),
            Codec.INT.fieldOf("center_z").forGetter(IslandState::centerZ),
            Codec.INT.optionalFieldOf("current_radius", INITIAL_RADIUS).forGetter(IslandState::currentRadius),
            Codec.STRING.listOf().optionalFieldOf("integrated_drugs", List.of())
                    .forGetter(state -> state.integratedDrugs.stream().map(DrugId::serializedName).toList()),
            Codec.STRING.listOf().optionalFieldOf("generated_structures", List.of())
                    .forGetter(state -> List.copyOf(state.generatedStructures)),
            Codec.BOOL.optionalFieldOf("initial_island_built", false).forGetter(IslandState::initialIslandBuilt),
            Codec.BOOL.optionalFieldOf("dream_aligned", false).forGetter(IslandState::dreamAligned),
            Codec.BOOL.optionalFieldOf("has_dream_coordinate", false).forGetter(IslandState::hasDreamCoordinate),
            Codec.INT.optionalFieldOf("dream_x", 0).forGetter(IslandState::dreamX),
            Codec.INT.optionalFieldOf("dream_y", 0).forGetter(IslandState::dreamY),
            Codec.INT.optionalFieldOf("dream_z", 0).forGetter(IslandState::dreamZ),
            InnerLegacyInnerDimensionState.CODEC.optionalFieldOf("v6", InnerLegacyInnerDimensionState.EMPTY)
                    .forGetter(state -> InnerLegacyInnerDimensionState.EMPTY),
            InnerV7State.CODEC.optionalFieldOf("v7", InnerV7State.EMPTY).forGetter(IslandState::v7State)
    ).apply(instance, IslandState::new));

    public static final Codec<InnerDimensionSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("integrated_count", 0).forGetter(d -> 0),
            Codec.STRING.listOf().optionalFieldOf("integrated_drugs", List.of()).forGetter(d -> List.of()),
            Codec.STRING.listOf().optionalFieldOf("generated_structures", List.of()).forGetter(d -> List.of()),
            Codec.INT.optionalFieldOf("current_radius", INITIAL_RADIUS).forGetter(d -> INITIAL_RADIUS),
            Codec.BOOL.optionalFieldOf("initial_island_built", false).forGetter(d -> false),
            Codec.unboundedMap(Codec.STRING, ISLAND_CODEC).optionalFieldOf("player_islands", Map.of())
                    .forGetter(InnerDimensionSavedData::encodedIslands)
    ).apply(instance, InnerDimensionSavedData::new));

    private static final String DATA_ID = "mydrugs_inner_dimension";
    public static final SavedDataType<InnerDimensionSavedData> TYPE =
            new SavedDataType<>(DATA_ID, InnerDimensionSavedData::new, CODEC);

    private final Set<DrugId> legacyIntegratedDrugs;
    private final Set<String> legacyGeneratedStructures;
    private final Map<UUID, IslandState> islands = new LinkedHashMap<>();
    private final int legacyIntegratedCount;
    private final int legacyCurrentRadius;
    private final boolean legacyInitialIslandBuilt;

    public InnerDimensionSavedData() {
        this(0, List.of(), List.of(), INITIAL_RADIUS, false, Map.of());
    }

    private InnerDimensionSavedData(
            int integratedCount,
            List<String> integratedDrugs,
            List<String> generatedStructures,
            int currentRadius,
            boolean initialIslandBuilt,
            Map<String, IslandState> islands
    ) {
        this.legacyIntegratedCount = integratedCount;
        this.legacyIntegratedDrugs = integratedDrugs.stream()
                .map(DrugId::bySerializedNameOrNull)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        this.legacyGeneratedStructures = new LinkedHashSet<>(generatedStructures);
        this.legacyCurrentRadius = Math.max(INITIAL_RADIUS, currentRadius);
        this.legacyInitialIslandBuilt = initialIslandBuilt;
        for (Map.Entry<String, IslandState> entry : islands.entrySet()) {
            UUID owner = parseUuid(entry.getKey());
            if (owner == null) {
                owner = parseUuid(entry.getValue().ownerString());
            }
            if (owner != null) {
                this.islands.put(owner, entry.getValue().withOwner(owner));
            }
        }
    }

    public static InnerDimensionSavedData get(ServerLevel innerLevel) {
        return innerLevel.getDataStorage().computeIfAbsent(TYPE);
    }

    private static UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static int radiusDeltaFor(DrugId drugId) {
        return switch (drugId) {
            case COFFEE -> 12;
            case TOBACCO -> 12;
            case WEED -> 14;
            case HASH -> 14;
            case ALCOHOL -> 15;
            case COCAINE -> 16;
            case LSD -> 18;
            case METH -> 18;
            case MUSHROOMS -> 24;
            default -> 0;
        };
    }

    public static int radiusAfterIntegration(int beforeIntegrationsCount, DrugId drugId) {
        return radiusBefore(beforeIntegrationsCount) + radiusDeltaFor(drugId);
    }

    public static int radiusBefore(int integrationsCount) {
        int r = INITIAL_RADIUS;
        for (int i = 0; i < integrationsCount && i < CuratedDrugChain.ORDER.size(); i++) {
            r += radiusDeltaFor(CuratedDrugChain.ORDER.get(i));
        }
        return r;
    }

    public IslandState getOrCreateIsland(UUID playerId) {
        IslandState existing = islands.get(playerId);
        if (existing != null) {
            return existing;
        }

        int index = islands.size();
        int centerX = slotCenterX(index);
        int centerZ = slotCenterZ(index);
        IslandState created;
        if (islands.isEmpty() && hasLegacyProgress()) {
            created = IslandState.fromLegacy(
                    playerId,
                    centerX,
                    centerZ,
                    legacyCurrentRadius,
                    legacyIntegratedDrugs,
                    legacyGeneratedStructures,
                    legacyInitialIslandBuilt
            );
        } else {
            created = new IslandState(playerId, centerX, centerZ);
        }
        islands.put(playerId, created);
        setDirty();
        return created;
    }

    public boolean markInitialIslandBuilt(UUID playerId) {
        IslandState island = getOrCreateIsland(playerId);
        if (island.initialIslandBuilt) {
            return false;
        }
        island.initialIslandBuilt = true;
        setDirty();
        return true;
    }

    public boolean recordIntegration(UUID playerId, DrugId drugId, int newRadius) {
        IslandState island = getOrCreateIsland(playerId);
        if (drugId == null || !island.integratedDrugs.add(drugId)) {
            return false;
        }
        island.currentRadius = Math.max(island.currentRadius, newRadius);
        setDirty();
        return true;
    }

    public boolean markDreamAligned(UUID playerId, BlockPos resonatorPos) {
        IslandState island = getOrCreateIsland(playerId);
        island.dreamAligned = true;
        if (resonatorPos != null) {
            island.hasDreamCoordinate = true;
            island.dreamX = resonatorPos.getX();
            island.dreamY = resonatorPos.getY();
            island.dreamZ = resonatorPos.getZ();
        }
        setDirty();
        return true;
    }

    public boolean hasDreamAlignment(UUID playerId) {
        IslandState island = islands.get(playerId);
        return island != null && island.dreamAligned;
    }

    public boolean rescaleIslandRadius(UUID playerId) {
        IslandState island = getOrCreateIsland(playerId);
        int count = 0;
        int radius = INITIAL_RADIUS;
        for (DrugId drugId : CuratedDrugChain.ORDER) {
            if (!island.integratedDrugs.contains(drugId)) {
                continue;
            }
            radius = radiusAfterIntegration(count, drugId);
            count++;
        }
        if (island.currentRadius == radius) {
            return false;
        }
        island.currentRadius = radius;
        setDirty();
        return true;
    }

    public boolean markV7Generated(UUID playerId, String key) {
        IslandState island = getOrCreateIsland(playerId);
        if (key == null || key.isBlank() || !island.v7GeneratedKeys.add(key)) {
            return false;
        }
        island.v7Version = InnerV7Constants.VERSION;
        setDirty();
        return true;
    }

    public boolean markV7StructurePlaced(UUID playerId, String structureId) {
        IslandState island = getOrCreateIsland(playerId);
        if (structureId == null || structureId.isBlank() || !island.v7StructureMarkers.add(structureId)) {
            return false;
        }
        island.v7Version = InnerV7Constants.VERSION;
        setDirty();
        return true;
    }

    public boolean markV7MetricsSummary(UUID playerId, String summary) {
        IslandState island = getOrCreateIsland(playerId);
        String value = summary == null ? "" : summary;
        if (value.equals(island.v7MetricsSummary)) {
            return false;
        }
        island.v7MetricsSummary = value;
        island.v7Version = InnerV7Constants.VERSION;
        setDirty();
        return true;
    }

    public boolean clearV7Generated(UUID playerId) {
        IslandState island = getOrCreateIsland(playerId);
        if (island.v7GeneratedKeys.isEmpty()
                && island.v7StructureMarkers.isEmpty()
                && island.v7MetricsSummary.isBlank()) {
            return false;
        }
        island.v7GeneratedKeys.clear();
        island.v7StructureMarkers.clear();
        island.v7MetricsSummary = "";
        island.v7Version = InnerV7Constants.VERSION;
        setDirty();
        return true;
    }

    private boolean hasLegacyProgress() {
        return legacyInitialIslandBuilt
                || legacyCurrentRadius > INITIAL_RADIUS
                || legacyIntegratedCount > 0
                || !legacyIntegratedDrugs.isEmpty()
                || !legacyGeneratedStructures.isEmpty();
    }

    private Map<String, IslandState> encodedIslands() {
        Map<String, IslandState> out = new LinkedHashMap<>();
        for (Map.Entry<UUID, IslandState> entry : islands.entrySet()) {
            out.put(entry.getKey().toString(), entry.getValue());
        }
        return out;
    }

    private static int slotCenterX(int index) {
        return (index % 64) * InnerV7Constants.SLOT_SPACING;
    }

    private static int slotCenterZ(int index) {
        return (index / 64) * InnerV7Constants.SLOT_SPACING;
    }

    public static final class IslandState {
        private final int centerX;
        private final int centerZ;
        private final Set<DrugId> integratedDrugs;
        private final Set<String> generatedStructures;
        private final Set<String> v7GeneratedKeys;
        private final Set<String> v7StructureMarkers;
        private UUID owner;
        private int currentRadius;
        private boolean initialIslandBuilt;
        private boolean dreamAligned;
        private boolean hasDreamCoordinate;
        private int dreamX;
        private int dreamY;
        private int dreamZ;
        private String v7Version;
        private String v7MetricsSummary;
        private boolean v7MigratedFromLegacy;
        private int v7LegacyMarkerCount;

        private IslandState(UUID owner, int centerX, int centerZ) {
            this(owner.toString(), centerX, centerZ, INITIAL_RADIUS, List.of(), List.of(),
                    false, false, false, 0, 0, 0,
                    InnerLegacyInnerDimensionState.EMPTY,
                    new InnerV7State(InnerV7Constants.VERSION, centerX, centerZ, List.of(), List.of(), "", false, 0));
            this.owner = owner;
        }

        private IslandState(
                String owner,
                int centerX,
                int centerZ,
                int currentRadius,
                List<String> integratedDrugs,
                List<String> generatedStructures,
                boolean initialIslandBuilt,
                boolean dreamAligned,
                boolean hasDreamCoordinate,
                int dreamX,
                int dreamY,
                int dreamZ,
                InnerLegacyInnerDimensionState legacyState,
                InnerV7State v7State
        ) {
            this.owner = parseUuid(owner);
            InnerV7State migrated = InnerV7Migration.migrateIfNeeded(
                    centerX,
                    centerZ,
                    generatedStructures,
                    legacyState,
                    v7State
            );
            this.centerX = migrated.slotX();
            this.centerZ = migrated.slotZ();
            this.currentRadius = Math.max(INITIAL_RADIUS, currentRadius);
            this.integratedDrugs = integratedDrugs.stream()
                    .map(DrugId::bySerializedNameOrNull)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            this.generatedStructures = new HashSet<>(generatedStructures);
            this.initialIslandBuilt = initialIslandBuilt;
            this.dreamAligned = dreamAligned;
            this.hasDreamCoordinate = hasDreamCoordinate;
            this.dreamX = dreamX;
            this.dreamY = dreamY;
            this.dreamZ = dreamZ;
            this.v7Version = migrated.version().isBlank() ? InnerV7Constants.VERSION : migrated.version();
            this.v7GeneratedKeys = new LinkedHashSet<>(migrated.generatedKeys());
            this.v7StructureMarkers = new LinkedHashSet<>(migrated.structureMarkers());
            this.v7MetricsSummary = migrated.metricsSummary();
            this.v7MigratedFromLegacy = migrated.migratedFromLegacy();
            this.v7LegacyMarkerCount = migrated.legacyMarkerCount();
        }

        private static IslandState fromLegacy(UUID owner, int centerX, int centerZ, int currentRadius,
                                              Set<DrugId> integratedDrugs, Set<String> generatedStructures,
                                              boolean initialIslandBuilt) {
            IslandState state = new IslandState(owner, centerX, centerZ);
            state.currentRadius = Math.max(INITIAL_RADIUS, currentRadius);
            state.integratedDrugs.addAll(integratedDrugs);
            state.generatedStructures.addAll(generatedStructures);
            state.initialIslandBuilt = initialIslandBuilt;
            state.v7MigratedFromLegacy = true;
            state.v7LegacyMarkerCount = generatedStructures.size() + integratedDrugs.size();
            state.v7GeneratedKeys.add(InnerV7Constants.MIGRATION_KEY);
            return state;
        }

        private IslandState withOwner(UUID owner) {
            this.owner = owner;
            return this;
        }

        public UUID owner() {
            return owner;
        }

        public String ownerString() {
            return owner == null ? "" : owner.toString();
        }

        public int centerX() {
            return centerX;
        }

        public int centerZ() {
            return centerZ;
        }

        public int currentRadius() {
            return currentRadius;
        }

        public int integratedCount() {
            return integratedDrugs.size();
        }

        public Set<DrugId> integratedDrugs() {
            return Set.copyOf(integratedDrugs);
        }

        public boolean initialIslandBuilt() {
            return initialIslandBuilt;
        }

        public boolean dreamAligned() {
            return dreamAligned;
        }

        public boolean hasDreamCoordinate() {
            return hasDreamCoordinate;
        }

        public int dreamX() {
            return dreamX;
        }

        public int dreamY() {
            return dreamY;
        }

        public int dreamZ() {
            return dreamZ;
        }

        public String v7MetricsSummary() {
            return v7MetricsSummary;
        }

        public boolean v7MigratedFromLegacy() {
            return v7MigratedFromLegacy;
        }

        public Set<String> v7GeneratedKeys() {
            return Set.copyOf(v7GeneratedKeys);
        }

        public Set<String> v7StructureMarkers() {
            return Set.copyOf(v7StructureMarkers);
        }

        private InnerV7State v7State() {
            return new InnerV7State(
                    v7Version,
                    centerX,
                    centerZ,
                    List.copyOf(v7GeneratedKeys),
                    List.copyOf(v7StructureMarkers),
                    v7MetricsSummary,
                    v7MigratedFromLegacy,
                    v7LegacyMarkerCount
            );
        }
    }
}
