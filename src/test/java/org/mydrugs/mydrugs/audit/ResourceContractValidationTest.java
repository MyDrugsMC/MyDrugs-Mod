package org.mydrugs.mydrugs.audit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceContractValidationTest {
    private static final ResourceIndex RESOURCES = ResourceIndex.load();
    private static final RegistrySourceIndex REGISTRIES = RegistrySourceIndex.load();

    @Test
    void resourceRootsDoNotShadowTheSameLogicalPath() {
        assertTrue(RESOURCES.collisions().isEmpty(),
                "Hand-authored and generated roots contain the same logical paths: " + RESOURCES.collisions());
    }

    @Test
    void registryExtractionCoversKnownRegistrationPatterns() {
        assertTrue(REGISTRIES.items().size() >= 300, "Item extraction is incomplete: " + REGISTRIES.items().size());
        assertTrue(REGISTRIES.blocks().size() >= 140, "Block extraction is incomplete: " + REGISTRIES.blocks().size());
        assertEquals(25, REGISTRIES.recipeTypes().size(), "Recipe type extraction drifted");
        assertEquals(REGISTRIES.recipeTypes(), REGISTRIES.recipeSerializers(),
                "Recipe type and serializer IDs must match");
        for (String id : List.of("advanced_furnace", "distiller", "sieve", "fluid_filterer",
                "stomp_crafter", "steam_cracker", "btx_fractionation_tower")) {
            assertTrue(REGISTRIES.blockItems().contains(id), "Missing known block item from source index: " + id);
        }
        assertTrue(REGISTRIES.blocks().contains("aloe_vera_bush"), "Wild aloe bush must be indexed as a block");
        assertTrue(!REGISTRIES.items().contains("aloe_vera_bush"), "Wild aloe bush intentionally has no item");
    }

    @Test
    void resourceExceptionsHaveReasonsAndRemainNecessary() {
        Map<String, Map<String, String>> exceptionGroups = Map.of(
                "items without client definitions", ResourceExceptions.ITEMS_WITHOUT_CLIENT_DEFINITIONS,
                "items without translations", ResourceExceptions.ITEMS_WITHOUT_TRANSLATIONS,
                "blocks without translations", ResourceExceptions.BLOCKS_WITHOUT_TRANSLATIONS,
                "blocks without items", ResourceExceptions.BLOCKS_WITHOUT_ITEMS,
                "blocks without loot", ResourceExceptions.BLOCKS_WITHOUT_LOOT
        );
        exceptionGroups.forEach((group, entries) -> entries.forEach((id, reason) ->
                assertTrue(reason != null && !reason.isBlank(), group + " exception needs a reason: " + id)));

        ResourceExceptions.ITEMS_WITHOUT_CLIENT_DEFINITIONS.forEach((id, reason) ->
                assertTrue(!RESOURCES.exists("assets/mydrugs/items/" + id + ".json"),
                        "Stale client-definition exception: " + id));
        ResourceExceptions.ITEMS_WITHOUT_TRANSLATIONS.forEach((id, reason) ->
                assertTrue(!RESOURCES.object("assets/mydrugs/lang/en_us.json").has("item.mydrugs." + id),
                        "Stale item-translation exception: " + id));
        ResourceExceptions.BLOCKS_WITHOUT_TRANSLATIONS.forEach((id, reason) ->
                assertTrue(!RESOURCES.object("assets/mydrugs/lang/en_us.json").has("block.mydrugs." + id),
                        "Stale block-translation exception: " + id));
        ResourceExceptions.BLOCKS_WITHOUT_ITEMS.forEach((id, reason) -> {
            assertTrue(REGISTRIES.blocks().contains(id), "Exception refers to an unregistered block: " + id);
            assertTrue(!REGISTRIES.blockItems().contains(id), "Stale no-item exception: " + id);
        });
        ResourceExceptions.BLOCKS_WITHOUT_LOOT.forEach((id, reason) ->
                assertTrue(!RESOURCES.exists("data/mydrugs/loot_table/blocks/" + id + ".json"),
                        "Stale no-loot exception: " + id));
    }

    @Test
    void localizationCoversPlayerFacingRegistrationsAndStaticKeys() {
        JsonObject lang = RESOURCES.object("assets/mydrugs/lang/en_us.json");
        List<String> missingItems = new ArrayList<>();
        List<String> missingBlocks = new ArrayList<>();
        Set<String> missingStatic = new TreeSet<>();

        REGISTRIES.items().stream()
                .filter(id -> !REGISTRIES.blockItems().contains(id))
                .filter(id -> !ResourceExceptions.ITEMS_WITHOUT_TRANSLATIONS.containsKey(id))
                .map(id -> "item.mydrugs." + id)
                .filter(key -> !lang.has(key))
                .forEach(missingItems::add);
        REGISTRIES.blocks().stream()
                .filter(id -> !ResourceExceptions.BLOCKS_WITHOUT_TRANSLATIONS.containsKey(id))
                .map(id -> "block.mydrugs." + id)
                .filter(key -> !lang.has(key))
                .forEach(missingBlocks::add);

        String sources = SourceIndex.allSources();
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?:Component\\.)?translatable\\(\\s*\"([^\"]+)\"")
                .matcher(sources);
        while (matcher.find()) {
            String key = matcher.group(1);
            if (key.startsWith("mydrugs.") || key.contains(".mydrugs.")) {
                if (!key.contains("%") && !key.endsWith(".") && !lang.has(key)) {
                    missingStatic.add(key);
                }
            }
        }

        Set<String> requiredGroups = Set.of(
                "itemGroup.mydrugs.main", "itemGroup.mydrugs.machines", "itemGroup.mydrugs.materials",
                "itemGroup.mydrugs.plants", "itemGroup.mydrugs.fluids_and_gases",
                "itemGroup.mydrugs.recovery_and_psyche", "itemGroup.mydrugs.food_and_consumables"
        );
        requiredGroups.stream().filter(key -> !lang.has(key)).forEach(missingStatic::add);

        String failure = ResourceIndex.formatMissing("Missing item translations", missingItems)
                + "\n" + ResourceIndex.formatMissing("Missing block translations", missingBlocks)
                + "\n" + ResourceIndex.formatMissing("Missing static/UI translations", missingStatic);
        assertTrue(missingItems.isEmpty() && missingBlocks.isEmpty() && missingStatic.isEmpty(), failure);

        lang.entrySet().forEach(entry -> assertTrue(
                entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString(),
                "Localization value must be a string: " + entry.getKey()));
    }

    @Test
    void everyRegisteredItemHasAResolvableClientDefinitionAndModel() {
        ModelResourceValidator models = new ModelResourceValidator(RESOURCES);
        Set<String> missingDefinitions = new TreeSet<>();
        for (String id : REGISTRIES.items()) {
            if (ResourceExceptions.ITEMS_WITHOUT_CLIENT_DEFINITIONS.containsKey(id)) {
                continue;
            }
            String path = "assets/mydrugs/items/" + id + ".json";
            if (!RESOURCES.exists(path)) {
                missingDefinitions.add(path);
            } else {
                models.validateClientItem(path);
            }
        }
        models.validateAllMyDrugsModels();
        assertTrue(missingDefinitions.isEmpty(),
                ResourceIndex.formatMissing("Registered items without client definitions (run datagen)", missingDefinitions));
        assertTrue(models.missingModels().isEmpty(),
                ResourceIndex.formatMissing("Missing models", models.missingModels()));
        assertKnownTextureDebt(models.missingTextures());
    }

    @Test
    void everyRegisteredBlockHasBlockstateModelsAndRequiredLoot() {
        ModelResourceValidator models = new ModelResourceValidator(RESOURCES);
        Set<String> missingBlockstates = new TreeSet<>();
        Set<String> missingLoot = new TreeSet<>();
        for (String id : REGISTRIES.blocks()) {
            String blockstate = "assets/mydrugs/blockstates/" + id + ".json";
            if (!RESOURCES.exists(blockstate)) {
                missingBlockstates.add(blockstate);
            } else {
                models.validateBlockstate(blockstate);
            }

            boolean fluid = REGISTRIES.fluids().contains(id);
            if (!fluid && !ResourceExceptions.BLOCKS_WITHOUT_LOOT.containsKey(id)) {
                String loot = "data/mydrugs/loot_table/blocks/" + id + ".json";
                if (!RESOURCES.exists(loot)) {
                    missingLoot.add(loot);
                }
            }
        }
        assertTrue(missingBlockstates.isEmpty(),
                ResourceIndex.formatMissing("Registered blocks without blockstates (run datagen)", missingBlockstates));
        assertTrue(models.missingModels().isEmpty(),
                ResourceIndex.formatMissing("Blockstates with missing models", models.missingModels()));
        assertTrue(missingLoot.isEmpty(),
                ResourceIndex.formatMissing("Blocks without loot tables or explicit no-loot reasons", missingLoot));
    }

    private static void assertKnownTextureDebt(Map<String, Set<String>> missingTextures) {
        Path baseline = Path.of("src/test/resources/mydrugs/known_missing_textures.txt");
        assertTrue(Files.isRegularFile(baseline),
                "Missing known texture debt baseline. Run updateResourceAuditManifest after reviewing the findings.");
        Set<String> expected = new TreeSet<>();
        for (String line : SourceIndex.read(baseline).lines().toList()) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\\s*\\|\\s*", 2);
            assertEquals(2, parts.length, "Every texture exception needs a reason: " + line);
            assertTrue(!parts[1].isBlank(), "Texture exception reason cannot be blank: " + line);
            expected.add(parts[0]);
        }
        Set<String> actual = new TreeSet<>();
        missingTextures.forEach((path, owners) ->
                owners.forEach(owner -> actual.add(path + " <- " + owner)));
        Set<String> added = new TreeSet<>(actual);
        added.removeAll(expected);
        Set<String> stale = new TreeSet<>(expected);
        stale.removeAll(actual);
        assertTrue(added.isEmpty() && stale.isEmpty(),
                ResourceIndex.formatMissing("New missing textures", added)
                        + "\n" + ResourceIndex.formatMissing("Resolved textures to remove from baseline", stale));
    }
}
