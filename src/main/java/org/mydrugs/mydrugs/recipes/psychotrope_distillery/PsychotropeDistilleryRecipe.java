package org.mydrugs.mydrugs.recipes.psychotrope_distillery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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

public final class PsychotropeDistilleryRecipe implements Recipe<PsychotropeDistilleryRecipeInput> {
    private final Ingredient drugInput;
    private final Ingredient reagentInput;
    private final ItemStack result;
    private final Optional<ItemStack> residueResult;
    private final int residueEvery;
    private final int baseTicks;

    public PsychotropeDistilleryRecipe(
            Ingredient drugInput,
            Ingredient reagentInput,
            ItemStack result,
            Optional<ItemStack> residueResult,
            int residueEvery,
            int baseTicks
    ) {
        this.drugInput = drugInput;
        this.reagentInput = reagentInput;
        this.result = result.copy();
        this.residueResult = residueResult.map(ItemStack::copy);
        this.residueEvery = Math.max(0, residueEvery);
        this.baseTicks = Math.max(1, baseTicks);
    }

    public Ingredient drugInput() {
        return this.drugInput;
    }

    public Ingredient reagentInput() {
        return this.reagentInput;
    }

    public ItemStack result() {
        return this.result.copy();
    }

    public Optional<ItemStack> residueResult() {
        return this.residueResult.map(ItemStack::copy);
    }

    public int residueEvery() {
        return this.residueEvery;
    }

    public int baseTicks() {
        return this.baseTicks;
    }

    public boolean hasResidue() {
        return this.residueEvery > 0 && this.residueResult.isPresent() && !this.residueResult.get().isEmpty();
    }

    @Override
    public boolean matches(PsychotropeDistilleryRecipeInput input, Level level) {
        return this.drugInput.test(input.drugInput()) && this.reagentInput.test(input.reagentInput());
    }

    @Override
    public ItemStack assemble(PsychotropeDistilleryRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
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
    public RecipeSerializer<? extends Recipe<PsychotropeDistilleryRecipeInput>> getSerializer() {
        return ModRecipeSerializers.PSYCHOTROPE_DISTILLERY.get();
    }

    @Override
    public RecipeType<? extends Recipe<PsychotropeDistilleryRecipeInput>> getType() {
        return ModRecipeTypes.PSYCHOTROPE_DISTILLERY.get();
    }

    public static final class Serializer implements RecipeSerializer<PsychotropeDistilleryRecipe> {
        public static final MapCodec<PsychotropeDistilleryRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("drug_input").forGetter(PsychotropeDistilleryRecipe::drugInput),
                Ingredient.CODEC.fieldOf("reagent_input").forGetter(PsychotropeDistilleryRecipe::reagentInput),
                ItemStack.CODEC.fieldOf("result").forGetter(PsychotropeDistilleryRecipe::result),
                ItemStack.CODEC.optionalFieldOf("residue_result").forGetter(PsychotropeDistilleryRecipe::residueResult),
                Codec.INT.optionalFieldOf("residue_every", 0).forGetter(PsychotropeDistilleryRecipe::residueEvery),
                Codec.INT.optionalFieldOf("base_ticks", 200).forGetter(PsychotropeDistilleryRecipe::baseTicks)
        ).apply(instance, PsychotropeDistilleryRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PsychotropeDistilleryRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, PsychotropeDistilleryRecipe::drugInput,
                        Ingredient.CONTENTS_STREAM_CODEC, PsychotropeDistilleryRecipe::reagentInput,
                        ItemStack.STREAM_CODEC, PsychotropeDistilleryRecipe::result,
                        ByteBufCodecs.optional(ItemStack.OPTIONAL_STREAM_CODEC), PsychotropeDistilleryRecipe::residueResult,
                        ByteBufCodecs.VAR_INT, PsychotropeDistilleryRecipe::residueEvery,
                        ByteBufCodecs.VAR_INT, PsychotropeDistilleryRecipe::baseTicks,
                        PsychotropeDistilleryRecipe::new
                );

        @Override
        public MapCodec<PsychotropeDistilleryRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PsychotropeDistilleryRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
