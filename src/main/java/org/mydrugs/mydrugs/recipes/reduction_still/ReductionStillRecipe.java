package org.mydrugs.mydrugs.recipes.reduction_still;

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
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

import java.util.List;

public record ReductionStillRecipe(
        Ingredient cuttings,
        Ingredient solvent,
        int cuttingsPerBatch,
        ItemStack extractResult,
        ItemStack pulpResult,
        int work
) implements Recipe<ReductionStillRecipeInput> {

    public ReductionStillRecipe {
        if (cuttings.isEmpty()) throw new IllegalArgumentException("cuttings must not be empty");
        if (solvent.isEmpty()) throw new IllegalArgumentException("solvent must not be empty");
        if (extractResult.isEmpty()) throw new IllegalArgumentException("extract result must not be empty");
        cuttingsPerBatch = Math.max(1, cuttingsPerBatch);
        work = Math.max(1, work);
    }

    @Override
    public boolean matches(ReductionStillRecipeInput input, Level level) {
        return cuttings.test(input.cuttings())
                && input.cuttings().getCount() >= cuttingsPerBatch
                && solvent.test(input.solvent());
    }

    @Override
    public ItemStack assemble(ReductionStillRecipeInput input, HolderLookup.Provider registries) {
        return extractResult.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<ReductionStillRecipeInput>> getSerializer() {
        return ModRecipeSerializers.REDUCTION_STILL.get();
    }

    @Override
    public RecipeType<? extends Recipe<ReductionStillRecipeInput>> getType() {
        return ModRecipeTypes.REDUCTION_STILL.get();
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
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<ReductionStillRecipe> {
        public static final MapCodec<ReductionStillRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("cuttings").forGetter(ReductionStillRecipe::cuttings),
                Ingredient.CODEC.fieldOf("solvent").forGetter(ReductionStillRecipe::solvent),
                Codec.INT.optionalFieldOf("cuttings_per_batch", 16).forGetter(ReductionStillRecipe::cuttingsPerBatch),
                ItemStack.CODEC.fieldOf("extract_result").forGetter(ReductionStillRecipe::extractResult),
                ItemStack.CODEC.optionalFieldOf("pulp_result", ItemStack.EMPTY).forGetter(ReductionStillRecipe::pulpResult),
                Codec.INT.optionalFieldOf("work", 600).forGetter(ReductionStillRecipe::work)
        ).apply(inst, ReductionStillRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ReductionStillRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, ReductionStillRecipe::cuttings,
                Ingredient.CONTENTS_STREAM_CODEC, ReductionStillRecipe::solvent,
                ByteBufCodecs.VAR_INT, ReductionStillRecipe::cuttingsPerBatch,
                ItemStack.STREAM_CODEC, ReductionStillRecipe::extractResult,
                ItemStack.STREAM_CODEC, ReductionStillRecipe::pulpResult,
                ByteBufCodecs.VAR_INT, ReductionStillRecipe::work,
                ReductionStillRecipe::new
        );

        @Override
        public MapCodec<ReductionStillRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ReductionStillRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
