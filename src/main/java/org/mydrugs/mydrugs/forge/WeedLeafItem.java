package org.mydrugs.mydrugs.forge;

import net.minecraft.world.item.Item;
import org.mydrugs.mydrugs.core.DrugModel;
import org.mydrugs.mydrugs.core.DrugRegistry;

public class WeedLeafItem extends Item implements Drug {
    public WeedLeafItem(Properties properties) {
        super(properties);
    }

    @Override
    public DrugModel model() {
        return DrugRegistry.WEED;
    }
}
