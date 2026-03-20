package org.mydrugs.mydrugs.recipes.stompcrafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record CountedIngredient(Ingredient ingredient, int count) {
    public static final MapCodec<CountedIngredient> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(CountedIngredient::ingredient),
                    Codec.INT.optionalFieldOf("count", 1).forGetter(CountedIngredient::count)
            ).apply(instance, CountedIngredient::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CountedIngredient> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC,
                    CountedIngredient::ingredient,
                    ByteBufCodecs.VAR_INT,
                    CountedIngredient::count,
                    CountedIngredient::new
            );

    public boolean test(ItemStack stack) {
        return ingredient.test(stack);
    }

    public CountedIngredient {
        if (count < 1) {
            throw new IllegalArgumentException("count must be >= 1");
        }
    }
}