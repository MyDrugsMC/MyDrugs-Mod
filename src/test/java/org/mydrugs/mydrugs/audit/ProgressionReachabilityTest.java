package org.mydrugs.mydrugs.audit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressionReachabilityTest {
    private static final Path GUIDE_SOURCE = Path.of("docs/progression_guide_pages.md");
    private static final Path ALLOWLIST =
            Path.of("src/test/resources/mydrugs/progression_reachability_allowlist.txt");
    private static final List<Path> RESOURCE_ROOTS = List.of(
            Path.of("src/main/resources"),
            Path.of("src/generated/resources")
    );
    private static final Pattern GUIDE_ITEM = Pattern.compile("(?m)^\\s*@item\\s+(\\S+)");
    private static final Pattern ITEM_FIELD = Pattern.compile("item_\\d+");
    private static final Set<String> INGREDIENT_FIELDS = Set.of(
            "key", "ingredient", "ingredients", "item_inputs", "input", "input_a", "input_b",
            "drug_input", "reagent_input", "catalyst", "base", "material", "stabilizer", "vessel",
            "biomass_input", "cuttings", "solvent", "ergot", "tryptophan"
    );
    private static final Set<String> OUTPUT_FIELDS = Set.of(
            "result", "results", "output", "outputs", "fluid_output", "result_item", "output_item",
            "residue_result", "bonus_result", "fallback_result", "final_result", "middle_result",
            "bean_result", "biomass_result", "pulp_result", "extract_result", "result_a",
            "output_1", "output_2", "output_id"
    );

    @Test
    void curatedProgressionReferencesHaveSurvivalSources() {
        RegistrySourceIndex registries = RegistrySourceIndex.load();
        Set<String> registeredItems = new LinkedHashSet<>();
        registries.items().forEach(path -> registeredItems.add("mydrugs:" + path));
        Set<String> registeredFluids = new LinkedHashSet<>();
        registries.fluids().forEach(path -> registeredFluids.add("mydrugs:" + path));

        Map<String, Reference> references = new LinkedHashMap<>();
        collectMarkdownGuideReferences(references);
        collectJsonGuideReferences(references);

        Set<String> recipeResults = new LinkedHashSet<>();
        collectRecipeReferences(registeredItems, registeredFluids, references, recipeResults);
        Set<String> lootResults = collectLootResults(registeredItems);
        Map<String, String> allowlist = readAllowlist();
        allowlist.forEach((itemId, reason) -> {
            assertTrue(registeredItems.contains(itemId),
                    "Progression reachability allowlist contains an unregistered item: " + itemId);
            assertTrue(references.containsKey(itemId),
                    "Progression reachability allowlist contains an item that is no longer referenced: " + itemId);
        });

        assertTrue(recipeResults.contains("mydrugs:psychotrope_lens"),
                "mydrugs:psychotrope_lens must remain craftable for the Resonator progression.");
        assertFalse(references.containsKey("mydrugs:resonance_lens"),
                "Compatibility-only mydrugs:resonance_lens must not appear in active guide or recipe inputs.");
        assertFalse(allowlist.containsKey("mydrugs:resonance_lens"),
                "Do not allowlist compatibility-only mydrugs:resonance_lens as active progression.");

        List<String> unreachable = new ArrayList<>();
        references.forEach((itemId, reference) -> {
            if (!itemId.startsWith("mydrugs:")
                    || recipeResults.contains(itemId)
                    || lootResults.contains(itemId)
                    || allowlist.containsKey(itemId)) {
                return;
            }
            unreachable.add(itemId
                    + " [" + reference.kinds() + "]"
                    + "\n   referenced at: " + String.join(", ", reference.locations())
                    + "\n   suggestion: add a recipe/loot source, add a precise code-grant/world-only allowlist entry,"
                    + " or fix the bad reference");
        });

        assertTrue(unreachable.isEmpty(),
                "Unreachable curated progression items:\n - " + String.join("\n - ", unreachable));
    }

    private static void collectMarkdownGuideReferences(Map<String, Reference> references) {
        Matcher matcher = GUIDE_ITEM.matcher(SourceIndex.read(GUIDE_SOURCE));
        while (matcher.find()) {
            addReference(references, matcher.group(1), "guide", GUIDE_SOURCE.toString());
        }
    }

    private static void collectJsonGuideReferences(Map<String, Reference> references) {
        for (Path root : RESOURCE_ROOTS) {
            Path guide = root.resolve("assets/mydrugs/guide/pages.json");
            if (!Files.isRegularFile(guide)) {
                continue;
            }
            JsonElement json = parse(guide);
            collectGuideItems(json, guide, references);
        }
    }

    private static void collectGuideItems(
            JsonElement node,
            Path source,
            Map<String, Reference> references
    ) {
        if (node == null || node.isJsonNull()) {
            return;
        }
        if (node.isJsonArray()) {
            node.getAsJsonArray().forEach(value -> collectGuideItems(value, source, references));
            return;
        }
        if (!node.isJsonObject()) {
            return;
        }
        JsonObject object = node.getAsJsonObject();
        if (object.has("type") && object.has("text")
                && "item".equals(object.get("type").getAsString())) {
            addReference(references, object.get("text").getAsString(), "guide", source.toString());
        }
        object.entrySet().forEach(entry -> collectGuideItems(entry.getValue(), source, references));
    }

    private static void collectRecipeReferences(
            Set<String> registeredItems,
            Set<String> registeredFluids,
            Map<String, Reference> references,
            Set<String> results
    ) {
        forEachJsonUnder("data/mydrugs/recipe", recipe -> {
            JsonObject object = parse(recipe).getAsJsonObject();
            object.entrySet().forEach(entry -> {
                String field = entry.getKey();
                if (isIngredientField(field)) {
                    Set<String> ids = new LinkedHashSet<>();
                    collectItemIds(entry.getValue(), registeredItems, ids);
                    ids.forEach(id -> addReference(
                            references, id, "recipe ingredient", recipe.toString() + " [" + field + "]"));
                }
                if (isOutputField(field)) {
                    collectRecipeOutputs(entry.getValue(), registeredItems, registeredFluids, results);
                }
            });
        });
    }

    private static void collectRecipeOutputs(
            JsonElement node,
            Set<String> registeredItems,
            Set<String> registeredFluids,
            Set<String> results
    ) {
        if (node == null || node.isJsonNull()) {
            return;
        }
        if (node.isJsonPrimitive()) {
            if (node.getAsJsonPrimitive().isString()) {
                addRecipeOutput(node.getAsString(), registeredItems, registeredFluids, results);
            }
            return;
        }
        if (node.isJsonArray()) {
            node.getAsJsonArray().forEach(value ->
                    collectRecipeOutputs(value, registeredItems, registeredFluids, results));
            return;
        }
        for (Map.Entry<String, JsonElement> entry : node.getAsJsonObject().entrySet()) {
            JsonElement value = entry.getValue();
            if (Set.of("item", "id", "fluid").contains(entry.getKey())) {
                collectRecipeOutputs(value, registeredItems, registeredFluids, results);
            } else if (value.isJsonArray() || value.isJsonObject()) {
                collectRecipeOutputs(value, registeredItems, registeredFluids, results);
            }
        }
    }

    private static void addRecipeOutput(
            String value,
            Set<String> registeredItems,
            Set<String> registeredFluids,
            Set<String> results
    ) {
        if (registeredItems.contains(value)) {
            results.add(value);
        }
        if (registeredFluids.contains(value)) {
            String bucket = value + "_bucket";
            if (registeredItems.contains(bucket)) {
                results.add(bucket);
            }
        }
    }

    private static Set<String> collectLootResults(Set<String> registeredItems) {
        Set<String> results = new LinkedHashSet<>();
        forEachJsonUnder("data/mydrugs/loot_table", loot -> collectLootItems(parse(loot), registeredItems, results));
        forEachJsonUnder("data/mydrugs/loot_tables", loot -> collectLootItems(parse(loot), registeredItems, results));
        return results;
    }

    private static void collectLootItems(
            JsonElement node,
            Set<String> registeredItems,
            Set<String> results
    ) {
        if (node == null || node.isJsonNull()) {
            return;
        }
        if (node.isJsonArray()) {
            node.getAsJsonArray().forEach(value -> collectLootItems(value, registeredItems, results));
            return;
        }
        if (!node.isJsonObject()) {
            return;
        }
        JsonObject object = node.getAsJsonObject();
        if (object.has("type") && object.has("name")
                && "minecraft:item".equals(object.get("type").getAsString())) {
            addIfRegisteredItem(object.get("name").getAsString(), registeredItems, results);
        }
        object.entrySet().forEach(entry -> collectLootItems(entry.getValue(), registeredItems, results));
    }

    private static void collectItemIds(
            JsonElement node,
            Set<String> registeredItems,
            Set<String> sink
    ) {
        if (node == null || node.isJsonNull()) {
            return;
        }
        if (node.isJsonPrimitive()) {
            if (node.getAsJsonPrimitive().isString()) {
                addIfRegisteredItem(node.getAsString(), registeredItems, sink);
            }
            return;
        }
        if (node.isJsonArray()) {
            node.getAsJsonArray().forEach(value -> collectItemIds(value, registeredItems, sink));
            return;
        }
        for (Map.Entry<String, JsonElement> entry : node.getAsJsonObject().entrySet()) {
            String field = entry.getKey();
            JsonElement value = entry.getValue();
            if (Set.of("item", "items", "id", "ingredient").contains(field)) {
                collectItemIds(value, registeredItems, sink);
            } else if (value.isJsonArray() || value.isJsonObject()) {
                collectItemIds(value, registeredItems, sink);
            }
        }
    }

    private static void addIfRegisteredItem(String value, Set<String> registeredItems, Set<String> sink) {
        if (registeredItems.contains(value)) {
            sink.add(value);
        }
    }

    private static boolean isIngredientField(String field) {
        return INGREDIENT_FIELDS.contains(field) || ITEM_FIELD.matcher(field).matches();
    }

    private static boolean isOutputField(String field) {
        return OUTPUT_FIELDS.contains(field)
                || field.startsWith("result_")
                || field.startsWith("output_")
                || field.endsWith("_result");
    }

    private static void forEachJsonUnder(String relativePath, java.util.function.Consumer<Path> consumer) {
        for (Path root : RESOURCE_ROOTS) {
            Path directory = root.resolve(relativePath);
            if (!Files.isDirectory(directory)) {
                continue;
            }
            try (Stream<Path> paths = Files.walk(directory)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json"))
                        .sorted()
                        .forEach(consumer);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static JsonElement parse(Path path) {
        try {
            return JsonParser.parseString(Files.readString(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map<String, String> readAllowlist() {
        assertTrue(Files.isRegularFile(ALLOWLIST), "Missing progression reachability allowlist: " + ALLOWLIST);
        Map<String, String> entries = new LinkedHashMap<>();
        for (String rawLine : SourceIndex.read(ALLOWLIST).lines().toList()) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\\s+#\\s+", 2);
            assertTrue(parts.length == 2 && !parts[1].isBlank(),
                    "Every progression reachability allowlist entry needs a '# reason': " + rawLine);
            String itemId = parts[0].trim();
            assertFalse(entries.containsKey(itemId),
                    "Duplicate progression reachability allowlist entry: " + itemId);
            entries.put(itemId, parts[1].trim());
        }
        return entries;
    }

    private static void addReference(
            Map<String, Reference> references,
            String itemId,
            String kind,
            String location
    ) {
        if (!itemId.startsWith("mydrugs:")) {
            return;
        }
        references.computeIfAbsent(itemId, ignored -> new Reference())
                .add(kind, location);
    }

    private static final class Reference {
        private final Set<String> kinds = new LinkedHashSet<>();
        private final Set<String> locations = new LinkedHashSet<>();

        void add(String kind, String location) {
            kinds.add(kind);
            locations.add(location);
        }

        String kinds() {
            return String.join(" + ", kinds);
        }

        Set<String> locations() {
            return locations;
        }
    }
}
