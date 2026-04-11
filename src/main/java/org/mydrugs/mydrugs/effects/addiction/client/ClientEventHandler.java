package org.mydrugs.mydrugs.effects.addiction.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.client.shaders.WithdrawalTunnelShader;
import org.mydrugs.mydrugs.effects.addiction.client.render.AddictionHudRenderer;
import org.mydrugs.mydrugs.effects.addiction.client.render.hallucination.FakeEntityRenderController;
import org.mydrugs.mydrugs.effects.addiction.client.sound.ClientSoundController;
import org.mydrugs.mydrugs.effects.addiction.client.sound.HeadphonesMusicController;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionPayloads;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class ClientEventHandler {
    private ClientEventHandler() {}

    @SubscribeEvent
    public static void onRegisterClientPayloads(RegisterClientPayloadHandlersEvent event) {
        AddictionPayloads.registerClient(event);
    }

    @EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
    public static final class Game {
        private Game() {}

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();

            AddictionClientState.tick();
            ClientSoundController.tick();
            HeadphonesMusicController.tick();
            FakeEntityRenderController.tick();
            HeartbeatPulse.tick();

            WithdrawalTunnelShader.INSTANCE.tick(mc);
        }

        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent.AfterParticles event) {
            FakeEntityRenderController.render(event);
        }

        @SubscribeEvent
        public static void onRenderGui(RenderGuiEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();

            if (WithdrawalTunnelShader.INSTANCE.shouldRender()) {
                WithdrawalTunnelShader.INSTANCE.render(mc);
            }

            AddictionHudRenderer.render(event.getGuiGraphics());
        }

        @SubscribeEvent
        public static void onComputeFov(ViewportEvent.ComputeFov event) {
            float original = event.getFOV();
            float withdrawal = WithdrawalTunnelShader.INSTANCE.getStrength();

            float baseTunnel = withdrawal * 4.0F;
            float beatKick = HeartbeatPulse.getFovOffset(withdrawal);

            event.setFOV(original - baseTunnel - beatKick);
        }
    }
}