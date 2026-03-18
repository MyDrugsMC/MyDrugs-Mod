package org.mydrugs.mydrugs.items.drugs;

import org.mydrugs.mydrugs.core.drug.DrugId;

public class MethPowderItem extends DrugItem {
    public MethPowderItem(Properties properties, DrugId drugId) {
        super(properties, drugId);
    }

    @Override
    public boolean isSmokable() {
        return true;
    }
}
