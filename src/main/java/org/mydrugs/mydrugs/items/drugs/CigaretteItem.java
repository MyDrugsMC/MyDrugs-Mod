package org.mydrugs.mydrugs.items.drugs;

import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.items.rolling.RolledDrugContent;

public class CigaretteItem extends RolledSmokedItem {
    public CigaretteItem(Properties properties, DrugId id, ConsumptionStrategy strategy) {
        super(properties, id, strategy);
    }

    @Override
    protected RolledDrugContent defaultContent() {
        return RolledDrugContent.allTobacco();
    }
}
