package org.mydrugs.mydrugs.recipes.filterer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record FluidFiltererFluidStack(ResourceLocation fluid, int amount) {
    public static final Codec<FluidFiltererFluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(FluidFiltererFluidStack::fluid),
            Codec.INT.fieldOf("amount").forGetter(FluidFiltererFluidStack::amount)
    ).apply(instance, FluidFiltererFluidStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidFiltererFluidStack> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, FluidFiltererFluidStack::fluid,
                    ByteBufCodecs.VAR_INT, FluidFiltererFluidStack::amount,
                    FluidFiltererFluidStack::new
            );
}