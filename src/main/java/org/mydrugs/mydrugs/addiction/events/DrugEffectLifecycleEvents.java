package org.mydrugs.mydrugs.addiction.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.runtime.DrugEffectRuntimeManager;

/**
 * Bridges player/server lifecycle events into {@link DrugEffectRuntimeManager} so acute drug
 * effects persist across logout and restart, are reapplied on login/respawn/dimension change,
 * and never leak stale UUID cache entries for offline players.
 */
@EventBusSubscriber(modid = MyDrugs.MODID)
public final class DrugEffectLifecycleEvents {
    private DrugEffectLifecycleEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DrugEffectRuntimeManager.onPlayerLogin(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DrugEffectRuntimeManager.onPlayerLogout(player);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer original
                && event.getEntity() instanceof ServerPlayer clone) {
            DrugEffectRuntimeManager.onPlayerClone(original, clone, event.isWasDeath());
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DrugEffectRuntimeManager.onDimensionChange(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        DrugEffectRuntimeManager.onServerStop(event.getServer());
    }
}
