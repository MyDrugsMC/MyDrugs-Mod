package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.dimension.InnerDimensions;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class InnerTrialEvents {
    private InnerTrialEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.level().dimension().equals(InnerDimensions.INNER_LEVEL)) {
            InnerTrialManager.tickPlayer(player);
            InnerCommunicationManager.tickPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(InnerDimensions.INNER_LEVEL)) {
            return;
        }
        if (InnerTrialManager.handleRightClick(player, level, event.getPos(), event.getHand())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getLevel() instanceof ServerLevel level
                && level.dimension().equals(InnerDimensions.INNER_LEVEL)) {
            InnerTrialManager.handlePlacement(player, level, event.getPos(), event.getPlacedBlock());
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.level().dimension().equals(InnerDimensions.INNER_LEVEL)) {
            InnerTrialManager.resetStillPoint(player);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.level().dimension().equals(InnerDimensions.INNER_LEVEL)) {
            InnerTrialManager.resetStillPoint(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            InnerTrialManager.clearPlayer(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // Progress resets when leaving the Inner Dimension so a round-trip
        // starts fresh rather than carrying stale mid-trial state.
        if (event.getEntity() instanceof ServerPlayer player
                && event.getFrom().equals(InnerDimensions.INNER_LEVEL)) {
            InnerTrialManager.clearPlayer(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        InnerTrialManager.clearAll();
        InnerCommunicationManager.clearAll();
        InnerMessageCooldowns.clearAll();
    }
}
