package org.mydrugs.mydrugs.items.drugs;

import org.mydrugs.mydrugs.core.drug.DrugId;

public class WeedLeafItem extends DrugItem {

    public WeedLeafItem(Properties properties, DrugId drugId) {
        super(properties, drugId);
    }

    @Override
    public boolean isCrushable() {
        return true;
    }
}
