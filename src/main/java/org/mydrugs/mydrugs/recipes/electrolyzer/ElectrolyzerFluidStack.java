package org.mydrugs.mydrugs.recipes.electrolyzer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ElectrolyzerFluidStack(ResourceLocation fluid, int amount) {
    public static final Codec<ElectrolyzerFluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(ElectrolyzerFluidStack::fluid),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(ElectrolyzerFluidStack::amount)
    ).apply(instance, ElectrolyzerFluidStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ElectrolyzerFluidStack> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, ElectrolyzerFluidStack::fluid,
                    ByteBufCodecs.VAR_INT, ElectrolyzerFluidStack::amount,
                    ElectrolyzerFluidStack::new
            );
}
