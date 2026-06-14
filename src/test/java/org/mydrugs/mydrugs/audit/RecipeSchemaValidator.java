package org.mydrugs.mydrugs.audit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

final class RecipeSchemaValidator {
    private static final Set<String> VANILLA_TYPES = Set.of(
            "minecraft:crafting_shaped", "minecraft:crafting_shapeless",
            "minecraft:smelting", "minecraft:blasting", "minecraft:smoking",
            "minecraft:campfire_cooking", "minecraft:stonecutting", "minecraft:smithing_transform",
            "minecraft:smithing_trim"
    );
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    private static final Map<String, Set<String>> REQUIRED_FIELDS = requiredFields();

    private final ResourceIndex resources;
    private final RegistrySourceIndex registries;
    private final List<String> errors = new ArrayList<>();

    RecipeSchemaValidator(ResourceIndex resources, RegistrySourceIndex registries) {
        this.resources = resources;
        this.registries = registries;
    }

    List<String> validate(ResourceIndex.Entry entry) {
        errors.clear();
        JsonObject recipe = resources.object(entry.logicalPath());
        String owner = recipeId(entry.logicalPath());
        String type = string(recipe, "type", owner);
        if (type == null) {
            return List.copyOf(errors);
        }
        if (VANILLA_TYPES.contains(type)) {
            validateVanilla(type, recipe, owner);
        } else if (type.startsWith("mydrugs:")) {
            String path = type.substring("mydrugs:".length());
            if (!registries.recipeTypes().contains(path)) {
                error(owner, "uses unregistered custom recipe type " + type);
            } else {
                validateCustom(path, recipe, owner);
            }
        } else {
            error(owner, "uses unknown recipe type " + type);
        }
        return List.copyOf(errors);
    }

    static Set<String> coveredCustomTypes() {
        return REQUIRED_FIELDS.keySet();
    }

    private void validateVanilla(String type, JsonObject recipe, String owner) {
        if ("minecraft:crafting_shaped".equals(type)) {
            JsonArray pattern = array(recipe, "pattern", owner);
            JsonObject key = object(recipe, "key", owner);
            if (pattern != null && key != null) {
                if (pattern.isEmpty() || pattern.size() > 3) {
                    error(owner, "shaped pattern height must be 1-3");
                }
                int width = pattern.isEmpty() ? 0 : pattern.get(0).getAsString().length();
                if (width < 1 || width > 3) {
                    error(owner, "shaped pattern width must be 1-3");
                }
                Set<String> used = new java.util.LinkedHashSet<>();
                for (JsonElement rowElement : pattern) {
                    String row = rowElement.getAsString();
                    if (row.length() != width) {
                        error(owner, "shaped pattern rows have inconsistent widths");
                    }
                    row.chars().filter(ch -> ch != ' ').mapToObj(ch -> Character.toString((char) ch)).forEach(used::add);
                }
                for (Map.Entry<String, JsonElement> entry : key.entrySet()) {
                    if (entry.getKey().length() != 1 || " ".equals(entry.getKey())) {
                        error(owner, "shaped key symbol must be one non-space character: " + entry.getKey());
                    }
                    validateIngredient(entry.getValue(), owner + " key '" + entry.getKey() + "'");
                }
                Set<String> missing = new java.util.LinkedHashSet<>(used);
                missing.removeAll(key.keySet());
                Set<String> unused = new java.util.LinkedHashSet<>(key.keySet());
                unused.removeAll(used);
                if (!missing.isEmpty()) {
                    error(owner, "pattern symbols without key entries: " + missing);
                }
                if (!unused.isEmpty()) {
                    error(owner, "unused shaped key entries: " + unused);
                }
            }
            validateItemStack(recipe.get("result"), owner + " result");
            return;
        }
        if ("minecraft:crafting_shapeless".equals(type)) {
            JsonArray ingredients = array(recipe, "ingredients", owner);
            if (ingredients != null) {
                if (ingredients.isEmpty() || ingredients.size() > 9) {
                    error(owner, "shapeless ingredient count must be 1-9");
                }
                ingredients.forEach(ingredient -> validateIngredient(ingredient, owner + " ingredient"));
            }
            validateItemStack(recipe.get("result"), owner + " result");
            return;
        }
        if (Set.of("minecraft:smelting", "minecraft:blasting", "minecraft:smoking",
                "minecraft:campfire_cooking").contains(type)) {
            validateIngredient(recipe.get("ingredient"), owner + " ingredient");
            validateItemStack(recipe.get("result"), owner + " result");
            positive(recipe, "cookingtime", owner);
            if (recipe.has("experience") && recipe.get("experience").getAsDouble() < 0) {
                error(owner, "experience must be non-negative");
            }
        }
    }

