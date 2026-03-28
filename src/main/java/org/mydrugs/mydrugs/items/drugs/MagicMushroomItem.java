package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.world.level.block.Block;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;

public class MagicMushroomItem extends DrugBlockItem {
    public MagicMushroomItem(Block block, Properties properties, DrugId id, ConsumptionStrategy... strategy) {
        super(block, properties, id, strategy);
    }

    @Override
    public boolean isCrushable() {
        return true;
    }
}
