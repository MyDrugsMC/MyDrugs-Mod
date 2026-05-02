package org.mydrugs.mydrugs.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class PsyTooltipItem extends Item {
    private final String tooltipKey;
    private final String useMessageKey;

    public PsyTooltipItem(Properties properties, String tooltipKey) {
        this(properties, tooltipKey, "");
    }

    public PsyTooltipItem(Properties properties, String tooltipKey, String useMessageKey) {
        super(properties);
        this.tooltipKey = tooltipKey;
        this.useMessageKey = useMessageKey;
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
        if (!this.tooltipKey.isBlank()) {
            tooltipAdder.accept(Component.translatable(this.tooltipKey));
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (this.useMessageKey.isBlank()) {
            return super.use(level, player, hand);
        }
        if (!level.isClientSide()) {
            player.displayClientMessage(Component.translatable(this.useMessageKey), true);
        }
        return InteractionResult.SUCCESS;
    }
}
