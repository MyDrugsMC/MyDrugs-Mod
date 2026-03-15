package org.mydrugs.mydrugs.forge.items;

import net.minecraft.world.item.Item;
import org.mydrugs.mydrugs.core.DrugModel;
import org.mydrugs.mydrugs.core.DrugRegistry;
import org.mydrugs.mydrugs.forge.effects.DrugItem;

public class WeedLeafItem extends Item implements DrugItem {
    public WeedLeafItem(Properties properties) {
        super(properties);
    }

    @Override
    public DrugModel model() {
        return DrugRegistry.WEED;
    }
}
