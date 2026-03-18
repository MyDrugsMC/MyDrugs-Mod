package org.mydrugs.mydrugs.items.drugs;

import org.mydrugs.mydrugs.core.drug.DrugId;

public class MethShardItem extends DrugItem {
    public MethShardItem(Properties properties, DrugId drugId) {
        super(properties, drugId);
    }

    @Override
    public boolean isCrushable() {
        return true;
    }
}
