package org.mydrugs.mydrugs.addiction.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.runtime.ServerInputDisruptionManager;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ServerInputDisruptionEvents {
    private ServerInputDisruptionEvents() {
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && ServerInputDisruptionManager.maybeCancelAction(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && ServerInputDisruptionManager.maybeCancelAction(player)) {
            event.setCanceled(true);
            event.setCancellationResult(ServerInputDisruptionManager.disruptedResult());
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player && ServerInputDisruptionManager.maybeCancelAction(player)) {
            event.setCanceled(true);
            event.setCancellationResult(ServerInputDisruptionManager.disruptedResult());
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer player && ServerInputDisruptionManager.maybeCancelAction(player)) {
            event.setCanceled(true);
            event.setCancellationResult(ServerInputDisruptionManager.disruptedResult());
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && ServerInputDisruptionManager.maybeCancelAction(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && ServerInputDisruptionManager.maybeCancelAction(player)) {
            event.setCanceled(true);
        }
    }
}
