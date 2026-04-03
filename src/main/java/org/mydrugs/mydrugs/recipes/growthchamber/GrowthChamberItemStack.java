package org.mydrugs.mydrugs.recipes.growthchamber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record GrowthChamberItemStack(ResourceLocation item, int count) {
    public static final Codec<GrowthChamberItemStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter(GrowthChamberItemStack::item),
            Codec.INT.optionalFieldOf("count", 1).forGetter(GrowthChamberItemStack::count)
    ).apply(instance, GrowthChamberItemStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GrowthChamberItemStack> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, GrowthChamberItemStack::item,
                    ByteBufCodecs.VAR_INT, GrowthChamberItemStack::count,
                    GrowthChamberItemStack::new
            );

    public ItemStack toStack() {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(this.item), this.count);
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty()
                && stack.is(BuiltInRegistries.ITEM.getValue(this.item))
                && stack.getCount() >= this.count;
    }
}