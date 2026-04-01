package org.mydrugs.mydrugs.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.recipes.advanced_furnace.AdvancedFurnaceRecipe;
import org.mydrugs.mydrugs.recipes.distiller.DistillerRecipe;
import org.mydrugs.mydrugs.recipes.drying.DryingRecipe;
import org.mydrugs.mydrugs.recipes.filterer.FluidFiltererRecipe;
import org.mydrugs.mydrugs.recipes.grinder.GrindingRecipe;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatRecipe;
import org.mydrugs.mydrugs.recipes.sieving.SieveRecipe;
import org.mydrugs.mydrugs.recipes.stompcrafting.StompCraftingRecipe;

import java.util.function.Supplier;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, MyDrugs.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<GrindingRecipe>> GRINDING =
            RECIPE_TYPES.register("grinding", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "grinding")
            ));

    public static final Supplier<RecipeType<StompCraftingRecipe>> STOMP_CRAFTING =
            RECIPE_TYPES.register("stomp_crafting", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "stomp_crafting")
            ));

    public static final Supplier<RecipeType<AdvancedFurnaceRecipe>> ADVANCED_FURNACE =
            RECIPE_TYPES.register("advanced_furnace", RecipeType::simple);

    public static final Supplier<RecipeType<MixingVatRecipe>> MIXING_VAT =
            RECIPE_TYPES.register("mixing_vat", RecipeType::simple);

    public static final Supplier<RecipeType<DistillerRecipe>> DISTILLER =
            RECIPE_TYPES.register("distiller", RecipeType::simple);

    public static final Supplier<RecipeType<DryingRecipe>> DRYING_TYPE =
            RECIPE_TYPES.register("drying", RecipeType::simple);

    public static final Supplier<RecipeType<SieveRecipe>> SIEVING_TYPE =
            RECIPE_TYPES.register("sieving", RecipeType::simple);

    public static final Supplier<RecipeType<FluidFiltererRecipe>> FLUID_FILTERING =
            RECIPE_TYPES.register("fluid_filtering", RecipeType::simple);

    private ModRecipeTypes() {
    }
}
