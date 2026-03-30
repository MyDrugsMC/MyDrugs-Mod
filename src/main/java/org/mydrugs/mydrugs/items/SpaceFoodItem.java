package org.mydrugs.mydrugs.items;

import net.minecraft.world.item.Item;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.items.drugs.DrugItem;

public class SpaceFoodItem extends DrugItem {
    private final Item baseFood;

    public SpaceFoodItem(Item baseFood, Properties properties, DrugId drugId, ConsumptionStrategy strategies) {
        super(properties, drugId, strategies);
        this.baseFood = baseFood;
    }

    public Item getBaseFood() {
        return this.baseFood;
    }

    @Override
    public boolean isCrushable() {
        return false;
    }
}