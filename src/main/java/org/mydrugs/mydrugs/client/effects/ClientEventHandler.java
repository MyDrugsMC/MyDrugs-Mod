package org.mydrugs.mydrugs.client.effects;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.client.BiomeFinderCompassOverlay;
import org.mydrugs.mydrugs.client.PsyBlueprintGhostRenderer;
import org.mydrugs.mydrugs.client.PsyBlueprintPreviewClientState;
import org.mydrugs.mydrugs.client.psy_mixer.PsyMixerRitualClientState;
import org.mydrugs.mydrugs.client.psy_mixer.PsyMixerRitualOverlay;
import org.mydrugs.mydrugs.client.recovery.RecoveryRoomOverlay;
import org.mydrugs.mydrugs.client.recovery.RecoveryRoomParticleClient;
import org.mydrugs.mydrugs.client.recovery.music.CustomDiscPlaybackController;
import org.mydrugs.mydrugs.client.shaders.ShaderManager;
import org.mydrugs.mydrugs.client.sounds.ClientSoundsHandler;
import org.mydrugs.mydrugs.client.shaders.WithdrawalTunnelShader;
import org.mydrugs.mydrugs.client.effects.hud.AddictionHudRenderer;
import org.mydrugs.mydrugs.client.effects.render.FlexibleDrugVisualOverlay;
import org.mydrugs.mydrugs.client.effects.render.BadTripScreamerOverlay;
import org.mydrugs.mydrugs.client.effects.render.BadTripSkyTint;
import org.mydrugs.mydrugs.client.effects.render.VomitOverlayClientState;
import org.mydrugs.mydrugs.client.effects.hallucination.FakeEntityRenderController;
import org.mydrugs.mydrugs.client.effects.sound.ClientSoundController;
import org.mydrugs.mydrugs.client.effects.sound.HeadphonesMusicController;
import org.mydrugs.mydrugs.client.effects.input.ClientInputInterceptor;

/**
 * Hosts {@link Dist#CLIENT}-side gameplay subscribers. Client payload handler
 * registration lives in {@link org.mydrugs.mydrugs.client.network.ClientPayloadHandlers}.
 */
