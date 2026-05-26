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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

final class VanillaRecipeSnapshotWriter {
    private final PackOutput.PathProvider recipePathProvider;
    private final List<CompletableFuture<?>> futures;
    private final CachedOutput cachedOutput;

    VanillaRecipeSnapshotWriter(
            PackOutput.PathProvider recipePathProvider,
            List<CompletableFuture<?>> futures,
            CachedOutput cachedOutput
    ) {
        this.recipePathProvider = recipePathProvider;
        this.futures = futures;
        this.cachedOutput = cachedOutput;
    }

    void shaped(
            String name,
            String[] pattern,
            Map<String, Object> key,
            String result,
            int count
    ) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shaped");
        json.addProperty("category", "misc");

        JsonArray patternArray = new JsonArray();
        for (String row : pattern) {
            patternArray.add(new JsonPrimitive(row));
        }
        json.add("pattern", patternArray);

        JsonObject keyObject = new JsonObject();
        for (Map.Entry<String, Object> entry : key.entrySet()) {
            keyObject.add(entry.getKey(), ingredient(entry.getValue()));
        }
        json.add("key", keyObject);

        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("id", result);
        resultObject.addProperty("count", count);
        json.add("result", resultObject);

        saveRecipe(name, json);
    }

    void shapeless(
            String name,
            Object[] ingredients,
            String result,
            int count
    ) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shapeless");
        json.addProperty("category", "misc");

        JsonArray ingredientsArray = new JsonArray();
        for (Object value : ingredients) {
            ingredientsArray.add(ingredient(value));
        }
        json.add("ingredients", ingredientsArray);

        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("id", result);
        resultObject.addProperty("count", count);
        json.add("result", resultObject);

        saveRecipe(name, json);
    }

    void smelting(
            String name,
            String ingredient,
            String result,
            float experience,
            int cookingTime
    ) {
        cooking(name, "minecraft:smelting", ingredient, result, experience, cookingTime);
    }

    void blasting(
            String name,
            String ingredient,
            String result,
            float experience,
            int cookingTime
    ) {
        cooking(name, "minecraft:blasting", ingredient, result, experience, cookingTime);
    }

    private void cooking(
            String name,
            String type,
            String ingredient,
            String result,
            float experience,
            int cookingTime
    ) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty("category", "misc");
        json.add("ingredient", ingredient(ingredient));

        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("id", result);
        json.add("result", resultObject);

        json.addProperty("experience", experience);
        json.addProperty("cookingtime", cookingTime);

        saveRecipe(name, json);
    }

    private void saveRecipe(String name, JsonObject json) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        Path path = this.recipePathProvider.json(id);
        futures.add(DataProvider.saveStable(cachedOutput, json, path));
    }

    private static JsonElement ingredient(Object value) {
        if (value instanceof String string) {
            return new JsonPrimitive(string);
        }

        if (value instanceof String[] alternatives) {
            JsonArray array = new JsonArray();
            for (String alternative : alternatives) {
                array.add(new JsonPrimitive(alternative));
            }
            return array;
        }

        throw new IllegalArgumentException("Unsupported recipe ingredient value: " + value);
    }

    static Map<String, Object> key(Object... values) {
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Recipe key entries must be provided as key/value pairs.");
        }

        Map<String, Object> map = new LinkedHashMap<>();

        for (int i = 0; i < values.length; i += 2) {
            if (!(values[i] instanceof String key)) {
                throw new IllegalArgumentException("Recipe key must be a String. Got: " + values[i]);
            }

            map.put(key, values[i + 1]);
        }

        return map;
    }

    static String[] alt(String... alternatives) {
        return alternatives;
    }
}
