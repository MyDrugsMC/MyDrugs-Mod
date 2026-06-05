package org.mydrugs.mydrugs.audit;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bootstrap-free recipe registry audit: every declared recipe type must have a serializer with the
 * same id (and vice versa), the distiller display must be registered, and any recipe type without a
 * display must be explicitly listed in {@link org.mydrugs.mydrugs.recipes.ModRecipeContent}.
 */
class RecipeRegistryConsistencyTest {
    private static final Path TYPES = Path.of("src/main/java/org/mydrugs/mydrugs/recipes/ModRecipeTypes.java");
    private static final Path SERIALIZERS = Path.of("src/main/java/org/mydrugs/mydrugs/recipes/ModRecipeSerializers.java");
    private static final Path DISPLAYS = Path.of("src/main/java/org/mydrugs/mydrugs/recipes/ModRecipeDisplays.java");
    private static final Path CONTENT = Path.of("src/main/java/org/mydrugs/mydrugs/recipes/ModRecipeContent.java");

    private static final Pattern REGISTER = Pattern.compile("register\\(\\s*\"([a-z0-9_]+)\"");

    private static Set<String> ids(Path path) {
        Set<String> ids = new LinkedHashSet<>();
        Matcher matcher = REGISTER.matcher(SourceIndex.read(path));
        while (matcher.find()) {
            ids.add(matcher.group(1));
        }
        return ids;
    }

    @Test
    void everyRecipeTypeHasAMatchingSerializer() {
        Set<String> typeIds = ids(TYPES);
        Set<String> serializerIds = ids(SERIALIZERS);

        assertTrue(typeIds.size() >= 20, "Expected the full set of recipe types, found " + typeIds);
        assertEquals(typeIds, serializerIds,
                "Recipe type ids and serializer ids must match exactly (no exemptions configured).");
    }

    @Test
    void distillerDisplayIsRegistered() {
        String displays = SourceIndex.read(DISPLAYS);
        assertTrue(displays.contains("register(\"distiller\""),
                "The distiller recipe display must be registered in ModRecipeDisplays.");
    }

    @Test
    void recipeTypesWithoutDisplaysAreTrackedExplicitly() {
        Set<String> typeIds = ids(TYPES);
        String content = SourceIndex.read(CONTENT);

        // Every recipe type must appear in the intentional ModRecipeContent table, so that a missing
        // display is a deliberate (tracked) decision rather than an accidental omission.
        for (String id : typeIds) {
            assertTrue(SourceIndex.containsLiteral(content, id),
                    "Recipe type '" + id + "' is not described in ModRecipeContent; add it so its display "
                            + "status (present or intentionally absent) is explicit.");
        }
    }
}
