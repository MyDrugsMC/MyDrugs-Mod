package org.mydrugs.mydrugs.recipes.steam_cracker;

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

public class SteamCrackerRecipe implements Recipe<SteamCrackerRecipeInput> {
    private final Optional<SteamCrackerFluidStack> inputFluid;
    private final Optional<SteamCrackerGasStack> inputGas;
    private final Optional<SteamCrackerFluidStack> outputFluid1;
    private final Optional<SteamCrackerGasStack> outputGas1;
    private final Optional<SteamCrackerFluidStack> outputFluid2;
    private final Optional<SteamCrackerGasStack> outputGas2;
    private final Optional<SteamCrackerFluidStack> outputFluid3;
    private final Optional<SteamCrackerGasStack> outputGas3;
    private final Optional<SteamCrackerFluidStack> outputFluid4;
    private final Optional<SteamCrackerGasStack> outputGas4;
    private final int baseTicks;

    public SteamCrackerRecipe(
            Optional<SteamCrackerFluidStack> inputFluid,
            Optional<SteamCrackerGasStack> inputGas,
            Optional<SteamCrackerFluidStack> outputFluid1,
            Optional<SteamCrackerGasStack> outputGas1,
            Optional<SteamCrackerFluidStack> outputFluid2,
            Optional<SteamCrackerGasStack> outputGas2,
            Optional<SteamCrackerFluidStack> outputFluid3,
            Optional<SteamCrackerGasStack> outputGas3,
            Optional<SteamCrackerFluidStack> outputFluid4,
            Optional<SteamCrackerGasStack> outputGas4,
            int baseTicks
    ) {
        validatePort(inputFluid, inputGas, "input");
        validatePort(outputFluid1, outputGas1, "output 1");
        validatePort(outputFluid2, outputGas2, "output 2");
        validatePort(outputFluid3, outputGas3, "output 3");
        validatePort(outputFluid4, outputGas4, "output 4");
        if (baseTicks <= 0) {
            throw new IllegalArgumentException("baseTicks must be > 0");
        }
        this.inputFluid = inputFluid;
        this.inputGas = inputGas;
        this.outputFluid1 = outputFluid1;
        this.outputGas1 = outputGas1;
        this.outputFluid2 = outputFluid2;
        this.outputGas2 = outputGas2;
        this.outputFluid3 = outputFluid3;
        this.outputGas3 = outputGas3;
        this.outputFluid4 = outputFluid4;
        this.outputGas4 = outputGas4;
        this.baseTicks = baseTicks;
    }

    private static void validatePort(Optional<?> fluid, Optional<?> gas, String name) {
        if (fluid.isPresent() == gas.isPresent()) {
            throw new IllegalArgumentException(name + " must define exactly one of fluid or gas");
        }
    }

    private static boolean matchesFluid(net.neoforged.neoforge.fluids.FluidStack stack, SteamCrackerFluidStack req) {
        Fluid fluid = BuiltInRegistries.FLUID.getValue(req.fluid());
        return !stack.isEmpty() && fluid != null && fluid != Fluids.EMPTY && stack.is(fluid) && stack.getAmount() >= req.amount();
    }

    private static boolean matchesGas(GasStack stack, SteamCrackerGasStack req) {
        return !stack.isEmpty() && stack.type() != null && stack.type().id().equals(req.gas()) && stack.amount() >= req.amount();
    }

    public Optional<SteamCrackerFluidStack> inputFluid() {
        return inputFluid;
    }

    public Optional<SteamCrackerGasStack> inputGas() {
        return inputGas;
    }

    public Optional<SteamCrackerFluidStack> outputFluid1() {
        return outputFluid1;
    }

    public Optional<SteamCrackerGasStack> outputGas1() {
        return outputGas1;
    }

    public Optional<SteamCrackerFluidStack> outputFluid2() {
        return outputFluid2;
    }

    public Optional<SteamCrackerGasStack> outputGas2() {
        return outputGas2;
    }

