package org.mydrugs.mydrugs.audit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancementResourceValidationTest {
    private static final ResourceIndex RESOURCES = ResourceIndex.load();
    private static final RegistrySourceIndex REGISTRIES = RegistrySourceIndex.load();

    @Test
    void advancementDisplayParentsIconsAndRewardsResolve() {
        JsonObject lang = RESOURCES.object("assets/mydrugs/lang/en_us.json");
        Set<String> advancements = ids("advancement");
        advancements.addAll(ids("advancements"));
        Set<String> recipes = recipeIds();
        List<String> errors = new ArrayList<>();

        List<ResourceIndex.Entry> files = new ArrayList<>(RESOURCES.under("data/mydrugs/advancement"));
        files.addAll(RESOURCES.under("data/mydrugs/advancements"));
        assertTrue(!files.isEmpty(), "No advancement JSON found; run datagen before validation");
        for (ResourceIndex.Entry entry : files) {
            if (!entry.logicalPath().endsWith(".json")) {
                continue;
            }
            JsonObject json = RESOURCES.object(entry.logicalPath());
            String id = advancementId(entry.logicalPath());
            if (json.has("display")) {
                JsonObject display = json.getAsJsonObject("display");
                requireDisplayText(display, "title", lang, id, errors);
                requireDisplayText(display, "description", lang, id, errors);
                if (!display.has("icon") || !display.get("icon").isJsonObject()) {
                    errors.add(id + ": display.icon is required");
                } else {
                    JsonObject icon = display.getAsJsonObject("icon");
                    String iconId = icon.has("id") ? icon.get("id").getAsString()
                            : icon.has("item") ? icon.get("item").getAsString() : null;
                    if (iconId == null) {
                        errors.add(id + ": display.icon needs id/item");
                    } else {
                        ResourceIndex.ResourceId parsed = ResourceIndex.ResourceId.parse(iconId, "minecraft").orElse(null);
                        if (parsed == null || ("mydrugs".equals(parsed.namespace())
                                && !REGISTRIES.items().contains(parsed.path()))) {
                            errors.add(id + ": icon item does not exist: " + iconId);
                        }
                    }
                }
            }
            if (json.has("parent")) {
                String parent = json.get("parent").getAsString();
                if (parent.startsWith("mydrugs:") && !advancements.contains(parent)) {
                    errors.add(id + ": missing parent " + parent);
                }
            }
            if (json.has("rewards") && json.getAsJsonObject("rewards").has("recipes")) {
                JsonArray rewards = json.getAsJsonObject("rewards").getAsJsonArray("recipes");
                rewards.forEach(value -> {
                    if (!recipes.contains(value.getAsString())) {
                        errors.add(id + ": missing reward recipe " + value.getAsString());
                    }
                });
            }
        }
        assertTrue(errors.isEmpty(), "Advancement contract failures:\n - " + String.join("\n - ", errors));
    }

    private static void requireDisplayText(
            JsonObject display,
            String field,
            JsonObject lang,
            String id,
            List<String> errors
    ) {
        if (!display.has(field)) {
            errors.add(id + ": display." + field + " is required");
            return;
        }
        JsonElement value = display.get(field);
        if (value.isJsonObject() && value.getAsJsonObject().has("translate")) {
            String key = value.getAsJsonObject().get("translate").getAsString();
            if (!lang.has(key)) {
                errors.add(id + ": missing translation " + key);
            }
        } else if (!value.isJsonPrimitive() || value.getAsString().isBlank()) {
            errors.add(id + ": display." + field + " must be translated or nonblank text");
        }
    }

    private static Set<String> ids(String folder) {
        Set<String> result = new LinkedHashSet<>();
        for (ResourceIndex.Entry entry : RESOURCES.under("data/mydrugs/" + folder)) {
            if (entry.logicalPath().endsWith(".json")) {
                result.add(advancementId(entry.logicalPath()));
            }
        }
        return result;
    }

    private static String advancementId(String path) {
        int singular = path.indexOf("/advancement/");
        String folder = singular >= 0 ? "/advancement/" : "/advancements/";
        int separator = singular >= 0 ? singular : path.indexOf(folder);
        return path.substring("data/".length(), separator) + ":"
                + path.substring(separator + folder.length(), path.length() - ".json".length());
    }

    private static Set<String> recipeIds() {
        Set<String> ids = new LinkedHashSet<>();
        RESOURCES.logicalPaths(path -> path.matches("data/[^/]+/recipe/.+\\.json")).forEach(path -> {
            int separator = path.indexOf("/recipe/");
            ids.add(path.substring("data/".length(), separator) + ":"
                    + path.substring(separator + "/recipe/".length(), path.length() - ".json".length()));
        });
        return ids;
    }
}
