package org.mydrugs.mydrugs.recipes.electrolyzer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ElectrolyzerGasStack(ResourceLocation gas, int amount) {
    public static final Codec<ElectrolyzerGasStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("gas").forGetter(ElectrolyzerGasStack::gas),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(ElectrolyzerGasStack::amount)
    ).apply(instance, ElectrolyzerGasStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ElectrolyzerGasStack> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, ElectrolyzerGasStack::gas,
                    ByteBufCodecs.VAR_INT, ElectrolyzerGasStack::amount,
                    ElectrolyzerGasStack::new
            );
}