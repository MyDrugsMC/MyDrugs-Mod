package org.mydrugs.mydrugs.items.drugs;

import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;

public class WeedLeafItem extends DrugItem {

    public WeedLeafItem(Properties properties, DrugId drugId, ConsumptionStrategy... strategy) {
        super(properties, drugId, strategy);
    }

    @Override
    public boolean isCrushable() {
        return true;
    }
}
