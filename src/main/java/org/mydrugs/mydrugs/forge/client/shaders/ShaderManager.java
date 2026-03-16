package org.mydrugs.mydrugs.forge.client.shaders;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.ClientShaderManager;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class ShaderManager extends ClientShaderManager<AnimatedShader> {
    public static final ShaderManager INSTANCE = new ShaderManager();

    private ShaderManager() {}

    public void registerShaders() {
        register(FogShader.INSTANCE);
        register(AcidWarpShader.INSTANCE);
        register(ChromaticDreamShader.INSTANCE);
        register(VoidPulseShader.INSTANCE);
    }

    @SubscribeEvent
    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        for (AnimatedShader shader : INSTANCE.getShaders()) {
            System.out.println("registering shader " + shader.getClass() + "...");
            shader.buildPipeline();
            event.registerPipeline(shader.getRenderPipeline());
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        for (AnimatedShader shader : INSTANCE.getShaders()) {
            shader.tick(mc);
        }

        if (mc.player != null) {
            INSTANCE.tick();
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderLevelStageEvent.AfterLevel event) {
        Minecraft mc = Minecraft.getInstance();

        for (AnimatedShader shader : INSTANCE.getShaders()) {
            shader.render(mc);
        }
    }
}