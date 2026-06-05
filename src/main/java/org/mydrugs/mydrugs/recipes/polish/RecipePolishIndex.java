package org.mydrugs.mydrugs.recipes.polish;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class RecipePolishIndex {
    private static final String RESOURCE_PATH = "assets/mydrugs/recipe_polish/recipes.json";
    private static final Map<ResourceLocation, RecipePolishMetadata> FALLBACKS = Map.ofEntries(
            entry("chemical_reactor/methanol", RecipeRole.SUPPORT_REAGENT, MachineLane.CHEMICAL_GAS_FLUID, "Lab Chemistry", "Acids and Reagents", List.of("Advanced Mixing Vat", "late stimulant route"), "recipe_hint.mydrugs.methanol", 2),
            entry("chemical_reactor/sulfur_trioxide", RecipeRole.SUPPORT_REAGENT, MachineLane.CHEMICAL_GAS_FLUID, "Lab Chemistry", "Acids and Reagents", List.of("sulfuric acid"), "recipe_hint.mydrugs.sulfur_trioxide", 2),
            entry("distiller/naphtha", RecipeRole.SUPPORT_REAGENT, MachineLane.INDUSTRIAL_PETROLEUM, "Petroleum", "Petroleum", List.of("Steam Cracker", "Catalytic Reformer"), "recipe_hint.mydrugs.naphtha", 3),
            entry("steam_cracker/btx_mix", RecipeRole.SUPPORT_REAGENT, MachineLane.INDUSTRIAL_PETROLEUM, "Petroleum", "Petroleum", List.of("BTX Fractionation Tower"), "recipe_hint.mydrugs.btx_mix", 3),
            entry("advanced_mixing_vat/late_stimulant_intermediate", RecipeRole.MAIN_PATH, MachineLane.CHEMICAL_GAS_FLUID, "Lab Chemistry", "Meth", List.of("Overclocked route"), "recipe_hint.mydrugs.late_stimulant_intermediate", 4)
    );
    private static final Map<ResourceLocation, RecipePolishMetadata> LOADED = loadSidecar();

    private RecipePolishIndex() {
    }

    public static Optional<RecipePolishMetadata> find(ResourceLocation recipeId) {
        RecipePolishMetadata loaded = LOADED.get(recipeId);
        return Optional.ofNullable(loaded != null ? loaded : FALLBACKS.get(recipeId));
    }

    private static Map.Entry<ResourceLocation, RecipePolishMetadata> entry(
            String path,
            RecipeRole role,
            MachineLane lane,
            String chapter,
            String guidePage,
            List<String> usedNext,
            String hintKey,
            int complexity
    ) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, path);
        return Map.entry(id, new RecipePolishMetadata(id, role, lane, chapter, guidePage, usedNext, hintKey, complexity));
    }

    private static Map<ResourceLocation, RecipePolishMetadata> loadSidecar() {
        Map<ResourceLocation, RecipePolishMetadata> loaded = new HashMap<>();
        ClassLoader loader = RecipePolishIndex.class.getClassLoader();
        try (var stream = loader.getResourceAsStream(RESOURCE_PATH)) {
            if (stream == null) {
                return Map.of();
            }

            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray recipes = root.getAsJsonArray("recipes");
            if (recipes == null) {
                return Map.of();
            }

            for (JsonElement element : recipes) {
                if (!element.isJsonObject()) {
                    continue;
                }
                RecipePolishMetadata metadata = parseMetadata(element.getAsJsonObject());
                loaded.put(metadata.recipe(), metadata);
            }
        } catch (RuntimeException | java.io.IOException ignored) {
            return Map.of();
        }
        return Map.copyOf(loaded);
    }

    private static RecipePolishMetadata parseMetadata(JsonObject object) {
        ResourceLocation recipe = ResourceLocation.parse(requiredString(object, "recipe"));
        RecipeRole role = RecipeRole.valueOf(requiredString(object, "role"));
        MachineLane lane = MachineLane.valueOf(requiredString(object, "lane"));
        String chapter = stringOrEmpty(object, "chapter");
        String guidePage = stringOrEmpty(object, "guide_page");
        String hint = stringOrEmpty(object, "hint");
        int complexity = object.has("complexity") ? object.get("complexity").getAsInt() : 1;
        List<String> usedNext = usedNext(object);
        return new RecipePolishMetadata(recipe, role, lane, chapter, guidePage, usedNext, hint, complexity);
    }

    private static List<String> usedNext(JsonObject object) {
        if (!object.has("used_next") || !object.get("used_next").isJsonArray()) {
            return List.of();
        }
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        for (JsonElement element : object.getAsJsonArray("used_next")) {
            values.add(element.getAsString());
        }
        return List.copyOf(values);
    }

    private static String requiredString(JsonObject object, String key) {
        if (!object.has(key)) {
            throw new IllegalArgumentException("Missing recipe polish key: " + key);
        }
        return object.get(key).getAsString();
    }

    private static String stringOrEmpty(JsonObject object, String key) {
        return object.has(key) ? object.get(key).getAsString() : "";
    }
}
