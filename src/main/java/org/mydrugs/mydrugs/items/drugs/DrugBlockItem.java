package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import org.mydrugs.mydrugs.core.drug.DrugHolder;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;

public abstract class DrugBlockItem extends BlockItem implements DrugHolder {
    private final DrugModel model;
    private final ConsumptionStrategy strategy;

    public DrugBlockItem(Block block, Properties properties, DrugId id, ConsumptionStrategy strategy) {
        super(block, DrugItemProperties.prepare(properties, strategy));
        model = DrugRegistry.getDrug(id);
        this.strategy = strategy;
    }

    @Override
    public DrugModel getDrugModel() {
        return model;
    }

    @Override
    public ConsumptionStrategy getConsumptionStrategy() {
        return strategy;
    }
}
