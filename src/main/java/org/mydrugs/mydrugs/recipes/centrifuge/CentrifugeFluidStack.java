package org.mydrugs.mydrugs.recipes.centrifuge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record CentrifugeFluidStack(ResourceLocation fluid, int amount) {
    public static final Codec<CentrifugeFluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(CentrifugeFluidStack::fluid),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(CentrifugeFluidStack::amount)
    ).apply(instance, CentrifugeFluidStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CentrifugeFluidStack> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, CentrifugeFluidStack::fluid,
                    ByteBufCodecs.VAR_INT, CentrifugeFluidStack::amount,
                    CentrifugeFluidStack::new
            );
}