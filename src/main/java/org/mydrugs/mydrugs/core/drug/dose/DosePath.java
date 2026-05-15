package org.mydrugs.mydrugs.core.drug.dose;

import org.mydrugs.mydrugs.core.drug.DrugCategory;

/**
 * Which intoxication ladder a drug category climbs. Coffee and tobacco don't
 * participate in the dose system — they only feed addiction. Alcohol (DEPRESSANT)
 * follows the DRUNK -> COMA ladder; everything else uses the HIGH -> OVERDOSE ladder.
 */
public enum DosePath {
    NONE,
    ALCOHOL,
    DRUG;

    public static DosePath of(DrugCategory category) {
        return switch (category) {
            case DEPRESSANT -> ALCOHOL;
            case CAFFEINE, NICOTINIC -> NONE;
            default -> DRUG;
        };
    }
}
