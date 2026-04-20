package org.mydrugs.mydrugs.recipes.centrifuge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

import java.util.Optional;

public class CentrifugeRecipe implements Recipe<CentrifugeRecipeInput> {
    private final CentrifugeFluidStack input;
    private final CentrifugeFluidStack output1;
    private final Optional<CentrifugeFluidStack> output2;
    private final int baseTicks;

    public CentrifugeRecipe(
            CentrifugeFluidStack input,
            CentrifugeFluidStack output1,
            Optional<CentrifugeFluidStack> output2,
            int baseTicks
    ) {
        this.input = input;
        this.output1 = output1;
        this.output2 = output2;
        this.baseTicks = baseTicks;
    }

    public CentrifugeFluidStack input() {
        return input;
    }

    public CentrifugeFluidStack output1() {
        return output1;
    }

    public Optional<CentrifugeFluidStack> output2() {
        return output2;
    }

    public int baseTicks() {
        return baseTicks;
    }

    @Override
    public boolean matches(CentrifugeRecipeInput recipeInput, Level level) {
        if (recipeInput.inputFluid() == null) {
            return false;
        }

        return recipeInput.inputFluid().equals(this.input.fluid())
                && recipeInput.inputAmount() >= this.input.amount();
    }

    @Override
    public ItemStack assemble(CentrifugeRecipeInput recipeInput, HolderLookup.Provider registries) {
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
    public RecipeSerializer<? extends Recipe<CentrifugeRecipeInput>> getSerializer() {
        return ModRecipeSerializers.CENTRIFUGE.get();
    }

    @Override
    public RecipeType<? extends Recipe<CentrifugeRecipeInput>> getType() {
        return ModRecipeTypes.CENTRIFUGE.get();
    }

    public static class Serializer implements RecipeSerializer<CentrifugeRecipe> {
        public static final MapCodec<CentrifugeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CentrifugeFluidStack.CODEC.fieldOf("input").forGetter(CentrifugeRecipe::input),
                CentrifugeFluidStack.CODEC.fieldOf("output_1").forGetter(CentrifugeRecipe::output1),
                CentrifugeFluidStack.CODEC.optionalFieldOf("output_2").forGetter(CentrifugeRecipe::output2),
                Codec.INT.optionalFieldOf("base_ticks", 200).forGetter(CentrifugeRecipe::baseTicks)
        ).apply(instance, CentrifugeRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CentrifugeRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        CentrifugeFluidStack.STREAM_CODEC, CentrifugeRecipe::input,
                        CentrifugeFluidStack.STREAM_CODEC, CentrifugeRecipe::output1,
                        CentrifugeFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional), CentrifugeRecipe::output2,
                        ByteBufCodecs.VAR_INT, CentrifugeRecipe::baseTicks,
                        CentrifugeRecipe::new
                );

        @Override
        public MapCodec<CentrifugeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CentrifugeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}