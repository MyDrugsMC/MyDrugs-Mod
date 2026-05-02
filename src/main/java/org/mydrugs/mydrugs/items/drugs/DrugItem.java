package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugHolder;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.core.drug.use.DrugUseSource;

import java.util.List;
import java.util.function.Consumer;

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
        if (!level.isClientSide() && livingEntity instanceof ServerPlayer player) {
            consumeFromStack(player, stack);
        }

        return super.finishUsingItem(stack, level, livingEntity);
    }

    protected final void consumeFromStack(ServerPlayer player, ItemStack stack) {
        DrugUseSource source = this.model == null ? DrugUseSource.ROLLED_ITEM : DrugUseSource.ITEM;
        MyDrugs.DRUG_USE_SERVICE.consumeStack(player, stack, strategy, source);
    }

    public static void consumeDrug(ServerPlayer player, DrugModel drugModel, ConsumptionStrategy strategy) {
        MyDrugs.DRUG_USE_SERVICE.consume(player, drugModel, strategy, DrugUseSource.ITEM);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<net.minecraft.network.chat.Component> tooltipAdder,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        DrugTooltipBuilder.append(stack, getDrugModels(stack), this.strategy, flag, tooltipAdder);
    }
}
