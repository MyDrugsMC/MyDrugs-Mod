package org.mydrugs.mydrugs.recipes.coffee_pulping;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

import java.util.List;

public record CoffeePulpingRecipe(Ingredient ingredient, ItemStack beanResult, ItemStack biomassResult, int work)
        implements Recipe<SingleRecipeInput> {
    public CoffeePulpingRecipe {
        if (ingredient.isEmpty()) throw new IllegalArgumentException("ingredient must not be empty");
        if (beanResult.isEmpty()) throw new IllegalArgumentException("bean result must not be empty");
        work = Math.max(1, work);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return beanResult.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return ModRecipeSerializers.COFFEE_PULPING.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipeTypes.COFFEE_PULPING.get();
    }

    private static PlacementInfo placementInfo;

    @Override
    public PlacementInfo placementInfo() {
        if (placementInfo == null) {
            placementInfo = PlacementInfo.create(ingredient);
        }
        return placementInfo;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<CoffeePulpingRecipe> {
        public static final MapCodec<CoffeePulpingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(CoffeePulpingRecipe::ingredient),
                ItemStack.CODEC.fieldOf("bean_result").forGetter(CoffeePulpingRecipe::beanResult),
                ItemStack.CODEC.optionalFieldOf("biomass_result", ItemStack.EMPTY).forGetter(CoffeePulpingRecipe::biomassResult),
                Codec.INT.optionalFieldOf("work", 80).forGetter(CoffeePulpingRecipe::work)
        ).apply(inst, CoffeePulpingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CoffeePulpingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, CoffeePulpingRecipe::ingredient,
                ItemStack.STREAM_CODEC, CoffeePulpingRecipe::beanResult,
                ItemStack.STREAM_CODEC, CoffeePulpingRecipe::biomassResult,
                ByteBufCodecs.VAR_INT, CoffeePulpingRecipe::work,
                CoffeePulpingRecipe::new
        );

        @Override
        public MapCodec<CoffeePulpingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CoffeePulpingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
