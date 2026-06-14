package org.mydrugs.mydrugs.audit;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeResourceValidationTest {
    private static final ResourceIndex RESOURCES = ResourceIndex.load();
    private static final RegistrySourceIndex REGISTRIES = RegistrySourceIndex.load();

    @Test
    void everyRegisteredCustomRecipeTypeHasAnExplicitSchema() {
        assertEquals(REGISTRIES.recipeTypes(), RecipeSchemaValidator.coveredCustomTypes(),
                "Custom recipe schema dispatch must track the recipe registry exactly");
    }

    @Test
    void everyRecipeParsesAndSatisfiesItsTypedSchema() {
        RecipeSchemaValidator validator = new RecipeSchemaValidator(RESOURCES, REGISTRIES);
        List<String> errors = new ArrayList<>();
        List<ResourceIndex.Entry> recipes = RESOURCES.logicalPaths(path ->
                        path.matches("data/[^/]+/recipe/.+\\.json"))
                .stream()
                .map(RESOURCES::require)
                .toList();
        assertTrue(!recipes.isEmpty(), "No recipe resources found; run datagen before validation");
        recipes.forEach(entry -> errors.addAll(validator.validate(entry)));
        assertTrue(errors.isEmpty(), "Recipe contract failures:\n - " + String.join("\n - ", errors));
    }
}
