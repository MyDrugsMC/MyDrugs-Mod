package org.mydrugs.mydrugs.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    protected ModRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
        super(provider, output);
    }

    @Override
    protected void buildRecipes() {
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModItems.RAW_PLATINUM.get()),
                        RecipeCategory.MISC,
                        ModItems.PLATINUM_INGOT.get(),
                        0.7F,
                        200
                )
                .unlockedBy("has_raw_platinum", has(ModItems.RAW_PLATINUM.get()))
                .save(this.output, "platinum_ingot_from_raw_platinum_smelting");

        SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(ModItems.RAW_PLATINUM.get()),
                        RecipeCategory.MISC,
                        ModItems.PLATINUM_INGOT.get(),
                        0.7F,
                        100
                )
                .unlockedBy("has_raw_platinum", has(ModItems.RAW_PLATINUM.get()))
                .save(this.output, "platinum_ingot_from_raw_platinum_blasting");
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