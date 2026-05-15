package org.mydrugs.mydrugs.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.items.data.MutationPayloadData;
import org.mydrugs.mydrugs.items.data.MutationStatValue;
import org.mydrugs.mydrugs.mutation.MutationManager;
import org.mydrugs.mydrugs.mutation.MutationStat;

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

    public static boolean hasBlood(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.BLOOD_AMOUNT.get(), 0) > 0
                && stack.get(ModDataComponents.BLOOD_SAMPLE.get()) != null;
    }

    private static void fillFromTarget(ItemStack stack, LivingEntity target) {
        boolean sterileBeforeDraw = isSterile(stack);
        clearMutagenicBlood(stack);
        stack.set(ModDataComponents.FILLED.get(), true); // optional backward compatibility
        stack.set(ModDataComponents.BLOOD_AMOUNT.get(), CAPACITY_MB);
        stack.set(ModDataComponents.BLOOD_SAMPLE.get(), BloodSample.fromEntity(target));
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(DEFAULT_BLOOD_COLOR));
        markDirty(stack);
        if (!sterileBeforeDraw && target instanceof ServerPlayer serverPlayer) {
            MutationManager.injectDirty(serverPlayer);
        }
    }

    public static boolean hasMutagenicBlood(ItemStack stack) {
        MutationPayloadData payload = stack.get(ModDataComponents.MUTATION_PAYLOAD.get());
        return payload != null && !payload.stats().isEmpty();
    }

    public static boolean isSterile(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.STERILE.get(), false);
    }

    public static boolean isEmptySyringe(ItemStack stack) {
        return stack.is(ModItems.SYRINGE.get()) && !hasBlood(stack) && !hasMutagenicBlood(stack);
    }

    public static boolean canSterilize(ItemStack stack) {
        return isEmptySyringe(stack) && !isSterile(stack);
    }

    public static void markSterile(ItemStack stack) {
        if (stack.is(ModItems.SYRINGE.get())) {
            stack.set(ModDataComponents.STERILE.get(), true);
        }
    }

    public static void markDirty(ItemStack stack) {
        stack.remove(ModDataComponents.STERILE.get());
    }

    public static void clearBloodAndMarkDirty(ItemStack stack) {
        clearBlood(stack);
        markDirty(stack);
    }

    public static void clearMutationPayloadAndMarkDirty(ItemStack stack) {
        clearMutagenicBlood(stack);
        stack.remove(ModDataComponents.FILLED.get());
        stack.remove(DataComponents.DYED_COLOR);
        markDirty(stack);
    }

    private static void clearBlood(ItemStack stack) {
        stack.remove(ModDataComponents.FILLED.get());
        stack.remove(ModDataComponents.BLOOD_AMOUNT.get());
        stack.remove(ModDataComponents.BLOOD_SAMPLE.get());
        stack.remove(DataComponents.DYED_COLOR);
    }

    private static void clearMutagenicBlood(ItemStack stack) {
        stack.remove(ModDataComponents.MUTATION_PAYLOAD.get());
    }

    public static boolean tryLoadMutagenicBlood(ItemStack syringe, ItemStack vial, Player player) {
        if (!syringe.is(ModItems.SYRINGE.get()) || !vial.is(ModItems.MUTAGENIC_BLOOD_VIAL.get())) {
            return false;
        }

        MutationPayloadData payload = vial.get(ModDataComponents.MUTATION_PAYLOAD.get());
        if (payload == null || payload.stats().isEmpty()) {
            return false;
        }

        if (MutationManager.containsSource(payload, player.getUUID().toString())) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.translatable("message.mydrugs.mutation.self_genetics_rejected").withStyle(ChatFormatting.RED), true);
            }
            return false;
        }

        if (!isEmptySyringe(syringe) || !isSterile(syringe)) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.translatable("message.mydrugs.mutation.syringe_dirty").withStyle(ChatFormatting.RED), true);
            }
            return false;
        }

        if (!player.level().isClientSide()) {
            syringe.set(ModDataComponents.MUTATION_PAYLOAD.get(), payload);
            syringe.set(ModDataComponents.FILLED.get(), true);
            syringe.set(DataComponents.DYED_COLOR, new DyedItemColor(0xB3204A));
            if (!player.getAbilities().instabuild) {
                vial.shrink(1);
            }
            player.displayClientMessage(Component.translatable("message.mydrugs.mutation.syringe_loaded").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
        return true;
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

        if (hasMutagenicBlood(handStack)) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }

        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);
        if (tryLoadMutagenicBlood(handStack, otherStack, player)) {
            return InteractionResult.SUCCESS.heldItemTransformedTo(handStack);
        }

        if (hasBlood(handStack)) {
            if (player.isShiftKeyDown()) {
                if (!level.isClientSide()) {
                    clearBloodAndMarkDirty(handStack);
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
        if (!level.isClientSide() && hasMutagenicBlood(stack) && livingEntity instanceof ServerPlayer serverPlayer) {
            injectMutagenicBlood(stack, serverPlayer);
            InteractionHand usedHand = serverPlayer.getUsedItemHand();
            serverPlayer.setItemInHand(usedHand, stack);
            return stack;
        }

        if (!level.isClientSide() && !hasBlood(stack) && !hasMutagenicBlood(stack)) {
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

        if (hasBlood(handStack) || hasMutagenicBlood(handStack)) {
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

    private static void injectMutagenicBlood(ItemStack stack, ServerPlayer player) {
        MutationPayloadData payload = stack.get(ModDataComponents.MUTATION_PAYLOAD.get());
        if (payload == null || payload.stats().isEmpty()) {
            clearMutationPayloadAndMarkDirty(stack);
            return;
        }

        if (!isSterile(stack)) {
            MutationManager.injectDirty(player);
            clearMutationPayloadAndMarkDirty(stack);
            return;
        }

        if (MutationManager.injectSterile(player, payload)) {
            clearMutationPayloadAndMarkDirty(stack);
        }
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
        MutationPayloadData payload = stack.get(ModDataComponents.MUTATION_PAYLOAD.get());

        tooltipAdder.accept(Component.translatable(isSterile(stack)
                        ? "tooltip.mydrugs.syringe.sterile"
                        : "tooltip.mydrugs.syringe.dirty")
                .withStyle(isSterile(stack) ? ChatFormatting.AQUA : ChatFormatting.RED));
        if (!isSterile(stack)) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.syringe.dirty_warning").withStyle(ChatFormatting.YELLOW));
        }

        if (payload != null && !payload.stats().isEmpty()) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.syringe.mutagenic_blood").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.mutation_payload.sources", String.join(", ", payload.sourceNames())).withStyle(ChatFormatting.GRAY));
            for (MutationStatValue statValue : payload.stats()) {
                MutationStat stat = MutationStat.bySerializedNameOrNull(statValue.statId());
                Component statName = stat == null
                        ? Component.translatable("mutation.mydrugs.stat.unknown", statValue.statId())
                        : Component.translatable(stat.translationKey());
                tooltipAdder.accept(Component.translatable(
                        "tooltip.mydrugs.mutation_payload.stat",
                        statName,
                        Math.round(statValue.value() * 100.0F)
                ).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            tooltipAdder.accept(Component.translatable(
                    "tooltip.mydrugs.mutation_payload.rejection_risk",
                    Math.round(payload.rejectionRisk() * 100.0F)
            ).withStyle(ChatFormatting.RED));
            return;
        }

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
                Component.translatable("tooltip.mydrugs.syringe.blood_amount", amount, CAPACITY_MB)
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
                        Component.translatable("tooltip.mydrugs.syringe.source_id", sample.sourceId())
                                .withStyle(ChatFormatting.DARK_GRAY)
                );
            }
        }
    }
}
