package org.mydrugs.mydrugs.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.recipes.grinder.GrindingRecipe;
import org.mydrugs.mydrugs.recipes.stompcrafting.StompCraftingRecipe;

import java.util.function.Supplier;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, MyDrugs.MODID);

    public static final DeferredHolder<RecipeType<?>,RecipeType<GrindingRecipe>> GRINDING =
            RECIPE_TYPES.register("grinding", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "grinding")
            ));

    public static final Supplier<RecipeType<StompCraftingRecipe>> STOMP_CRAFTING =
            RECIPE_TYPES.register("stomp_crafting", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "stomp_crafting")
            ));

}
