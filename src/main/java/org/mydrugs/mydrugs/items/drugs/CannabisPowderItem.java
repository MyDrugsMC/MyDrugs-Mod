package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.items.rolling.RollingIngredient;

public class CannabisPowderItem extends DrugItem implements RollingIngredient {
    public CannabisPowderItem(Properties properties, DrugId drugId, ConsumptionStrategy strategy) {
        super(properties, drugId, strategy);
    }

    @Override
    public DrugId getRollingDrug(ItemStack stack) {
        return DrugId.WEED;
    }
}
