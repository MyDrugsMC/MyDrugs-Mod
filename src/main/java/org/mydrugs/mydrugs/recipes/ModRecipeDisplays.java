package org.mydrugs.mydrugs.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.recipes.distiller.DistillerRecipeDisplay;

import java.util.function.Supplier;

public class ModRecipeDisplays {
    private ModRecipeDisplays() {}

    public static final DeferredRegister<RecipeDisplay.Type<?>> RECIPE_DISPLAYS =
            DeferredRegister.create(Registries.RECIPE_DISPLAY, MyDrugs.MODID);

    public static Supplier<RecipeDisplay.Type<DistillerRecipeDisplay>> DISTILLER = RECIPE_DISPLAYS.register("distiller",
            () -> new RecipeDisplay.Type<>(DistillerRecipeDisplay.CODEC, DistillerRecipeDisplay.STREAM_CODEC));
}
