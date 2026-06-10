package org.mydrugs.mydrugs.client.compat;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.List;
import java.util.function.Function;

final class JeiCategoryDescriptors {
    static final List<JeiCategoryDescriptor<?>> ALL = List.of(
            descriptor("advanced_furnace", "block.mydrugs.advanced_furnace",
                    AdvancedFurnaceRecipeCategory::new, AdvancedFurnaceRecipeCategory.TYPE,
                    "getAdvancedFurnaceRecipes", ModBlocks.class, "ADVANCED_FURNACE_ITEM", "ADVANCED_FURNACE"),
            descriptor("psychotrope_distillery", "block.mydrugs.psychotrope_distillery",
                    PsychotropeDistilleryRecipeCategory::new, PsychotropeDistilleryRecipeCategory.TYPE,
                    "getPsychotropeDistilleryRecipes", ModBlocks.class, "PSYCHOTROPE_DISTILLERY_ITEM", "PSYCHOTROPE_DISTILLERY"),
            descriptor("centrifuge", "block.mydrugs.centrifuge",
                    CentrifugeRecipeCategory::new, CentrifugeRecipeCategory.TYPE,
                    "getCentrifugeRecipes", ModBlocks.class, "CENTRIFUGE"),
            descriptor("electrolyzer", "block.mydrugs.electrolyzer",
                    ElectrolyzerRecipeCategory::new, ElectrolyzerRecipeCategory.TYPE,
                    "getElectrolyzerRecipes", ModBlocks.class, "ELECTROLYZER"),
            descriptor("distiller", "block.mydrugs.distiller",
                    DistillerRecipeCategory::new, DistillerRecipeCategory.TYPE,
                    "getDistillerRecipes", ModBlocks.class, "DISTILLER"),
            descriptor("drying", "block.mydrugs.drying_rack",
                    DryingRecipeCategory::new, DryingRecipeCategory.TYPE,
                    "getDryingRecipes", ModBlocks.class, "DRYING_RACK", "COFFEE_DRYING_MAT"),
            descriptor("coffee_pulping", "block.mydrugs.manual_coffee_pulper",
                    CoffeePulpingRecipeCategory::new, CoffeePulpingRecipeCategory.TYPE,
                    "getCoffeePulpingRecipes", ModBlocks.class, "MANUAL_COFFEE_PULPER_ITEM", "MANUAL_COFFEE_PULPER"),
            descriptor("reduction_still", "block.mydrugs.reduction_still",
                    ReductionStillRecipeCategory::new, ReductionStillRecipeCategory.TYPE,
                    "getReductionStillRecipes", ModBlocks.class, "REDUCTION_STILL_ITEM", "REDUCTION_STILL"),
            descriptor("evaporation_tray", "block.mydrugs.evaporation_tray",
                    EvaporationTrayRecipeCategory::new, EvaporationTrayRecipeCategory.TYPE,
                    "getEvaporationTrayRecipes", ModBlocks.class, "EVAPORATION_TRAY"),
            descriptor("grinding", "block.mydrugs.grinding_bowl",
                    GrindingRecipeCategory::new, GrindingRecipeCategory.TYPE,
                    "getGrindingRecipes", ModBlocks.class, "GRINDING_BOWL"),
            descriptor("advanced_mixing_vat", "block.mydrugs.advanced_mixing_vat",
                    AdvancedMixingVatRecipeCategory::new, AdvancedMixingVatRecipeCategory.TYPE,
                    "getAdvancedMixingVatRecipes", ModBlocks.class, "ADVANCED_MIXING_VAT"),
            descriptor("biochemical_reactor", "block.mydrugs.biochemical_reactor",
                    BiochemicalReactorRecipeCategory::new, BiochemicalReactorRecipeCategory.TYPE,
                    "getBiochemicalReactorRecipes", ModBlocks.class, "BIOCHEMICAL_REACTOR"),
            descriptor("chemical_reactor", "block.mydrugs.chemical_reactor",
                    ChemicalReactorRecipeCategory::new, ChemicalReactorRecipeCategory.TYPE,
                    "getChemicalReactorRecipes", ModBlocks.class, "CHEMICAL_REACTOR"),
            descriptor("fluid_filtering", "block.mydrugs.fluid_filterer",
                    FluidFiltererRecipeCategory::new, FluidFiltererRecipeCategory.TYPE,
                    "getFluidFiltererRecipes", ModBlocks.class, "FLUID_FILTERER"),
            descriptor("gasifier", "block.mydrugs.gasifier",
                    GasifierRecipeCategory::new, GasifierRecipeCategory.TYPE,
                    "getGasifierRecipes", ModBlocks.class, "GASIFIER"),
            descriptor("growth_chamber", "block.mydrugs.growth_chamber",
                    GrowthChamberRecipeCategory::new, GrowthChamberRecipeCategory.TYPE,
                    "getGrowthChamberRecipes", ModBlocks.class, "GROWTH_CHAMBER"),
            descriptor("mixing_vat", "block.mydrugs.mixing_vat",
                    MixingVatRecipeCategory::new, MixingVatRecipeCategory.TYPE,
                    "getMixingVatRecipes", ModBlocks.class, "MIXING_VAT"),
            descriptor("sieving", "block.mydrugs.sieve",
                    SieveRecipeCategory::new, SieveRecipeCategory.TYPE,
                    "getSieveRecipes", ModBlocks.class, "SIEVE"),
            descriptor("stomp_crafting", "item.mydrugs.stomp_plate",
                    StompCraftingRecipeCategory::new, StompCraftingRecipeCategory.TYPE,
                    "getStompCraftingRecipes", ModItems.class, "STOMP_PLATE"),
            descriptor("catalytic_reformer", "block.mydrugs.catalytic_reformer",
                    CatalyticReformerRecipeCategory::new, CatalyticReformerRecipeCategory.TYPE,
                    "getCatalyticReformerRecipes", ModBlocks.class, "CATALYTIC_REFORMER"),
            descriptor("steam_cracker", "block.mydrugs.steam_cracker",
                    SteamCrackerRecipeCategory::new, SteamCrackerRecipeCategory.TYPE,
                    "getSteamCrackerRecipes", ModBlocks.class, "STEAM_CRACKER"),
            descriptor("btx_fractionation", "block.mydrugs.btx_fractionation_tower",
                    BTXFractionationTowerRecipeCategory::new, BTXFractionationTowerRecipeCategory.TYPE,
                    "getBtxFractionationRecipes", ModBlocks.class, "BTX_FRACTIONATION_TOWER_ITEM", "BTX_FRACTIONATION_TOWER"),
            descriptor("aromatic_extractor", "block.mydrugs.aromatic_extractor",
                    AromaticExtractorRecipeCategory::new, AromaticExtractorRecipeCategory.TYPE,
                    "getAromaticExtractorRecipes", ModBlocks.class, "AROMATIC_EXTRACTOR"),
            descriptor("psy_anvil", "block.mydrugs.psy_anvil",
                    PsyAnvilRecipeCategory::new, PsyAnvilRecipeCategory.TYPE,
                    "getPsyAnvilRecipes", ModBlocks.class, "PSY_ANVIL_ITEM", "PSY_ANVIL"),
            descriptor("psy_mixer", "menu.mydrugs.psy_mixer",
                    PsyMixerRecipeCategory::new, PsyMixerRecipeCategory.TYPE,
                    "getPsyMixerRecipes", ModBlocks.class, "PAINTED_CLAY_BOWL_ITEM", "PAINTED_CLAY_BOWL")
    );

    private JeiCategoryDescriptors() {
    }

    private static <T> JeiCategoryDescriptor<T> descriptor(
            String recipeTypeId,
            String titleKey,
            Function<IGuiHelper, IRecipeCategory<T>> categoryFactory,
            RecipeType<T> jeiType,
            String cacheMethod,
            Class<?> catalystOwner,
            String... catalystFields
    ) {
        return new JeiCategoryDescriptor<>(
                recipeTypeId,
                titleKey,
                categoryFactory,
                jeiType,
                cacheMethod,
                catalystOwner,
                catalystFields
        );
    }
}
