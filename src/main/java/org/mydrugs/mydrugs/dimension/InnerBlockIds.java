package org.mydrugs.mydrugs.dimension;

import java.util.List;

/**
 * Plain string ids of the inner-dimension blocks, with <em>no</em> block registration or other
 * static initialisation. Both the datagen asset provider and the texture-report test read from here
 * so they can never drift — and, crucially, the test can reference these lists without triggering
 * {@link ModInnerDimensionBlocks}'s registry initialisation (which is unavailable in a bare unit
 * test and would throw {@code ExceptionInInitializerError}).
 *
 * <p>Order is fixed so generated assets are stable.
 */
public final class InnerBlockIds {
    /** Cube-shaped crystal / echo / geode / slag node blocks (own {@code cube_all} texture). */
    public static final List<String> NODE_BLOCKS = List.of(
            "lucid_echo_node",
            "bitter_echo_node",
            "calming_echo_node",
            "pressed_calm_node",
            "fermented_memory_node",
            "redline_crystal_node",
            "dream_residue_geode",
            "overdrive_slag",
            "mycelial_insight_node"
    );

    /** Cross-shaped symbolic plant blocks (own {@code cross} texture). */
    public static final List<String> SYMBOLIC_PLANTS = List.of(
            "breath_grass",
            "calming_fern",
            "memory_reeds",
            "redline_thorn",
            "mycelial_root",
            "lucid_clover",
            "ash_grass",
            "moss_breath_carpet",
            "quartz_needlegrass",
            "mycelial_threads",
            "dream_orchid",
            "spore_bloom",
            "bitter_sprout",
            "redline_spark_bloom",
            "calming_bush",
            "memory_sedge",
            "redline_bramble",
            "crystal_shrub",
            "fermented_shrub",
            "mud_reeds",
            "memory_lotus",
            "breath_lily",
            "prism_lotus"
    );

    private InnerBlockIds() {
    }
}
