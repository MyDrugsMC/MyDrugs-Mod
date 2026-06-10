package org.mydrugs.mydrugs.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class ImplementationPlanResourceTest {

    @Test
    void distilleryRecipesCoverIndustrialAndIntegrationOutputs() throws IOException {
        String provider = read("src/main/java/org/mydrugs/mydrugs/datagen/ModPsychotropeDistilleryRecipeProvider.java");

        for (String output : List.of(
                "mydrugs:lucid_extract",
                "mydrugs:calming_resin",
                "mydrugs:redline_fuel",
                "mydrugs:overdrive_fuel",
                "mydrugs:dream_residue",
                "mydrugs:mycelial_insight",
                "mydrugs:unstable_essence"
        )) {
            assertTrue(provider.contains(output), "Distillery provider must generate " + output + " recipes");
        }
        assertTrue(provider.contains("residue_every"), "Distillery byproducts must be deterministic, not random");
    }

    @Test
    void distilleryIsRegisteredInJeiWithRecipesAndCatalyst() throws IOException {
        String jei = read("src/main/java/org/mydrugs/mydrugs/client/compat/JEIModPlugin.java");
        String descriptors = read("src/main/java/org/mydrugs/mydrugs/client/compat/JeiCategoryDescriptors.java");
        String cache = read("src/main/java/org/mydrugs/mydrugs/client/compat/ClientRecipesCache.java");
        String category = read("src/main/java/org/mydrugs/mydrugs/client/compat/PsychotropeDistilleryRecipeCategory.java");

        assertTrue(jei.contains("JeiCategoryDescriptors.ALL"));
        assertTrue(descriptors.contains("\"psychotrope_distillery\""));
        assertTrue(descriptors.contains("PsychotropeDistilleryRecipeCategory::new"));
        assertTrue(descriptors.contains("\"getPsychotropeDistilleryRecipes\""));
        assertTrue(descriptors.contains("\"PSYCHOTROPE_DISTILLERY_ITEM\""));
        assertTrue(cache.contains("ModRecipeTypes.PSYCHOTROPE_DISTILLERY"));
        assertTrue(category.contains("MachineGuiRenderer.drawPsychotropeDistillery"));
    }

    @Test
    void guideUsesRenamedResonatorAndNotDeletedCoreBlocks() throws IOException {
        String guideSource = read("docs/progression_guide_pages.md");
        String guideJson = read("src/main/resources/assets/mydrugs/guide/pages.json");

        for (String guide : List.of(guideSource, guideJson)) {
            assertTrue(guide.contains("mydrugs:psychotrope_resonator"));
            assertTrue(guide.contains("mydrugs:dream_residue"));
            assertTrue(guide.contains("mydrugs:integration_core"));
            assertFalse(guide.contains("mydrugs:psychotrope_core"));
            assertFalse(guide.contains("mydrugs:psychotrope_component"));
        }
    }

    @Test
    void oldPsychotropeEnergyImplementationClassesAreGone() {
        for (String oldClass : List.of(
                "src/main/java/org/mydrugs/mydrugs/energy/PsychotropeEnergyStorage.java",
                "src/main/java/org/mydrugs/mydrugs/energy/PsychotropeEnergyMachines.java",
                "src/main/java/org/mydrugs/mydrugs/energy/PsychotropeEnergyConstants.java",
                "src/main/java/org/mydrugs/mydrugs/blocks/entity/PsychotropeCoreBlockEntity.java"
        )) {
            assertFalse(Files.exists(Path.of(oldClass)), oldClass + " should not remain after the rename");
        }
    }

    @Test
    void registeredRecipeTypesHaveJeiOrGuideCoverageForNewSystems() throws IOException {
        String recipeTypes = read("src/main/java/org/mydrugs/mydrugs/recipes/ModRecipeTypes.java");
        String jei = read("src/main/java/org/mydrugs/mydrugs/client/compat/JeiCategoryDescriptors.java");
        String guide = read("src/main/resources/assets/mydrugs/guide/pages.json");

        assertTrue(recipeTypes.contains("PSYCHOTROPE_DISTILLERY"));
        assertTrue(jei.contains("PsychotropeDistilleryRecipeCategory"));
        assertTrue(guide.contains("Psychotrope Distillery") || guide.contains("psychotrope_distillery"));
        assertTrue(guide.contains("Distillate Engine") || guide.contains("distillate_engine"));
        assertTrue(guide.contains("Psychotrope Resonator") || guide.contains("psychotrope_resonator"));
    }

    private static String read(String path) throws IOException {
        return Files.readString(Path.of(path));
    }
}
