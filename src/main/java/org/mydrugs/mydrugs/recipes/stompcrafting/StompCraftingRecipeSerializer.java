package org.mydrugs.mydrugs.recipes.stompcrafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class StompCraftingRecipeSerializer implements RecipeSerializer<StompCraftingRecipe> {
    public static final MapCodec<StompCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    CountedIngredient.MAP_CODEC.codec().listOf()
                            .fieldOf("ingredients")
                            .forGetter(StompCraftingRecipe::ingredients),
                    ItemStack.CODEC.fieldOf("result")
                            .forGetter(StompCraftingRecipe::result)
            ).apply(instance, StompCraftingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, StompCraftingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    CountedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    StompCraftingRecipe::ingredients,
                    ItemStack.STREAM_CODEC,
                    StompCraftingRecipe::result,
                    StompCraftingRecipe::new
            );

    @Override
    public MapCodec<StompCraftingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, StompCraftingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}