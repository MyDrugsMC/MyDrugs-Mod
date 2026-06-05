package org.mydrugs.mydrugs.audit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Protects public registry IDs. Every ID tracked in {@code registry_snapshot.json} must still be
 * present as a registration literal in the sources. New IDs are allowed (the test only checks for
 * disappearance); intentional removals require editing the snapshot.
 */
class RegistrySnapshotTest {
    private static final Path SNAPSHOT = Path.of("src/test/resources/mydrugs/registry_snapshot.json");

    @Test
    void everyTrackedIdStillExists() {
        String sources = SourceIndex.allSources();
        JsonObject snapshot = JsonParser.parseString(SourceIndex.read(SNAPSHOT)).getAsJsonObject();

        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, JsonElement> category : snapshot.entrySet()) {
            if (category.getKey().startsWith("_")) {
                continue;
            }
            JsonArray ids = category.getValue().getAsJsonArray();
            for (JsonElement id : ids) {
                String value = id.getAsString();
                if (!SourceIndex.containsLiteral(sources, value)) {
                    missing.add(category.getKey() + ":" + value);
                }
            }
        }

        assertTrue(missing.isEmpty(),
                "Tracked public registry IDs disappeared from the sources: " + missing
                        + ". If this is intentional, update registry_snapshot.json.");
    }

    @Test
    void legacyDryerBlockTypeIdIsGone() {
        String blockTypes = SourceIndex.read(Path.of("src/main/java/org/mydrugs/mydrugs/blocks/ModBlockTypes.java"));
        assertFalse(blockTypes.contains("\"dryer\""),
                "Legacy block-type id mydrugs:dryer must not be registered; it was renamed to mydrugs:sieve.");
        assertTrue(blockTypes.contains("\"sieve\""), "Block-type id mydrugs:sieve should be registered.");
    }
}
