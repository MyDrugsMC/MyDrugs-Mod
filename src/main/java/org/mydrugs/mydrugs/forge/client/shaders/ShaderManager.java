package org.mydrugs.mydrugs.forge.client.shaders;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class ShaderManager {
    private static final List<AnimatedShader> SHADERS = new ArrayList<>();
    private static float ticksLeft = 0f;
    private static AnimatedShader currentShader = null;

    public static void registerShaders() {
        register(FogShader.INSTANCE);
        register(AcidWarpShader.INSTANCE);
        register(ChromaticDreamShader.INSTANCE);
        register(VoidPulseShader.INSTANCE);
    }

    public static void register(AnimatedShader shader) {
        SHADERS.add(shader);
    }

    @SubscribeEvent
    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        for (AnimatedShader shader : SHADERS) {
            shader.buildPipeline();
            event.registerPipeline(shader.getRenderPipeline());
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        for (AnimatedShader shader : SHADERS) {
            shader.tick(mc);
        }
        if (mc.player == null) return;

        if (ticksLeft > 0) {
            ticksLeft--;

            if (currentShader == null) return;

            if (ticksLeft == 0) {
                currentShader.setEnabled(false);
                currentShader = null;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderLevelStageEvent.AfterLevel event) {
        Minecraft mc = Minecraft.getInstance();
        for (AnimatedShader shader : SHADERS) {
            shader.render(mc);
        }
    }

    public static void start(int durationTicks, Class<? extends AnimatedShader> clazz) {
        ticksLeft = durationTicks;
        for (AnimatedShader shader : SHADERS) {
            if (shader.getClass() == clazz) {
                currentShader = shader;
                currentShader.setEnabled(true);
                return;
            }
        }
    }

    private ShaderManager() {}
}