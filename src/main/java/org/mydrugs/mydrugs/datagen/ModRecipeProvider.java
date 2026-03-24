package org.mydrugs.mydrugs.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    protected ModRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
        super(provider, output);
    }

    @Override
    protected void buildRecipes() {
        // Keep custom Advanced Furnace / Mixing Vat recipes handwritten for now.
        // Add normal crafting recipes here later if you want.
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new ModRecipeProvider(provider, output);
        }

        @Override
        public String getName() {
            return "MyDrugs Recipe Provider Runner";
        }
    }
}