@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class ClientEventHandler {
    private ClientEventHandler() {
    }

        @SubscribeEvent
        public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
            clearWorldScopedState();
        }

        @SubscribeEvent
        public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            clearWorldScopedState();
        }

        private static void clearWorldScopedState() {
            AddictionClientState.clear();
            ShaderManager.INSTANCE.clearActive();
            ClientSoundsHandler.clear();
            HeadphonesMusicController.clear();
            HeartbeatPulse.clear();
            FakeEntityRenderController.clear();
            VomitOverlayClientState.clear();
            BadTripScreamerOverlay.clear();
            BadTripSkyTint.clear();
            PsyMixerRitualClientState.clear();
            RecoveryRoomOverlay.clear();
            RecoveryRoomParticleClient.clear();
            CustomDiscPlaybackController.clear();
            org.mydrugs.mydrugs.client.DistillateEngineAreaPreviewClientState.clear();
            org.mydrugs.mydrugs.client.PsyCurrentPulseClientState.clear();
            org.mydrugs.mydrugs.client.InnerEntrySequence.clear();
            org.mydrugs.mydrugs.client.InnerSkyClientState.clear();
            org.mydrugs.mydrugs.client.InnerRegionCrossingController.clear();
            org.mydrugs.mydrugs.client.InnerSoundscapeController.clear();
            org.mydrugs.mydrugs.client.InnerPlayerResponseController.clear();
            org.mydrugs.mydrugs.client.InnerAtmosphereClient.clear();
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();

            AddictionClientState.tick();
            ClientSoundController.tick();
            HeadphonesMusicController.tick();
            FakeEntityRenderController.tick();
            HeartbeatPulse.tick();
            org.mydrugs.mydrugs.client.DistillateEngineAreaPreviewClientState.tick();
            org.mydrugs.mydrugs.client.PsyCurrentPulseClientState.tick();
            PsyBlueprintPreviewClientState.tick();
            VomitOverlayClientState.tick();
            BadTripScreamerOverlay.tick();
            BadTripSkyTint.tick();
            org.mydrugs.mydrugs.client.InnerEntrySequence.tick(mc);
            org.mydrugs.mydrugs.client.InnerSkyClientState.tick();
            org.mydrugs.mydrugs.client.InnerRegionCrossingController.tick(mc);
            org.mydrugs.mydrugs.client.InnerSoundscapeController.tick(mc);
            org.mydrugs.mydrugs.client.InnerPlayerResponseController.tick(mc);
            org.mydrugs.mydrugs.client.InnerAmbientParticleController.tick(mc);
            PsyMixerRitualClientState.tick();
            RecoveryRoomOverlay.tick();
            RecoveryRoomParticleClient.tick();
            CustomDiscPlaybackController.tick();

            WithdrawalTunnelShader.INSTANCE.tick(mc);
            org.mydrugs.mydrugs.client.shaders.InnerAtmosphereShader.INSTANCE.tick(mc);
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

            if (org.mydrugs.mydrugs.client.shaders.InnerAtmosphereShader.INSTANCE.shouldRender()) {
                org.mydrugs.mydrugs.client.shaders.InnerAtmosphereShader.INSTANCE.render(mc);
            }

            org.mydrugs.mydrugs.client.InnerRegionCrossingController.render(event.getGuiGraphics());
            FlexibleDrugVisualOverlay.render(event.getGuiGraphics());
            VomitOverlayClientState.render(event.getGuiGraphics());
            BadTripScreamerOverlay.render(event.getGuiGraphics());
            AddictionHudRenderer.render(event.getGuiGraphics());
            BiomeFinderCompassOverlay.render(event.getGuiGraphics());
            PsyMixerRitualOverlay.render(event.getGuiGraphics());
            RecoveryRoomOverlay.render(event.getGuiGraphics());
            // Entry veil last so it covers the HUD as the world resolves.
            org.mydrugs.mydrugs.client.InnerEntrySequence.render(event.getGuiGraphics());
        }

        @SubscribeEvent
        public static void onComputeFov(ViewportEvent.ComputeFov event) {
            if (Config.CLIENT.disableForcedCameraMovement.get()
                    || Config.CLIENT.reducedMotionMode.get()
                    || !Config.CLIENT.enableCameraShake.get()
                    || Config.CLIENT.fovPulseIntensity.get() <= 0.0D) {
                return;
            }
            float original = event.getFOV();
            float withdrawal = WithdrawalTunnelShader.INSTANCE.getStrength();

            float intensity = Config.CLIENT.cameraShakeIntensity.get().floatValue();
            float fovScale = Config.CLIENT.fovPulseIntensity.get().floatValue();
            float baseTunnel = withdrawal * 4.0F * intensity;
            float beatKick = HeartbeatPulse.getFovOffset(withdrawal) * intensity;
            float customPulse = AddictionClientState.getEffectIntensity(org.mydrugs.mydrugs.core.drug.effect.EffectType.CUSTOM_NAUSEA) * 2.5F;
            customPulse += AddictionClientState.getEffectIntensity(org.mydrugs.mydrugs.core.drug.effect.EffectType.BLUR) * 1.5F;

            event.setFOV(original - (baseTunnel + beatKick + customPulse) * fovScale);
        }

        @SubscribeEvent
        public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
            BadTripSkyTint.applyFogColor(event);
        }

        @SubscribeEvent
        public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
            if (!Config.CLIENT.enableCameraShake.get() || Config.CLIENT.disableForcedCameraMovement.get()) {
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
            float swayScale = Config.CLIENT.cameraSwayIntensity.get().floatValue();
            float amplitude = Math.min(1.0F, sway) * 2.4F * motionScale * configScale * swayScale;
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
            if (Config.CLIENT.disableScreenRoll.get()) {
                roll = 0.0F;
            }

            event.setYaw(event.getYaw() + yaw);
            event.setPitch(event.getPitch() + pitch);
            event.setRoll(event.getRoll() + roll);
        }
}
