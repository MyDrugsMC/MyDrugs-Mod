package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.items.rolling.RollingIngredient;

public class CrackShardItem extends DrugItem implements RollingIngredient {
    public CrackShardItem(Properties properties, DrugId id, ConsumptionStrategy strategy) {
        super(properties, id, strategy);
    }

    @Override
    public DrugId getRollingDrug(ItemStack stack) {
        return DrugId.CRACK;
    }
}
