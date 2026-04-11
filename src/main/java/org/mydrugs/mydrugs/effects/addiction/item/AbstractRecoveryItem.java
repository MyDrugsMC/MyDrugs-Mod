package org.mydrugs.mydrugs.effects.addiction.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

public abstract class AbstractRecoveryItem extends Item {
    private final int useDuration;
    private final int cooldownTicks;
    private final ItemUseAnimation useAnim;
    private final boolean consumeOnFinish;

    protected AbstractRecoveryItem(
            Properties properties,
            int useDuration,
            int cooldownTicks,
            ItemUseAnimation useAnim,
            boolean consumeOnFinish
    ) {
        super(properties);
        this.useDuration = useDuration;
        this.cooldownTicks = cooldownTicks;
        this.useAnim = useAnim;
        this.consumeOnFinish = consumeOnFinish;
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
        return useDuration;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return useAnim;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            applyEffects(player);
            afterUse(player);

            player.getCooldowns().addCooldown(stack, cooldownTicks);

            if (consumeOnFinish) {
                stack.consume(1, player);
            }
        }

        return stack;
    }

    protected void afterUse(ServerPlayer player) {
    }

    protected abstract void applyEffects(ServerPlayer player);
}