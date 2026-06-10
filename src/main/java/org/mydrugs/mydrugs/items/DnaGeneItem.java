package org.mydrugs.mydrugs.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.mydrugs.mydrugs.items.data.DnaGeneData;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.items.data.MutationStatValue;
import org.mydrugs.mydrugs.mutation.MutationStat;

import java.util.function.Consumer;

public final class DnaGeneItem extends Item {
    public DnaGeneItem(Properties properties) {
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

        DnaGeneData data = stack.get(ModDataComponents.DNA_GENE_DATA.get());
        if (data == null) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.dna_gene.empty").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        if (data.broken()) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.dna_gene.broken").withStyle(ChatFormatting.RED));
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.dna_gene.restart").withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.dna_gene.flavor").withStyle(ChatFormatting.DARK_PURPLE));
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.dna_gene.sources", String.join(", ", data.sourceNames())).withStyle(ChatFormatting.GRAY));
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.dna_gene.signature", data.geneticSignature()).withStyle(ChatFormatting.DARK_PURPLE));

        for (MutationStatValue statValue : data.stats()) {
            MutationStat stat = MutationStat.bySerializedNameOrNull(statValue.statId());
            Component statName = stat == null
                    ? Component.translatable("mutation.mydrugs.stat.unknown", statValue.statId())
                    : Component.translatable(stat.translationKey());
            int percent = Math.round(statValue.value() * 100.0F);
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.dna_gene.stat", statName, percent).withStyle(MutationTooltipColors.value(statValue.value())));
        }

        tooltipAdder.accept(Component.translatable(
                "tooltip.mydrugs.dna_gene.same_source_warning",
                String.join(", ", data.sourceNames())
        ).withStyle(ChatFormatting.YELLOW));

        if (flag.isAdvanced()) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.dna_gene.source_uuids", String.join(", ", data.sourceUuids())).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

}
