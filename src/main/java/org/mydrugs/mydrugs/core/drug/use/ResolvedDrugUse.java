package org.mydrugs.mydrugs.core.drug.use;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;

public record ResolvedDrugUse(
        ServerPlayer player,
        DrugModel model,
        ConsumptionStrategy strategy,
        float baseDose,
        float effectiveDose,
        DrugUseSource source,
        ItemStack sourceStack
) {
}
