package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualQuality;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.ritual.MixedDrugData;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.items.rolling.RollingIngredient;

import java.util.List;
import java.util.function.Consumer;

public final class MixedDrugItem extends DrugItem implements RollingIngredient {
    public MixedDrugItem(Properties properties, ConsumptionStrategy strategy) {
        super(properties, null, strategy);
    }

    @Override
    public Component getName(ItemStack stack) {
        MixedDrugData data = stack.get(ModDataComponents.MIXED_DRUG_DATA.get());
        if (data != null && !data.displayName().isBlank()) {
            Component formulaName = Component.literal(data.displayName());
            return switch (data.quality()) {
                case CRUDE -> Component.translatable("item.mydrugs.mixed_drug.name.crude", formulaName);
                case PERFECT -> Component.translatable("item.mydrugs.mixed_drug.name.perfect", formulaName);
                case MASTERWORK -> Component.translatable("item.mydrugs.mixed_drug.name.masterwork", formulaName);
                case BASE -> formulaName;
            };
        }
        return super.getName(stack);
    }

    @Override
    public List<DrugModel> getDrugModels(ItemStack stack) {
        MixedDrugData data = stack.get(ModDataComponents.MIXED_DRUG_DATA.get());
        if (data == null) {
            return List.of();
        }
        DrugModel base = DrugRegistry.getDrug(data.baseDrug());
        return List.of(base.withAdditionalEffects(data.addedEffects().stream()
                .map(data.quality()::applyTo)
                .map(org.mydrugs.mydrugs.core.drug.ritual.RitualDrugEffectData::toDrugEffect)
                .toList()));
    }

    @Override
    public DrugId getRollingDrug(ItemStack stack) {
        MixedDrugData data = stack.get(ModDataComponents.MIXED_DRUG_DATA.get());
        return data == null ? DrugId.WEED : data.baseDrug();
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag
    ) {
        MixedDrugData data = stack.get(ModDataComponents.MIXED_DRUG_DATA.get());
        if (data != null) {
            PsyMixerRitualQuality quality = data.quality();
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.mixed_drug.quality", Component.translatable(quality.translationKey())));
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.mixed_drug.quality_effects", quality.positivePercent(), quality.negativePercent()));
            if (!data.authorName().isBlank()) {
                tooltipAdder.accept(Component.translatable("tooltip.mydrugs.mixed_drug.author", data.authorName()));
            }
        }
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }
}
