package org.mydrugs.mydrugs.recipes.btx_fractionation;

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
import org.mydrugs.mydrugs.recipes.chemical_reactor.FluidRequirement;

import java.util.List;

/**
 * Data-driven recipe for the BTX Fractionation Tower: consumes a fixed amount of an input fluid
 * and produces up to three fluid outputs over a given number of ticks. Replaces the original
 * hardcoded {@code 1000 mB btx_mix -> 350/300/350 benzene/toluene/xylene over 300 ticks} logic
 * that lived directly inside {@link org.mydrugs.mydrugs.blocks.entity.BTXFractionationTowerBlockEntity}.
 */
public final class BTXFractionationRecipe implements Recipe<BTXFractionationRecipeInput> {
    private final FluidRequirement input;
    private final List<FluidRequirement> outputs;
    private final int processTime;

    public BTXFractionationRecipe(FluidRequirement input, List<FluidRequirement> outputs, int processTime) {
        if (input == null) {
            throw new IllegalArgumentException("input is required");
        }
        if (input.amount() <= 0) {
            throw new IllegalArgumentException("input amount must be > 0");
        }
        if (outputs == null || outputs.isEmpty()) {
            throw new IllegalArgumentException("outputs must have at least one entry");
        }
        if (processTime <= 0) {
            throw new IllegalArgumentException("processTime must be > 0");
        }
        this.input = input;
        this.outputs = List.copyOf(outputs);
        this.processTime = processTime;
    }

    public FluidRequirement input() {
        return input;
    }

    public List<FluidRequirement> outputs() {
        return outputs;
    }

    public int processTime() {
        return processTime;
    }

    @Override
    public boolean matches(BTXFractionationRecipeInput input, Level level) {
        return this.input.test(input.inputFluid());
    }

    @Override
    public ItemStack assemble(BTXFractionationRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends Recipe<BTXFractionationRecipeInput>> getSerializer() {
        return ModRecipeSerializers.BTX_FRACTIONATION.get();
    }

    @Override
    public RecipeType<? extends Recipe<BTXFractionationRecipeInput>> getType() {
        return ModRecipeTypes.BTX_FRACTIONATION.get();
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
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<BTXFractionationRecipe> {
        public static final Codec<FluidRequirement> FLUID_REQ_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                net.minecraft.resources.ResourceLocation.CODEC.fieldOf("fluid").forGetter(FluidRequirement::fluidId),
                Codec.INT.fieldOf("amount").forGetter(FluidRequirement::amount)
        ).apply(instance, FluidRequirement::new));

        public static final MapCodec<BTXFractionationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                FLUID_REQ_CODEC.fieldOf("input").forGetter(BTXFractionationRecipe::input),
                FLUID_REQ_CODEC.listOf().fieldOf("outputs").forGetter(BTXFractionationRecipe::outputs),
                Codec.INT.optionalFieldOf("process_time", 300).forGetter(BTXFractionationRecipe::processTime)
        ).apply(instance, BTXFractionationRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FluidRequirement> FLUID_REQ_STREAM_CODEC =
                StreamCodec.composite(
                        net.minecraft.resources.ResourceLocation.STREAM_CODEC, FluidRequirement::fluidId,
                        ByteBufCodecs.VAR_INT, FluidRequirement::amount,
                        FluidRequirement::new
                );

        public static final StreamCodec<RegistryFriendlyByteBuf, BTXFractionationRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        FLUID_REQ_STREAM_CODEC, BTXFractionationRecipe::input,
                        FLUID_REQ_STREAM_CODEC.apply(ByteBufCodecs.list(8)), BTXFractionationRecipe::outputs,
                        ByteBufCodecs.VAR_INT, BTXFractionationRecipe::processTime,
                        BTXFractionationRecipe::new
                );

        @Override
        public MapCodec<BTXFractionationRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BTXFractionationRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
