package org.mydrugs.mydrugs.recipes.grinder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class GrindingRecipeSerializer implements RecipeSerializer<GrindingRecipe> {
    public static final MapCodec<GrindingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(GrindingRecipe::ingredient),
            ItemStack.CODEC.fieldOf("result").forGetter(GrindingRecipe::result),
            Codec.INT.fieldOf("clicks_required").forGetter(GrindingRecipe::clicksRequired)
    ).apply(inst, GrindingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, GrindingRecipe::ingredient,
                    ItemStack.STREAM_CODEC, GrindingRecipe::result,
                    ByteBufCodecs.INT, GrindingRecipe::clicksRequired,
                    GrindingRecipe::new
            );

    @Override
    public MapCodec<GrindingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}