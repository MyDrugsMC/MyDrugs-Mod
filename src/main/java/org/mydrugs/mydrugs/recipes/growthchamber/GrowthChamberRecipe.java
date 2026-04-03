package org.mydrugs.mydrugs.recipes.growthchamber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

public class GrowthChamberRecipe implements Recipe<GrowthChamberRecipeInput> {
    private final GrowthChamberItemStack input;
    private final GrowthChamberItemStack biomassInput;
    private final GrowthChamberItemStack middleResult;
    private final GrowthChamberItemStack finalResult;
    private final int water;
    private final int baseTicks;

    public GrowthChamberRecipe(
            GrowthChamberItemStack input,
            GrowthChamberItemStack biomassInput,
            GrowthChamberItemStack middleResult,
            GrowthChamberItemStack finalResult,
            int water,
            int baseTicks
    ) {
        this.input = input;
        this.biomassInput = biomassInput;
        this.middleResult = middleResult;
        this.finalResult = finalResult;
        this.water = water;
        this.baseTicks = baseTicks;
    }

    public GrowthChamberItemStack input() {
        return input;
    }

    public GrowthChamberItemStack biomassInput() {
        return biomassInput;
    }

    public GrowthChamberItemStack middleResult() {
        return middleResult;
    }

    public GrowthChamberItemStack finalResult() {
        return finalResult;
    }

    public int water() {
        return water;
    }

    public int baseTicks() {
        return baseTicks;
    }

    public boolean matchesInput(ItemStack stack) {
        return this.input.matches(stack);
    }

    public boolean matchesBiomass(ItemStack stack) {
        return this.biomassInput.matches(stack);
    }

    public boolean matchesStage1(ItemStack inputStack, ItemStack biomassStack) {
        return this.input.matches(inputStack) && this.biomassInput.matches(biomassStack);
    }

    public boolean matchesMiddle(ItemStack stack) {
        return this.middleResult.matches(stack);
    }

    @Override
    public boolean matches(GrowthChamberRecipeInput recipeInput, Level level) {
        return matchesInput(recipeInput.inputStack()) || matchesMiddle(recipeInput.middleStack());
    }

    @Override
    public ItemStack assemble(GrowthChamberRecipeInput recipeInput, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public RecipeSerializer<? extends Recipe<GrowthChamberRecipeInput>> getSerializer() {
        return ModRecipeSerializers.GROWTH_CHAMBER.get();
    }

    @Override
    public RecipeType<? extends Recipe<GrowthChamberRecipeInput>> getType() {
        return ModRecipeTypes.GROWTH_CHAMBER.get();
    }

    public static class Serializer implements RecipeSerializer<GrowthChamberRecipe> {
        public static final MapCodec<GrowthChamberRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                GrowthChamberItemStack.CODEC.fieldOf("input").forGetter(GrowthChamberRecipe::input),
                GrowthChamberItemStack.CODEC.fieldOf("biomass_input").forGetter(GrowthChamberRecipe::biomassInput),
                GrowthChamberItemStack.CODEC.fieldOf("middle_result").forGetter(GrowthChamberRecipe::middleResult),
                GrowthChamberItemStack.CODEC.fieldOf("final_result").forGetter(GrowthChamberRecipe::finalResult),
                Codec.INT.optionalFieldOf("water", 250).forGetter(GrowthChamberRecipe::water),
                Codec.INT.optionalFieldOf("base_ticks", 200).forGetter(GrowthChamberRecipe::baseTicks)
        ).apply(instance, GrowthChamberRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, GrowthChamberRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        GrowthChamberItemStack.STREAM_CODEC, GrowthChamberRecipe::input,
                        GrowthChamberItemStack.STREAM_CODEC, GrowthChamberRecipe::biomassInput,
                        GrowthChamberItemStack.STREAM_CODEC, GrowthChamberRecipe::middleResult,
                        GrowthChamberItemStack.STREAM_CODEC, GrowthChamberRecipe::finalResult,
                        ByteBufCodecs.VAR_INT, GrowthChamberRecipe::water,
                        ByteBufCodecs.VAR_INT, GrowthChamberRecipe::baseTicks,
                        GrowthChamberRecipe::new
                );

        @Override
        public MapCodec<GrowthChamberRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GrowthChamberRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}