    private void validateCustom(String type, JsonObject recipe, String owner) {
        Set<String> required = REQUIRED_FIELDS.get(type);
        if (required == null) {
            error(owner, "has no explicit schema validator for mydrugs:" + type);
            return;
        }
        for (String field : required) {
            if (!recipe.has(field) || recipe.get(field).isJsonNull()) {
                error(owner, "missing required field '" + field + "'");
            }
        }

        if ("advanced_mixing_vat".equals(type)
                && !recipe.has("fluid_inputs") && !recipe.has("item_inputs")) {
            error(owner, "requires fluid_inputs or item_inputs");
        }
        if ("mixing_vat".equals(type)
                && !recipe.has("result_item") && !recipe.has("result_fluid")) {
            error(owner, "requires result_item or result_fluid");
        }
        if (Set.of("centrifuge", "fluid_filtering").contains(type)
                && !recipe.has("base_ticks") && !recipe.has("clicks_required")) {
            error(owner, "requires base_ticks or clicks_required");
        }

        validateTypedReferences(recipe, owner, null);
        validateNumbers(recipe, owner);

        if ("chemical_reactor".equals(type) && recipe.has("output_id")) {
            String kind = recipe.get("output_kind").getAsString();
            validateRegistryReference(recipe.get("output_id").getAsString(), kind, owner + " output_id");
        }
        if (recipe.has("required_knowledge")) {
            validateRegistryReference(recipe.get("required_knowledge").getAsString(), "knowledge",
                    owner + " required_knowledge");
        }
    }

    private void validateTypedReferences(JsonElement node, String owner, String field) {
        if (node == null || node.isJsonNull()) {
            return;
        }
        if (node.isJsonArray()) {
            if (node.getAsJsonArray().isEmpty()) {
                error(owner, "array field '" + field + "' cannot be empty");
            }
            node.getAsJsonArray().forEach(value -> validateTypedReferences(value, owner, field));
            return;
        }
        if (!node.isJsonObject()) {
            if (node.isJsonPrimitive() && node.getAsJsonPrimitive().isString()) {
                validatePrimitiveReference(field, node.getAsString(), owner);
            }
            return;
        }
        node.getAsJsonObject().entrySet()
                .forEach(entry -> validateTypedReferences(entry.getValue(), owner, entry.getKey()));
    }

    private void validatePrimitiveReference(String field, String value, String owner) {
        if (field == null || "type".equals(field) || field.endsWith("_kind")
                || field.startsWith("required_drug") || field.equals("ritual_actions")) {
            return;
        }
        if ("fluid".equals(field) || field.contains("fluid")) {
            validateRegistryReference(value, "fluid", owner + " " + field);
        } else if ("gas".equals(field) || field.contains("gas")) {
            validateRegistryReference(value, "gas", owner + " " + field);
        } else if (field.contains("knowledge")) {
            validateRegistryReference(value, "knowledge", owner + " " + field);
        } else if (field.equals("id") || field.equals("item") || field.equals("items")
                || field.equals("ingredient") || field.matches("item_\\d+(?:_count)?")
                || Set.of("input_a", "input_b", "drug_input", "reagent_input", "catalyst",
                "base", "material", "stabilizer", "vessel", "gas_output").contains(field)) {
            if (!field.endsWith("_count") && !"gas_output".equals(field)) {
                validateItemReference(value, owner + " " + field);
            }
        }
    }

