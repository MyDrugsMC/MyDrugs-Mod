package org.mydrugs.mydrugs.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.joml.Vector4f;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.fluids.FluidEntry;
import org.mydrugs.mydrugs.fluids.ModFluids;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class ModFluidClientExtensions {
    private static final ResourceLocation WATER_STILL =
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still");
    private static final ResourceLocation WATER_FLOW =
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_flow");
    private static final ResourceLocation WATER_OVERLAY =
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_overlay");
    private static final ResourceLocation UNDERWATER =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/underwater.png");

    /*
     * How far the player can see while inside the liquid.
     *
     * Alpha 0x00 -> roughly MAX_UNDER_FLUID_VISIBILITY blocks.
     * Alpha 0xFF -> roughly MIN_UNDER_FLUID_VISIBILITY blocks.
     *
     * So if a fluid tint is 0xFF7A1014, it becomes almost impossible to see through.
     * If a fluid tint is 0x33FFFFFF, it stays much clearer.
     */
    private static final float MIN_UNDER_FLUID_VISIBILITY = 0.25F;
    private static final float MAX_UNDER_FLUID_VISIBILITY = 14.0F;

    private ModFluidClientExtensions() {
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (FluidEntry entry : ModFluids.ALL.values()) {
            register(event, entry);
        }
    }

    private static void register(RegisterClientExtensionsEvent event, FluidEntry entry) {
        final int tint = entry.tint();

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return WATER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }

            @Override
            public ResourceLocation getOverlayTexture() {
                return WATER_OVERLAY;
            }

            @Override
            public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
                return UNDERWATER;
            }

            @Override
            public int getTintColor() {
                return tint; // ARGB
            }

            @Override
            public Vector4f modifyFogColor(
                    Camera camera,
                    float partialTick,
                    ClientLevel level,
                    int renderDistance,
                    float darkenWorldAmount,
                    Vector4f fluidFogColor
            ) {
                return new Vector4f(red(tint), green(tint), blue(tint), alpha(tint));
            }

            @Override
            public void modifyFogRender(
                    Camera camera,
                    FogEnvironment environment,
                    float renderDistance,
                    float partialTick,
                    FogData fogData
            ) {
                float alpha = alpha(tint);
                float visibility = lerp(MAX_UNDER_FLUID_VISIBILITY, MIN_UNDER_FLUID_VISIBILITY, alpha);

                fogData.environmentalStart = 0.0F;
                fogData.environmentalEnd = visibility;

                fogData.renderDistanceStart = 0.0F;
                fogData.renderDistanceEnd = visibility;

                fogData.skyEnd = Math.min(fogData.skyEnd, visibility);
                fogData.cloudEnd = Math.min(fogData.cloudEnd, visibility);
            }
        }, entry.type().get());
    }

    private static float alpha(int argb) {
        return ((argb >>> 24) & 0xFF) / 255.0F;
    }

    private static float red(int argb) {
        return ((argb >>> 16) & 0xFF) / 255.0F;
    }

    private static float green(int argb) {
        return ((argb >>> 8) & 0xFF) / 255.0F;
    }

    private static float blue(int argb) {
        return (argb & 0xFF) / 255.0F;
    }

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * amount;
    }
}