package org.mydrugs.mydrugs.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.recipes.advanced_furnace.AdvancedFurnaceRecipe;
import org.mydrugs.mydrugs.recipes.advanced_mixing_vat.AdvancedMixingVatRecipe;
import org.mydrugs.mydrugs.recipes.biochemical_reactor.BiochemicalReactorRecipe;
import org.mydrugs.mydrugs.recipes.centrifuge.CentrifugeRecipe;
import org.mydrugs.mydrugs.recipes.chemical_reactor.ChemicalReactorRecipe;
import org.mydrugs.mydrugs.recipes.distiller.DistillerRecipe;
import org.mydrugs.mydrugs.recipes.drying.DryingRecipe;
import org.mydrugs.mydrugs.recipes.evaporation_tray.EvaporationTrayRecipe;
import org.mydrugs.mydrugs.recipes.filterer.FluidFiltererRecipe;
import org.mydrugs.mydrugs.recipes.gasifier.GasifierRecipe;
import org.mydrugs.mydrugs.recipes.grinder.GrindingRecipe;
import org.mydrugs.mydrugs.recipes.growth_chamber.GrowthChamberRecipe;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatRecipe;
import org.mydrugs.mydrugs.recipes.sieving.SieveRecipe;
import org.mydrugs.mydrugs.recipes.stomp_crafting.StompCraftingRecipe;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, MyDrugs.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<GrindingRecipe>> GRINDING =
            RECIPE_TYPES.register("grinding", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "grinding")
            ));

    public static final DeferredHolder<RecipeType<?>, RecipeType<StompCraftingRecipe>> STOMP_CRAFTING =
            RECIPE_TYPES.register("stomp_crafting", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "stomp_crafting")
            ));

    public static final DeferredHolder<RecipeType<?>, RecipeType<AdvancedFurnaceRecipe>> ADVANCED_FURNACE =
            RECIPE_TYPES.register("advanced_furnace", RecipeType::simple);

    public static final DeferredHolder<RecipeType<?>, RecipeType<MixingVatRecipe>> MIXING_VAT =
            RECIPE_TYPES.register("mixing_vat", RecipeType::simple);

    public static final DeferredHolder<RecipeType<?>, RecipeType<DistillerRecipe>> DISTILLER =
            RECIPE_TYPES.register("distiller", RecipeType::simple);

    public static final DeferredHolder<RecipeType<?>, RecipeType<DryingRecipe>> DRYING =
            RECIPE_TYPES.register("drying", RecipeType::simple);

    public static final DeferredHolder<RecipeType<?>, RecipeType<SieveRecipe>> SIEVING =
            RECIPE_TYPES.register("sieving", RecipeType::simple);

    public static final DeferredHolder<RecipeType<?>, RecipeType<FluidFiltererRecipe>> FLUID_FILTERING =
            RECIPE_TYPES.register("fluid_filtering", RecipeType::simple);

    public static final DeferredHolder<RecipeType<?>, RecipeType<EvaporationTrayRecipe>> EVAPORATION_TRAY =
            RECIPE_TYPES.register("evaporation_tray", RecipeType::simple);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CentrifugeRecipe>> CENTRIFUGE =
            RECIPE_TYPES.register("centrifuge", RecipeType::simple);

    public static final DeferredHolder<RecipeType<?>, RecipeType<GrowthChamberRecipe>> GROWTH_CHAMBER =
            RECIPE_TYPES.register("growth_chamber", RecipeType::simple);

    public static final DeferredHolder<RecipeType<?>, RecipeType<BiochemicalReactorRecipe>> BIOCHEMICAL_REACTOR =
            RECIPE_TYPES.register("biochemical_reactor", RecipeType::simple);

    public static final DeferredHolder<RecipeType<?>, RecipeType<ChemicalReactorRecipe>> CHEMICAL_REACTOR =
            RECIPE_TYPES.register("chemical_reactor", RecipeType::simple);

    public static final DeferredHolder<RecipeType<?>, RecipeType<GasifierRecipe>> GASIFIER =
            RECIPE_TYPES.register("gasifier", RecipeType::simple);

    public static final DeferredHolder<RecipeType<?>, RecipeType<AdvancedMixingVatRecipe>> ADVANCED_MIXING_VAT =
            RECIPE_TYPES.register("advanced_mixing_vat", RecipeType::simple);

    private ModRecipeTypes() {
    }
}
