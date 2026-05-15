package org.mydrugs.mydrugs.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.mydrugs.mydrugs.items.data.AdnGeneData;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.items.data.MutationStatValue;
import org.mydrugs.mydrugs.mutation.MutationStat;

import java.util.function.Consumer;

public final class AdnGeneItem extends Item {
    public AdnGeneItem(Properties properties) {
        super(properties);
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

        AdnGeneData data = stack.get(ModDataComponents.ADN_GENE_DATA.get());
        if (data == null) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_gene.empty").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        if (data.broken()) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_gene.broken").withStyle(ChatFormatting.RED));
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_gene.restart").withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_gene.flavor").withStyle(ChatFormatting.DARK_PURPLE));
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_gene.sources", String.join(", ", data.sourceNames())).withStyle(ChatFormatting.GRAY));
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_gene.signature", data.geneticSignature()).withStyle(ChatFormatting.DARK_PURPLE));

        for (MutationStatValue statValue : data.stats()) {
            MutationStat stat = MutationStat.bySerializedNameOrNull(statValue.statId());
            Component statName = stat == null
                    ? Component.translatable("mutation.mydrugs.stat.unknown", statValue.statId())
                    : Component.translatable(stat.translationKey());
            int percent = Math.round(statValue.value() * 100.0F);
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_gene.stat", statName, percent).withStyle(valueColor(statValue.value())));
        }

        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_gene.same_source_warning").withStyle(ChatFormatting.YELLOW));

        if (flag.isAdvanced()) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_gene.source_uuids", String.join(", ", data.sourceUuids())).withStyle(ChatFormatting.DARK_GRAY));
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
