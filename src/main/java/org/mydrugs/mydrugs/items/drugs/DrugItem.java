package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugHolder;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.effects.addiction.manager.AddictionManager;

import java.util.List;

public abstract class DrugItem extends Item implements DrugHolder {
    private final @Nullable DrugModel model;
    private final ConsumptionStrategy strategy;

    public DrugItem(Properties properties, @Nullable DrugId id, ConsumptionStrategy strategy) {
        super(DrugItemProperties.prepare(properties, strategy));
        this.model = id == null ? null : DrugRegistry.getDrug(id);
        this.strategy = strategy;
    }

    @Override
    public DrugModel getDrugModel() {
        if (this.model == null) {
            throw new IllegalStateException("Dynamic drug item: use getDrugModels(stack) instead.");
        }
        return this.model;
    }

    public List<DrugModel> getDrugModels(ItemStack stack) {
        return this.model == null ? List.of() : List.of(this.model);
    }

    protected final void consumeAll(ItemStack stack, ConsumptionStrategy strategy) {
        for (DrugModel model : this.getDrugModels(stack)) {
            MyDrugs.DRUG_SERVICE.consume(model, strategy);
        }
    }

    @Override
    public ConsumptionStrategy getConsumptionStrategy() {
        return strategy;
    }

    @Override
    public boolean isCrushable() {
        return false;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (level.isClientSide()) return super.finishUsingItem(stack, level, livingEntity);
        if (!(livingEntity instanceof ServerPlayer player)) return super.finishUsingItem(stack, level, livingEntity);
        if (this.model == null) {
            for (DrugModel drugModel : getDrugModels(stack)) {
                MyDrugs.DRUG_SERVICE.consume(drugModel, strategy);
                AddictionManager.consume(player, drugModel, 1, strategy);
            }
        } else {
            MyDrugs.DRUG_SERVICE.consume(getDrugModel(), strategy);
            AddictionManager.consume(player, getDrugModel(), 1, strategy);
        }

        return super.finishUsingItem(stack, level, livingEntity);
    }
}