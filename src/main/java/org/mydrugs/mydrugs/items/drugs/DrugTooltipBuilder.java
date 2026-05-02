package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.EatingStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.InjectingStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.SmokingStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.SniffingStrategy;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.items.rolling.RolledDrugContent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class DrugTooltipBuilder {
    private DrugTooltipBuilder() {
    }

    public static void append(ItemStack stack,
                              List<DrugModel> models,
                              ConsumptionStrategy strategy,
                              TooltipFlag flag,
                              Consumer<Component> tooltipAdder) {
        tooltipAdder.accept(Component.translatable(routeKey(strategy)).withStyle(ChatFormatting.GRAY));

        if (models.isEmpty()) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.drug.empty").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        DrugModel primary = models.getFirst();
        tooltipAdder.accept(Component.translatable(
                "tooltip.mydrugs.drug.category",
                Component.translatable("drug_category.mydrugs." + primary.getDrugCategory().name().toLowerCase())
        ).withStyle(ChatFormatting.GRAY));

        tooltipAdder.accept(Component.translatable(
                "tooltip.mydrugs.drug.addiction",
                Component.translatable(riskKey(primary.getAddictionRate()))
        ).withStyle(ChatFormatting.GRAY));

        appendRolledComposition(stack, tooltipAdder);

        if (flag.isAdvanced()) {
            for (DrugModel model : models) {
                tooltipAdder.accept(Component.translatable(
                        "tooltip.mydrugs.drug.model",
                        Component.translatable("drug.mydrugs." + model.getId().serializedName())
                ).withStyle(ChatFormatting.DARK_AQUA));
                for (DrugEffect effect : model.getDrugEffects()) {
                    int duration = strategy != null ? strategy.getNewDuration(effect) : effect.getBaseDuration();
                    int potency = strategy != null ? strategy.getNewPotency(effect) : effect.getBasePotency();
                    tooltipAdder.accept(Component.translatable(
                            "tooltip.mydrugs.drug.effect",
                            Component.translatable("effect_type.mydrugs." + effect.getEffectType().serializedName()),
                            duration / 20,
                            potency
                    ).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        } else {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.drug.advanced_hint").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static void appendRolledComposition(ItemStack stack, Consumer<Component> tooltipAdder) {
        RolledDrugContent content = stack.get(ModDataComponents.ROLLED_CONTENT.get());
        if (content == null) {
            return;
        }

        Map<DrugId, Integer> counts = new LinkedHashMap<>();
        for (DrugId drugId : content.asList()) {
            counts.merge(drugId, 1, Integer::sum);
        }

        for (Map.Entry<DrugId, Integer> entry : counts.entrySet()) {
            tooltipAdder.accept(Component.translatable(
                    "tooltip.mydrugs.drug.composition",
                    Component.translatable("drug.mydrugs." + entry.getKey().serializedName()),
                    entry.getValue() * 100 / 3
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static String routeKey(ConsumptionStrategy strategy) {
        if (strategy instanceof EatingStrategy) {
            return "tooltip.mydrugs.drug.route.eating";
        }
        if (strategy instanceof SmokingStrategy smoking) {
            return smoking.bang()
                    ? "tooltip.mydrugs.drug.route.bang"
                    : "tooltip.mydrugs.drug.route.smoking";
        }
        if (strategy instanceof InjectingStrategy) {
            return "tooltip.mydrugs.drug.route.injecting";
        }
        if (strategy instanceof SniffingStrategy) {
            return "tooltip.mydrugs.drug.route.sniffing";
        }
        return "tooltip.mydrugs.drug.route.unknown";
    }

    private static String riskKey(float addictionRate) {
        if (addictionRate >= 8.0F) {
            return "tooltip.mydrugs.risk.extreme";
        }
        if (addictionRate >= 5.0F) {
            return "tooltip.mydrugs.risk.high";
        }
        if (addictionRate >= 2.0F) {
            return "tooltip.mydrugs.risk.medium";
        }
        return "tooltip.mydrugs.risk.low";
    }
}
