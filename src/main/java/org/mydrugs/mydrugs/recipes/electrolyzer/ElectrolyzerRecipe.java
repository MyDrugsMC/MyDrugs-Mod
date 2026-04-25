package org.mydrugs.mydrugs.recipes.electrolyzer;

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

public class ElectrolyzerRecipe implements Recipe<ElectrolyzerRecipeInput> {
    private final ElectrolyzerFluidStack inputFluid;

    private final Optional<ElectrolyzerFluidStack> outputFluid1;
    private final Optional<ElectrolyzerGasStack> outputGas1;

    private final Optional<ElectrolyzerFluidStack> outputFluid2;
    private final Optional<ElectrolyzerGasStack> outputGas2;

    private final Optional<ElectrolyzerFluidStack> outputFluid3;
    private final Optional<ElectrolyzerGasStack> outputGas3;

    private final int baseTicks;

    public ElectrolyzerRecipe(
            ElectrolyzerFluidStack inputFluid,
            Optional<ElectrolyzerFluidStack> outputFluid1,
            Optional<ElectrolyzerGasStack> outputGas1,
            Optional<ElectrolyzerFluidStack> outputFluid2,
            Optional<ElectrolyzerGasStack> outputGas2,
            Optional<ElectrolyzerFluidStack> outputFluid3,
            Optional<ElectrolyzerGasStack> outputGas3,
            int baseTicks
    ) {
        validateRequiredSlot(outputFluid1, outputGas1, "output 1");
        validateOptionalSlot(outputFluid2, outputGas2, "output 2");
        validateOptionalSlot(outputFluid3, outputGas3, "output 3");

        this.inputFluid = inputFluid;
        this.outputFluid1 = outputFluid1;
        this.outputGas1 = outputGas1;
        this.outputFluid2 = outputFluid2;
        this.outputGas2 = outputGas2;
        this.outputFluid3 = outputFluid3;
        this.outputGas3 = outputGas3;
        this.baseTicks = baseTicks;
    }

    private static void validateRequiredSlot(Optional<?> fluid, Optional<?> gas, String slotName) {
        if (fluid.isPresent() == gas.isPresent()) {
            throw new IllegalArgumentException(slotName + " must define exactly one of fluid or gas");
        }
    }

    private static void validateOptionalSlot(Optional<?> fluid, Optional<?> gas, String slotName) {
        if (fluid.isPresent() && gas.isPresent()) {
            throw new IllegalArgumentException(slotName + " cannot define both fluid and gas");
        }
    }

    public ElectrolyzerFluidStack inputFluid() {
        return inputFluid;
    }

    public Optional<ElectrolyzerFluidStack> outputFluid1() {
        return outputFluid1;
    }

    public Optional<ElectrolyzerGasStack> outputGas1() {
        return outputGas1;
    }

    public Optional<ElectrolyzerFluidStack> outputFluid2() {
        return outputFluid2;
    }

    public Optional<ElectrolyzerGasStack> outputGas2() {
        return outputGas2;
    }

    public Optional<ElectrolyzerFluidStack> outputFluid3() {
        return outputFluid3;
    }

    public Optional<ElectrolyzerGasStack> outputGas3() {
        return outputGas3;
    }

    public boolean hasOutput2() {
        return outputFluid2.isPresent() || outputGas2.isPresent();
    }

    public boolean hasOutput3() {
        return outputFluid3.isPresent() || outputGas3.isPresent();
    }

    public int baseTicks() {
        return baseTicks;
    }

    @Override
    public boolean matches(ElectrolyzerRecipeInput recipeInput, Level level) {
        if (recipeInput.inputFluid() == null) {
            return false;
        }

        return recipeInput.inputFluid().equals(this.inputFluid.fluid())
                && recipeInput.inputAmount() >= this.inputFluid.amount();
    }

    @Override
    public ItemStack assemble(ElectrolyzerRecipeInput recipeInput, HolderLookup.Provider registries) {
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
    public RecipeSerializer<? extends Recipe<ElectrolyzerRecipeInput>> getSerializer() {
        return ModRecipeSerializers.ELECTROLYZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<ElectrolyzerRecipeInput>> getType() {
        return ModRecipeTypes.ELECTROLYZER.get();
    }

    public static class Serializer implements RecipeSerializer<ElectrolyzerRecipe> {
        public static final MapCodec<ElectrolyzerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ElectrolyzerFluidStack.CODEC.fieldOf("input_fluid").forGetter(ElectrolyzerRecipe::inputFluid),

                ElectrolyzerFluidStack.CODEC.optionalFieldOf("output_fluid_1").forGetter(ElectrolyzerRecipe::outputFluid1),
                ElectrolyzerGasStack.CODEC.optionalFieldOf("output_gas_1").forGetter(ElectrolyzerRecipe::outputGas1),

                ElectrolyzerFluidStack.CODEC.optionalFieldOf("output_fluid_2").forGetter(ElectrolyzerRecipe::outputFluid2),
                ElectrolyzerGasStack.CODEC.optionalFieldOf("output_gas_2").forGetter(ElectrolyzerRecipe::outputGas2),

                ElectrolyzerFluidStack.CODEC.optionalFieldOf("output_fluid_3").forGetter(ElectrolyzerRecipe::outputFluid3),
                ElectrolyzerGasStack.CODEC.optionalFieldOf("output_gas_3").forGetter(ElectrolyzerRecipe::outputGas3),

                Codec.INT.optionalFieldOf("base_ticks", 200).forGetter(ElectrolyzerRecipe::baseTicks)
        ).apply(instance, ElectrolyzerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ElectrolyzerRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ElectrolyzerFluidStack.STREAM_CODEC, ElectrolyzerRecipe::inputFluid,

                        ElectrolyzerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional), ElectrolyzerRecipe::outputFluid1,
                        ElectrolyzerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional), ElectrolyzerRecipe::outputGas1,

                        ElectrolyzerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional), ElectrolyzerRecipe::outputFluid2,
                        ElectrolyzerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional), ElectrolyzerRecipe::outputGas2,

                        ElectrolyzerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional), ElectrolyzerRecipe::outputFluid3,
                        ElectrolyzerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional), ElectrolyzerRecipe::outputGas3,

                        ByteBufCodecs.VAR_INT, ElectrolyzerRecipe::baseTicks,
                        ElectrolyzerRecipe::new
                );

        @Override
        public MapCodec<ElectrolyzerRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ElectrolyzerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}