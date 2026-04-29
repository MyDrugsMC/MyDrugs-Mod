package org.mydrugs.mydrugs.recipes.catalytic_reformer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.gas.GasStack;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

import java.util.Optional;

public class CatalyticReformerRecipe implements Recipe<CatalyticReformerRecipeInput> {
    private final Optional<CatalyticReformerFluidStack> inputFluid1;
    private final Optional<CatalyticReformerGasStack> inputGas1;

    private final Optional<CatalyticReformerFluidStack> inputFluid2;
    private final Optional<CatalyticReformerGasStack> inputGas2;

    private final Ingredient catalyst;
    private final boolean consumeCatalyst;

    private final Optional<CatalyticReformerFluidStack> outputFluid1;
    private final Optional<CatalyticReformerGasStack> outputGas1;

    private final Optional<CatalyticReformerFluidStack> outputFluid2;
    private final Optional<CatalyticReformerGasStack> outputGas2;

    private final Optional<CatalyticReformerFluidStack> outputFluid3;
    private final Optional<CatalyticReformerGasStack> outputGas3;

    private final int baseTicks;

    public CatalyticReformerRecipe(
            Optional<CatalyticReformerFluidStack> inputFluid1,
            Optional<CatalyticReformerGasStack> inputGas1,
            Optional<CatalyticReformerFluidStack> inputFluid2,
            Optional<CatalyticReformerGasStack> inputGas2,
            Ingredient catalyst,
            boolean consumeCatalyst,
            Optional<CatalyticReformerFluidStack> outputFluid1,
            Optional<CatalyticReformerGasStack> outputGas1,
            Optional<CatalyticReformerFluidStack> outputFluid2,
            Optional<CatalyticReformerGasStack> outputGas2,
            Optional<CatalyticReformerFluidStack> outputFluid3,
            Optional<CatalyticReformerGasStack> outputGas3,
            int baseTicks
    ) {
        validateRequiredPort(inputFluid1, inputGas1, "input 1");
        validateRequiredPort(inputFluid2, inputGas2, "input 2");

        validateRequiredPort(outputFluid1, outputGas1, "output 1");
        validateOptionalPort(outputFluid2, outputGas2, "output 2");
        validateOptionalPort(outputFluid3, outputGas3, "output 3");

        if (baseTicks <= 0) {
            throw new IllegalArgumentException("baseTicks must be > 0");
        }

        this.inputFluid1 = inputFluid1;
        this.inputGas1 = inputGas1;
        this.inputFluid2 = inputFluid2;
        this.inputGas2 = inputGas2;
        this.catalyst = catalyst;
        this.consumeCatalyst = consumeCatalyst;
        this.outputFluid1 = outputFluid1;
        this.outputGas1 = outputGas1;
        this.outputFluid2 = outputFluid2;
        this.outputGas2 = outputGas2;
        this.outputFluid3 = outputFluid3;
        this.outputGas3 = outputGas3;
        this.baseTicks = baseTicks;
    }

    private static void validateRequiredPort(Optional<?> fluid, Optional<?> gas, String name) {
        if (fluid.isPresent() == gas.isPresent()) {
            throw new IllegalArgumentException(name + " must define exactly one of fluid or gas");
        }
    }

    private static void validateOptionalPort(Optional<?> fluid, Optional<?> gas, String name) {
        if (fluid.isPresent() && gas.isPresent()) {
            throw new IllegalArgumentException(name + " must define only one of fluid or gas");
        }
    }

