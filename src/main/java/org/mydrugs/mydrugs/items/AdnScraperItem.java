package org.mydrugs.mydrugs.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.damage.ModDamageTypes;

import java.util.List;
import java.util.function.Consumer;

public final class AdnScraperItem extends Item {
    private static final int COOLDOWN_TICKS = 15;
    private static final float SELF_EXTRACTION_DAMAGE = 1.0F;

    private static final List<EntityType<?>> NON_BIOLOGICAL_TYPES = List.of(
            EntityType.SKELETON_HORSE,
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
    );

    public AdnScraperItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (player.getHealth() <= 2.0F) {
                player.displayClientMessage(Component.translatable("message.mydrugs.adn_scraper.too_weak").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            giveScrap(serverPlayer, AdnScrapItem.createScrapFrom(player));
            player.hurt(ModDamageTypes.bloodDraw(player), SELF_EXTRACTION_DAMAGE);
            damageScraper(stack, player, hand);
            player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
            level.playSound(null, player.blockPosition(), SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 0.6F, 0.85F);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        return scrapeTarget(stack, player, target, hand);
    }

    public static InteractionResult scrapeTarget(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (!canScrape(target)) {
            player.displayClientMessage(Component.translatable("message.mydrugs.adn_scraper.invalid_target").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.FAIL;
        }

        giveScrap(serverPlayer, AdnScrapItem.createScrapFrom(target));
        damageScraper(stack, player, hand);
        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        player.level().playSound(null, target.blockPosition(), SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 0.6F, 1.25F);
        player.swing(hand, true);
        return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
    }

    public static boolean canScrape(LivingEntity target) {
        return !NON_BIOLOGICAL_TYPES.contains(target.getType());
    }

    private static void giveScrap(ServerPlayer player, ItemStack scrap) {
        if (!player.getInventory().add(scrap)) {
            player.drop(scrap, false);
        }
    }

    private static void damageScraper(ItemStack stack, Player player, InteractionHand hand) {
        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, hand);
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
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.adn_scraper").withStyle(ChatFormatting.GRAY));
    }
}
