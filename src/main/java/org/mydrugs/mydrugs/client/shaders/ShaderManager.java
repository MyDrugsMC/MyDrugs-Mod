package org.mydrugs.mydrugs.client.shaders;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.client.ClientState;
import org.mydrugs.mydrugs.core.client.shader.ClientShaderManager;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class ShaderManager extends ClientShaderManager<AnimatedShader> {
    public static final ShaderManager INSTANCE = new ShaderManager(MyDrugs.CLIENT_STATE);

    public ShaderManager(ClientState clientState) {
        super(clientState);
    }

    @SubscribeEvent
    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        for (AnimatedShader shader : INSTANCE.getShaders()) {
            MyDrugs.getLOGGER().info("registering shader {}...", shader.getClass());
            shader.buildPipeline();
            event.registerPipeline(shader.getRenderPipeline());
        }
        WithdrawalTunnelShader.INSTANCE.buildPipeline();
        event.registerPipeline(WithdrawalTunnelShader.INSTANCE.getRenderPipeline());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        for (AnimatedShader shader : INSTANCE.getActiveShaders()) {
            shader.tick(mc);
        }

        if (mc.player != null) {
            INSTANCE.tick();
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderLevelStageEvent.AfterLevel event) {
        Minecraft mc = Minecraft.getInstance();

        for (AnimatedShader shader : INSTANCE.getActiveShaders()) {
            shader.render(mc);
        }
    }

    public void registerShaders() {
        register(EffectType.FOG, FogShader.INSTANCE);
        register(EffectType.ACID_WARP, AcidWarpShader.INSTANCE);
        register(EffectType.CHROMATIC_DREAM, ChromaticDreamShader.INSTANCE);
        register(EffectType.VOID_PULSE, VoidPulseShader.INSTANCE);
        register(EffectType.LUCID_DREAM, LucidDreamShader.INSTANCE);
        register(EffectType.MELT_REALITY, MeltRealityShader.INSTANCE);
        register(EffectType.EVENT_HORIZON, EventHorizonShader.INSTANCE);
        register(EffectType.OPAL_WAVE, OpalWaveShader.INSTANCE);
        register(EffectType.QUANTUM_FLOWER, QuantumFlowerShader.INSTANCE);
        register(EffectType.AURORA_RIBBONS, AuroraRibbonsShader.INSTANCE);
        register(EffectType.SPECTRAL_POSTER, SpectralPosterShader.INSTANCE);
        register(EffectType.DRUNK_VISION, DrunkVisionShader.INSTANCE);

    }
}