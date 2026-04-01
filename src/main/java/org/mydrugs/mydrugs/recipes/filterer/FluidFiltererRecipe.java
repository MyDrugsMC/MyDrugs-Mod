package org.mydrugs.mydrugs.recipes.filterer;

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

import java.util.Optional;

public class FluidFiltererRecipe implements Recipe<FluidFiltererRecipeInput> {
    private final FluidFiltererFluidStack input;
    private final FluidFiltererFluidStack output1;
    private final Optional<FluidFiltererFluidStack> output2;
    private final Optional<FluidFiltererItemResult> outputItem;
    private final int clicksRequired;
    private final int hungerPerTick;

    public FluidFiltererRecipe(
            FluidFiltererFluidStack input,
            FluidFiltererFluidStack output1,
            Optional<FluidFiltererFluidStack> output2,
            Optional<FluidFiltererItemResult> outputItem,
            int clicksRequired,
            int hungerPerTick
    ) {
        this.input = input;
        this.output1 = output1;
        this.output2 = output2;
        this.outputItem = outputItem;
        this.clicksRequired = clicksRequired;
        this.hungerPerTick = hungerPerTick;
    }

    public FluidFiltererFluidStack input() {
        return input;
    }

    public FluidFiltererFluidStack output1() {
        return output1;
    }

    public Optional<FluidFiltererFluidStack> output2() {
        return output2;
    }

    public Optional<FluidFiltererItemResult> outputItem() {
        return outputItem;
    }

    public int clicksRequired() {
        return clicksRequired;
    }

    public int hungerPerTick() {
        return hungerPerTick;
    }

    @Override
    public boolean matches(FluidFiltererRecipeInput recipeInput, Level level) {
        if (recipeInput.inputFluid() == null) {
            return false;
        }

        return recipeInput.inputFluid().equals(this.input.fluid())
                && recipeInput.inputAmount() >= this.input.amount();
    }

    @Override
    public ItemStack assemble(FluidFiltererRecipeInput recipeInput, HolderLookup.Provider registries) {
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
    public RecipeSerializer<? extends Recipe<FluidFiltererRecipeInput>> getSerializer() {
        return ModRecipeSerializers.FLUID_FILTERING.get();
    }

    @Override
    public RecipeType<? extends Recipe<FluidFiltererRecipeInput>> getType() {
        return ModRecipeTypes.FLUID_FILTERING.get();
    }

    public static class Serializer implements RecipeSerializer<FluidFiltererRecipe> {
        public static final MapCodec<FluidFiltererRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                FluidFiltererFluidStack.CODEC.fieldOf("input").forGetter(FluidFiltererRecipe::input),
                FluidFiltererFluidStack.CODEC.fieldOf("output_1").forGetter(FluidFiltererRecipe::output1),
                FluidFiltererFluidStack.CODEC.optionalFieldOf("output_2").forGetter(FluidFiltererRecipe::output2),
                FluidFiltererItemResult.CODEC.optionalFieldOf("output_item").forGetter(FluidFiltererRecipe::outputItem),
                Codec.INT.optionalFieldOf("clicks_required", 10).forGetter(FluidFiltererRecipe::clicksRequired),
                Codec.INT.optionalFieldOf("hunger_per_tick", 1).forGetter(FluidFiltererRecipe::hungerPerTick)
        ).apply(instance, FluidFiltererRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidFiltererRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        FluidFiltererFluidStack.STREAM_CODEC, FluidFiltererRecipe::input,
                        FluidFiltererFluidStack.STREAM_CODEC, FluidFiltererRecipe::output1,
                        FluidFiltererFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional), FluidFiltererRecipe::output2,
                        FluidFiltererItemResult.STREAM_CODEC.apply(ByteBufCodecs::optional), FluidFiltererRecipe::outputItem,
                        ByteBufCodecs.VAR_INT, FluidFiltererRecipe::clicksRequired,
                        ByteBufCodecs.VAR_INT, FluidFiltererRecipe::hungerPerTick,
                        FluidFiltererRecipe::new
                );

        @Override
        public MapCodec<FluidFiltererRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FluidFiltererRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}