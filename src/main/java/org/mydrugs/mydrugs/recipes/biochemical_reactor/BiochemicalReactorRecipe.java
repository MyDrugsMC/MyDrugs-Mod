package org.mydrugs.mydrugs.recipes.biochemical_reactor;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

public class BiochemicalReactorRecipe implements Recipe<BiochemicalReactorRecipeInput> {
    private final CountedIngredient ergot;
    private final CountedIngredient tryptophan;
    private final FluidResult fluidOutput;
    private final int processingTime;
    private final int minimumHeat;
    private final int ergotSpeedCap;
    private final float ergotSpeedPerItem;
    private final float manualBoostFactor;
    private final float heatBonusFactor;

    public BiochemicalReactorRecipe(
            CountedIngredient ergot,
            CountedIngredient tryptophan,
            FluidResult fluidOutput,
            int processingTime,
            int minimumHeat,
            int ergotSpeedCap,
            float ergotSpeedPerItem,
            float manualBoostFactor,
            float heatBonusFactor
    ) {
        this.ergot = ergot;
        this.tryptophan = tryptophan;
        this.fluidOutput = fluidOutput;
        this.processingTime = processingTime;
        this.minimumHeat = minimumHeat;
        this.ergotSpeedCap = ergotSpeedCap;
        this.ergotSpeedPerItem = ergotSpeedPerItem;
        this.manualBoostFactor = manualBoostFactor;
        this.heatBonusFactor = heatBonusFactor;
    }

    @Override
    public boolean matches(BiochemicalReactorRecipeInput input, Level level) {
        return this.ergot.matches(input.ergot()) && this.tryptophan.matches(input.tryptophan());
    }

    @Override
    public ItemStack assemble(BiochemicalReactorRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<? extends Recipe<BiochemicalReactorRecipeInput>> getSerializer() {
        return ModRecipeSerializers.BIOCHEMICAL_REACTOR.get();
    }

    @Override
    public RecipeType<? extends Recipe<BiochemicalReactorRecipeInput>> getType() {
        return ModRecipeTypes.BIOCHEMICAL_REACTOR.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public CountedIngredient ergot() {
        return ergot;
    }

    public CountedIngredient tryptophan() {
        return tryptophan;
    }

    public FluidResult fluidOutput() {
        return fluidOutput;
    }

    public int processingTime() {
        return processingTime;
    }

    public int minimumHeat() {
        return minimumHeat;
    }

    public int ergotSpeedCap() {
        return ergotSpeedCap;
    }

    public float ergotSpeedPerItem() {
        return ergotSpeedPerItem;
    }

    public float manualBoostFactor() {
        return manualBoostFactor;
    }

    public float heatBonusFactor() {
        return heatBonusFactor;
    }
}