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

    public void registerShaders() {
        register(EffectType.FOG, FogShader.INSTANCE);
        register(EffectType.ACID_WARP, AcidWarpShader.INSTANCE);
        register(EffectType.CHROMATIC_DREAM, ChromaticDreamShader.INSTANCE);
        register(EffectType.VOID_PULSE, VoidPulseShader.INSTANCE);
    }

    @SubscribeEvent
    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        for (AnimatedShader shader : INSTANCE.getShaders()) {
            MyDrugs.getLOGGER().info("registering shader {}...", shader.getClass());
            shader.buildPipeline();
            event.registerPipeline(shader.getRenderPipeline());
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        AnimatedShader shader = (AnimatedShader) MyDrugs.CLIENT_STATE.getShader();

        if (shader != null) {
            shader.tick(mc);
        }

        if (mc.player != null) {
            INSTANCE.tick();
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderLevelStageEvent.AfterLevel event) {
        Minecraft mc = Minecraft.getInstance();

        AnimatedShader shader = (AnimatedShader) MyDrugs.CLIENT_STATE.getShader();

        if (shader != null) {
            shader.render(mc);
        }
    }
}