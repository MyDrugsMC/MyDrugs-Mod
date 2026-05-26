package org.mydrugs.mydrugs.core.drug.integration;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;

import java.util.List;

/**
 * The fixed progression order of the curated drug set and the knowledge each stage grants.
 *
 * Used by active recovery (Phase B) to know which drug is "next" so that recovering from drug N
 * accrues fastest while the player is doing drug N+1's production.
 */
public final class CuratedDrugChain {
    /** Curated drugs in progression order (matches the IntegratedTrait declaration order). */
    public static final List<DrugId> ORDER = List.of(
            DrugId.COFFEE,
            DrugId.TOBACCO,
            DrugId.WEED,
            DrugId.HASH,
            DrugId.ALCOHOL,
            DrugId.COCAINE,
            DrugId.LSD,
            DrugId.METH,
            DrugId.MUSHROOMS
    );

    private CuratedDrugChain() {
    }

    /** The drug immediately after {@code drug} in the chain, or null if last/uncurated. */
    public static @Nullable DrugId next(DrugId drug) {
        int index = ORDER.indexOf(drug);
        if (index < 0 || index + 1 >= ORDER.size()) {
            return null;
        }
        return ORDER.get(index + 1);
    }

    /** The knowledge key the player holds once they have reached this drug's production stage. */
    public static @Nullable PsyKnowledgeKey stageKnowledge(DrugId drug) {
        if (drug == null) {
            return null;
        }
        return switch (drug) {
            case COFFEE -> PsyKnowledgeKey.CAFFEINE;
            case TOBACCO -> PsyKnowledgeKey.NICOTINIC;
            case WEED -> PsyKnowledgeKey.CANNABINOID;
            case HASH -> PsyKnowledgeKey.STEEL_PLATING;
            case ALCOHOL -> PsyKnowledgeKey.FERMENTED;
            case COCAINE -> PsyKnowledgeKey.STIMULANT;
            case LSD -> PsyKnowledgeKey.LYSERGIC;
            case METH -> PsyKnowledgeKey.OVERCLOCKED;
            case MUSHROOMS -> PsyKnowledgeKey.MYCELIAL;
            default -> null;
        };
    }
}
