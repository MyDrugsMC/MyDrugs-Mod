package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.world.level.block.Block;
import org.mydrugs.mydrugs.core.drug.DrugId;

public final class InnerDimensionConstants {
    public static final String SYSTEM_NAME = "Inner Dimension";
    public static final int OVERLAY_SCHEMA_VERSION = 2;

    public static final int SLOT_SPACING = 4096;
    public static final int ISLAND_RADIUS = 1280;
    public static final int CORE_RADIUS = 112;
    public static final int SATELLITE_REACH = 420;

    public static final int BASE_Y = 64;
    public static final int MIN_Y = 0;
    public static final int GEN_DEPTH = 256;
    /**
     * Deliberate choice: a fixed base seed, NOT mixed with {@code level.getSeed()}. The inner
     * dimension is an archetypal psyche: its structure (sanctuary, one region per drug, radiating
     * paths, shrines) is meant to be the same symbolic map in every world. Per-player variation
     * still exists because {@link InnerTerrain#seedForSlot} mixes the slot's centre coordinates, so
     * two players get different islands while a given player's island is identical across saves.
     * Mixing the world seed would also require threading it through the static, seedless worldgen
     * path, which we intentionally avoid.
     */
    public static final long BASE_SEED = 0x4D59445255475321L;

    // Concentric ring "roads" were removed (they read as a drawn diagram). MID_RING_RADIUS is kept
    // only as a distance landmark still referenced by InnerPathSceneBuilder for a path waypoint.
    public static final int MID_RING_RADIUS = 420;
    public static final int LANDMARK_BASE_RADIUS = 520;
    public static final int LANDMARK_RADIUS_STEP = 58;

    public static final int UPDATE_FLAGS = Block.UPDATE_ALL;
    public static final int RECREATE_UPDATE_FLAGS = Block.UPDATE_CLIENTS
            | Block.UPDATE_KNOWN_SHAPE
            | Block.UPDATE_SUPPRESS_DROPS;
    public static final int OVERLAY_CHUNKS_PER_TICK = 6;
    public static final int RECREATE_CHUNKS_PER_TICK = 1;
    public static final int MAX_OVERLAY_BLOCKS_PER_CHUNK = 3600;
    public static final int FULL_RECREATE_BLOCK_RADIUS = ISLAND_RADIUS + SATELLITE_REACH + 192;
    public static final int FULL_RECREATE_CHUNK_RADIUS = (FULL_RECREATE_BLOCK_RADIUS + 15) / 16;

    public static final String MARKER_SANCTUARY = "sanctuary:center";
    public static final String MARKER_OWNER_READY = "owner:ready";

    private InnerDimensionConstants() {
    }

    /**
     * Sanctuary marker keyed by integration count so the gate lets the sanctuary regrow each
     * time a new drug is integrated, then skips re-placement on idle chunk loads at the same count.
     */
    public static String sanctuaryMarker(int integratedCount) {
        return MARKER_SANCTUARY + ":" + integratedCount;
    }

    /**
     * Landmark marker keyed by its unlocked state so the locked->unlocked transition re-places the
     * shrine once, after which idle loads skip it.
     */
    public static String landmarkMarker(DrugId drugId, boolean unlocked) {
        return "landmark:" + drugId.serializedName() + (unlocked ? ":unlocked" : ":locked");
    }

    public static String pathMarker(DrugId drugId) {
        return "path:" + drugId.serializedName();
    }
}
