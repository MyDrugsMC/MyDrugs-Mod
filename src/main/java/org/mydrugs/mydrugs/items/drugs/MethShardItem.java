package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;

import java.util.function.Consumer;

public class MethShardItem extends DrugItem {
    public MethShardItem(Properties properties, DrugId drugId, ConsumptionStrategy strategy) {
        super(properties, drugId, strategy);
    }

    @Override
    public boolean isCrushable() {
        return true;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag
    ) {
        MethPurityTooltip.append(stack, tooltipAdder);
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }
}
