package org.mydrugs.mydrugs.items;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.menu.SingleSlotItemContainer;
import org.mydrugs.mydrugs.menu.SingleSlotMenu;
import org.mydrugs.mydrugs.recipes.grinder.GrindingRecipe;
import org.mydrugs.mydrugs.recipes.grinder.GrindingRecipes;

public class PortableGrinderItem extends Item implements SingleSlotContainerItem {
    private final int FULL_CHARGE_TICKS = 40;

    public PortableGrinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack bang = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (!containsGrindable(bang, (ServerLevel) level)) {
            openMenu(player, hand);
            return InteractionResult.SUCCESS;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack bang = context.getItemInHand();
        if (!containsGrindable(bang, (ServerLevel) level)) {
            openMenu(player, context.getHand());
            return InteractionResult.SUCCESS;
        }

        player.startUsingItem(context.getHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return FULL_CHARGE_TICKS;
    }

    @Override
    public net.minecraft.world.item.ItemUseAnimation getUseAnimation(ItemStack stack) {
        return net.minecraft.world.item.ItemUseAnimation.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack bang, int remainingUseDuration) {

        if (!(living instanceof ServerPlayer)) return;

        super.onUseTick(level, living, bang, remainingUseDuration);

        int usedTicks = this.getUseDuration(bang, living) - remainingUseDuration;
        if (usedTicks - 1 % 20 == 0) {
            level.playSound(
                    null,
                    living.getX(),
                    living.getY(),
                    living.getZ(),
                    SoundEvents.GRINDSTONE_USE,
                    SoundSource.PLAYERS,
                    0.7F,
                    0.8F
            );
        }

        if (usedTicks < FULL_CHARGE_TICKS - 1) {
            return;
        }
        if (!level.isClientSide()) {
            if (grindLoadedContent(bang, (ServerLevel) level)) {
                level.playSound(
                        null,
                        living.getX(),
                        living.getY(),
                        living.getZ(),
                        SoundEvents.GRINDSTONE_USE,
                        SoundSource.PLAYERS,
                        0.7F,
                        0.8F
                );
            }
        }

        living.stopUsingItem();
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeCharged) {
        return super.releaseUsing(stack, level, living, timeCharged);
        // Optional: add "released too early" behavior here.
    }

    private void openMenu(Player player, InteractionHand hand) {
        ItemStack carrier = player.getItemInHand(hand);

        MenuProvider provider = new SimpleMenuProvider(
                (containerId, playerInventory, p) -> new SingleSlotMenu(containerId, playerInventory, carrier, hand, (ServerLevel) p.level()),
                Component.translatable("menu.mydrugs.single_slot_container")
        );

        player.openMenu(provider);
    }

    private boolean containsGrindable(ItemStack bang, ServerLevel level) {
        SingleSlotItemContainer inv = new SingleSlotItemContainer(bang);
        ItemStack in = inv.getItem(0);
        return GrindingRecipes.get(in, level) != null;
    }

    private boolean grindLoadedContent(ItemStack grinder, ServerLevel level) {
        SingleSlotItemContainer inv = new SingleSlotItemContainer(grinder);
        ItemStack inside = inv.getItem(0);
        GrindingRecipe recipe = GrindingRecipes.get(inside, level);
        if (recipe == null) return false;
        inv.removeAllItems();
        inv.addItem(recipe.result());
        inv.setChanged();
        return !inv.getItem(0).isEmpty();
    }

    @Override
    public boolean mayPlace(ItemStack itemStack, ServerLevel level) {
        return GrindingRecipes.get(itemStack, level) != null;
    }
}
