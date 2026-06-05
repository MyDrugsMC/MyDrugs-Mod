package org.mydrugs.mydrugs.recipes.polish;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record RecipePolishMetadata(
        ResourceLocation recipe,
        RecipeRole role,
        MachineLane lane,
        String chapter,
        String guidePage,
        List<String> usedNext,
        String hintKey,
        int complexity
) {
}
