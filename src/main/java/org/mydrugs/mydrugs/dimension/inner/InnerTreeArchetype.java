package org.mydrugs.mydrugs.dimension.inner;

import org.mydrugs.mydrugs.core.drug.DrugId;

enum InnerTreeArchetype {
    COFFEE_ORCHARD,
    TOBACCO_SNAG,
    WEED_CANOPY,
    HASH_CRYSTAL_GROVE,
    ALCOHOL_DROWNED_CYPRESS,
    COCAINE_REDLINE_THICKET,
    LSD_PRISM_CANOPY,
    METH_LIGHTNING_SPIRE,
    MUSHROOM_MOTHER_GROVE;

    static InnerTreeArchetype forDrug(DrugId drugId) {
        return switch (drugId) {
            case TOBACCO -> TOBACCO_SNAG;
            case WEED -> WEED_CANOPY;
            case HASH -> HASH_CRYSTAL_GROVE;
            case ALCOHOL -> ALCOHOL_DROWNED_CYPRESS;
            case COCAINE -> COCAINE_REDLINE_THICKET;
            case LSD -> LSD_PRISM_CANOPY;
            case METH -> METH_LIGHTNING_SPIRE;
            case MUSHROOMS -> MUSHROOM_MOTHER_GROVE;
            default -> COFFEE_ORCHARD;
        };
    }
}
