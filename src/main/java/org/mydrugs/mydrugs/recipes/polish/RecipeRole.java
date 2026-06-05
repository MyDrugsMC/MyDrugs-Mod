package org.mydrugs.mydrugs.recipes.polish;

public enum RecipeRole {
    MAIN_PATH,
    SUPPORT_REAGENT,
    OPTIONAL,
    BYPASS,
    BYPRODUCT,
    AUTOMATION;

    public String translationKey() {
        return "recipe_polish.mydrugs.role." + name().toLowerCase();
    }
}
