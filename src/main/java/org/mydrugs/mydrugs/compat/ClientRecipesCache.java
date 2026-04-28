package org.mydrugs.mydrugs.compat;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.advanced_furnace.AdvancedFurnaceRecipe;
import org.mydrugs.mydrugs.recipes.advanced_mixing_vat.AdvancedMixingVatRecipe;
import org.mydrugs.mydrugs.recipes.aromatic_extractor.AromaticExtractorRecipe;
import org.mydrugs.mydrugs.recipes.biochemical_reactor.BiochemicalReactorRecipe;
import org.mydrugs.mydrugs.recipes.catalytic_reformer.CatalyticReformerRecipe;
import org.mydrugs.mydrugs.recipes.centrifuge.CentrifugeRecipe;
import org.mydrugs.mydrugs.recipes.chemical_reactor.ChemicalReactorRecipe;
import org.mydrugs.mydrugs.recipes.distiller.DistillerRecipe;
import org.mydrugs.mydrugs.recipes.drying.DryingRecipe;
import org.mydrugs.mydrugs.recipes.electrolyzer.ElectrolyzerRecipe;
import org.mydrugs.mydrugs.recipes.evaporation_tray.EvaporationTrayRecipe;
import org.mydrugs.mydrugs.recipes.filterer.FluidFiltererRecipe;
import org.mydrugs.mydrugs.recipes.gasifier.GasifierRecipe;
import org.mydrugs.mydrugs.recipes.grinder.GrindingRecipe;
import org.mydrugs.mydrugs.recipes.growth_chamber.GrowthChamberRecipe;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatRecipe;
import org.mydrugs.mydrugs.recipes.sieving.SieveRecipe;
import org.mydrugs.mydrugs.recipes.steam_cracker.SteamCrackerRecipe;
import org.mydrugs.mydrugs.recipes.stomp_crafting.StompCraftingRecipe;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class ClientRecipesCache {
    private static final List<AdvancedFurnaceRecipe> ADVANCED_FURNACE_RECIPES = new ArrayList<>();
    private static final List<AdvancedMixingVatRecipe> ADVANCED_MIXING_VAT_RECIPES = new ArrayList<>();
    private static final List<BiochemicalReactorRecipe> BIOCHEMICAL_REACTOR_RECIPES = new ArrayList<>();
    private static final List<CentrifugeRecipe> CENTRIFUGE_RECIPES = new ArrayList<>();
    private static final List<ElectrolyzerRecipe> ELECTROLYZER_RECIPES = new ArrayList<>();
    private static final List<ChemicalReactorRecipe> CHEMICAL_REACTOR_RECIPES = new ArrayList<>();
    private static final List<DistillerRecipe> DISTILLER_RECIPES = new ArrayList<>();
    private static final List<DryingRecipe> DRYING_RECIPES = new ArrayList<>();
    private static final List<EvaporationTrayRecipe> EVAPORATION_TRAY_RECIPES = new ArrayList<>();
    private static final List<FluidFiltererRecipe> FLUID_FILTERER_RECIPES = new ArrayList<>();
    private static final List<GasifierRecipe> GASIFIER_RECIPES = new ArrayList<>();
    private static final List<GrindingRecipe> GRINDING_RECIPES = new ArrayList<>();
    private static final List<GrowthChamberRecipe> GROWTH_CHAMBER_RECIPES = new ArrayList<>();
    private static final List<MixingVatRecipe> MIXING_VAT_RECIPES = new ArrayList<>();
    private static final List<SieveRecipe> SIEVE_RECIPES = new ArrayList<>();
    private static final List<StompCraftingRecipe> STOMP_CRAFTING_RECIPES = new ArrayList<>();
    private static final List<CatalyticReformerRecipe> CATALYTIC_REFORMER_RECIPES = new ArrayList<>();
    private static final List<SteamCrackerRecipe> STEAM_CRACKER_RECIPES = new ArrayList<>();
    private static final List<AromaticExtractorRecipe> AROMATIC_EXTRACTOR_RECIPES = new ArrayList<>();


    public static List<AdvancedFurnaceRecipe> getAdvancedFurnaceRecipes() {
        return List.copyOf(ADVANCED_FURNACE_RECIPES);
    }

    public static List<AdvancedMixingVatRecipe> getAdvancedMixingVatRecipes() {
        return List.copyOf(ADVANCED_MIXING_VAT_RECIPES);
    }

    public static List<BiochemicalReactorRecipe> getBiochemicalReactorRecipes() {
        return List.copyOf(BIOCHEMICAL_REACTOR_RECIPES);
    }

    public static List<CentrifugeRecipe> getCentrifugeRecipes() {
        return List.copyOf(CENTRIFUGE_RECIPES);
    }

    public static List<ElectrolyzerRecipe> getElectrolyzerRecipes() {
        return List.copyOf(ELECTROLYZER_RECIPES);
    }

    public static List<ChemicalReactorRecipe> getChemicalReactorRecipes() {
        return List.copyOf(CHEMICAL_REACTOR_RECIPES);
    }

    public static List<DistillerRecipe> getDistillerRecipes() {
        return List.copyOf(DISTILLER_RECIPES);
    }

    public static List<DryingRecipe> getDryingRecipes() {
        return List.copyOf(DRYING_RECIPES);
    }

    public static List<EvaporationTrayRecipe> getEvaporationTrayRecipes() {
        return List.copyOf(EVAPORATION_TRAY_RECIPES);
    }

    public static List<FluidFiltererRecipe> getFluidFiltererRecipes() {
        return List.copyOf(FLUID_FILTERER_RECIPES);
    }

    public static List<GasifierRecipe> getGasifierRecipes() {
        return List.copyOf(GASIFIER_RECIPES);
    }

    public static List<GrindingRecipe> getGrindingRecipes() {
        return List.copyOf(GRINDING_RECIPES);
    }

    public static List<GrowthChamberRecipe> getGrowthChamberRecipes() {
        return List.copyOf(GROWTH_CHAMBER_RECIPES);
    }

    public static List<MixingVatRecipe> getMixingVatRecipes() {
        return List.copyOf(MIXING_VAT_RECIPES);
    }

    public static List<SieveRecipe> getSieveRecipes() {
        return List.copyOf(SIEVE_RECIPES);
    }

    public static List<StompCraftingRecipe> getStompCraftingRecipes() {
        return List.copyOf(STOMP_CRAFTING_RECIPES);
    }

    public static List<CatalyticReformerRecipe> getCatalyticReformerRecipes() {
        return List.copyOf(CATALYTIC_REFORMER_RECIPES);
    }

    public static List<SteamCrackerRecipe> getSteamCrackerRecipes() {
        return List.copyOf(STEAM_CRACKER_RECIPES);
    }

    public static List<AromaticExtractorRecipe> getAromaticExtractorRecipes() {
        return List.copyOf(AROMATIC_EXTRACTOR_RECIPES);
    }

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        ADVANCED_FURNACE_RECIPES.clear();
        ADVANCED_MIXING_VAT_RECIPES.clear();
        BIOCHEMICAL_REACTOR_RECIPES.clear();
        CENTRIFUGE_RECIPES.clear();
        ELECTROLYZER_RECIPES.clear();
        CHEMICAL_REACTOR_RECIPES.clear();
        DISTILLER_RECIPES.clear();
        DRYING_RECIPES.clear();
        EVAPORATION_TRAY_RECIPES.clear();
        FLUID_FILTERER_RECIPES.clear();
        GASIFIER_RECIPES.clear();
        GRINDING_RECIPES.clear();
        GROWTH_CHAMBER_RECIPES.clear();
        MIXING_VAT_RECIPES.clear();
        SIEVE_RECIPES.clear();
        STOMP_CRAFTING_RECIPES.clear();
        CATALYTIC_REFORMER_RECIPES.clear();
        STEAM_CRACKER_RECIPES.clear();
        AROMATIC_EXTRACTOR_RECIPES.clear();

        event.getRecipeMap()
                .byType(ModRecipeTypes.ADVANCED_FURNACE.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(ADVANCED_FURNACE_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.ADVANCED_MIXING_VAT.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(ADVANCED_MIXING_VAT_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.BIOCHEMICAL_REACTOR.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(BIOCHEMICAL_REACTOR_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.CENTRIFUGE.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(CENTRIFUGE_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.ELECTROLYZER.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(ELECTROLYZER_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.CHEMICAL_REACTOR.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(CHEMICAL_REACTOR_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.DISTILLER.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(DISTILLER_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.DRYING.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(DRYING_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.EVAPORATION_TRAY.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(EVAPORATION_TRAY_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.FLUID_FILTERING.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(FLUID_FILTERER_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.GASIFIER.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(GASIFIER_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.GRINDING.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(GRINDING_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.GROWTH_CHAMBER.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(GROWTH_CHAMBER_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.MIXING_VAT.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(MIXING_VAT_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.SIEVING.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(SIEVE_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.STOMP_CRAFTING.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(STOMP_CRAFTING_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.CATALYTIC_REFORMER.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(CATALYTIC_REFORMER_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.STEAM_CRACKER.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(STEAM_CRACKER_RECIPES::add);

        event.getRecipeMap()
                .byType(ModRecipeTypes.AROMATIC_EXTRACTOR.get())
                .stream()
                .map(RecipeHolder::value)
                .forEach(AROMATIC_EXTRACTOR_RECIPES::add);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ADVANCED_FURNACE_RECIPES.clear();
        ADVANCED_MIXING_VAT_RECIPES.clear();
        BIOCHEMICAL_REACTOR_RECIPES.clear();
        CENTRIFUGE_RECIPES.clear();
        ELECTROLYZER_RECIPES.clear();
        CHEMICAL_REACTOR_RECIPES.clear();
        DISTILLER_RECIPES.clear();
        DRYING_RECIPES.clear();
        EVAPORATION_TRAY_RECIPES.clear();
        FLUID_FILTERER_RECIPES.clear();
        GASIFIER_RECIPES.clear();
        GRINDING_RECIPES.clear();
        GROWTH_CHAMBER_RECIPES.clear();
        MIXING_VAT_RECIPES.clear();
        SIEVE_RECIPES.clear();
        STOMP_CRAFTING_RECIPES.clear();
        CATALYTIC_REFORMER_RECIPES.clear();
        STEAM_CRACKER_RECIPES.clear();
        AROMATIC_EXTRACTOR_RECIPES.clear();
    }
}
