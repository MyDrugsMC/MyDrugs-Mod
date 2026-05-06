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
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.client.PsychotropeAreaPreviewClientState;
import org.mydrugs.mydrugs.client.PsyBlueprintGhostRenderer;
import org.mydrugs.mydrugs.client.PsyBlueprintPreviewClientState;
import org.mydrugs.mydrugs.client.shaders.WithdrawalTunnelShader;
import org.mydrugs.mydrugs.effects.addiction.client.render.AddictionHudRenderer;
import org.mydrugs.mydrugs.effects.addiction.client.render.FlexibleDrugVisualOverlay;
import org.mydrugs.mydrugs.effects.addiction.client.render.VomitOverlayClientState;
import org.mydrugs.mydrugs.effects.addiction.client.render.hallucination.FakeEntityRenderController;
import org.mydrugs.mydrugs.effects.addiction.client.sound.ClientSoundController;
import org.mydrugs.mydrugs.effects.addiction.client.sound.HeadphonesMusicController;
import org.mydrugs.mydrugs.effects.addiction.client.network.ClientPayloadHandler;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionDebugOpenPayload;
import org.mydrugs.mydrugs.effects.addiction.network.DoseSyncPayload;
import org.mydrugs.mydrugs.effects.addiction.network.HeadphonesStatePayload;
import org.mydrugs.mydrugs.effects.addiction.network.DrugEffectSyncPayload;
import org.mydrugs.mydrugs.effects.addiction.network.VomitOverlayPayload;
import org.mydrugs.mydrugs.effects.addiction.client.input.ClientInputInterceptor;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class ClientEventHandler {
    private ClientEventHandler() {
    }

    @SubscribeEvent
    public static void onRegisterClientPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(AddictionClientSnapshotPayload.TYPE, ClientPayloadHandler::handleSnapshot);
        event.register(HeadphonesStatePayload.TYPE, ClientPayloadHandler::handleHeadphonesState);
        event.register(DoseSyncPayload.TYPE, ClientPayloadHandler::handleDoseSync);
        event.register(DrugEffectSyncPayload.TYPE, ClientPayloadHandler::handleDrugEffectSync);
        event.register(VomitOverlayPayload.TYPE, ClientPayloadHandler::handleVomitOverlay);
        event.register(AddictionDebugOpenPayload.TYPE, ClientPayloadHandler::handleAddictionDebugOpen);
    }

    @EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
    public static final class Game {
        private Game() {
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();

            AddictionClientState.tick();
            ClientSoundController.tick();
            HeadphonesMusicController.tick();
            FakeEntityRenderController.tick();
            HeartbeatPulse.tick();
            PsychotropeAreaPreviewClientState.tick();
            PsyBlueprintPreviewClientState.tick();
            VomitOverlayClientState.tick();

            WithdrawalTunnelShader.INSTANCE.tick(mc);
            ClientInputInterceptor.tick(mc);
            ClientGammaController.tick(mc);
        }


        @SubscribeEvent
        public static void onMovementInput(net.neoforged.neoforge.client.event.MovementInputUpdateEvent event) {
            ClientInputInterceptor.applyToInput(event.getInput(), event.getEntity().tickCount);
        }

        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent.AfterParticles event) {
            FakeEntityRenderController.render(event);
            PsyBlueprintGhostRenderer.render(event);
        }

        @SubscribeEvent
        public static void onRenderGui(RenderGuiEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();

            if (WithdrawalTunnelShader.INSTANCE.shouldRender()) {
                WithdrawalTunnelShader.INSTANCE.render(mc);
            }

            FlexibleDrugVisualOverlay.render(event.getGuiGraphics());
            VomitOverlayClientState.render(event.getGuiGraphics());
            AddictionHudRenderer.render(event.getGuiGraphics());
        }

        @SubscribeEvent
        public static void onComputeFov(ViewportEvent.ComputeFov event) {
            if (Config.CLIENT.reducedMotionMode.get() || !Config.CLIENT.enableCameraShake.get()) {
                return;
            }
            float original = event.getFOV();
            float withdrawal = WithdrawalTunnelShader.INSTANCE.getStrength();

            float intensity = Config.CLIENT.cameraShakeIntensity.get().floatValue();
            float baseTunnel = withdrawal * 4.0F * intensity;
            float beatKick = HeartbeatPulse.getFovOffset(withdrawal) * intensity;
            float customPulse = AddictionClientState.getEffectIntensity(org.mydrugs.mydrugs.core.drug.effect.EffectType.CUSTOM_NAUSEA) * 2.5F;
            customPulse += AddictionClientState.getEffectIntensity(org.mydrugs.mydrugs.core.drug.effect.EffectType.BLUR) * 1.5F;

            event.setFOV(original - baseTunnel - beatKick - customPulse);
        }
    }
}
