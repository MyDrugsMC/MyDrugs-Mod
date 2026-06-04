package org.mydrugs.mydrugs.items.bottle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.items.data.ComponentCodecs;

public record BottleFluidContent(ResourceLocation fluidId, int amountMb) {
    public static final Codec<BottleFluidContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("fluid_id").forGetter(BottleFluidContent::fluidId),
            ComponentCodecs.intRange(1, GlassBottleItem.CAPACITY_MB).fieldOf("amount_mb").forGetter(BottleFluidContent::amountMb)
    ).apply(instance, BottleFluidContent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BottleFluidContent> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, BottleFluidContent::fluidId,
                    // Enforce the same [1, CAPACITY_MB] range as the persistent codec.
                    ComponentCodecs.checkedVarInt(1, GlassBottleItem.CAPACITY_MB), BottleFluidContent::amountMb,
                    BottleFluidContent::new
            );
}