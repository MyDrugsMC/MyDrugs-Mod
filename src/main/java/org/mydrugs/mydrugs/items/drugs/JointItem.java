package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.items.rolling.RolledDrugContent;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.util.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class JointItem extends RolledSmokedItem {
    public JointItem(Properties properties, DrugId id, ConsumptionStrategy strategy) {
        super(properties, id, strategy);
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

        RolledDrugContent content = stack.get(ModDataComponents.ROLLED_CONTENT.get());
        if (content == null) {
            return;
        }

        Map<DrugId, Integer> counts = new HashMap<>();
        counts.put(content.first(), 1);
        counts.merge(content.second(), 1, Integer::sum);
        counts.merge(content.third(), 1, Integer::sum);

        for (Map.Entry<DrugId, Integer> entry : counts.entrySet()) {
            String idName = TextUtils.prettify(entry.getKey().name());
            int percent = Math.round(entry.getValue() / 3.0f * 100.0f);
            tooltipAdder.accept(Component.literal(idName + " : " + percent + "%"));
        }
    }
}
