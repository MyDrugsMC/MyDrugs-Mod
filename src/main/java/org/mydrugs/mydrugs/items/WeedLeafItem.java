package org.mydrugs.mydrugs.items;

import net.minecraft.world.item.Item;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;

public class WeedLeafItem extends Item implements DrugItem {
    public WeedLeafItem(Properties properties) {
        super(properties);
    }

    @Override
    public DrugModel model() {
        return DrugRegistry.getDrug(DrugId.WEED);
    }
}
