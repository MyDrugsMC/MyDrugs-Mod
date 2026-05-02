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
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugHolder;
import org.mydrugs.mydrugs.core.drug.use.DrugUseSource;
import org.mydrugs.mydrugs.core.drug.strategy.SmokingStrategy;
import org.mydrugs.mydrugs.menu.SingleSlotItemContainer;
import org.mydrugs.mydrugs.menu.SingleSlotMenu;

public class BangItem extends Item implements SingleSlotContainerItem {
    private static final int FULL_CHARGE_TICKS = 20; // 1 second

    public BangItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack bang = player.getItemInHand(hand);

        // Shift + right click => open inventory
        if (player.isSecondaryUseActive()) {
            if (!level.isClientSide()) {
                openMenu(player, hand);
            }
            return InteractionResult.SUCCESS;
        }

        // Normal right click => start charging, but only if something is loaded
        if (!hasLoadedContent(bang)) {
            return InteractionResult.FAIL;
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

        if (player.isSecondaryUseActive()) {
            if (!context.getLevel().isClientSide()) {
                openMenu(player, context.getHand());
            }
            return InteractionResult.SUCCESS;
        }

        ItemStack bang = context.getItemInHand();
        if (!hasLoadedContent(bang)) {
            return InteractionResult.FAIL;
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

        if (!(living instanceof ServerPlayer serverPlayer)) return;

        super.onUseTick(level, living, bang, remainingUseDuration);

        int usedTicks = this.getUseDuration(bang, living) - remainingUseDuration;

        if (usedTicks < FULL_CHARGE_TICKS - 1) {
            return;
        }
        if (!level.isClientSide()) {
            ItemStack consumed = consumeLoadedContent(bang);
            if (!consumed.isEmpty()) {
                MyDrugs.DRUG_USE_SERVICE.consumeStack(
                        serverPlayer,
                        consumed,
                        new SmokingStrategy(true, false),
                        DrugUseSource.BANG
                );
                level.playSound(
                        null,
                        living.getX(),
                        living.getY(),
                        living.getZ(),
                        SoundEvents.FIRE_EXTINGUISH,
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

    private boolean hasLoadedContent(ItemStack bang) {
        SingleSlotItemContainer inv = new SingleSlotItemContainer(bang);
        return !inv.getItem(0).isEmpty();
    }

    private ItemStack consumeLoadedContent(ItemStack bang) {
        SingleSlotItemContainer inv = new SingleSlotItemContainer(bang);
        ItemStack inside = inv.removeItemNoUpdate(0);
        inv.setChanged();
        return inside;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack, ServerLevel level) {
        if (!(itemStack.getItem() instanceof DrugHolder holder)) return false;
        return holder.getConsumptionStrategy() instanceof SmokingStrategy;
    }
}