    private void validateIngredient(JsonElement ingredient, String owner) {
        if (ingredient == null || ingredient.isJsonNull()) {
            error(owner, "is missing");
        } else if (ingredient.isJsonArray()) {
            if (ingredient.getAsJsonArray().isEmpty()) {
                error(owner, "alternatives cannot be empty");
            }
            ingredient.getAsJsonArray().forEach(value -> validateIngredient(value, owner));
        } else if (ingredient.isJsonPrimitive()) {
            validateItemReference(ingredient.getAsString(), owner);
        } else if (ingredient.isJsonObject()) {
            JsonObject object = ingredient.getAsJsonObject();
            if (object.has("item")) {
                validateItemReference(object.get("item").getAsString(), owner);
            } else if (object.has("tag")) {
                validateTagReference(object.get("tag").getAsString(), owner);
            } else if (object.has("items")) {
                validateItemReference(object.get("items").getAsString(), owner);
            } else {
                error(owner, "must contain item, items, or tag");
            }
        }
    }

    private void validateItemStack(JsonElement stack, String owner) {
        if (stack == null || stack.isJsonNull()) {
            error(owner, "is missing");
            return;
        }
        if (stack.isJsonPrimitive()) {
            validateItemReference(stack.getAsString(), owner);
            return;
        }
        if (!stack.isJsonObject()) {
            error(owner, "must be an item id or object");
            return;
        }
        JsonObject object = stack.getAsJsonObject();
        String id = object.has("id") ? object.get("id").getAsString()
                : object.has("item") ? object.get("item").getAsString() : null;
        if (id == null) {
            error(owner, "has no id/item");
        } else {
            validateItemReference(id, owner);
        }
        if (object.has("count") && object.get("count").getAsInt() <= 0) {
            error(owner, "count must be positive");
        }
    }

    private void validateItemReference(String value, String owner) {
        if (value.startsWith("#")) {
            validateTagReference(value.substring(1), owner);
            return;
        }
        validateRegistryReference(value, "item", owner);
    }

    private void validateTagReference(String value, String owner) {
        String normalized = value.startsWith("#") ? value.substring(1) : value;
        if (!ID.matcher(normalized).matches()) {
            error(owner, "has invalid tag id " + value);
            return;
        }
        if (normalized.startsWith("mydrugs:")) {
            String path = normalized.substring("mydrugs:".length());
            String logicalPath = "data/mydrugs/tags/item/" + path + ".json";
            if (!resources.exists(logicalPath)) {
                error(owner, "references missing item tag #" + normalized);
            }
        }
    }

    private void validateRegistryReference(String value, String kind, String owner) {
        if (!ID.matcher(value).matches()) {
            error(owner, "has invalid " + kind + " id " + value);
            return;
        }
        if (!value.startsWith("mydrugs:")) {
            return;
        }
        String path = value.substring("mydrugs:".length());
        Set<String> known = switch (kind) {
            case "item" -> registries.items();
            case "fluid" -> registries.fluids();
            case "gas" -> registries.gases();
            case "knowledge" -> registries.knowledge();
            default -> Set.of();
        };
        if (!known.contains(path)) {
            error(owner, "references unknown mydrugs " + kind + " " + value);
        }
    }

