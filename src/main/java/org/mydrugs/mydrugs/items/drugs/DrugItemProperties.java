package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.EatingStrategy;

public final class DrugItemProperties {
    private DrugItemProperties() {}

    public static Item.Properties prepare(Item.Properties properties, ConsumptionStrategy... strategies) {
        for (ConsumptionStrategy strategy : strategies) {
            if (strategy instanceof EatingStrategy) {
                properties.food(new FoodProperties.Builder()
                        .nutrition(0)
                        .saturationModifier(0.0f)
                        .alwaysEdible()
                        .build());
            }
        }
        return properties;
    }
}