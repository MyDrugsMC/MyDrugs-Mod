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
import org.mydrugs.mydrugs.client.BiomeFinderCompassOverlay;
import org.mydrugs.mydrugs.client.PsychotropeAreaPreviewClientState;
import org.mydrugs.mydrugs.client.PsyBlueprintGhostRenderer;
import org.mydrugs.mydrugs.client.PsyBlueprintPreviewClientState;
import org.mydrugs.mydrugs.client.shaders.WithdrawalTunnelShader;
import org.mydrugs.mydrugs.effects.addiction.client.render.AddictionHudRenderer;
import org.mydrugs.mydrugs.effects.addiction.client.render.FlexibleDrugVisualOverlay;
import org.mydrugs.mydrugs.effects.addiction.client.render.BadTripScreamerOverlay;
import org.mydrugs.mydrugs.effects.addiction.client.render.VomitOverlayClientState;
import org.mydrugs.mydrugs.effects.addiction.client.render.hallucination.FakeEntityRenderController;
import org.mydrugs.mydrugs.effects.addiction.client.sound.ClientSoundController;
import org.mydrugs.mydrugs.effects.addiction.client.sound.HeadphonesMusicController;
import org.mydrugs.mydrugs.effects.addiction.client.network.ClientPayloadHandler;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionDebugOpenPayload;
import org.mydrugs.mydrugs.effects.addiction.network.BadTripPayload;
import org.mydrugs.mydrugs.effects.addiction.network.BadTripScreamerPayload;
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
        event.register(BadTripPayload.TYPE, ClientPayloadHandler::handleBadTrip);
        event.register(BadTripScreamerPayload.TYPE, ClientPayloadHandler::handleBadTripScreamer);
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
            BadTripScreamerOverlay.tick();

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
            BadTripScreamerOverlay.render(event.getGuiGraphics());
            AddictionHudRenderer.render(event.getGuiGraphics());
            BiomeFinderCompassOverlay.render(event.getGuiGraphics());
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

        @SubscribeEvent
        public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
            if (!Config.CLIENT.enableCameraShake.get()) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }

            float sway = AddictionClientState.getEffectIntensity(org.mydrugs.mydrugs.core.drug.effect.EffectType.CAMERA_SWAY);
            float tremor = AddictionClientState.getEffectIntensity(org.mydrugs.mydrugs.core.drug.effect.EffectType.TREMOR);
            float reduction = AddictionClientState.getEffectIntensity(org.mydrugs.mydrugs.core.drug.effect.EffectType.TREMOR_REDUCTION);
            tremor = Math.max(0.0F, tremor - reduction);
            if (sway <= 0.01F && tremor <= 0.01F) {
                return;
            }

            float motionScale = Config.CLIENT.reducedMotionMode.get() ? 0.28F : 1.0F;
            float configScale = Config.CLIENT.cameraShakeIntensity.get().floatValue();
            float amplitude = Math.min(1.0F, sway) * 2.4F * motionScale * configScale;
            float tremorAmplitude = Math.min(1.0F, tremor) * 0.85F * motionScale * configScale;
            float time = mc.player.tickCount + (float) event.getPartialTick();

            float yaw = (net.minecraft.util.Mth.sin(time * 0.113F) * 0.70F
                    + net.minecraft.util.Mth.sin(time * 0.047F + 1.71F) * 0.55F) * amplitude;
            float pitch = (net.minecraft.util.Mth.sin(time * 0.091F + 2.32F) * 0.62F
                    + net.minecraft.util.Mth.cos(time * 0.035F + 0.43F) * 0.36F) * amplitude;
            float roll = (net.minecraft.util.Mth.sin(time * 0.079F + 3.65F) * 1.00F
                    + net.minecraft.util.Mth.sin(time * 0.161F + 0.90F) * 0.28F) * amplitude;
            yaw += net.minecraft.util.Mth.sin(time * 0.91F + 0.7F) * tremorAmplitude;
            pitch += net.minecraft.util.Mth.cos(time * 1.13F + 2.2F) * tremorAmplitude;
            roll += net.minecraft.util.Mth.sin(time * 1.37F + 4.1F) * tremorAmplitude * 0.45F;

            event.setYaw(event.getYaw() + yaw);
            event.setPitch(event.getPitch() + pitch);
            event.setRoll(event.getRoll() + roll);
        }
    }
}
