package org.mydrugs.mydrugs.recipes.catalytic_reformer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record CatalyticReformerGasStack(ResourceLocation gas, int amount) {
    public static final Codec<CatalyticReformerGasStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("gas").forGetter(CatalyticReformerGasStack::gas),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(CatalyticReformerGasStack::amount)
    ).apply(instance, CatalyticReformerGasStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CatalyticReformerGasStack> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, CatalyticReformerGasStack::gas,
                    ByteBufCodecs.VAR_INT, CatalyticReformerGasStack::amount,
                    CatalyticReformerGasStack::new
            );
}