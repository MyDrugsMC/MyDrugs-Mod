package org.mydrugs.mydrugs.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.items.ModItems;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModVanillaRecipeJsonProvider implements DataProvider {
    private final PackOutput.PathProvider recipePathProvider;

    public ModVanillaRecipeJsonProvider(PackOutput output) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        saveCooking(futures, cachedOutput, "platinum_ingot_from_raw_platinum_smelting", "minecraft:smelting",
                "mydrugs:raw_platinum", "mydrugs:platinum_ingot", 0.7F, 200);
        saveCooking(futures, cachedOutput, "platinum_ingot_from_raw_platinum_blasting", "minecraft:blasting",
                "mydrugs:raw_platinum", "mydrugs:platinum_ingot", 0.7F, 100);
        saveCookingPair(futures, cachedOutput, "platinum_ingot_from_platinum_ore",
                "mydrugs:platinum_ore", "mydrugs:platinum_ingot");
        saveCookingPair(futures, cachedOutput, "platinum_ingot_from_deepslate_platinum_ore",
                "mydrugs:deepslate_platinum_ore", "mydrugs:platinum_ingot");

        saveCookingPair(futures, cachedOutput, "aluminium_ingot_from_raw_aluminium",
                "mydrugs:raw_aluminium", "mydrugs:aluminium_ingot");
        saveCookingPair(futures, cachedOutput, "aluminium_ingot_from_aluminium_ore",
                "mydrugs:aluminium_ore", "mydrugs:aluminium_ingot");
        saveCookingPair(futures, cachedOutput, "aluminium_ingot_from_deepslate_aluminium_ore",
                "mydrugs:deepslate_aluminium_ore", "mydrugs:aluminium_ingot");

        saveStorageBlock(futures, cachedOutput, "raw_platinum_block", "mydrugs:raw_platinum", "mydrugs:raw_platinum_block");
        saveUnpackBlock(futures, cachedOutput, "raw_platinum_from_block", "mydrugs:raw_platinum_block", "mydrugs:raw_platinum");
        saveStorageBlock(futures, cachedOutput, "platinum_block", "mydrugs:platinum_ingot", "mydrugs:platinum_block");
        saveUnpackBlock(futures, cachedOutput, "platinum_ingot_from_block", "mydrugs:platinum_block", "mydrugs:platinum_ingot");
        saveStorageBlock(futures, cachedOutput, "raw_aluminium_block", "mydrugs:raw_aluminium", "mydrugs:raw_aluminium_block");
        saveUnpackBlock(futures, cachedOutput, "raw_aluminium_from_block", "mydrugs:raw_aluminium_block", "mydrugs:raw_aluminium");
        saveStorageBlock(futures, cachedOutput, "aluminium_block", "mydrugs:aluminium_ingot", "mydrugs:aluminium_block");
        saveUnpackBlock(futures, cachedOutput, "aluminium_ingot_from_block", "mydrugs:aluminium_block", "mydrugs:aluminium_ingot");

        saveShaped(futures, cachedOutput, "energy_upgrade",
                List.of("RGR", "PCP", "RGR"),
                Map.of(
                        "R", "minecraft:redstone",
                        "G", "minecraft:gold_ingot",
                        "P", "mydrugs:platinum_ingot",
                        "C", "mydrugs:reaction_core"
                ),
                "mydrugs:energy_upgrade", 1);
        saveShaped(futures, cachedOutput, "automation_upgrade",
                List.of(" A ", "PEP", " R "),
                Map.of(
                        "A", "mydrugs:agitator",
                        "P", "mydrugs:aluminium_ingot",
                        "E", "mydrugs:energy_upgrade",
                        "R", "minecraft:redstone"
                ),
                "mydrugs:automation_upgrade", 1);
        saveShaped(futures, cachedOutput, "fluid_pump",
                List.of(" V ", "PHP", " C "),
                Map.of(
                        "V", "mydrugs:valve",
                        "P", "mydrugs:copper_tube",
                        "H", "mydrugs:pump_head",
                        "C", "mydrugs:pressure_casing"
                ),
                "mydrugs:fluid_pump", 1);
        saveShaped(futures, cachedOutput, "psy_blueprint",
                List.of("V", "P", "V"),
                Map.of(
                        "V", itemId(Items.VINE),
                        "P", itemId(Items.PAPER)
                ),
                itemId(ModItems.PSY_BLUEPRINT.get()), 1);

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void saveCookingPair(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, String ingredient, String result) {
        saveCooking(futures, cachedOutput, name + "_smelting", "minecraft:smelting", ingredient, result, 0.7F, 200);
        saveCooking(futures, cachedOutput, name + "_blasting", "minecraft:blasting", ingredient, result, 0.7F, 100);
    }

    private void saveCooking(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, String type,
                             String ingredient, String result, float experience, int cookingTime) {
        JsonObject root = new JsonObject();
        root.addProperty("type", type);
        root.addProperty("category", "misc");
        root.addProperty("ingredient", ingredient);
        root.add("result", result(result, 1));
        root.addProperty("experience", experience);
        root.addProperty("cookingtime", cookingTime);
        saveRecipe(futures, cachedOutput, name, root);
    }

    private void saveStorageBlock(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, String ingredient, String result) {
        saveShaped(futures, cachedOutput, name, List.of("AAA", "AAA", "AAA"), Map.of("A", ingredient), result, 1);
    }

    private void saveUnpackBlock(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, String ingredient, String result) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(ingredient);
        root.add("ingredients", ingredients);
        root.add("result", result(result, 9));
        saveRecipe(futures, cachedOutput, name, root);
    }

    private void saveShaped(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, List<String> pattern,
                            Map<String, String> keys, String result, int count) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        JsonArray patternArray = new JsonArray();
        for (String line : pattern) {
            patternArray.add(line);
        }
        root.add("pattern", patternArray);

        JsonObject keyObject = new JsonObject();
        for (Map.Entry<String, String> entry : keys.entrySet()) {
            keyObject.addProperty(entry.getKey(), entry.getValue());
        }
        root.add("key", keyObject);
        root.add("result", result(result, count));
        saveRecipe(futures, cachedOutput, name, root);
    }

    private JsonObject result(String id, int count) {
        JsonObject result = new JsonObject();
        result.addProperty("id", id);
        if (count > 1) {
            result.addProperty("count", count);
        }
        return result;
    }

    private String itemId(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).toString();
    }

    private void saveRecipe(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, JsonObject root) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        Path path = this.recipePathProvider.json(id);
        futures.add(DataProvider.saveStable(cachedOutput, root, path));
    }

    @Override
    public String getName() {
        return "MyDrugs Vanilla Recipe JSONs";
    }
}
