package org.mydrugs.mydrugs.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.items.data.MutationPayloadData;
import org.mydrugs.mydrugs.items.data.MutationStatValue;
import org.mydrugs.mydrugs.mutation.MutationStat;

import java.util.function.Consumer;

public final class MutationPayloadItem extends Item {
    private final String emptyTooltipKey;
    private final String footerTooltipKey;

    public MutationPayloadItem(Properties properties, String emptyTooltipKey, String footerTooltipKey) {
        super(properties);
        this.emptyTooltipKey = emptyTooltipKey;
        this.footerTooltipKey = footerTooltipKey;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack handStack = player.getItemInHand(hand);
        if (!handStack.is(ModItems.MUTAGENIC_BLOOD_VIAL.get())) {
            return super.use(level, player, hand);
        }

        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);
        if (SyringeItem.tryLoadMutagenicBlood(otherStack, handStack, player)) {
            return InteractionResult.SUCCESS.heldItemTransformedTo(handStack);
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);

        MutationPayloadData payload = stack.get(ModDataComponents.MUTATION_PAYLOAD.get());
        if (payload == null || payload.stats().isEmpty()) {
            tooltipAdder.accept(Component.translatable(this.emptyTooltipKey).withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        if (stack.is(ModItems.MUTATION_VECTOR.get())) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.mutation_vector.flavor").withStyle(ChatFormatting.DARK_PURPLE));
        } else if (stack.is(ModItems.MUTAGENIC_BLOOD_VIAL.get())) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.mutagenic_blood_vial.flavor").withStyle(ChatFormatting.DARK_RED));
        }
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.mutation_payload.sources", String.join(", ", payload.sourceNames())).withStyle(ChatFormatting.GRAY));
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.mutation_payload.signature", payload.geneticSignature()).withStyle(ChatFormatting.DARK_PURPLE));

        for (MutationStatValue statValue : payload.stats()) {
            MutationStat stat = MutationStat.bySerializedNameOrNull(statValue.statId());
            Component statName = stat == null
                    ? Component.translatable("mutation.mydrugs.stat.unknown", statValue.statId())
                    : Component.translatable(stat.translationKey());
            int percent = Math.round(statValue.value() * 100.0F);
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.mutation_payload.stat", statName, percent).withStyle(valueColor(statValue.value())));
        }

        tooltipAdder.accept(Component.translatable(
                "tooltip.mydrugs.mutation_payload.assimilation_difficulty",
                Math.round(payload.assimilationDifficulty() * 100.0F)
        ).withStyle(ChatFormatting.YELLOW));
        tooltipAdder.accept(Component.translatable(
                "tooltip.mydrugs.mutation_payload.rejection_risk",
                Math.round(payload.rejectionRisk() * 100.0F)
        ).withStyle(ChatFormatting.RED));
        tooltipAdder.accept(Component.translatable(this.footerTooltipKey).withStyle(ChatFormatting.GRAY));

        if (flag.isAdvanced()) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.mutation_payload.source_uuids", String.join(", ", payload.sourceUuids())).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static ChatFormatting valueColor(float value) {
        if (value < 0.20F) {
            return ChatFormatting.DARK_GRAY;
        }
        if (value < 0.40F) {
            return ChatFormatting.WHITE;
        }
        if (value < 0.60F) {
            return ChatFormatting.GREEN;
        }
        if (value < 0.80F) {
            return ChatFormatting.AQUA;
        }
        return ChatFormatting.LIGHT_PURPLE;
    }
}
