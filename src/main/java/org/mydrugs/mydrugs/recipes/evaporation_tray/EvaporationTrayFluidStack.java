package org.mydrugs.mydrugs.recipes.evaporation_tray;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record EvaporationTrayFluidStack(ResourceLocation fluid, int amount) {
    public static final Codec<EvaporationTrayFluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(EvaporationTrayFluidStack::fluid),
            Codec.INT.fieldOf("amount").forGetter(EvaporationTrayFluidStack::amount)
    ).apply(instance, EvaporationTrayFluidStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EvaporationTrayFluidStack> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, EvaporationTrayFluidStack::fluid,
                    ByteBufCodecs.VAR_INT, EvaporationTrayFluidStack::amount,
                    EvaporationTrayFluidStack::new
            );
}