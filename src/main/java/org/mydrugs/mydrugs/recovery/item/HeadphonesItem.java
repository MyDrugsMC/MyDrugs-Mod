package org.mydrugs.mydrugs.recovery.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.addiction.manager.ItemEffectHandler;

public final class HeadphonesItem extends Item {
    private static final int INTERACT_COOLDOWN = 10;

    public HeadphonesItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                ItemEffectHandler.cycleHeadphonesTrack(serverPlayer);
                serverPlayer.sendSystemMessage(Component.translatable("message.mydrugs.headphones.track_changed"));
            } else {
                boolean enabled = ItemEffectHandler.toggleHeadphones(serverPlayer);
                serverPlayer.sendSystemMessage(Component.translatable(
                        enabled ? "message.mydrugs.headphones.enabled" : "message.mydrugs.headphones.disabled"
                ));
            }

            player.getCooldowns().addCooldown(stack, INTERACT_COOLDOWN);
        }

        return InteractionResult.SUCCESS;
    }
}
