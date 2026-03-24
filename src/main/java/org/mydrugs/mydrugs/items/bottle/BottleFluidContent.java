package org.mydrugs.mydrugs.items.bottle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record BottleFluidContent(ResourceLocation fluidId, int amountMb) {
    public static final Codec<BottleFluidContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("fluid_id").forGetter(BottleFluidContent::fluidId),
            Codec.intRange(1, 100).fieldOf("amount_mb").forGetter(BottleFluidContent::amountMb)
    ).apply(instance, BottleFluidContent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BottleFluidContent> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, BottleFluidContent::fluidId,
                    ByteBufCodecs.VAR_INT, BottleFluidContent::amountMb,
                    BottleFluidContent::new
            );
}