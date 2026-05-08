package org.mydrugs.mydrugs.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.network.StimulantDashPayload;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class StimulantDashClient {
    private static final KeyMapping DASH_KEY = new KeyMapping(
            "key.mydrugs.stimulant_dash",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KeyMapping.Category.GAMEPLAY
    );

    private StimulantDashClient() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(DASH_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (DASH_KEY.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null || minecraft.screen != null) {
                continue;
            }
            float forward = player.zza;
            float strafe = player.xxa;
            if (Math.abs(forward) < 0.001F && Math.abs(strafe) < 0.001F) {
                forward = 1.0F;
            }
            ClientPacketDistributor.sendToServer(new StimulantDashPayload(forward, strafe));
        }
    }
}
