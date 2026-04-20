package org.mydrugs.mydrugs.recipes.distiller;

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
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackSlotDisplay;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

import java.util.List;
import java.util.Optional;

public class DistillerRecipe implements Recipe<DistillerRecipeInput> {
    private final DistillerFluidStack input;
    private final DistillerFluidStack output1;
    private final Optional<DistillerFluidStack> output2;
    private final int baseTicks;

    public DistillerRecipe(
            DistillerFluidStack input,
            DistillerFluidStack output1,
            Optional<DistillerFluidStack> output2,
            int baseTicks
    ) {
        this.input = input;
        this.output1 = output1;
        this.output2 = output2;
        this.baseTicks = baseTicks;
    }

    public DistillerFluidStack input() {
        return input;
    }

    public DistillerFluidStack output1() {
        return output1;
    }

    public Optional<DistillerFluidStack> output2() {
        return output2;
    }

    public int baseTicks() {
        return baseTicks;
    }

    @Override
    public boolean matches(DistillerRecipeInput recipeInput, Level level) {
        if (recipeInput.inputFluid() == null) {
            return false;
        }

        return recipeInput.inputFluid().equals(this.input.fluid())
                && recipeInput.inputAmount() >= this.input.amount();
    }

    @Override
    public ItemStack assemble(DistillerRecipeInput recipeInput, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
                new DistillerRecipeDisplay(
                        new FluidStackSlotDisplay(input.toFluidStack()),
                        new FluidStackSlotDisplay(output1.toFluidStack()),
                        output2.map(o -> new FluidStackSlotDisplay(o.toFluidStack())),
                        new SlotDisplay.ItemSlotDisplay(ModBlocks.DISTILLER_ITEM.get())
                )
        );
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
    public RecipeSerializer<? extends Recipe<DistillerRecipeInput>> getSerializer() {
        return ModRecipeSerializers.DISTILLER.get();
    }

    @Override
    public RecipeType<? extends Recipe<DistillerRecipeInput>> getType() {
        return ModRecipeTypes.DISTILLER.get();
    }

    public static class Serializer implements RecipeSerializer<DistillerRecipe> {
        public static final MapCodec<DistillerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                DistillerFluidStack.CODEC.fieldOf("input").forGetter(DistillerRecipe::input),
                DistillerFluidStack.CODEC.fieldOf("output_1").forGetter(DistillerRecipe::output1),
                DistillerFluidStack.CODEC.optionalFieldOf("output_2").forGetter(DistillerRecipe::output2),
                Codec.INT.optionalFieldOf("base_ticks", 200).forGetter(DistillerRecipe::baseTicks)
        ).apply(instance, DistillerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, DistillerRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        DistillerFluidStack.STREAM_CODEC, DistillerRecipe::input,
                        DistillerFluidStack.STREAM_CODEC, DistillerRecipe::output1,
                        DistillerFluidStack.STREAM_CODEC.apply(ByteBufCodecs::optional), DistillerRecipe::output2,
                        ByteBufCodecs.VAR_INT, DistillerRecipe::baseTicks,
                        DistillerRecipe::new
                );

        @Override
        public MapCodec<DistillerRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DistillerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}