    public Optional<SteamCrackerFluidStack> outputFluid3() {
        return outputFluid3;
    }

    public Optional<SteamCrackerGasStack> outputGas3() {
        return outputGas3;
    }

    public Optional<SteamCrackerFluidStack> outputFluid4() {
        return outputFluid4;
    }

    public Optional<SteamCrackerGasStack> outputGas4() {
        return outputGas4;
    }

    public int baseTicks() {
        return baseTicks;
    }

    @Override
    public boolean matches(SteamCrackerRecipeInput input, Level level) {
        return this.inputFluid.map(req -> matchesFluid(input.inputFluid(), req)).orElseGet(
                () -> matchesGas(input.inputGas(), this.inputGas.orElseThrow())
        );
    }

    @Override
    public ItemStack assemble(SteamCrackerRecipeInput input, HolderLookup.Provider registries) {
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
    public RecipeSerializer<? extends Recipe<SteamCrackerRecipeInput>> getSerializer() {
        return ModRecipeSerializers.STEAM_CRACKER.get();
    }

    @Override
    public RecipeType<? extends Recipe<SteamCrackerRecipeInput>> getType() {
        return ModRecipeTypes.STEAM_CRACKER.get();
    }

    public static class Serializer implements RecipeSerializer<SteamCrackerRecipe> {
        public static final MapCodec<SteamCrackerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SteamCrackerFluidStack.CODEC.optionalFieldOf("input_fluid").forGetter(SteamCrackerRecipe::inputFluid),
                SteamCrackerGasStack.CODEC.optionalFieldOf("input_gas").forGetter(SteamCrackerRecipe::inputGas),
                SteamCrackerFluidStack.CODEC.optionalFieldOf("output_fluid_1").forGetter(SteamCrackerRecipe::outputFluid1),
                SteamCrackerGasStack.CODEC.optionalFieldOf("output_gas_1").forGetter(SteamCrackerRecipe::outputGas1),
                SteamCrackerFluidStack.CODEC.optionalFieldOf("output_fluid_2").forGetter(SteamCrackerRecipe::outputFluid2),
                SteamCrackerGasStack.CODEC.optionalFieldOf("output_gas_2").forGetter(SteamCrackerRecipe::outputGas2),
                SteamCrackerFluidStack.CODEC.optionalFieldOf("output_fluid_3").forGetter(SteamCrackerRecipe::outputFluid3),
                SteamCrackerGasStack.CODEC.optionalFieldOf("output_gas_3").forGetter(SteamCrackerRecipe::outputGas3),
                SteamCrackerFluidStack.CODEC.optionalFieldOf("output_fluid_4").forGetter(SteamCrackerRecipe::outputFluid4),
                SteamCrackerGasStack.CODEC.optionalFieldOf("output_gas_4").forGetter(SteamCrackerRecipe::outputGas4),
                Codec.INT.optionalFieldOf("base_ticks", 200).forGetter(SteamCrackerRecipe::baseTicks)
        ).apply(instance, SteamCrackerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SteamCrackerRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    SteamCrackerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.inputFluid());
                    SteamCrackerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.inputGas());
                    SteamCrackerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.outputFluid1());
                    SteamCrackerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.outputGas1());
                    SteamCrackerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.outputFluid2());
                    SteamCrackerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.outputGas2());
                    SteamCrackerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.outputFluid3());
                    SteamCrackerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.outputGas3());
                    SteamCrackerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.outputFluid4());
                    SteamCrackerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.outputGas4());
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.baseTicks());
                },
                buf -> new SteamCrackerRecipe(
                        SteamCrackerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                        SteamCrackerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                        SteamCrackerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                        SteamCrackerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                        SteamCrackerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                        SteamCrackerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                        SteamCrackerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                        SteamCrackerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                        SteamCrackerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                        SteamCrackerGasStack.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf)
                )
        );

        @Override
        public MapCodec<SteamCrackerRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SteamCrackerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
