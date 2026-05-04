package org.mydrugs.mydrugs.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class LogStrippingEvents {
    private static final int REQUIRED_STEPS = 3;

    // After this many ticks without another axe use, the crack overlay disappears.
    private static final int RESET_AFTER_TICKS = 80; // 4 seconds

    private static final Map<ServerLevel, Map<BlockPos, StripProgress>> STRIP_PROGRESS = new WeakHashMap<>();

    private record StripProgress(int step, long lastTouchedGameTime) {}

    private LogStrippingEvents() {}

    @SubscribeEvent
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        // Axe stripping happens in the item-after-block phase.
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_AFTER_BLOCK) {
            return;
        }

        UseOnContext context = event.getUseOnContext();
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player == null) {
            return;
        }

        ItemStack axeStack = context.getItemInHand();

        // Supports vanilla axes and modded axes that expose the axe-strip ability.
        if (!axeStack.canPerformAction(ItemAbilities.AXE_STRIP)) {
            return;
        }

        BlockPos pos = context.getClickedPos();
        BlockState currentState = level.getBlockState(pos);

        // Ask NeoForge/vanilla: "what would this block become if stripped?"
        // Returns null if this block is not strip-able.
        BlockState strippedState = currentState.getToolModifiedState(
                context,
                ItemAbilities.AXE_STRIP,
                false
        );

        if (strippedState == null) {
            return;
        }

        // Cancel vanilla axe stripping on BOTH sides.
        // Server does the actual progress, drops, durability, and block change.
        event.cancelWithResult(InteractionResult.SUCCESS_SERVER);

        if (level.isClientSide()) {
            return;
        }

        doStrippingStep((ServerLevel) level, player, pos, strippedState, axeStack, context.getHand());
    }

    private static void doStrippingStep(
            ServerLevel level,
            Player player,
            BlockPos pos,
            BlockState strippedState,
            ItemStack axeStack,
            InteractionHand hand
    ) {
        Map<BlockPos, StripProgress> map = STRIP_PROGRESS.computeIfAbsent(level, ignored -> new HashMap<>());

        BlockPos immutablePos = pos.immutable();
        StripProgress oldProgress = map.getOrDefault(immutablePos, new StripProgress(0, level.getGameTime()));

        int newStep = oldProgress.step() + 1;

        // Drop one resin per axe step.
        // If you want only ONE resin total, move this line into the final-step branch below.
        dropResin(level, pos);

        level.playSound(
                null,
                pos,
                SoundEvents.AXE_STRIP,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        hurtAxe(player, axeStack, hand);
        player.awardStat(Stats.ITEM_USED.get(axeStack.getItem()));

        if (newStep >= REQUIRED_STEPS) {
            map.remove(immutablePos);

            // Clear crack overlay.
            level.destroyBlockProgress(breakerId(pos), pos, -1);

            // Finally strip the log.
            level.setBlock(pos, strippedState, Block.UPDATE_ALL);
        } else {
            map.put(immutablePos, new StripProgress(newStep, level.getGameTime()));

            // Vanilla crack overlay stages are 0..9.
            // For 3 steps: first click shows ~3, second click shows ~6.
            int crackStage = Math.min(9, (newStep * 10) / REQUIRED_STEPS);

            level.destroyBlockProgress(breakerId(pos), pos, crackStage);
        }
    }

    private static void dropResin(ServerLevel level, BlockPos pos) {
        Block.popResource(
                level,
                pos,
                new ItemStack(ModItems.RESIN.get()) // or ModItem.RESIN.get()
        );
    }

    private static void hurtAxe(Player player, ItemStack axeStack, InteractionHand hand) {
        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND
                : EquipmentSlot.OFFHAND;

        axeStack.hurtAndBreak(1, player, slot);
    }

    private static int breakerId(BlockPos pos) {
        // destroyBlockProgress needs a stable id so later calls can update/clear the same overlay.
        return pos.hashCode();
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            clearProgress(level, event.getPos());
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        Map<BlockPos, StripProgress> map = STRIP_PROGRESS.get(level);
        if (map == null || map.isEmpty()) {
            return;
        }

        long now = level.getGameTime();

        Iterator<Map.Entry<BlockPos, StripProgress>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, StripProgress> entry = iterator.next();

            if (now - entry.getValue().lastTouchedGameTime() > RESET_AFTER_TICKS) {
                BlockPos pos = entry.getKey();
                level.destroyBlockProgress(breakerId(pos), pos, -1);
                iterator.remove();
            }
        }
    }

    private static void clearProgress(ServerLevel level, BlockPos pos) {
        Map<BlockPos, StripProgress> map = STRIP_PROGRESS.get(level);
        if (map != null) {
            map.remove(pos);
        }

        level.destroyBlockProgress(breakerId(pos), pos, -1);
    }
}