package org.mydrugs.mydrugs.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.compat.gas.*;
import org.mydrugs.mydrugs.items.ModItems;

@JeiPlugin
public class JEIModPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(
                new AdvancedFurnaceRecipeCategory(guiHelper),
                new CentrifugeRecipeCategory(guiHelper),
                new ElectrolyzerRecipeCategory(guiHelper),
                new DistillerRecipeCategory(guiHelper),
                new DryingRecipeCategory(guiHelper),
                new EvaporationTrayRecipeCategory(guiHelper),
                new GrindingRecipeCategory(guiHelper),
                new AdvancedMixingVatRecipeCategory(guiHelper),
                new BiochemicalReactorRecipeCategory(guiHelper),
                new ChemicalReactorRecipeCategory(guiHelper),
                new FluidFiltererRecipeCategory(guiHelper),
                new GasifierRecipeCategory(guiHelper),
                new GrowthChamberRecipeCategory(guiHelper),
                new MixingVatRecipeCategory(guiHelper),
                new SieveRecipeCategory(guiHelper),
                new CatalyticReformerRecipeCategory(guiHelper),
                new BTXFractionationTowerRecipeCategory(guiHelper),
                new StompCraftingRecipeCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(AdvancedFurnaceRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getAdvancedFurnaceRecipes"));
        registration.addRecipes(CentrifugeRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getCentrifugeRecipes"));
        registration.addRecipes(ElectrolyzerRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getElectrolyzerRecipes"));
        registration.addRecipes(DistillerRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getDistillerRecipes"));
        registration.addRecipes(DryingRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getDryingRecipes"));
        registration.addRecipes(EvaporationTrayRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getEvaporationTrayRecipes"));
        registration.addRecipes(GrindingRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getGrindingRecipes"));

        registration.addRecipes(AdvancedMixingVatRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getAdvancedMixingVatRecipes"));
        registration.addRecipes(BiochemicalReactorRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getBiochemicalReactorRecipes"));
        registration.addRecipes(ChemicalReactorRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getChemicalReactorRecipes"));
        registration.addRecipes(FluidFiltererRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getFluidFiltererRecipes"));
        registration.addRecipes(GasifierRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getGasifierRecipes"));
        registration.addRecipes(GrowthChamberRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getGrowthChamberRecipes"));
        registration.addRecipes(MixingVatRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getMixingVatRecipes"));
        registration.addRecipes(SieveRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getSieveRecipes"));
        registration.addRecipes(StompCraftingRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getStompCraftingRecipes"));
        registration.addRecipes(CatalyticReformerRecipeCategory.TYPE, JeiCompatUtil.cachedRecipes("getCatalyticReformerRecipes"));
        registration.addRecipes(BTXFractionationTowerRecipeCategory.TYPE, BTXFractionationTowerRecipeCategory.RECIPES);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        JeiCompatUtil.registerFieldCatalyst(registration, AdvancedFurnaceRecipeCategory.TYPE, ModBlocks.class, "ADVANCED_FURNACE_ITEM", "ADVANCED_FURNACE");
        JeiCompatUtil.registerFieldCatalyst(registration, CentrifugeRecipeCategory.TYPE, ModBlocks.class, "CENTRIFUGE");
        JeiCompatUtil.registerFieldCatalyst(registration, ElectrolyzerRecipeCategory.TYPE, ModBlocks.class, "ELECTROLYZER");
        JeiCompatUtil.registerFieldCatalyst(registration, DistillerRecipeCategory.TYPE, ModBlocks.class, "DISTILLER");
        JeiCompatUtil.registerFieldCatalyst(registration, DryingRecipeCategory.TYPE, ModBlocks.class, "DRYING_RACK");
        JeiCompatUtil.registerFieldCatalyst(registration, EvaporationTrayRecipeCategory.TYPE, ModBlocks.class, "EVAPORATION_TRAY");
        JeiCompatUtil.registerFieldCatalyst(registration, GrindingRecipeCategory.TYPE, ModBlocks.class, "GRINDING_BOWL");

        JeiCompatUtil.registerFieldCatalyst(registration, AdvancedMixingVatRecipeCategory.TYPE, ModBlocks.class, "ADVANCED_MIXING_VAT");
        JeiCompatUtil.registerFieldCatalyst(registration, BiochemicalReactorRecipeCategory.TYPE, ModBlocks.class, "BIOCHEMICAL_REACTOR");
        JeiCompatUtil.registerFieldCatalyst(registration, ChemicalReactorRecipeCategory.TYPE, ModBlocks.class, "CHEMICAL_REACTOR");
        JeiCompatUtil.registerFieldCatalyst(registration, FluidFiltererRecipeCategory.TYPE, ModBlocks.class, "FLUID_FILTERER");
        JeiCompatUtil.registerFieldCatalyst(registration, GasifierRecipeCategory.TYPE, ModBlocks.class, "GASIFIER");
        JeiCompatUtil.registerFieldCatalyst(registration, GrowthChamberRecipeCategory.TYPE, ModBlocks.class, "GROWTH_CHAMBER");
        JeiCompatUtil.registerFieldCatalyst(registration, MixingVatRecipeCategory.TYPE, ModBlocks.class, "MIXING_VAT");
        JeiCompatUtil.registerFieldCatalyst(registration, SieveRecipeCategory.TYPE, ModBlocks.class, "SIEVE", "SIEVING_TABLE");
        JeiCompatUtil.registerFieldCatalyst(registration, StompCraftingRecipeCategory.TYPE, ModItems.class, "STOMP_PLATE");
        JeiCompatUtil.registerFieldCatalyst(registration, CatalyticReformerRecipeCategory.TYPE, ModBlocks.class, "CATALYTIC_REFORMER");
        JeiCompatUtil.registerFieldCatalyst(registration, BTXFractionationTowerRecipeCategory.TYPE, ModBlocks.class, "BTX_FRACTIONATION_TOWER_ITEM", "BTX_FRACTIONATION_TOWER");
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        registration.register(
                GasJeiTypes.GAS,
                GasJeiUtil.allIngredients(),
                new GasIngredientHelper(),
                new GasIngredientRenderer(),
                GasJeiIngredient.CODEC
        );
    }
}