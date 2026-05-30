package org.mydrugs.mydrugs.dimension.inner.v7;

import net.minecraft.world.level.block.Block;

public final class InnerV7Constants {
    public static final String VERSION = "VOID_CONTINENT_V7_2026_05_30";
    public static final String KEY_PREFIX = "inner_v7:";
    public static final String MIGRATION_KEY = KEY_PREFIX + "migration:from_legacy";

    public static final int SLOT_SPACING = 4096;
    public static final int ISLAND_RADIUS = 1280;
    public static final int CORE_RADIUS = 96;
    public static final int SATELLITE_REACH = 360;
    public static final int FULL_REGENERATION_PADDING = 160;
    public static final int FULL_REGENERATION_CHUNK_RADIUS =
            (ISLAND_RADIUS + FULL_REGENERATION_PADDING + 15) / 16;

    public static final int BASE_Y = 64;
    public static final int MIN_Y = 0;
    public static final int GEN_DEPTH = 256;
    public static final int MAX_VERTICAL_UP = 64;
    public static final int MAX_VERTICAL_DOWN = 52;
    public static final long BASE_SEED = 0x4D59445255475307L;

    public static final int UPDATE_FLAGS = Block.UPDATE_ALL;
    public static final int OVERLAY_CHUNKS_PER_TICK = 6;
    public static final int MAX_OVERLAY_BLOCKS_PER_CHUNK = 320;

    private InnerV7Constants() {
    }
}
