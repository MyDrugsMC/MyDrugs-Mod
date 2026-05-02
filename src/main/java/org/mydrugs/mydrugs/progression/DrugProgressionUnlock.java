package org.mydrugs.mydrugs.progression;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugId;

public record DrugProgressionUnlock(
        DrugId drugId,
        @Nullable PsyKnowledgeKey requiredKnowledge,
        @Nullable PsyKnowledgeKey grantedKnowledge
) {
}
