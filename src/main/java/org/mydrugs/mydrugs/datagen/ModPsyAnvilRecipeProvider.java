package org.mydrugs.mydrugs.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ModPsyAnvilRecipeProvider implements DataProvider {
    private final PackOutput.PathProvider recipePathProvider;

    public ModPsyAnvilRecipeProvider(PackOutput output) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        psyAnvil(futures, cachedOutput, "copper_plate",
                "mydrugs:cannabinoid",
                ingredients(
                        ingredient("minecraft:copper_ingot", 2)
                ),
                "mydrugs:copper_plate",
                1
        );

        psyAnvil(futures, cachedOutput, "iron_mesh",
                "mydrugs:nicotinic",
                ingredients(
                        ingredient("minecraft:iron_ingot", 1)
                ),
                "mydrugs:iron_mesh",
                1
        );

        psyAnvil(futures, cachedOutput, "steel_plate",
                "mydrugs:steel_plating",
                ingredients(
                        ingredient("mydrugs:steel_ingot", 2)
                ),
                "mydrugs:steel_plate",
                1
        );

        psyAnvil(futures, cachedOutput, "heavy_iron",
                "mydrugs:fermented",
                ingredients(
                        ingredient("minecraft:iron_ingot", 2)
                ),
                "mydrugs:heavy_iron",
                1
        );

        psyAnvil(futures, cachedOutput, "heavy_iron_plate",
                "mydrugs:fermented",
                ingredients(
                        ingredient("mydrugs:heavy_iron", 2)
                ),
                "mydrugs:heavy_iron_plate",
                1
        );

        psyAnvil(futures, cachedOutput, "insulated_wire",
                "mydrugs:stimulant",
                ingredients(
                        ingredient("mydrugs:rubber", 2),
                        ingredient("minecraft:copper_ingot", 1)
                ),
                "mydrugs:insulated_wire",
                6
        );

        psyAnvil(futures, cachedOutput, "advanced_control_circuit",
                "mydrugs:lysergic",
                ingredients(
                        ingredient("mydrugs:insulated_wire", 4),
                        ingredient("minecraft:diamond", 1),
                        ingredient("minecraft:gold_ingot", 2),
                        ingredient("mydrugs:control_circuit", 1),
                        ingredient("minecraft:lapis_lazuli", 1)
                ),
                "mydrugs:advanced_control_circuit",
                1
        );

        psyAnvil(futures, cachedOutput, "mycelial_resonator",
                "mydrugs:overclocked",
                ingredients(
                        ingredient("minecraft:amethyst_shard", 4),
                        ingredient("mydrugs:magic_mushroom_powder", 2),
                        ingredient("minecraft:redstone", 2),
                        ingredient("mydrugs:advanced_control_circuit", 1)
                ),
                "mydrugs:mycelial_resonator",
                1
        );

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void psyAnvil(
            List<CompletableFuture<?>> futures,
            CachedOutput cachedOutput,
            String name,
            String requiredKnowledge,
            JsonArray ingredients,
            String result,
            int count
    ) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "mydrugs:psy_anvil");
        json.addProperty("required_knowledge", requiredKnowledge);
        json.add("ingredients", ingredients);

        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("id", result);
        resultObject.addProperty("count", count);
        json.add("result", resultObject);

        json.addProperty("show_if_locked", true);

        saveRecipe(futures, cachedOutput, name, json);
    }

    private static JsonArray ingredients(JsonObject... ingredients) {
        JsonArray array = new JsonArray();

        for (JsonObject ingredient : ingredients) {
            array.add(ingredient);
        }

        return array;
    }

    private static JsonObject ingredient(String ingredient, int count) {
        JsonObject object = new JsonObject();
        object.add("ingredient", ingredientValue(ingredient));
        object.addProperty("count", count);
        return object;
    }

    private static JsonObject ingredient(String[] alternatives, int count) {
        JsonObject object = new JsonObject();
        object.add("ingredient", ingredientValue(alternatives));
        object.addProperty("count", count);
        return object;
    }

    private static JsonElement ingredientValue(String value) {
        return new JsonPrimitive(value);
    }

    private static JsonElement ingredientValue(String[] alternatives) {
        JsonArray array = new JsonArray();

        for (String alternative : alternatives) {
            array.add(new JsonPrimitive(alternative));
        }

        return array;
    }

    private void saveRecipe(
            List<CompletableFuture<?>> futures,
            CachedOutput cachedOutput,
            String name,
            JsonObject json
    ) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        Path path = this.recipePathProvider.json(id);
        futures.add(DataProvider.saveStable(cachedOutput, json, path));
    }

    @Override
    public String getName() {
        return "MyDrugs Psy Anvil Recipes";
    }
}