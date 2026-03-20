package org.mydrugs.mydrugs.items.drugs;

import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.EatingStrategy;

public class LsdDropItem extends DrugItem {
    public LsdDropItem(Properties properties, DrugId drugId, ConsumptionStrategy... strategy) {
        super(properties, drugId, strategy);
    }

    @Override
    public boolean isCrushable() {
        return false;
    }
}
