package org.mydrugs.mydrugs.advancement;

import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.use.DrugUseSource;

public record DrugKnowledgeResult(
        DrugId drugId,
        DrugCategory category,
        String route,
        DrugUseSource source,
        float effectiveDose,
        boolean firstDrug,
        boolean firstCategory,
        boolean firstRoute
) {
    public boolean anyFirstTime() {
        return firstDrug || firstCategory || firstRoute;
    }
}
