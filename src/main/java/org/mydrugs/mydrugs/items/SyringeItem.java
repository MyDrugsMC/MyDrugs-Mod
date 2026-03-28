package org.mydrugs.mydrugs.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.damage.ModDamageTypes;
import org.mydrugs.mydrugs.items.data.BloodSample;
import org.mydrugs.mydrugs.registry.ModDataComponents;

import java.util.List;
import java.util.function.Consumer;

public class SyringeItem extends Item {
    public static final int CAPACITY_MB = 100;

    private static final int FULL_CHARGE_TICKS = 20;
    private static final float BLOOD_DRAW_DAMAGE = 0.5F;
    private static final int DEFAULT_BLOOD_COLOR = 0x8E1B1B;

    public SyringeItem(Properties properties) {
        super(properties);
    }

    private static boolean hasBlood(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.BLOOD_AMOUNT.get(), 0) > 0
                && stack.get(ModDataComponents.BLOOD_SAMPLE.get()) != null;
    }

    private static void fillFromTarget(ItemStack stack, LivingEntity target) {
        stack.set(ModDataComponents.FILLED.get(), true); // optional backward compatibility
        stack.set(ModDataComponents.BLOOD_AMOUNT.get(), CAPACITY_MB);
        stack.set(ModDataComponents.BLOOD_SAMPLE.get(), BloodSample.fromEntity(target));
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(DEFAULT_BLOOD_COLOR));
    }

    private static void clearBlood(ItemStack stack) {
        stack.remove(ModDataComponents.FILLED.get());
        stack.remove(ModDataComponents.BLOOD_AMOUNT.get());
        stack.remove(ModDataComponents.BLOOD_SAMPLE.get());
        stack.remove(DataComponents.DYED_COLOR);
    }

    private static void hurtForBloodDraw(LivingEntity target, LivingEntity causer) {
        if (target instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return;
            }
        }

        target.hurt(ModDamageTypes.bloodDraw(causer), BLOOD_DRAW_DAMAGE);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack handStack = player.getItemInHand(hand);

        if (hasBlood(handStack)) {
            if (player.isShiftKeyDown()) {
                if (!level.isClientSide()) {
                    clearBlood(handStack);
                    player.setItemInHand(hand, handStack);
                }
                return InteractionResult.SUCCESS.heldItemTransformedTo(handStack);
            }

            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide() && !hasBlood(stack)) {
            fillFromTarget(stack, livingEntity);
            hurtForBloodDraw(livingEntity, livingEntity);

            if (livingEntity instanceof Player player) {
                InteractionHand usedHand = player.getUsedItemHand();
                player.setItemInHand(usedHand, stack);
            }
        }

        return stack;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        ItemStack handStack = player.getItemInHand(hand);

        if (hasBlood(handStack)) {
            return InteractionResult.FAIL;
        }

        if (List.of(EntityType.SKELETON_HORSE,
                EntityType.SKELETON,
                EntityType.WITHER_SKELETON,
                EntityType.STRAY,
                EntityType.IRON_GOLEM,
                EntityType.COPPER_GOLEM,
                EntityType.SNOW_GOLEM,
                EntityType.ALLAY,
                EntityType.VEX,
                EntityType.BOGGED,
                EntityType.CREAKING
        ).contains(target.getType())) return InteractionResult.FAIL;

        if (!player.level().isClientSide()) {
            fillFromTarget(handStack, target);
            hurtForBloodDraw(target, player);
            player.setItemInHand(hand, handStack);
        }

        return InteractionResult.SUCCESS.heldItemTransformedTo(handStack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return FULL_CHARGE_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
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

        BloodSample sample = stack.get(ModDataComponents.BLOOD_SAMPLE.get());
        int amount = stack.getOrDefault(ModDataComponents.BLOOD_AMOUNT.get(), 0);

        if (amount <= 0 || sample == null) {
            tooltipAdder.accept(
                    Component.translatable("tooltip.mydrugs.syringe.empty")
                            .withStyle(ChatFormatting.GRAY)
            );
            return;
        }

        tooltipAdder.accept(
                Component.translatable("tooltip.mydrugs.syringe.filled")
                        .withStyle(ChatFormatting.RED)
        );
        tooltipAdder.accept(
                Component.literal(amount + " / " + CAPACITY_MB + " mB blood")
                        .withStyle(ChatFormatting.DARK_RED)
        );

        if (sample.isPlayerSource()) {
            tooltipAdder.accept(
                    Component.translatable("tooltip.mydrugs.syringe.source_player", sample.sourceName())
                            .withStyle(ChatFormatting.GRAY)
            );
        } else {
            tooltipAdder.accept(
                    Component.translatable("tooltip.mydrugs.syringe.source_entity", sample.sourceName())
                            .withStyle(ChatFormatting.GRAY)
            );

            if (flag.isAdvanced()) {
                tooltipAdder.accept(
                        Component.literal(sample.sourceId())
                                .withStyle(ChatFormatting.DARK_GRAY)
                );
            }
        }
    }
}