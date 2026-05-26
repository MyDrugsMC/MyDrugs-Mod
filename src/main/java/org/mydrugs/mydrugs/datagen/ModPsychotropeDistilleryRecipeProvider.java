package org.mydrugs.mydrugs.datagen;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ModPsychotropeDistilleryRecipeProvider implements DataProvider {
    private final PackOutput.PathProvider recipePathProvider;

    public ModPsychotropeDistilleryRecipeProvider(PackOutput output) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        recipe(futures, cachedOutput, "lucid_extract_from_coffee",
                "mydrugs:coffee_powder", "mydrugs:activated_coal",
                "mydrugs:lucid_extract", "mydrugs:bitter_residue", 3, 220);
        recipe(futures, cachedOutput, "lucid_extract_from_tobacco",
                "mydrugs:tobacco_handful", "mydrugs:activated_coal",
                "mydrugs:lucid_extract", "mydrugs:spent_filter", 3, 220);
        recipe(futures, cachedOutput, "lucid_extract_from_dried_tobacco",
                "mydrugs:dried_tobacco_leaf", "mydrugs:activated_coal",
                "mydrugs:lucid_extract", "mydrugs:spent_filter", 4, 180);

        recipe(futures, cachedOutput, "calming_resin_from_cannabis",
                "mydrugs:cannabis_powder", "mydrugs:fluid_filter",
                "mydrugs:calming_resin", "mydrugs:spent_filter", 4, 240);
        recipe(futures, cachedOutput, "calming_resin_from_hash_piece",
                "mydrugs:hash_piece", "mydrugs:fluid_filter",
                "mydrugs:calming_resin", "mydrugs:spent_filter", 3, 260);
        recipe(futures, cachedOutput, "calming_resin_from_hash_brick",
                "mydrugs:hash_brick", "mydrugs:fluid_filter",
                "mydrugs:calming_resin", "mydrugs:spent_filter", 2, 320);
        recipe(futures, cachedOutput, "calming_resin_from_cannabis_resin",
                "mydrugs:cannabis_resin", "mydrugs:fluid_filter",
                "mydrugs:calming_resin", "mydrugs:spent_filter", 3, 260);

        recipe(futures, cachedOutput, "redline_fuel_from_cocaine",
                "mydrugs:cocaine_powder", "mydrugs:activated_coal",
                "mydrugs:redline_fuel", "mydrugs:burnt_nerve_residue", 3, 260);
        recipe(futures, cachedOutput, "redline_fuel_from_coca_paste",
                "mydrugs:coca_paste", "mydrugs:activated_coal",
                "mydrugs:redline_fuel", "mydrugs:burnt_nerve_residue", 4, 240);

        recipe(futures, cachedOutput, "overdrive_fuel_from_meth_powder",
                "mydrugs:meth_powder", "mydrugs:activated_coal",
                "mydrugs:overdrive_fuel", "mydrugs:burnt_nerve_residue", 2, 320);
        recipe(futures, cachedOutput, "overdrive_fuel_from_meth_shard",
                "mydrugs:meth_shard", "mydrugs:activated_coal",
                "mydrugs:overdrive_fuel", "mydrugs:burnt_nerve_residue", 2, 340);

        recipe(futures, cachedOutput, "dream_residue_from_lsd",
                "mydrugs:lsd_drop", "mydrugs:resonance_lens",
                "mydrugs:dream_residue", "mydrugs:bitter_residue", 2, 360);
        recipe(futures, cachedOutput, "mycelial_insight_from_mushroom",
                "mydrugs:magic_mushroom_powder", "mydrugs:fungal_fiber",
                "mydrugs:mycelial_insight", "mydrugs:spent_filter", 3, 300);
        recipe(futures, cachedOutput, "mycelial_insight_from_whole_mushroom",
                "mydrugs:magic_mushroom", "mydrugs:fungal_fiber",
                "mydrugs:mycelial_insight", "mydrugs:spent_filter", 4, 260);

        recipe(futures, cachedOutput, "unstable_essence_from_mixed_drug",
                "mydrugs:mixed_drug", "mydrugs:distillation_coil",
                "mydrugs:unstable_essence", "mydrugs:burnt_nerve_residue", 2, 420);
        recipe(futures, cachedOutput, "unstable_essence_from_mixed_lsd",
                "mydrugs:mixed_lsd_drug", "mydrugs:distillation_coil",
                "mydrugs:unstable_essence", "mydrugs:burnt_nerve_residue", 2, 420);
        recipe(futures, cachedOutput, "unstable_essence_from_mixed_mushrooms",
                "mydrugs:mixed_mushrooms_drug", "mydrugs:distillation_coil",
                "mydrugs:unstable_essence", "mydrugs:burnt_nerve_residue", 2, 420);

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void recipe(
            List<CompletableFuture<?>> futures,
            CachedOutput cachedOutput,
            String name,
            String drugInput,
            String reagentInput,
            String result,
            String residue,
            int residueEvery,
            int baseTicks
    ) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "mydrugs:psychotrope_distillery");
        json.addProperty("drug_input", drugInput);
        json.addProperty("reagent_input", reagentInput);
        json.add("result", stack(result));
        json.add("residue_result", stack(residue));
        json.addProperty("residue_every", residueEvery);
        json.addProperty("base_ticks", baseTicks);
        saveRecipe(futures, cachedOutput, "psychotrope_distillery/" + name, json);
    }

    private static JsonObject stack(String item) {
        JsonObject result = new JsonObject();
        result.addProperty("id", item);
        result.addProperty("count", 1);
        return result;
    }

    private void saveRecipe(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, JsonObject json) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        Path path = this.recipePathProvider.json(id);
        futures.add(DataProvider.saveStable(cachedOutput, json, path));
    }

    @Override
    public String getName() {
        return "MyDrugs Psychotrope Distillery Recipes";
    }
}