    private static boolean matchesFluidRequirement(net.neoforged.neoforge.fluids.FluidStack stack, CatalyticReformerFluidStack req) {
        if (stack.isEmpty()) {
            return false;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(req.fluid());
        if (fluid == null || fluid == Fluids.EMPTY) {
            return false;
        }

        return stack.is(fluid) && stack.getAmount() >= req.amount();
    }

    private static boolean matchesGasRequirement(GasStack stack, CatalyticReformerGasStack req) {
        return !stack.isEmpty()
                && stack.type() != null
                && stack.type().id().equals(req.gas())
                && stack.amount() >= req.amount();
    }

    public Optional<CatalyticReformerFluidStack> inputFluid1() {
        return inputFluid1;
    }

    public Optional<CatalyticReformerGasStack> inputGas1() {
        return inputGas1;
    }

    public Optional<CatalyticReformerFluidStack> inputFluid2() {
        return inputFluid2;
    }

    public Optional<CatalyticReformerGasStack> inputGas2() {
        return inputGas2;
    }

    public Ingredient catalyst() {
        return catalyst;
    }

    public boolean consumeCatalyst() {
        return consumeCatalyst;
    }

    public Optional<CatalyticReformerFluidStack> outputFluid1() {
        return outputFluid1;
    }

    public Optional<CatalyticReformerGasStack> outputGas1() {
        return outputGas1;
    }

    public Optional<CatalyticReformerFluidStack> outputFluid2() {
        return outputFluid2;
    }

    public Optional<CatalyticReformerGasStack> outputGas2() {
        return outputGas2;
    }

    public Optional<CatalyticReformerFluidStack> outputFluid3() {
        return outputFluid3;
    }

    public Optional<CatalyticReformerGasStack> outputGas3() {
        return outputGas3;
    }

    public int baseTicks() {
        return baseTicks;
    }

    @Override
    public boolean matches(CatalyticReformerRecipeInput input, Level level) {
        if (!this.catalyst.test(input.catalyst())) {
            return false;
        }

        if (this.inputFluid1.isPresent()) {
            if (!matchesFluidRequirement(input.inputFluid1(), this.inputFluid1.get())) {
                return false;
            }
        } else if (!matchesGasRequirement(input.inputGas1(), this.inputGas1.orElseThrow())) {
            return false;
        }

        if (this.inputFluid2.isPresent()) {
            if (!matchesFluidRequirement(input.inputFluid2(), this.inputFluid2.get())) {
                return false;
            }
        } else if (!matchesGasRequirement(input.inputGas2(), this.inputGas2.orElseThrow())) {
            return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(CatalyticReformerRecipeInput input, HolderLookup.Provider registries) {
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
    public RecipeSerializer<? extends Recipe<CatalyticReformerRecipeInput>> getSerializer() {
        return ModRecipeSerializers.CATALYTIC_REFORMER.get();
    }

    @Override
    public RecipeType<? extends Recipe<CatalyticReformerRecipeInput>> getType() {
        return ModRecipeTypes.CATALYTIC_REFORMER.get();
    }

    public static class Serializer implements RecipeSerializer<CatalyticReformerRecipe> {
        public static final MapCodec<CatalyticReformerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CatalyticReformerFluidStack.CODEC.optionalFieldOf("input_fluid_1").forGetter(CatalyticReformerRecipe::inputFluid1),
                CatalyticReformerGasStack.CODEC.optionalFieldOf("input_gas_1").forGetter(CatalyticReformerRecipe::inputGas1),

                CatalyticReformerFluidStack.CODEC.optionalFieldOf("input_fluid_2").forGetter(CatalyticReformerRecipe::inputFluid2),
                CatalyticReformerGasStack.CODEC.optionalFieldOf("input_gas_2").forGetter(CatalyticReformerRecipe::inputGas2),

                Ingredient.CODEC.fieldOf("catalyst").forGetter(CatalyticReformerRecipe::catalyst),
                Codec.BOOL.optionalFieldOf("consume_catalyst", false).forGetter(CatalyticReformerRecipe::consumeCatalyst),

                CatalyticReformerFluidStack.CODEC.optionalFieldOf("output_fluid_1").forGetter(CatalyticReformerRecipe::outputFluid1),
                CatalyticReformerGasStack.CODEC.optionalFieldOf("output_gas_1").forGetter(CatalyticReformerRecipe::outputGas1),

                CatalyticReformerFluidStack.CODEC.optionalFieldOf("output_fluid_2").forGetter(CatalyticReformerRecipe::outputFluid2),
                CatalyticReformerGasStack.CODEC.optionalFieldOf("output_gas_2").forGetter(CatalyticReformerRecipe::outputGas2),

                CatalyticReformerFluidStack.CODEC.optionalFieldOf("output_fluid_3").forGetter(CatalyticReformerRecipe::outputFluid3),
                CatalyticReformerGasStack.CODEC.optionalFieldOf("output_gas_3").forGetter(CatalyticReformerRecipe::outputGas3),

                Codec.INT.optionalFieldOf("base_ticks", 200).forGetter(CatalyticReformerRecipe::baseTicks)
        ).apply(instance, CatalyticReformerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CatalyticReformerRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            CatalyticReformerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.inputFluid1());
                            CatalyticReformerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.inputGas1());

                            CatalyticReformerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.inputFluid2());
                            CatalyticReformerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.inputGas2());

                            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.catalyst());
                            ByteBufCodecs.BOOL.encode(buf, recipe.consumeCatalyst());

                            CatalyticReformerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.outputFluid1());
                            CatalyticReformerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.outputGas1());

                            CatalyticReformerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.outputFluid2());
                            CatalyticReformerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.outputGas2());

                            CatalyticReformerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.outputFluid3());
                            CatalyticReformerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.outputGas3());

                            ByteBufCodecs.VAR_INT.encode(buf, recipe.baseTicks());
                        },
                        buf -> new CatalyticReformerRecipe(
                                CatalyticReformerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                                CatalyticReformerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),

                                CatalyticReformerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                                CatalyticReformerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),

                                Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                                ByteBufCodecs.BOOL.decode(buf),

                                CatalyticReformerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                                CatalyticReformerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),

                                CatalyticReformerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                                CatalyticReformerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),

                                CatalyticReformerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                                CatalyticReformerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),

                                ByteBufCodecs.VAR_INT.decode(buf)
                        )
                );

        @Override
        public MapCodec<CatalyticReformerRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CatalyticReformerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}