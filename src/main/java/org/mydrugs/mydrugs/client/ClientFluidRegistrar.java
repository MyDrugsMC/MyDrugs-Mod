package org.mydrugs.mydrugs.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.fluids.FluidEntry;
import org.mydrugs.mydrugs.fluids.ModFluids;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class ClientFluidRegistrar {
    private ClientFluidRegistrar() {
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (FluidEntry entry : ModFluids.ALL.values()) {
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return ModFluids.rl("block/" + entry.name() + "_still");
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return ModFluids.rl("block/" + entry.name() + "_flow");
                }

                @Override
                public ResourceLocation getOverlayTexture() {
                    return ModFluids.rl("block/" + entry.name() + "_overlay");
                }

                @Override
                public int getTintColor() {
                    return entry.tint();
                }
            }, entry.type().get());
        }
    }
}