    private void validateNumbers(JsonElement node, String owner) {
        if (node == null || !node.isJsonObject()) {
            if (node != null && node.isJsonArray()) {
                node.getAsJsonArray().forEach(value -> validateNumbers(value, owner));
            }
            return;
        }
        for (Map.Entry<String, JsonElement> entry : node.getAsJsonObject().entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                double number = value.getAsDouble();
                String field = entry.getKey();
                if (!Double.isFinite(number)) {
                    error(owner, field + " must be finite");
                }
                if ((field.equals("count") || field.contains("amount") || field.contains("time")
                        || field.contains("ticks") || field.equals("work") || field.contains("stirs")
                        || field.contains("clicks") || field.equals("residue_every"))
                        && number <= 0) {
                    error(owner, field + " must be positive");
                }
                if (field.contains("chance") && (number < 0 || number > 1)) {
                    error(owner, field + " is outside the expected 0..1 range");
                }
                if ((field.endsWith("_factor") || field.equals("intensity")) && number < 0) {
                    error(owner, field + " must be non-negative");
                }
            } else {
                validateNumbers(value, owner);
            }
        }
    }

    private static String recipeId(String logicalPath) {
        int data = "data/".length();
        int recipe = logicalPath.indexOf("/recipe/");
        String namespace = logicalPath.substring(data, recipe);
        String path = logicalPath.substring(recipe + "/recipe/".length(), logicalPath.length() - 5);
        return namespace + ":" + path;
    }

    private String string(JsonObject object, String field, String owner) {
        if (!object.has(field) || !object.get(field).isJsonPrimitive()
                || !object.get(field).getAsJsonPrimitive().isString()) {
            error(owner, "missing string field '" + field + "'");
            return null;
        }
        return object.get(field).getAsString();
    }

    private JsonArray array(JsonObject object, String field, String owner) {
        if (!object.has(field) || !object.get(field).isJsonArray()) {
            error(owner, "missing array field '" + field + "'");
            return null;
        }
        return object.getAsJsonArray(field);
    }

    private JsonObject object(JsonObject object, String field, String owner) {
        if (!object.has(field) || !object.get(field).isJsonObject()) {
            error(owner, "missing object field '" + field + "'");
            return null;
        }
        return object.getAsJsonObject(field);
    }

    private void positive(JsonObject object, String field, String owner) {
        if (!object.has(field) || !object.get(field).isJsonPrimitive()
                || !object.get(field).getAsJsonPrimitive().isNumber()
                || object.get(field).getAsDouble() <= 0) {
            error(owner, field + " must be a positive number");
        }
    }

    private void error(String owner, String message) {
        errors.add(owner + ": " + message);
    }

    private static Map<String, Set<String>> requiredFields() {
        Map<String, Set<String>> schemas = new LinkedHashMap<>();
        schemas.put("advanced_furnace", Set.of("input_a", "result_a", "cook_time"));
        schemas.put("advanced_mixing_vat", Set.of("output", "processing_time"));
        schemas.put("aromatic_extractor", Set.of("input", "catalyst", "output_1", "output_2", "base_ticks"));
        schemas.put("biochemical_reactor", Set.of("ergot", "tryptophan", "fluid_output", "processing_time"));
        schemas.put("btx_fractionation", Set.of("input", "outputs", "process_time"));
        schemas.put("catalytic_reformer", Set.of("input_fluid_1", "catalyst", "output_fluid_1", "base_ticks"));
        schemas.put("centrifuge", Set.of("input", "output_1"));
        schemas.put("chemical_reactor", Set.of("primary_gas", "output_kind", "output_id", "output_amount", "process_time"));
        schemas.put("coffee_pulping", Set.of("ingredient", "bean_result", "biomass_result", "work"));
        schemas.put("distiller", Set.of("input", "output_1", "base_ticks"));
        schemas.put("drying", Set.of("ingredient", "result", "dry_time"));
        schemas.put("electrolyzer", Set.of("input_fluid", "output_gas_1", "output_gas_2", "base_ticks"));
        schemas.put("evaporation_tray", Set.of("input_fluid", "input_amount", "result", "processing_time"));
        schemas.put("fluid_filtering", Set.of("input", "output_1", "output_item"));
        schemas.put("gasifier", Set.of("input", "gas_output", "gas_amount", "process_time"));
        schemas.put("grinding", Set.of("ingredient", "result", "work"));
        schemas.put("growth_chamber", Set.of("input", "biomass_input", "middle_result", "final_result", "water", "biomass", "base_ticks"));
        schemas.put("mixing_vat", Set.of("fluid_input_1", "required_stirs"));
        schemas.put("psy_anvil", Set.of("ingredients", "required_knowledge", "result"));
        schemas.put("psy_mixer", Set.of("base", "catalyst", "effects", "fallback_result", "material",
                "required_drug", "required_knowledge", "ritual_actions", "ritual_time", "stabilizer", "vessel"));
        schemas.put("psychotrope_distillery", Set.of("base_ticks", "drug_input", "reagent_input", "result"));
        schemas.put("reduction_still", Set.of("cuttings", "solvent", "cuttings_per_batch", "extract_result", "pulp_result", "work"));
        schemas.put("sieving", Set.of("ingredient", "result", "sieve_time"));
        schemas.put("steam_cracker", Set.of("input_fluid", "output_gas_1", "output_gas_2", "output_gas_3", "output_fluid_4", "base_ticks"));
        schemas.put("stomp_crafting", Set.of("ingredients", "result", "work"));
        return Map.copyOf(schemas);
    }
}
