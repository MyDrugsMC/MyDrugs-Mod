package org.mydrugs.mydrugs.items.rolling;

import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.core.drug.DrugId;

public interface RollingIngredient {
    DrugId getRollingDrug(ItemStack stack);
}