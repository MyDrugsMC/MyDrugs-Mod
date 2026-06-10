package org.mydrugs.mydrugs.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.mydrugs.mydrugs.mutation.MutationManager;
import org.mydrugs.mydrugs.recovery.item.AbstractRecoveryItem;

import java.util.function.Consumer;

public final class AntibioticDoseItem extends AbstractRecoveryItem {
    public static final float SEVERITY_REDUCTION = 0.20F;
    public static final int ROLLBACK_TICKS = 20 * 75;
    public static final int COOLDOWN_TICKS = 20 * 30;

    public AntibioticDoseItem(Properties properties) {
        super(properties.stacksTo(16), 32, COOLDOWN_TICKS, ItemUseAnimation.DRINK, true);
    }

    @Override
    protected void applyEffects(ServerPlayer player) {
        MutationManager.treatInfection(player, SEVERITY_REDUCTION, ROLLBACK_TICKS);
        player.displayClientMessage(
                Component.translatable("message.mydrugs.antibiotic.oral_treatment").withStyle(ChatFormatting.GREEN),
                true
        );
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
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.antibiotic_dose.effect").withStyle(ChatFormatting.GREEN));
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.antibiotic_dose.early").withStyle(ChatFormatting.GRAY));
    }
}
