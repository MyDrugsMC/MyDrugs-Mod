package org.mydrugs.mydrugs.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.recipes.grinder.GrindingRecipe;
import org.mydrugs.mydrugs.recipes.grinder.GrindingRecipeSerializer;
import org.mydrugs.mydrugs.recipes.stompcrafting.StompCraftingRecipe;
import org.mydrugs.mydrugs.recipes.stompcrafting.StompCraftingRecipeSerializer;

import java.util.function.Supplier;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MyDrugs.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GrindingRecipe>> GRINDING =
            RECIPE_SERIALIZERS.register("grinding", GrindingRecipeSerializer::new);

    public static final Supplier<RecipeSerializer<StompCraftingRecipe>> STOMP_CRAFTING =
            RECIPE_SERIALIZERS.register("stomp_crafting", StompCraftingRecipeSerializer::new);
}
