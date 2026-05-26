package org.mydrugs.mydrugs.recipes.evaporation_tray;

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
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

import java.util.List;
import java.util.Objects;

public record EvaporationTrayRecipe(
        ResourceLocation inputFluid,
        int inputAmount,
        ItemStack result,
        int processingTime,
        float purityMin,
        float purityMax
) implements Recipe<EvaporationTrayRecipeInput> {

    public EvaporationTrayRecipe(ResourceLocation inputFluid, int inputAmount, ItemStack result, int processingTime) {
        this(inputFluid, inputAmount, result, processingTime, 1.0F, 1.0F);
    }

    public boolean hasPurityRoll() {
        return purityMin < 1.0F || purityMax < 1.0F;
    }

    @Override
    public boolean matches(EvaporationTrayRecipeInput input, Level level) {
        return input.fluidId() != null
                && Objects.equals(input.fluidId(), this.inputFluid)
                && input.fluidAmount() >= this.inputAmount;
    }

    @Override
    public ItemStack assemble(EvaporationTrayRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public ItemStack result() {
        return result.copy();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeType<? extends Recipe<EvaporationTrayRecipeInput>> getType() {
        return ModRecipeTypes.EVAPORATION_TRAY.get();
    }

    @Override
    public RecipeSerializer<? extends Recipe<EvaporationTrayRecipeInput>> getSerializer() {
        return ModRecipeSerializers.EVAPORATION_TRAY.get();
    }

    public static class Serializer implements RecipeSerializer<EvaporationTrayRecipe> {
        public static final MapCodec<EvaporationTrayRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("input_fluid").forGetter(EvaporationTrayRecipe::inputFluid),
                Codec.INT.fieldOf("input_amount").forGetter(EvaporationTrayRecipe::inputAmount),
                ItemStack.CODEC.fieldOf("result").forGetter(EvaporationTrayRecipe::result),
                Codec.INT.optionalFieldOf("processing_time", 200).forGetter(EvaporationTrayRecipe::processingTime),
                Codec.floatRange(0.0F, 1.0F).optionalFieldOf("purity_min", 1.0F).forGetter(EvaporationTrayRecipe::purityMin),
                Codec.floatRange(0.0F, 1.0F).optionalFieldOf("purity_max", 1.0F).forGetter(EvaporationTrayRecipe::purityMax)
        ).apply(instance, EvaporationTrayRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, EvaporationTrayRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC, EvaporationTrayRecipe::inputFluid,
                        ByteBufCodecs.INT, EvaporationTrayRecipe::inputAmount,
                        ItemStack.STREAM_CODEC, EvaporationTrayRecipe::result,
                        ByteBufCodecs.INT, EvaporationTrayRecipe::processingTime,
                        ByteBufCodecs.FLOAT, EvaporationTrayRecipe::purityMin,
                        ByteBufCodecs.FLOAT, EvaporationTrayRecipe::purityMax,
                        EvaporationTrayRecipe::new
                );

        @Override
        public MapCodec<EvaporationTrayRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EvaporationTrayRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}