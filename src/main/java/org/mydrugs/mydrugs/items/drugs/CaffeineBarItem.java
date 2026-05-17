package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugHolder;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.EatingStrategy;
import org.mydrugs.mydrugs.core.drug.use.DrugUseResult;
import org.mydrugs.mydrugs.core.drug.use.DrugUseSource;

import java.util.List;
import java.util.function.Consumer;

public final class CaffeineBarItem extends Item implements DrugHolder {
    private static final ConsumptionStrategy STRATEGY = new OneDoseFoodStrategy();
    private static final DrugModel COFFEE = DrugRegistry.getDrug(DrugId.COFFEE);

    public CaffeineBarItem(Properties properties) {
        super(properties.stacksTo(16).food(new FoodProperties.Builder()
                .nutrition(3)
                .saturationModifier(1.0F / 6.0F)
                .build()));
    }

    @Override
    public DrugModel getDrugModel() {
        return COFFEE;
    }

    @Override
    public ConsumptionStrategy getConsumptionStrategy() {
        return STRATEGY;
    }

    @Override
    public boolean isCrushable() {
        return false;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide() && livingEntity instanceof ServerPlayer player) {
            DrugUseResult result = MyDrugs.DRUG_USE_SERVICE.consume(
                    player,
                    getDrugModel(),
                    getConsumptionStrategy(),
                    DrugUseSource.ITEM,
                    stack
            );
            if (result.status() == DrugUseResult.Status.BLOCKED_MISSING_KNOWLEDGE) {
                return stack;
            }
        }

        return super.finishUsingItem(stack, level, livingEntity);
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
        DrugTooltipBuilder.append(stack, List.of(getDrugModel()), getConsumptionStrategy(), flag, tooltipAdder);
    }

    private static final class OneDoseFoodStrategy extends EatingStrategy {
        @Override
        public float getNewIntensity(DrugEffect drugEffect) {
            return drugEffect.getBaseIntensity();
        }

        @Override
        public int getNewDuration(DrugEffect drugEffect) {
            return drugEffect.getBaseDuration();
        }

        @Override
        public float getNewDose(float baseDose) {
            return baseDose;
        }
    }
}
