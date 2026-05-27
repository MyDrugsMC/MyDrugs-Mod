package org.mydrugs.mydrugs.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class InnerDimensionSavedData extends SavedData {
    private static final String DATA_ID = "mydrugs_inner_dimension";
    private static final int ISLAND_SPACING = 512;

    public static final int INITIAL_RADIUS = 3;

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
            Codec.INT.optionalFieldOf("dream_z", 0).forGetter(IslandState::dreamZ)
    ).apply(instance, IslandState::new));

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
                    .forGetter(d -> d.initialIslandBuilt),
            Codec.unboundedMap(Codec.STRING, ISLAND_CODEC).optionalFieldOf("player_islands", Map.of())
                    .forGetter(InnerDimensionSavedData::encodedIslands)
    ).apply(instance, InnerDimensionSavedData::new));

    public static final SavedDataType<InnerDimensionSavedData> TYPE =
            new SavedDataType<>(DATA_ID, InnerDimensionSavedData::new, CODEC);

    private int integratedCount;
    private final Set<DrugId> integratedDrugs;
    private final Set<String> generatedStructures;
    private int currentRadius;
    private boolean initialIslandBuilt;
    private final Map<UUID, IslandState> islands = new LinkedHashMap<>();

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
        this.integratedCount = integratedCount;
        this.integratedDrugs = integratedDrugs.stream()
                .map(DrugId::bySerializedNameOrNull)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        this.generatedStructures = new HashSet<>(generatedStructures);
        this.currentRadius = Math.max(INITIAL_RADIUS, currentRadius);
        this.initialIslandBuilt = initialIslandBuilt;
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

    public boolean recordIntegration(DrugId drugId, int newRadius) {
        if (drugId == null || !this.integratedDrugs.add(drugId)) {
            return false;
        }
        this.integratedCount = this.integratedDrugs.size();
        this.currentRadius = Math.max(this.currentRadius, newRadius);
        setDirty();
        return true;
    }

    public boolean markStructureGenerated(String structureId) {
        if (structureId == null || !this.generatedStructures.add(structureId)) {
            return false;
        }
        setDirty();
        return true;
    }

    public IslandState getOrCreateIsland(UUID playerId) {
        IslandState existing = islands.get(playerId);
        if (existing != null) {
            return existing;
        }

        IslandState created;
        if (islands.isEmpty() && hasLegacyProgress()) {
            created = IslandState.fromLegacy(playerId, 0, 0, currentRadius, integratedDrugs,
                    generatedStructures, initialIslandBuilt);
        } else {
            int index = islands.size();
            int centerX = (index % 64) * ISLAND_SPACING;
            int centerZ = (index / 64) * ISLAND_SPACING;
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

    public boolean isStructureGenerated(UUID playerId, String structureId) {
        IslandState island = getOrCreateIsland(playerId);
        return structureId != null && island.generatedStructures.contains(structureId);
    }

    public boolean markStructureGenerated(UUID playerId, String structureId) {
        IslandState island = getOrCreateIsland(playerId);
        if (structureId == null || !island.generatedStructures.add(structureId)) {
            return false;
        }
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

    private boolean hasLegacyProgress() {
        return initialIslandBuilt
                || currentRadius > INITIAL_RADIUS
                || !integratedDrugs.isEmpty()
                || !generatedStructures.isEmpty();
    }

    private Map<String, IslandState> encodedIslands() {
        Map<String, IslandState> out = new LinkedHashMap<>();
        for (Map.Entry<UUID, IslandState> entry : islands.entrySet()) {
            out.put(entry.getKey().toString(), entry.getValue());
        }
        return out;
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

    public static final class IslandState {
        private UUID owner;
        private final int centerX;
        private final int centerZ;
        private int currentRadius;
        private final Set<DrugId> integratedDrugs;
        private final Set<String> generatedStructures;
        private boolean initialIslandBuilt;
        private boolean dreamAligned;
        private boolean hasDreamCoordinate;
        private int dreamX;
        private int dreamY;
        private int dreamZ;

        private IslandState(UUID owner, int centerX, int centerZ) {
            this(owner.toString(), centerX, centerZ, INITIAL_RADIUS, List.of(), List.of(),
                    false, false, false, 0, 0, 0);
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
                int dreamZ
        ) {
            this.owner = parseUuid(owner);
            this.centerX = centerX;
            this.centerZ = centerZ;
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
        }

        private static IslandState fromLegacy(UUID owner, int centerX, int centerZ, int currentRadius,
                                              Set<DrugId> integratedDrugs, Set<String> generatedStructures,
                                              boolean initialIslandBuilt) {
            IslandState state = new IslandState(owner, centerX, centerZ);
            state.currentRadius = Math.max(INITIAL_RADIUS, currentRadius);
            state.integratedDrugs.addAll(integratedDrugs);
            state.generatedStructures.addAll(generatedStructures);
            state.initialIslandBuilt = initialIslandBuilt;
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
    }
}
