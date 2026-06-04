package org.mydrugs.mydrugs.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.inner.InnerDimensionConstants;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class InnerDimensionSavedData extends SavedData {
    public static final Codec<IslandState> ISLAND_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("owner").forGetter(IslandState::ownerString),
            Codec.INT.fieldOf("center_x").forGetter(IslandState::centerX),
            Codec.INT.fieldOf("center_z").forGetter(IslandState::centerZ),
            Codec.STRING.listOf().optionalFieldOf("integrated_drugs", List.of())
                    .forGetter(state -> state.integratedDrugs.stream().map(DrugId::serializedName).toList()),
            Codec.STRING.listOf().optionalFieldOf("placed_markers", List.of())
                    .forGetter(state -> List.copyOf(state.placedMarkers)),
            Codec.BOOL.optionalFieldOf("initial_island_built", false).forGetter(IslandState::initialIslandBuilt),
            Codec.BOOL.optionalFieldOf("dream_aligned", false).forGetter(IslandState::dreamAligned),
            Codec.BOOL.optionalFieldOf("has_dream_coordinate", false).forGetter(IslandState::hasDreamCoordinate),
            Codec.INT.optionalFieldOf("dream_x", 0).forGetter(IslandState::dreamX),
            Codec.INT.optionalFieldOf("dream_y", 0).forGetter(IslandState::dreamY),
            Codec.INT.optionalFieldOf("dream_z", 0).forGetter(IslandState::dreamZ),
            Codec.INT.optionalFieldOf("overlay_schema", InnerDimensionConstants.OVERLAY_SCHEMA_VERSION)
                    .forGetter(IslandState::overlaySchemaVersion),
            Codec.STRING.optionalFieldOf("last_overlay_metrics", "").forGetter(IslandState::lastOverlayMetricsSummary),
            // Explicit slot index so a removed island can never make a survivor's slot collide.
            // Default -1 = legacy data; the constructor then derives it from the stored centers.
            Codec.INT.optionalFieldOf("slot_index", -1).forGetter(IslandState::slotIndex)
    ).apply(instance, IslandState::new));

    public static final Codec<InnerDimensionSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, ISLAND_CODEC).optionalFieldOf("player_islands", Map.of())
                    .forGetter(InnerDimensionSavedData::encodedIslands)
    ).apply(instance, InnerDimensionSavedData::new));

    private static final String DATA_ID = "mydrugs_inner_dimension";
    public static final SavedDataType<InnerDimensionSavedData> TYPE =
            new SavedDataType<>(DATA_ID, InnerDimensionSavedData::new, CODEC);

    private final Map<UUID, IslandState> islands = new LinkedHashMap<>();

    public InnerDimensionSavedData() {
        this(Map.of());
    }

    private InnerDimensionSavedData(Map<String, IslandState> islands) {
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

    public IslandState getOrCreateIsland(UUID playerId) {
        IslandState existing = islands.get(playerId);
        if (existing != null) {
            return existing;
        }

        IslandState created = new IslandState(playerId, nextFreeSlotIndex());
        islands.put(playerId, created);
        setDirty();
        return created;
    }

    /** Smallest non-negative slot index not already in use, robust to islands having been removed. */
    private int nextFreeSlotIndex() {
        Set<Integer> used = new LinkedHashSet<>();
        for (IslandState island : islands.values()) {
            used.add(island.slotIndex());
        }
        int index = 0;
        while (used.contains(index)) {
            index++;
        }
        return index;
    }

    /**
     * Find the already-existing island assigned to a given slot center, or {@code null}. Does not
     * create islands, used by lazy chunk-load decoration to discover a chunk's owner.
     */
    public IslandState findIslandBySlot(int centerX, int centerZ) {
        for (IslandState island : islands.values()) {
            if (island.centerX() == centerX && island.centerZ() == centerZ) {
                return island;
            }
        }
        return null;
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

    public boolean recordIntegration(UUID playerId, DrugId drugId) {
        IslandState island = getOrCreateIsland(playerId);
        if (drugId == null || !island.integratedDrugs.add(drugId)) {
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

    public boolean markStructurePlaced(UUID playerId, String marker) {
        IslandState island = getOrCreateIsland(playerId);
        if (marker == null || marker.isBlank() || !island.placedMarkers.add(marker)) {
            return false;
        }
        island.overlaySchemaVersion = InnerDimensionConstants.OVERLAY_SCHEMA_VERSION;
        setDirty();
        return true;
    }

    public boolean updateOverlayMetrics(UUID playerId, String summary) {
        IslandState island = getOrCreateIsland(playerId);
        String value = summary == null ? "" : summary;
        if (value.equals(island.lastOverlayMetricsSummary)) {
            return false;
        }
        island.lastOverlayMetricsSummary = value;
        island.overlaySchemaVersion = InnerDimensionConstants.OVERLAY_SCHEMA_VERSION;
        setDirty();
        return true;
    }

    public boolean clearOverlayMarkers(UUID playerId) {
        IslandState island = getOrCreateIsland(playerId);
        if (island.placedMarkers.isEmpty() && island.lastOverlayMetricsSummary.isBlank()) {
            return false;
        }
        island.placedMarkers.clear();
        island.lastOverlayMetricsSummary = "";
        island.overlaySchemaVersion = InnerDimensionConstants.OVERLAY_SCHEMA_VERSION;
        setDirty();
        return true;
    }

    private Map<String, IslandState> encodedIslands() {
        Map<String, IslandState> out = new LinkedHashMap<>();
        for (Map.Entry<UUID, IslandState> entry : islands.entrySet()) {
            out.put(entry.getKey().toString(), entry.getValue());
        }
        return out;
    }

    private static int slotCenterX(int index) {
        return (index % 64) * InnerDimensionConstants.SLOT_SPACING;
    }

    private static int slotCenterZ(int index) {
        return (index / 64) * InnerDimensionConstants.SLOT_SPACING;
    }

    /** Reverse of slotCenterX/Z: recover a slot index from legacy data that stored only centers. */
    private static int slotIndexFromCenter(int centerX, int centerZ) {
        int col = Math.floorDiv(centerX, InnerDimensionConstants.SLOT_SPACING);
        int row = Math.floorDiv(centerZ, InnerDimensionConstants.SLOT_SPACING);
        return row * 64 + col;
    }

    public static final class IslandState {
        private final int slotIndex;
        private final int centerX;
        private final int centerZ;
        private final Set<DrugId> integratedDrugs;
        private final Set<String> placedMarkers;
        private UUID owner;
        private boolean initialIslandBuilt;
        private boolean dreamAligned;
        private boolean hasDreamCoordinate;
        private int dreamX;
        private int dreamY;
        private int dreamZ;
        private int overlaySchemaVersion;
        private String lastOverlayMetricsSummary;

        private IslandState(UUID owner, int slotIndex) {
            this(
                    owner.toString(),
                    slotCenterX(slotIndex),
                    slotCenterZ(slotIndex),
                    List.of(),
                    List.of(),
                    false,
                    false,
                    false,
                    0,
                    0,
                    0,
                    InnerDimensionConstants.OVERLAY_SCHEMA_VERSION,
                    "",
                    slotIndex
            );
            this.owner = owner;
        }

        private IslandState(
                String owner,
                int centerX,
                int centerZ,
                List<String> integratedDrugs,
                List<String> placedMarkers,
                boolean initialIslandBuilt,
                boolean dreamAligned,
                boolean hasDreamCoordinate,
                int dreamX,
                int dreamY,
                int dreamZ,
                int overlaySchemaVersion,
                String lastOverlayMetricsSummary,
                int slotIndex
        ) {
            this.owner = parseUuid(owner);
            this.slotIndex = slotIndex >= 0 ? slotIndex : slotIndexFromCenter(centerX, centerZ);
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.integratedDrugs = integratedDrugs.stream()
                    .map(DrugId::bySerializedNameOrNull)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            this.placedMarkers = placedMarkers.stream()
                    .filter(marker -> marker != null && !marker.isBlank())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            this.initialIslandBuilt = initialIslandBuilt;
            this.dreamAligned = dreamAligned;
            this.hasDreamCoordinate = hasDreamCoordinate;
            this.dreamX = dreamX;
            this.dreamY = dreamY;
            this.dreamZ = dreamZ;
            this.overlaySchemaVersion = Math.max(1, overlaySchemaVersion);
            this.lastOverlayMetricsSummary = lastOverlayMetricsSummary == null ? "" : lastOverlayMetricsSummary;
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

        public int slotIndex() {
            return slotIndex;
        }

        public int centerX() {
            return centerX;
        }

        public int centerZ() {
            return centerZ;
        }

        public int integratedCount() {
            return integratedDrugs.size();
        }

        public Set<DrugId> integratedDrugs() {
            return Set.copyOf(integratedDrugs);
        }

        public Set<String> placedMarkers() {
            return Set.copyOf(placedMarkers);
        }

        public boolean hasMarker(String marker) {
            return placedMarkers.contains(marker);
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

        public int overlaySchemaVersion() {
            return overlaySchemaVersion;
        }

        public String lastOverlayMetricsSummary() {
            return lastOverlayMetricsSummary;
        }
    }
}
