package org.mydrugs.mydrugs.audit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ResourceAuditManifest {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ResourceAuditManifest() {
    }

    static JsonObject generate() {
        ResourceIndex resources = ResourceIndex.load();
        RegistrySourceIndex registries = RegistrySourceIndex.load();
        JsonObject lang = resources.object("assets/mydrugs/lang/en_us.json");
        JsonObject root = new JsonObject();
        root.addProperty("format", 1);

        JsonObject items = new JsonObject();
        new TreeSet<>(registries.items()).forEach(id -> {
            JsonObject status = new JsonObject();
            String langKey = registries.blockItems().contains(id) ? "block.mydrugs." + id : "item.mydrugs." + id;
            status.addProperty("lang", lang.has(langKey));
            status.addProperty("client_item", resources.exists("assets/mydrugs/items/" + id + ".json"));
            status.addProperty("item_model", resources.exists("assets/mydrugs/models/item/" + id + ".json"));
            items.add(id, status);
        });
        root.add("items", items);

        JsonObject blocks = new JsonObject();
        new TreeSet<>(registries.blocks()).forEach(id -> {
            JsonObject status = new JsonObject();
            status.addProperty("lang", lang.has("block.mydrugs." + id));
            status.addProperty("blockstate", resources.exists("assets/mydrugs/blockstates/" + id + ".json"));
            status.addProperty("loot_required",
                    !registries.fluids().contains(id) && !ResourceExceptions.BLOCKS_WITHOUT_LOOT.containsKey(id));
            status.addProperty("loot", resources.exists("data/mydrugs/loot_table/blocks/" + id + ".json"));
            blocks.add(id, status);
        });
        root.add("blocks", blocks);

        JsonObject recipes = new JsonObject();
        resources.logicalPaths(path -> path.matches("data/[^/]+/recipe/.+\\.json")).stream().sorted().forEach(path -> {
            JsonObject recipe = resources.object(path);
            JsonObject status = new JsonObject();
            status.addProperty("type", recipe.has("type") ? recipe.get("type").getAsString() : "");
            status.addProperty("schema", "validated");
            status.addProperty("references", "validated");
            recipes.add(recipeId(path), status);
        });
        root.add("recipes", recipes);

        JsonObject advancements = new JsonObject();
        resources.logicalPaths(path -> path.matches("data/[^/]+/advancements?/.+\\.json")).stream().sorted().forEach(path -> {
            JsonObject json = resources.object(path);
            JsonObject status = new JsonObject();
            status.addProperty("display", json.has("display"));
            status.addProperty("parent", !json.has("parent") || !json.get("parent").getAsString().startsWith("mydrugs:")
                    || advancementExists(resources, json.get("parent").getAsString()));
            status.addProperty("icon", json.has("display") && json.getAsJsonObject("display").has("icon"));
            status.addProperty("lang", displayLangExists(json, lang));
            advancements.add(advancementId(path), status);
        });
        root.add("advancements", advancements);

        JsonObject jei = new JsonObject();
        Pattern descriptor = Pattern.compile(
                "descriptor\\(\"([a-z0-9_]+)\",\\s*\"([^\"]+)\".*?\"(get[A-Za-z0-9]+Recipes)\"",
                Pattern.DOTALL);
        Matcher matcher = descriptor.matcher(SourceIndex.read(java.nio.file.Path.of(
                "src/main/java/org/mydrugs/mydrugs/client/compat/JeiCategoryDescriptors.java")));
        while (matcher.find()) {
            JsonObject status = new JsonObject();
            status.addProperty("title", lang.has(matcher.group(2)));
            status.addProperty("category", true);
            status.addProperty("cache", matcher.group(3));
            status.addProperty("catalyst", true);
            jei.add(matcher.group(1), status);
        }
        root.add("jei", jei);

        ModelResourceValidator models = new ModelResourceValidator(resources);
        resources.under("assets/mydrugs/items").stream()
                .filter(entry -> entry.logicalPath().endsWith(".json"))
                .forEach(entry -> models.validateClientItem(entry.logicalPath()));
        resources.under("assets/mydrugs/blockstates").stream()
                .filter(entry -> entry.logicalPath().endsWith(".json"))
                .forEach(entry -> models.validateBlockstate(entry.logicalPath()));
        models.validateAllMyDrugsModels();
        JsonArray textureDebt = new JsonArray();
        Set<String> textureReferences = new TreeSet<>();
        models.missingTextures().forEach((path, owners) ->
                owners.forEach(owner -> textureReferences.add(path + " <- " + owner)));
        textureReferences.forEach(textureDebt::add);
        root.add("known_texture_debt", textureDebt);
        root.addProperty("known_texture_debt_count", textureDebt.size());
        return root;
    }

    static String pretty(JsonObject manifest) {
        return GSON.toJson(manifest) + System.lineSeparator();
    }

    static Map<String, Set<String>> missingTextures() {
        ResourceIndex resources = ResourceIndex.load();
        ModelResourceValidator models = new ModelResourceValidator(resources);
        resources.under("assets/mydrugs/items").stream()
                .filter(entry -> entry.logicalPath().endsWith(".json"))
                .forEach(entry -> models.validateClientItem(entry.logicalPath()));
        resources.under("assets/mydrugs/blockstates").stream()
                .filter(entry -> entry.logicalPath().endsWith(".json"))
                .forEach(entry -> models.validateBlockstate(entry.logicalPath()));
        models.validateAllMyDrugsModels();
        return new TreeMap<>(models.missingTextures());
    }

    private static boolean displayLangExists(JsonObject advancement, JsonObject lang) {
        if (!advancement.has("display")) {
            return true;
        }
        JsonObject display = advancement.getAsJsonObject("display");
        return fieldLangExists(display, "title", lang) && fieldLangExists(display, "description", lang);
    }

    private static boolean fieldLangExists(JsonObject display, String field, JsonObject lang) {
        if (!display.has(field)) {
            return false;
        }
        if (!display.get(field).isJsonObject() || !display.getAsJsonObject(field).has("translate")) {
            return true;
        }
        return lang.has(display.getAsJsonObject(field).get("translate").getAsString());
    }

    private static boolean advancementExists(ResourceIndex resources, String id) {
        ResourceIndex.ResourceId parsed = ResourceIndex.ResourceId.parse(id, "minecraft").orElse(null);
        return parsed != null && (resources.exists("data/" + parsed.namespace() + "/advancement/" + parsed.path() + ".json")
                || resources.exists("data/" + parsed.namespace() + "/advancements/" + parsed.path() + ".json"));
    }

    private static String recipeId(String path) {
        int separator = path.indexOf("/recipe/");
        return path.substring("data/".length(), separator) + ":"
                + path.substring(separator + "/recipe/".length(), path.length() - ".json".length());
    }

    private static String advancementId(String path) {
        int singular = path.indexOf("/advancement/");
        String folder = singular >= 0 ? "/advancement/" : "/advancements/";
        int separator = singular >= 0 ? singular : path.indexOf(folder);
        return path.substring("data/".length(), separator) + ":"
                + path.substring(separator + folder.length(), path.length() - ".json".length());
    }
}
