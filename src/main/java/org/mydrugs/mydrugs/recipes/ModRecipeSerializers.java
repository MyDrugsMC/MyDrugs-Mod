package org.mydrugs.mydrugs.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.recipes.advanced_furnace.AdvancedFurnaceRecipe;
import org.mydrugs.mydrugs.recipes.advanced_mixing_vat.AdvancedMixingVatRecipe;
import org.mydrugs.mydrugs.recipes.aromatic_extractor.AromaticExtractorRecipe;
import org.mydrugs.mydrugs.recipes.biochemical_reactor.BiochemicalReactorRecipe;
import org.mydrugs.mydrugs.recipes.biochemical_reactor.BiochemicalReactorRecipeSerializer;
import org.mydrugs.mydrugs.recipes.catalytic_reformer.CatalyticReformerRecipe;
import org.mydrugs.mydrugs.recipes.centrifuge.CentrifugeRecipe;
import org.mydrugs.mydrugs.recipes.electrolyzer.ElectrolyzerRecipe;
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
import org.mydrugs.mydrugs.recipes.steam_cracker.SteamCrackerRecipe;
import org.mydrugs.mydrugs.recipes.stomp_crafting.StompCraftingRecipe;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MyDrugs.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GrindingRecipe>> GRINDING =
            RECIPE_SERIALIZERS.register("grinding", GrindingRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<StompCraftingRecipe>> STOMP_CRAFTING =
            RECIPE_SERIALIZERS.register("stomp_crafting", StompCraftingRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AdvancedFurnaceRecipe>> ADVANCED_FURNACE =
            RECIPE_SERIALIZERS.register("advanced_furnace", AdvancedFurnaceRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MixingVatRecipe>> MIXING_VAT =
            RECIPE_SERIALIZERS.register("mixing_vat", MixingVatRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DistillerRecipe>> DISTILLER =
            RECIPE_SERIALIZERS.register("distiller", DistillerRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DryingRecipe>> DRYING =
            RECIPE_SERIALIZERS.register("drying", DryingRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SieveRecipe>> SIEVING =
            RECIPE_SERIALIZERS.register("sieving", SieveRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FluidFiltererRecipe>> FLUID_FILTERING =
            RECIPE_SERIALIZERS.register("fluid_filtering", FluidFiltererRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EvaporationTrayRecipe>> EVAPORATION_TRAY =
            RECIPE_SERIALIZERS.register("evaporation_tray", EvaporationTrayRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CentrifugeRecipe>> CENTRIFUGE =
            RECIPE_SERIALIZERS.register("centrifuge", CentrifugeRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ElectrolyzerRecipe>> ELECTROLYZER =
            RECIPE_SERIALIZERS.register("electrolyzer", ElectrolyzerRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GrowthChamberRecipe>> GROWTH_CHAMBER =
            RECIPE_SERIALIZERS.register("growth_chamber", GrowthChamberRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BiochemicalReactorRecipe>> BIOCHEMICAL_REACTOR =
            RECIPE_SERIALIZERS.register("biochemical_reactor", BiochemicalReactorRecipeSerializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ChemicalReactorRecipe>> CHEMICAL_REACTOR =
            RECIPE_SERIALIZERS.register("chemical_reactor",
                    ChemicalReactorRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GasifierRecipe>> GASIFIER =
            RECIPE_SERIALIZERS.register("gasifier", GasifierRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AdvancedMixingVatRecipe>> ADVANCED_MIXING_VAT =
            RECIPE_SERIALIZERS.register(
                    "advanced_mixing_vat",
                    AdvancedMixingVatRecipe.Serializer::new
            );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CatalyticReformerRecipe>> CATALYTIC_REFORMER =
            RECIPE_SERIALIZERS.register("catalytic_reformer", CatalyticReformerRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SteamCrackerRecipe>> STEAM_CRACKER =
            RECIPE_SERIALIZERS.register("steam_cracker", SteamCrackerRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AromaticExtractorRecipe>> AROMATIC_EXTRACTOR =
            RECIPE_SERIALIZERS.register("aromatic_extractor", AromaticExtractorRecipe.Serializer::new);

    private ModRecipeSerializers() {
    }
}
