package org.mydrugs.mydrugs.recipes.drying;

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

public final class DryingRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient input;
    private final ItemStack result;
    private final int dryTime;

    private PlacementInfo placementInfo;

    public DryingRecipe(Ingredient input, ItemStack result, int dryTime) {
        this.input = input;
        this.result = result;
        this.dryTime = dryTime;
    }

    public Ingredient input() {
        return input;
    }

    public ItemStack result() {
        return result;
    }

    public int dryTime() {
        return dryTime;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.input);
        }
        return this.placementInfo;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        // Because isSpecial() is true, the recipe book will ignore this anyway.
        return RecipeBookCategories.FURNACE_MISC;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipeTypes.DRYING_TYPE.get();
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return ModRecipeSerializers.DRYING_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<DryingRecipe> {
        public static final MapCodec<DryingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(DryingRecipe::input),
                ItemStack.CODEC.fieldOf("result").forGetter(DryingRecipe::result),
                Codec.INT.optionalFieldOf("dry_time", 200).forGetter(DryingRecipe::dryTime)
        ).apply(instance, DryingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, DryingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, DryingRecipe::input,
                ItemStack.STREAM_CODEC, DryingRecipe::result,
                ByteBufCodecs.VAR_INT, DryingRecipe::dryTime,
                DryingRecipe::new
        );

        @Override
        public MapCodec<DryingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DryingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}