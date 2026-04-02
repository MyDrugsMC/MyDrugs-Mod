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
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
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
        int processingTime
) implements Recipe<EvaporationTrayRecipeInput> {

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
                Codec.INT.optionalFieldOf("processing_time", 200).forGetter(EvaporationTrayRecipe::processingTime)
        ).apply(instance, EvaporationTrayRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, EvaporationTrayRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC, EvaporationTrayRecipe::inputFluid,
                        ByteBufCodecs.INT, EvaporationTrayRecipe::inputAmount,
                        ItemStack.STREAM_CODEC, EvaporationTrayRecipe::result,
                        ByteBufCodecs.INT, EvaporationTrayRecipe::processingTime,
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