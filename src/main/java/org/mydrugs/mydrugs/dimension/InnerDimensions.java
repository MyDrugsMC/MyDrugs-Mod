package org.mydrugs.mydrugs.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.mydrugs.mydrugs.MyDrugs;

/**
 * Resource keys for the Inner Dimension (Phase G).
 *
 * The dimension itself is registered via datapack JSON
 * ({@code data/mydrugs/dimension/inner.json} +
 * {@code data/mydrugs/dimension_type/inner.json}).
 */
public final class InnerDimensions {
    private static final ResourceLocation INNER_ID =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "inner");

    public static final ResourceKey<Level> INNER_LEVEL =
            ResourceKey.create(Registries.DIMENSION, INNER_ID);

    public static final ResourceKey<DimensionType> INNER_DIMENSION_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, INNER_ID);

    /** Y-level the island sits at. */
    public static final int ISLAND_Y = 64;

    /** Y-level below which a player is considered to have fallen out and is returned. */
    public static final int FALL_OUT_Y = 8;

    private InnerDimensions() {
    }
}
