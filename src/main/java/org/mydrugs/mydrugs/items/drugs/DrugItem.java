package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.world.item.Item;
import org.mydrugs.mydrugs.core.drug.DrugHolder;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;

import java.util.List;

public abstract class DrugItem extends Item implements DrugHolder {
    private final DrugModel model;
    private final List<ConsumptionStrategy> strategies;

    public DrugItem(Properties properties, DrugId id, ConsumptionStrategy... strategy) {
        super(DrugItemProperties.prepare(properties, strategy));
        this.model = DrugRegistry.getDrug(id);
        this.strategies = List.of(strategy);
    }

    @Override
    public DrugModel getDrugModel() {
        return model;
    }

    @Override
    public List<ConsumptionStrategy> getConsumptionStrategies() {
        return strategies;
    }
}
