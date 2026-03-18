package org.mydrugs.mydrugs.items.drugs;

import org.mydrugs.mydrugs.core.drug.DrugId;

public class WeedPowderItem extends DrugItem {
    public WeedPowderItem(Properties properties, DrugId drugId) {
        super(properties, drugId);
    }

    @Override
    public boolean isSmokable() {
        return true;
    }
}
