package org.mydrugs.mydrugs.dimension.inner;

import java.util.Set;

final class InnerSymbolicFloraCatalog {
    private static final Set<String> IDS = Set.of(
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

    private InnerSymbolicFloraCatalog() {
    }

    static Set<String> idsForTest() {
        return IDS;
    }
}
