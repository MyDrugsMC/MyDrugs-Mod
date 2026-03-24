package org.mydrugs.mydrugs.recipes.advanced_furnace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

import java.util.Optional;

public class AdvancedFurnaceRecipe implements Recipe<AdvancedFurnaceRecipeInput> {
    private final Ingredient inputA;
    private final Optional<Ingredient> inputB;
    private final ItemStack resultA;
    private final ItemStack resultB;
    private final ResourceLocation fluidOutput;
    private final int fluidAmount;
    private final int cookTime;

    public AdvancedFurnaceRecipe(
            Ingredient inputA,
            Optional<Ingredient> inputB,
            ItemStack resultA,
            ItemStack resultB,
            ResourceLocation fluidOutput,
            int fluidAmount,
            int cookTime
    ) {
        this.inputA = inputA;
        this.inputB = inputB;
        this.resultA = resultA;
        this.resultB = resultB;
        this.fluidOutput = fluidOutput;
        this.fluidAmount = fluidAmount;
        this.cookTime = cookTime;
    }

    public Ingredient inputA() {
        return inputA;
    }

    public Optional<Ingredient> inputB() {
        return inputB;
    }

    public ItemStack resultA() {
        return resultA;
    }

    public ItemStack resultB() {
        return resultB;
    }

    public ResourceLocation fluidOutput() {
        return fluidOutput;
    }

    public int fluidAmount() {
        return fluidAmount;
    }

    public int cookTime() {
        return cookTime;
    }

    @Override
    public boolean matches(AdvancedFurnaceRecipeInput input, Level level) {
        if (!this.inputA.test(input.inputA())) {
            return false;
        }

        return this.inputB.map(ingredient -> ingredient.test(input.inputB()))
                .orElse(true);
    }

    @Override
    public ItemStack assemble(AdvancedFurnaceRecipeInput input, HolderLookup.Provider registries) {
        return this.resultA.copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<? extends Recipe<AdvancedFurnaceRecipeInput>> getSerializer() {
        return ModRecipeSerializers.ADVANCED_FURNACE.get();
    }

    @Override
    public RecipeType<? extends Recipe<AdvancedFurnaceRecipeInput>> getType() {
        return ModRecipeTypes.ADVANCED_FURNACE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public static class Serializer implements RecipeSerializer<AdvancedFurnaceRecipe> {
        public static final MapCodec<AdvancedFurnaceRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("input_a").forGetter(AdvancedFurnaceRecipe::inputA),
                Ingredient.CODEC.optionalFieldOf("input_b").forGetter(AdvancedFurnaceRecipe::inputB),
                ItemStack.CODEC.fieldOf("result_a").forGetter(AdvancedFurnaceRecipe::resultA),
                ItemStack.CODEC.optionalFieldOf("result_b", ItemStack.EMPTY).forGetter(AdvancedFurnaceRecipe::resultB),
                ResourceLocation.CODEC.fieldOf("fluid_output").forGetter(AdvancedFurnaceRecipe::fluidOutput),
                Codec.INT.fieldOf("fluid_amount").forGetter(AdvancedFurnaceRecipe::fluidAmount),
                Codec.INT.optionalFieldOf("cook_time", 200).forGetter(AdvancedFurnaceRecipe::cookTime)
        ).apply(instance, AdvancedFurnaceRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedFurnaceRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, AdvancedFurnaceRecipe::inputA,
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs::optional), AdvancedFurnaceRecipe::inputB,
                        ItemStack.STREAM_CODEC, AdvancedFurnaceRecipe::resultA,
                        ItemStack.STREAM_CODEC, AdvancedFurnaceRecipe::resultB,
                        ResourceLocation.STREAM_CODEC, AdvancedFurnaceRecipe::fluidOutput,
                        ByteBufCodecs.VAR_INT, AdvancedFurnaceRecipe::fluidAmount,
                        ByteBufCodecs.VAR_INT, AdvancedFurnaceRecipe::cookTime,
                        AdvancedFurnaceRecipe::new
                );

        @Override
        public MapCodec<AdvancedFurnaceRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AdvancedFurnaceRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}