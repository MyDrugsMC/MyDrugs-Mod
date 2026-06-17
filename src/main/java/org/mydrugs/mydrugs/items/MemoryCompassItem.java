package org.mydrugs.mydrugs.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.dimension.inner.InnerObjectiveHelper;

import java.util.function.Consumer;

public final class MemoryCompassItem extends Item {
    private static final int USE_COOLDOWN_TICKS = 20;

    public MemoryCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        serverPlayer.sendSystemMessage(
                InnerObjectiveHelper.memoryCompassMessage(serverPlayer)
                        .copy()
                        .withStyle(ChatFormatting.AQUA)
        );
        player.getCooldowns().addCooldown(stack, USE_COOLDOWN_TICKS);
        return InteractionResult.SUCCESS;
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
        tooltipAdder.accept(Component.translatable(
                "tooltip.mydrugs.memory_compass"
        ).withStyle(ChatFormatting.GRAY));
    }
}
