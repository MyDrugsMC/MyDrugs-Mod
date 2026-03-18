package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.world.item.Item;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;

public abstract class DrugItem extends Item {

    private final DrugModel model;

    public DrugItem(Properties properties, DrugId drugId) {
        super(properties);
        this.model = DrugRegistry.getDrug(drugId);
    }

    public DrugModel getModel() {
        return model;
    }

    public boolean isSmokable() {
        return false;
    }

    public boolean isCrushable() {
        return false;
    }
}
