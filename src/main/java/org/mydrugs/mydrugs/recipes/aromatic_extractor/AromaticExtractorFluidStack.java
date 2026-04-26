package org.mydrugs.mydrugs.recipes.aromatic_extractor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record AromaticExtractorFluidStack(ResourceLocation fluid, int amount) {
    public static final Codec<AromaticExtractorFluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(AromaticExtractorFluidStack::fluid),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(AromaticExtractorFluidStack::amount)
    ).apply(instance, AromaticExtractorFluidStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AromaticExtractorFluidStack> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, AromaticExtractorFluidStack::fluid,
                    ByteBufCodecs.VAR_INT, AromaticExtractorFluidStack::amount,
                    AromaticExtractorFluidStack::new
            );
}
