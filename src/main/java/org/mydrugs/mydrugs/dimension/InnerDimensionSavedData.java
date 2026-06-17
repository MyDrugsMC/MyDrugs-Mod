package org.mydrugs.mydrugs.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.inner.InnerDimensionConstants;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class InnerDimensionSavedData extends SavedData {
    private static final String DEFAULT_DREAM_DIMENSION = "minecraft:overworld";

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
            Codec.STRING.optionalFieldOf("dream_dimension", DEFAULT_DREAM_DIMENSION)
                    .forGetter(IslandState::dreamDimension),
            Codec.STRING.optionalFieldOf("last_overlay_metrics", "").forGetter(IslandState::lastOverlayMetricsSummary),
            // Explicit slot index so a removed island can never make a survivor's slot collide.
            // Default -1 = legacy data; the constructor then derives it from the stored centers.
            Codec.INT.optionalFieldOf("slot_index", -1).forGetter(IslandState::slotIndex),
            Codec.STRING.listOf().optionalFieldOf("completed_inner_trials", List.of())
                    .forGetter(state -> state.completedInnerTrials.stream().map(DrugId::serializedName).toList()),
            Codec.STRING.listOf().optionalFieldOf("restored_scar_markers", List.of())
                    .forGetter(state -> List.copyOf(state.restoredScarMarkers))
    ).apply(instance, IslandState::new));

    public static final Codec<InnerDimensionSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, ISLAND_CODEC).optionalFieldOf("player_islands", Map.of())
                    .forGetter(InnerDimensionSavedData::encodedIslands)
    ).apply(instance, InnerDimensionSavedData::new));

    private static final String DATA_ID = "mydrugs_inner_dimension";
    public static final SavedDataType<InnerDimensionSavedData> TYPE =
            new SavedDataType<>(DATA_ID, InnerDimensionSavedData::new, CODEC);

    private final Map<UUID, IslandState> islands = new LinkedHashMap<>();
    private final Map<Long, UUID> islandOwnersBySlotCenter = new LinkedHashMap<>();

    public InnerDimensionSavedData() {
        this(Map.of());
    }

    private InnerDimensionSavedData(Map<String, IslandState> islands) {
        LoadDiagnostics diagnostics = new LoadDiagnostics();
        for (Map.Entry<String, IslandState> entry : islands.entrySet()) {
            IslandState state = entry.getValue();
            UUID owner = parseUuid(entry.getKey());
            if (owner == null) {
                diagnostics.invalidUuidKeys++;
                owner = parseUuid(state.ownerString());
            }
            if (owner == null) {
                diagnostics.discardedIslandRecords++;
                continue;
            }
            IslandState placed = state.withOwner(owner);
            if (this.islands.put(owner, placed) != null) {
                // Two distinct map keys resolved to the same owner UUID; the earlier record is lost.
                diagnostics.discardedIslandRecords++;
            }
            diagnostics.absorb(placed);
        }
        rebuildSlotIndex();
        diagnostics.recordSlotCollisions(this.islands.values());
        diagnostics.report(this.islands.size());
    }

    public static InnerDimensionSavedData get(ServerLevel innerLevel) {
        return innerLevel.getDataStorage().computeIfAbsent(TYPE);
    }

    public IslandState island(UUID playerId) {
        return islands.get(playerId);
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
        indexIsland(created, false);
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
        UUID owner = islandOwnersBySlotCenter.get(slotCenterKey(centerX, centerZ));
        if (owner == null) {
            return null;
        }
        IslandState island = islands.get(owner);
        if (island != null && island.centerX() == centerX && island.centerZ() == centerZ) {
            return island;
        }

        rebuildSlotIndex();
        owner = islandOwnersBySlotCenter.get(slotCenterKey(centerX, centerZ));
        return owner == null ? null : islands.get(owner);
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
        if (playerId == null || drugId == null || !CuratedDrugChain.ORDER.contains(drugId)) {
            return false;
        }
        IslandState island = getOrCreateIsland(playerId);
        if (!island.integratedDrugs.add(drugId)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean completeInnerTrial(UUID playerId, DrugId drug) {
        IslandState island = getOrCreateIsland(playerId);
        if (drug == null
                || !CuratedDrugChain.ORDER.contains(drug)
                || !island.completedInnerTrials.add(drug)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean hasCompletedInnerTrial(UUID playerId, DrugId drug) {
        IslandState island = islands.get(playerId);
        return island != null && drug != null && island.completedInnerTrials.contains(drug);
    }

    public Set<DrugId> completedInnerTrials(UUID playerId) {
        IslandState island = islands.get(playerId);
        return island == null ? Set.of() : island.completedInnerTrials();
    }

    public int completedInnerTrialCount(UUID playerId) {
        IslandState island = islands.get(playerId);
        return island == null ? 0 : island.completedInnerTrialCount();
    }

    public boolean allInnerTrialsCompleted(UUID playerId) {
        IslandState island = islands.get(playerId);
        return island != null && island.allInnerTrialsCompleted();
    }

    public boolean resetInnerTrials(UUID playerId) {
        IslandState island = islands.get(playerId);
        if (island == null || island.completedInnerTrials.isEmpty()) {
            return false;
        }
        island.completedInnerTrials.clear();
        island.placedMarkers.removeIf(marker ->
                marker.startsWith(InnerDimensionConstants.MARKER_SANCTUARY)
                        || marker.startsWith(InnerDimensionConstants.MARKER_TRIAL_FIXTURE)
                        || marker.startsWith(InnerDimensionConstants.MARKER_RETURN_PENDING));
        setDirty();
        return true;
    }

    public boolean markTrialReturnPending(UUID playerId, DrugId drug) {
        IslandState island = getOrCreateIsland(playerId);
        boolean changed = island.placedMarkers.removeIf(
                marker -> marker.startsWith(InnerDimensionConstants.MARKER_RETURN_PENDING)
        );
        if (drug != null) {
            changed |= island.placedMarkers.add(InnerDimensionConstants.returnPendingMarker(drug));
        }
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean hasPendingTrialReturn(UUID playerId) {
        IslandState island = islands.get(playerId);
        return island != null && island.placedMarkers.stream()
                .anyMatch(marker -> marker.startsWith(InnerDimensionConstants.MARKER_RETURN_PENDING));
    }

    public boolean clearPendingTrialReturn(UUID playerId) {
        IslandState island = islands.get(playerId);
        if (island == null || !island.placedMarkers.removeIf(
                marker -> marker.startsWith(InnerDimensionConstants.MARKER_RETURN_PENDING))) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean markProgressMarker(UUID playerId, String marker) {
        if (marker == null || !marker.startsWith(InnerDimensionConstants.MARKER_PROGRESS_PREFIX)) {
            return false;
        }
        IslandState island = getOrCreateIsland(playerId);
        if (!island.placedMarkers.add(marker)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean hasProgressMarker(UUID playerId, String marker) {
        IslandState island = islands.get(playerId);
        return island != null
                && marker != null
                && marker.startsWith(InnerDimensionConstants.MARKER_PROGRESS_PREFIX)
                && island.placedMarkers.contains(marker);
    }

    public boolean markScarRestored(UUID playerId, String marker) {
        IslandState island = getOrCreateIsland(playerId);
        if (marker == null || marker.isBlank() || !island.restoredScarMarkers.add(marker)) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean isScarRestored(UUID playerId, String marker) {
        IslandState island = islands.get(playerId);
        return island != null && marker != null && island.restoredScarMarkers.contains(marker);
    }

    public Set<String> restoredScarMarkers(UUID playerId) {
        IslandState island = islands.get(playerId);
        return island == null ? Set.of() : island.restoredScarMarkers();
    }

    public boolean markSpiralCourtPlaced(UUID playerId) {
        return markStructurePlaced(playerId, InnerDimensionConstants.MARKER_SPIRAL_COURT);
    }

    public boolean isSpiralCourtPlaced(UUID playerId) {
        IslandState island = islands.get(playerId);
        return island != null && island.hasMarker(InnerDimensionConstants.MARKER_SPIRAL_COURT);
    }

    public boolean markSpiralCourtRewardPlaced(UUID playerId) {
        return markStructurePlaced(playerId, InnerDimensionConstants.MARKER_SPIRAL_REWARD);
    }

    public boolean isSpiralCourtRewardPlaced(UUID playerId) {
        IslandState island = islands.get(playerId);
        return island != null && island.hasMarker(InnerDimensionConstants.MARKER_SPIRAL_REWARD);
    }

    public boolean markDreamAligned(UUID playerId, BlockPos resonatorPos) {
        return markDreamAligned(playerId, resonatorPos, DEFAULT_DREAM_DIMENSION);
    }

    /**
     * Returns {@code true} only when persisted dream-alignment state changed and was marked dirty.
     */
    public boolean markDreamAligned(UUID playerId, BlockPos resonatorPos, String dimensionId) {
        IslandState island = getOrCreateIsland(playerId);
        String normalizedDimension = normalizeDreamDimension(dimensionId);
        boolean changed = !island.dreamAligned;
        if (resonatorPos != null) {
            changed = changed
                    || !island.hasDreamCoordinate
                    || island.dreamX != resonatorPos.getX()
                    || island.dreamY != resonatorPos.getY()
                    || island.dreamZ != resonatorPos.getZ()
                    || !Objects.equals(island.dreamDimension, normalizedDimension);
        }
        if (!changed) {
            return false;
        }
        island.dreamAligned = true;
        if (resonatorPos != null) {
            island.hasDreamCoordinate = true;
            island.dreamX = resonatorPos.getX();
            island.dreamY = resonatorPos.getY();
            island.dreamZ = resonatorPos.getZ();
            island.dreamDimension = normalizedDimension;
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
        setDirty();
        return true;
    }

    public boolean clearOverlayMarkers(UUID playerId) {
        IslandState island = getOrCreateIsland(playerId);
        boolean markersChanged = island.placedMarkers.removeIf(
                marker -> !marker.startsWith(InnerDimensionConstants.MARKER_PROGRESS_PREFIX)
        );
        boolean metricsChanged = !island.lastOverlayMetricsSummary.isBlank();
        if (!markersChanged && !metricsChanged) {
            return false;
        }
        island.lastOverlayMetricsSummary = "";
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

    private static String normalizeDreamDimension(String dimensionId) {
        return dimensionId == null || dimensionId.isBlank()
                ? DEFAULT_DREAM_DIMENSION
                : dimensionId;
    }

    private void rebuildSlotIndex() {
        islandOwnersBySlotCenter.clear();
        for (IslandState island : islands.values()) {
            indexIsland(island, true);
        }
    }

    private void indexIsland(IslandState island, boolean warnDuplicate) {
        if (island == null || island.owner() == null) {
            return;
        }
        long key = slotCenterKey(island.centerX(), island.centerZ());
        UUID previous = islandOwnersBySlotCenter.putIfAbsent(key, island.owner());
        if (warnDuplicate && previous != null && !previous.equals(island.owner())) {
            MyDrugs.getLOGGER().warn(
                    "Duplicate Inner Dimension island slot center {},{} for owners {} and {}; keeping {} in lookup index",
                    island.centerX(),
                    island.centerZ(),
                    previous,
                    island.owner(),
                    previous
            );
        }
    }

    private static long slotCenterKey(int centerX, int centerZ) {
        return ((long) centerX & 0xffffffffL) | (((long) centerZ & 0xffffffffL) << 32);
    }

    static boolean keepLoadedMarkerForTest(String marker) {
        return keepLoadedMarker(marker);
    }

    private static boolean keepLoadedMarker(String marker) {
        if (marker == null || marker.isBlank()) {
            return false;
        }
        if (marker.startsWith(InnerDimensionConstants.MARKER_PROGRESS_PREFIX)
                || marker.equals(InnerDimensionConstants.MARKER_OWNER_READY)
                || marker.startsWith("spiral_court:")) {
            return true;
        }
        int version = visualMarkerVersion(marker);
        return version < 0 || version >= InnerDimensionConstants.OVERLAY_SCHEMA_VERSION;
    }

    private static int visualMarkerVersion(String marker) {
        String[] parts = marker.split(":");
        for (String part : parts) {
            if (part.length() < 2 || part.charAt(0) != 'v') {
                continue;
            }
            int version = 0;
            for (int i = 1; i < part.length(); i++) {
                char c = part.charAt(i);
                if (c < '0' || c > '9') {
                    version = -1;
                    break;
                }
                version = version * 10 + (c - '0');
            }
            if (version >= 0) {
                return version;
            }
        }
        return -1;
    }

    /**
     * One-shot accumulator for diagnostics gathered while decoding persisted data. Never stored or
     * networked; it exists only so a single summary can be logged after a load completes, making
     * silent data cleanup observable for debugging without spamming a line per bad entry.
     */
    private static final class LoadDiagnostics {
        private int invalidUuidKeys;
        private int discardedIslandRecords;
        private int invalidDrugIds;
        private int malformedDreamDimensions;
        private int staleMarkerSchemas;
        private int discardedMarkerRecords;
        private int duplicateSlotCenters;
        private int duplicateSlotIndices;

        private void absorb(IslandState state) {
            invalidDrugIds += state.discardedDrugIdEntries;
            staleMarkerSchemas += state.staleMarkerEntries;
            discardedMarkerRecords += state.discardedMarkerEntries;
            if (state.malformedDreamDimension) {
                malformedDreamDimensions++;
            }
        }

        private void recordSlotCollisions(Collection<IslandState> states) {
            Set<Long> seenCenters = new HashSet<>();
            Set<Integer> seenIndices = new HashSet<>();
            for (IslandState state : states) {
                if (!seenCenters.add(slotCenterKey(state.centerX(), state.centerZ()))) {
                    duplicateSlotCenters++;
                }
                if (!seenIndices.add(state.slotIndex())) {
                    duplicateSlotIndices++;
                }
            }
        }

        private boolean hasAnomalies() {
            return invalidUuidKeys > 0
                    || discardedIslandRecords > 0
                    || invalidDrugIds > 0
                    || malformedDreamDimensions > 0
                    || staleMarkerSchemas > 0
                    || discardedMarkerRecords > 0
                    || duplicateSlotCenters > 0
                    || duplicateSlotIndices > 0;
        }

        private void report(int islandsLoaded) {
            if (hasAnomalies()) {
                MyDrugs.getLOGGER().info(
                        "Inner Dimension saved data loaded {} island(s); cleaned data: invalidUuidKeys={}, discardedIslands={}, invalidDrugIds={}, malformedDreamDimensions={}, staleMarkerSchemas={}, discardedMarkerRecords={}, duplicateSlotCenters={}, duplicateSlotIndices={}",
                        islandsLoaded,
                        invalidUuidKeys,
                        discardedIslandRecords,
                        invalidDrugIds,
                        malformedDreamDimensions,
                        staleMarkerSchemas,
                        discardedMarkerRecords,
                        duplicateSlotCenters,
                        duplicateSlotIndices
                );
            } else if (islandsLoaded > 0) {
                MyDrugs.getLOGGER().debug(
                        "Inner Dimension saved data loaded {} island(s) with no anomalies", islandsLoaded);
            }
        }
    }

    public static final class IslandState {
        private final int slotIndex;
        private final int centerX;
        private final int centerZ;
        private final Set<DrugId> integratedDrugs;
        private final Set<String> placedMarkers;
        private final Set<DrugId> completedInnerTrials;
        private final Set<String> restoredScarMarkers;
        private UUID owner;
        private boolean initialIslandBuilt;
        private boolean dreamAligned;
        private boolean hasDreamCoordinate;
        private int dreamX;
        private int dreamY;
        private int dreamZ;
        private String dreamDimension;
        private String lastOverlayMetricsSummary;
        // Load-time diagnostics computed during decode; never serialized or networked.
        private final transient int discardedDrugIdEntries;
        private final transient int staleMarkerEntries;
        private final transient int discardedMarkerEntries;
        private final transient boolean malformedDreamDimension;

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
                    DEFAULT_DREAM_DIMENSION,
                    "",
                    slotIndex,
                    List.of(),
                    List.of()
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
                String dreamDimension,
                String lastOverlayMetricsSummary,
                int slotIndex,
                List<String> completedInnerTrials,
                List<String> restoredScarMarkers
        ) {
            this.owner = parseUuid(owner);
            this.slotIndex = slotIndex >= 0 ? slotIndex : slotIndexFromCenter(centerX, centerZ);
            this.centerX = centerX;
            this.centerZ = centerZ;

            int invalidDrugs = 0;
            LinkedHashSet<DrugId> integrated = new LinkedHashSet<>();
            for (String name : integratedDrugs) {
                DrugId id = DrugId.bySerializedNameOrNull(name);
                if (id == null) {
                    invalidDrugs++;
                } else if (CuratedDrugChain.ORDER.contains(id)) {
                    integrated.add(id);
                }
                // Valid ids outside the curated chain are intentionally dropped, not counted as invalid.
            }
            this.integratedDrugs = integrated;

            LinkedHashSet<DrugId> trials = new LinkedHashSet<>();
            for (String name : completedInnerTrials) {
                DrugId id = DrugId.bySerializedNameOrNull(name);
                if (id == null) {
                    invalidDrugs++;
                } else if (CuratedDrugChain.ORDER.contains(id)) {
                    trials.add(id);
                }
                // Valid ids outside the curated chain are intentionally dropped, not counted as invalid.
            }
            this.completedInnerTrials = trials;
            this.discardedDrugIdEntries = invalidDrugs;

            int staleMarkers = 0;
            int discardedMarkers = 0;
            LinkedHashSet<String> markers = new LinkedHashSet<>();
            for (String marker : placedMarkers) {
                if (marker == null || marker.isBlank()) {
                    discardedMarkers++;
                } else if (!keepLoadedMarker(marker)) {
                    staleMarkers++;
                } else {
                    markers.add(marker);
                }
            }
            this.placedMarkers = markers;

            LinkedHashSet<String> scars = new LinkedHashSet<>();
            for (String marker : restoredScarMarkers) {
                if (marker == null || marker.isBlank()) {
                    discardedMarkers++;
                } else {
                    scars.add(marker);
                }
            }
            this.restoredScarMarkers = scars;
            this.staleMarkerEntries = staleMarkers;
            this.discardedMarkerEntries = discardedMarkers;

            this.initialIslandBuilt = initialIslandBuilt;
            this.dreamAligned = dreamAligned;
            this.hasDreamCoordinate = hasDreamCoordinate;
            this.dreamX = dreamX;
            this.dreamY = dreamY;
            this.dreamZ = dreamZ;
            this.malformedDreamDimension = dreamDimension != null
                    && !dreamDimension.isBlank()
                    && ResourceLocation.tryParse(dreamDimension) == null;
            this.dreamDimension = dreamDimension == null || dreamDimension.isBlank()
                    ? DEFAULT_DREAM_DIMENSION
                    : dreamDimension;
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

        public int integratedDrugCount() {
            return integratedDrugs.size();
        }

        public int integratedCount() {
            return integratedDrugCount();
        }

        public boolean hasIntegrated(DrugId drugId) {
            return drugId != null && integratedDrugs.contains(drugId);
        }

        public boolean allIntegratedTrialsCompleted() {
            for (DrugId drugId : integratedDrugs) {
                if (!completedInnerTrials.contains(drugId)) {
                    return false;
                }
            }
            return true;
        }

        public List<Integer> integratedDrugNetworkIds() {
            return integratedDrugs.stream().map(DrugId::networkId).toList();
        }

        public Set<DrugId> integratedDrugs() {
            return Set.copyOf(integratedDrugs);
        }

        public boolean hasCompletedInnerTrial(DrugId drug) {
            return drug != null && completedInnerTrials.contains(drug);
        }

        public Set<DrugId> completedInnerTrials() {
            return Set.copyOf(completedInnerTrials);
        }

        public int completedInnerTrialCount() {
            return completedInnerTrials.size();
        }

        public boolean allInnerTrialsCompleted() {
            return completedInnerTrials.containsAll(CuratedDrugChain.ORDER);
        }

        public Set<String> restoredScarMarkers() {
            return Set.copyOf(restoredScarMarkers);
        }

        public boolean isScarRestored(String marker) {
            return marker != null && restoredScarMarkers.contains(marker);
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

        public String dreamDimension() {
            return dreamDimension;
        }

        public String lastOverlayMetricsSummary() {
            return lastOverlayMetricsSummary;
        }
    }
}
