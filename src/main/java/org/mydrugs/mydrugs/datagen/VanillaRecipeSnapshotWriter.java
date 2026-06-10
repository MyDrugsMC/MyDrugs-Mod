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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

final class VanillaRecipeSnapshotWriter {
    private static final Pattern PATH = Pattern.compile("[a-z0-9_./-]+");
    private static final Pattern RESOURCE_ID = Pattern.compile("#?[a-z0-9_.-]+:[a-z0-9_./-]+");

    private final PackOutput.PathProvider recipePathProvider;
    private final List<CompletableFuture<?>> futures;
    private final CachedOutput cachedOutput;
    private final String owner;
    private final Set<String> recipeIds = new HashSet<>();

    VanillaRecipeSnapshotWriter(
            PackOutput.PathProvider recipePathProvider,
            List<CompletableFuture<?>> futures,
            CachedOutput cachedOutput,
            String owner
    ) {
        this.recipePathProvider = recipePathProvider;
        this.futures = futures;
        this.cachedOutput = cachedOutput;
        this.owner = owner;
    }

    void shaped(
            String name,
            String[] pattern,
            Map<String, Object> key,
            String result,
            int count
    ) {
        validateResult(name, result, count);
        validateShaped(name, pattern, key);

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
        validateResult(name, result, count);
        if (ingredients == null || ingredients.length == 0) {
            throw invalid(name, "shapeless recipes require at least one ingredient");
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shapeless");
        json.addProperty("category", "misc");

        JsonArray ingredientsArray = new JsonArray();
        for (Object value : ingredients) {
            validateIngredient(name, value);
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
        validateResult(name, result, 1);
        validateIngredient(name, ingredient);
        if (!Float.isFinite(experience) || experience < 0.0F) {
            throw invalid(name, "experience must be finite and non-negative");
        }
        if (cookingTime <= 0) {
            throw invalid(name, "cooking time must be positive");
        }

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
        if (name == null || !PATH.matcher(name).matches()) {
            throw invalid(String.valueOf(name), "recipe path must match " + PATH.pattern());
        }
        if (!recipeIds.add(name)) {
            throw invalid(name, "duplicate recipe id");
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        Path path = this.recipePathProvider.json(id);
        futures.add(DatagenOutputGuard.saveStable(owner, cachedOutput, json, path));
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
            if (map.putIfAbsent(key, values[i + 1]) != null) {
                throw new IllegalArgumentException("Duplicate recipe key symbol: '" + key + "'");
            }
        }

        return map;
    }

    static String[] alt(String... alternatives) {
        return alternatives;
    }

    private static void validateShaped(String name, String[] pattern, Map<String, Object> key) {
        if (pattern == null || pattern.length == 0 || pattern.length > 3) {
            throw invalid(name, "shaped pattern height must be between 1 and 3");
        }
        int width = pattern[0] == null ? 0 : pattern[0].length();
        if (width < 1 || width > 3) {
            throw invalid(name, "shaped pattern width must be between 1 and 3");
        }

        Set<String> used = new HashSet<>();
        for (String row : pattern) {
            if (row == null || row.length() != width) {
                throw invalid(name, "all shaped pattern rows must have width " + width);
            }
            for (int i = 0; i < row.length(); i++) {
                char symbol = row.charAt(i);
                if (symbol != ' ') {
                    used.add(String.valueOf(symbol));
                }
            }
        }
        if (used.isEmpty()) {
            throw invalid(name, "shaped pattern cannot contain only spaces");
        }

        for (Map.Entry<String, Object> entry : key.entrySet()) {
            if (entry.getKey().length() != 1 || " ".equals(entry.getKey())) {
                throw invalid(name, "key symbols must be one non-space character: '" + entry.getKey() + "'");
            }
            validateIngredient(name, entry.getValue());
        }

        Set<String> missing = new HashSet<>(used);
        missing.removeAll(key.keySet());
        if (!missing.isEmpty()) {
            throw invalid(name, "pattern symbols have no key entries: " + missing);
        }
        Set<String> unused = new HashSet<>(key.keySet());
        unused.removeAll(used);
        if (!unused.isEmpty()) {
            throw invalid(name, "key entries are unused by the pattern: " + unused);
        }
    }

    private static void validateIngredient(String name, Object value) {
        if (value instanceof String string) {
            if (!RESOURCE_ID.matcher(string).matches()) {
                throw invalid(name, "invalid ingredient id: '" + string + "'");
            }
            return;
        }
        if (value instanceof String[] alternatives) {
            if (alternatives.length == 0) {
                throw invalid(name, "ingredient alternatives cannot be empty");
            }
            for (String alternative : alternatives) {
                validateIngredient(name, alternative);
            }
            return;
        }
        throw invalid(name, "unsupported ingredient value: " + value);
    }

    private static void validateResult(String name, String result, int count) {
        if (result == null || result.startsWith("#") || !RESOURCE_ID.matcher(result).matches()) {
            throw invalid(name, "invalid result item id: '" + result + "'");
        }
        if (count <= 0) {
            throw invalid(name, "result count must be positive");
        }
    }

    private static IllegalArgumentException invalid(String name, String message) {
        return new IllegalArgumentException("Recipe 'mydrugs:" + name + "': " + message);
    }
}
