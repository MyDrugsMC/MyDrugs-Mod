package org.mydrugs.mydrugs.recipes.mixing_vat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record MixingVatFluidStack(ResourceLocation fluid, int amount) {
    public static final Codec<MixingVatFluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(MixingVatFluidStack::fluid),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(MixingVatFluidStack::amount)
    ).apply(instance, MixingVatFluidStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MixingVatFluidStack> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, MixingVatFluidStack::fluid,
                    ByteBufCodecs.VAR_INT, MixingVatFluidStack::amount,
                    MixingVatFluidStack::new
            );
}