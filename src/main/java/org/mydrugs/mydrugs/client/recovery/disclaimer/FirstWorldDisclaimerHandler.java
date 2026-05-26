package org.mydrugs.mydrugs.client.recovery.disclaimer;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.client.recovery.music.tools.MyDrugsClientConfig;

/**
 * Opens the {@link DisclaimerScreen} the first time a player enters a world with the mod present,
 * unless the content notice has already been acknowledged.
 *
 * <p>Client-only. The open is deferred to a client tick so the world is fully loaded and no other
 * screen is being shown — there is no flashing and no sudden sound.
 */
@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class FirstWorldDisclaimerHandler {
    private static boolean pending;
    private static boolean shownThisSession;

    private FirstWorldDisclaimerHandler() {
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        pending = !shownThisSession && !MyDrugsClientConfig.get().isDisclaimerAcknowledged();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!pending) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) {
            return;
        }
        pending = false;
        shownThisSession = true;
        mc.setScreen(new DisclaimerScreen(false));
    }

    static void acknowledgeDisclaimer() {
        pending = false;
        shownThisSession = true;
        MyDrugsClientConfig.get().setDisclaimerAcknowledged(true);
    }

    static void resetAcknowledgementForNextJoin() {
        pending = false;
        shownThisSession = false;
        MyDrugsClientConfig.get().setDisclaimerAcknowledged(false);
    }
}
