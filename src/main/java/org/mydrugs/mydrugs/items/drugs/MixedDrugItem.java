package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualQuality;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.ritual.MixedDrugData;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.DrinkingStrategy;
import org.mydrugs.mydrugs.core.drug.use.DrugUseResult;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.items.rolling.RollingIngredient;

import java.util.List;
import java.util.function.Consumer;

public final class MixedDrugItem extends DrugItem implements RollingIngredient {
    public MixedDrugItem(Properties properties, ConsumptionStrategy strategy) {
        super(properties, null, strategy);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!isDrinkRoute()) {
            return super.use(level, player, hand);
        }

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || stack.get(ModDataComponents.MIXED_DRUG_DATA.get()) == null) {
            return InteractionResult.PASS;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!isDrinkRoute()) {
            return super.finishUsingItem(stack, level, livingEntity);
        }

        if (!(livingEntity instanceof ServerPlayer player)) {
            return stack;
        }

        DrugUseResult result = consumeFromStack(player, stack);
        if (result.status() == DrugUseResult.Status.BLOCKED_MISSING_KNOWLEDGE) {
            return stack;
        }

        if (player.gameMode() != GameType.CREATIVE) {
            ItemStack emptyContainer = emptyContainerFor(stack);
            stack.shrink(1);
            if (stack.isEmpty()) {
                return emptyContainer;
            }
            if (!emptyContainer.isEmpty() && !player.getInventory().add(emptyContainer)) {
                player.drop(emptyContainer, false);
            }
        }
        return stack;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return isDrinkRoute() ? ItemUseAnimation.DRINK : super.getUseAnimation(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return isDrinkRoute() ? 32 : super.getUseDuration(stack, entity);
    }

    @Override
    public Component getName(ItemStack stack) {
        MixedDrugData data = stack.get(ModDataComponents.MIXED_DRUG_DATA.get());
        if (data != null && !data.displayName().isBlank()) {
            Component formulaName = data.displayName().startsWith("item.mydrugs.mixed_formula.")
                    ? Component.translatable(data.displayName())
                    : Component.literal(data.displayName());
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

    private boolean isDrinkRoute() {
        return getConsumptionStrategy() instanceof DrinkingStrategy;
    }

    private static ItemStack emptyContainerFor(ItemStack stack) {
        MixedDrugData data = stack.get(ModDataComponents.MIXED_DRUG_DATA.get());
        if (data == null) {
            return ItemStack.EMPTY;
        }

        Item item = switch (data.baseDrug()) {
            case COFFEE -> ModItems.CUP.get();
            case ALCOHOL -> ModItems.GLASS_BOTTLE.get();
            default -> null;
        };
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }
}
