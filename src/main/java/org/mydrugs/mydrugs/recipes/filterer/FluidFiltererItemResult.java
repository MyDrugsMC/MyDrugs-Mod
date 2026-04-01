package org.mydrugs.mydrugs.recipes.filterer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record FluidFiltererItemResult(ResourceLocation item, int count) {
    public static final Codec<FluidFiltererItemResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter(FluidFiltererItemResult::item),
            Codec.INT.optionalFieldOf("count", 1).forGetter(FluidFiltererItemResult::count)
    ).apply(instance, FluidFiltererItemResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidFiltererItemResult> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, FluidFiltererItemResult::item,
                    ByteBufCodecs.VAR_INT, FluidFiltererItemResult::count,
                    FluidFiltererItemResult::new
            );

    public ItemStack createStack() {
        Item itemValue = BuiltInRegistries.ITEM.getValue(this.item);
        return itemValue == null ? ItemStack.EMPTY : new ItemStack(itemValue, this.count);
    }
}