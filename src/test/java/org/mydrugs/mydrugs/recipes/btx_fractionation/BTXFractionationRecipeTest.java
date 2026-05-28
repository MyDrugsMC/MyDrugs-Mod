package org.mydrugs.mydrugs.recipes.btx_fractionation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins down what {@code data/mydrugs/recipe/btx_fractionation/btx_mix.json} contains and what
 * shape the BTX fractionation recipe schema accepts. These tests anchor the contract that
 * "1000 mB btx_mix becomes 350/300/350 benzene/toluene/xylene over 300 ticks" — the exact
 * numbers that used to live in {@code BTXFractionationTowerBlockEntity} as Java constants and
 * are now data-driven.
 *
 * <p>The full {@link BTXFractionationRecipe} class can't be loaded in a vanilla JUnit context
 * because {@code Recipe&lt;T&gt;}'s static init touches Minecraft state. Validating the JSON
 * directly is enough to detect any silent ratio change.
 */
class BTXFractionationRecipeTest {

    private static final Path SHIPPED_JSON_PATH = Paths.get(
            "src/main/resources/data/mydrugs/recipe/btx_fractionation/btx_mix.json");

    @Test
    void shippedJsonExistsAtCanonicalPath() {
        assertTrue(Files.exists(SHIPPED_JSON_PATH),
                "BTX fractionation recipe JSON should live at " + SHIPPED_JSON_PATH);
    }

    @Test
    void shippedJsonHasTheExactHardcodedNumbers() throws IOException {
        JsonObject recipe = readJsonObject(SHIPPED_JSON_PATH);

        assertEquals("mydrugs:btx_fractionation", recipe.get("type").getAsString(),
                "recipe must declare the btx_fractionation type so the serializer claims it");

        JsonObject input = recipe.getAsJsonObject("input");
        assertEquals("mydrugs:btx_mix", input.get("fluid").getAsString());
        assertEquals(1000, input.get("amount").getAsInt(),
                "input amount must match the legacy INPUT_PER_BATCH constant");

        JsonArray outputs = recipe.getAsJsonArray("outputs");
        assertEquals(3, outputs.size(), "three fractions: benzene, toluene, xylene");

        JsonObject benzene = findOutput(outputs, "mydrugs:benzene");
        assertNotNull(benzene, "benzene output must be present");
        assertEquals(350, benzene.get("amount").getAsInt(),
                "benzene amount must match the legacy BENZENE_PER_BATCH constant");

        JsonObject toluene = findOutput(outputs, "mydrugs:toluene");
        assertNotNull(toluene, "toluene output must be present");
        assertEquals(300, toluene.get("amount").getAsInt(),
                "toluene amount must match the legacy TOLUENE_PER_BATCH constant");

        JsonObject xylene = findOutput(outputs, "mydrugs:xylene");
        assertNotNull(xylene, "xylene output must be present");
        assertEquals(350, xylene.get("amount").getAsInt(),
                "xylene amount must match the legacy XYLENE_PER_BATCH constant");

        assertEquals(300, recipe.get("process_time").getAsInt(),
                "process_time must match the legacy BASE_TICKS constant");
    }

    private static JsonObject readJsonObject(Path path) throws IOException {
        String text = Files.readString(path);
        JsonElement element = JsonParser.parseString(text);
        return element.getAsJsonObject();
    }

    private static JsonObject findOutput(JsonArray outputs, String fluidId) {
        for (JsonElement element : outputs) {
            JsonObject obj = element.getAsJsonObject();
            if (fluidId.equals(obj.get("fluid").getAsString())) {
                return obj;
            }
        }
        return null;
    }
}
