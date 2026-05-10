package org.mydrugs.mydrugs.core.drug.use;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugHolder;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.EatingStrategy;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.items.drugs.DrugItem;

import java.util.ArrayList;
import java.util.List;

public final class DrugStackResolver {
    private DrugStackResolver() {
    }

    public static List<ResolvedStackDrug> resolve(ItemStack stack, @Nullable ConsumptionStrategy overrideStrategy) {
        if (stack.isEmpty()) {
            return List.of();
        }

        if (stack.getItem() instanceof DrugItem drugItem) {
            ConsumptionStrategy strategy = overrideStrategy != null ? overrideStrategy : drugItem.getConsumptionStrategy();
            return toResolved(drugItem.getDrugModels(stack), strategy);
        }

        if (stack.getItem() instanceof GlassBottleItem) {
            DrugModel model = GlassBottleItem.getBottleDrug(stack);
            ConsumptionStrategy strategy = overrideStrategy != null ? overrideStrategy : new EatingStrategy();
            return model == null ? List.of() : List.of(new ResolvedStackDrug(model, strategy));
        }

        if (stack.getItem() instanceof DrugHolder holder) {
            ConsumptionStrategy strategy = overrideStrategy != null ? overrideStrategy : holder.getConsumptionStrategy();
            try {
                DrugModel model = holder.getDrugModel();
                return model == null ? List.of() : List.of(new ResolvedStackDrug(model, strategy));
            } catch (IllegalStateException ignored) {
                return List.of();
            }
        }

        return List.of();
    }

    private static List<ResolvedStackDrug> toResolved(List<DrugModel> models, ConsumptionStrategy strategy) {
        if (models.isEmpty()) {
            return List.of();
        }

        List<ResolvedStackDrug> resolved = new ArrayList<>(models.size());
        for (DrugModel model : models) {
            if (model != null) {
                resolved.add(new ResolvedStackDrug(model, strategy));
            }
        }
        return resolved;
    }

    public record ResolvedStackDrug(DrugModel model, ConsumptionStrategy strategy) {
    }
}
