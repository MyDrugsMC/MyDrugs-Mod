package org.mydrugs.mydrugs.items;

import net.minecraft.ChatFormatting;
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
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.mutation.MutationManager;

import java.util.function.Consumer;

public final class AntibioticVialItem extends Item implements SterilizableItem {
    public static final float SEVERITY_REDUCTION = 0.55F;
    public static final int ROLLBACK_TICKS = 20 * 60 * 3;
    public static final int COOLDOWN_TICKS = 20 * 60;
    private static final int USE_TICKS = 20;

    private final boolean sterile;

    public AntibioticVialItem(Properties properties, boolean sterile) {
        super(properties.stacksTo(8));
        this.sterile = sterile;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            if (this.sterile) {
                MutationManager.treatInfection(player, SEVERITY_REDUCTION, ROLLBACK_TICKS);
                player.displayClientMessage(
                        Component.translatable("message.mydrugs.antibiotic.sterile_treatment").withStyle(ChatFormatting.AQUA),
                        true
                );
            } else {
                MutationManager.injectDirty(player);
            }
            player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
            stack.consume(1, player);
        }
        return stack;
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
        tooltipAdder.accept(Component.translatable(this.sterile
                        ? "tooltip.mydrugs.sterile_antibiotic_vial"
                        : "tooltip.mydrugs.crude_antibiotic_vial")
                .withStyle(this.sterile ? ChatFormatting.AQUA : ChatFormatting.RED));
    }

    @Override
    public boolean canBeSterilized(ItemStack stack) {
        return !this.sterile && stack.is(ModItems.CRUDE_ANTIBIOTIC_VIAL.get());
    }

    @Override
    public ItemStack createSterilizedStack(ItemStack stack) {
        return new ItemStack(ModItems.STERILE_ANTIBIOTIC_VIAL.get());
    }
}
