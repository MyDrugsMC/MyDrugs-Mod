package org.mydrugs.mydrugs.recipes.catalytic_reformer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record CatalyticReformerFluidStack(ResourceLocation fluid, int amount) {
    public static final Codec<CatalyticReformerFluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(CatalyticReformerFluidStack::fluid),
            Codec.INT.fieldOf("amount").forGetter(CatalyticReformerFluidStack::amount)
    ).apply(instance, CatalyticReformerFluidStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CatalyticReformerFluidStack> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, CatalyticReformerFluidStack::fluid,
                    ByteBufCodecs.VAR_INT, CatalyticReformerFluidStack::amount,
                    CatalyticReformerFluidStack::new
            );
}