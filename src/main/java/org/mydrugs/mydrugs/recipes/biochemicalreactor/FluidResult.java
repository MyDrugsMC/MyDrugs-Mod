package org.mydrugs.mydrugs.recipes.biochemicalreactor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record FluidResult(ResourceLocation fluidId, int amount) {
    public static final MapCodec<FluidResult> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(FluidResult::fluidId),
            Codec.INT.fieldOf("amount").forGetter(FluidResult::amount)
    ).apply(instance, FluidResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidResult> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, FluidResult::fluidId,
            ByteBufCodecs.VAR_INT, FluidResult::amount,
            FluidResult::new
    );
}