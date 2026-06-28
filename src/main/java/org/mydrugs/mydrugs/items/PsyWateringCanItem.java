package org.mydrugs.mydrugs.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Consumer;

public final class PsyWateringCanItem extends Item {
    private static final int RADIUS = 2;
    private static final int VERTICAL_RADIUS = 2;
    private static final int GROWTH_ATTEMPTS = 10;
    private static final int USE_COOLDOWN_TICKS = 5;

    public PsyWateringCanItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos origin = context.getClickedPos();
        if (level.isClientSide()) {
            return hasAcceleratableCrop(level, origin) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel) || context.getPlayer() == null) {
            return InteractionResult.PASS;
        }

        return water(serverLevel, origin, context.getPlayer(), context.getHand(), context.getItemInHand());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockPos origin = player.blockPosition();
        if (level.isClientSide()) {
            return hasAcceleratableCrop(level, origin) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        return water(serverLevel, origin, player, hand, stack);
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
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.psy_watering_can.water"));
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.psy_watering_can.growth"));
    }

    private InteractionResult water(ServerLevel level, BlockPos origin, Player player, InteractionHand hand, ItemStack stack) {
        int affected = accelerateCrops(level, origin);
        if (affected <= 0) {
            return InteractionResult.PASS;
        }

        level.playSound(null, origin, SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 0.7F, 1.15F);
        player.getCooldowns().addCooldown(stack, USE_COOLDOWN_TICKS);
        player.swing(hand, true);
        player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        return InteractionResult.SUCCESS;
    }

    private int accelerateCrops(ServerLevel level, BlockPos origin) {
        int affected = 0;
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -VERTICAL_RADIUS; y <= VERTICAL_RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (tryAccelerateCrop(level, pos)) {
                        level.levelEvent(1505, pos, 3);
                        affected++;
                    }
                }
            }
        }
        return affected;
    }

    private boolean hasAcceleratableCrop(Level level, BlockPos origin) {
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -VERTICAL_RADIUS; y <= VERTICAL_RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    if (isAcceleratableCrop(level, origin.offset(x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean tryAccelerateCrop(ServerLevel level, BlockPos pos) {
        if (!isAcceleratableCrop(level, pos)) {
            return false;
        }

        for (int attempt = 0; attempt < GROWTH_ATTEMPTS; attempt++) {
            BlockState currentState = level.getBlockState(pos);
            if (!isAcceleratableCrop(level, pos)) {
                break;
            }
            currentState.randomTick(level, pos, level.random);
        }

        return true;
    }

    private boolean isAcceleratableCrop(Level level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof CropBlock && state.isRandomlyTicking();
    }
}
