package org.mydrugs.mydrugs.datagen;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModSimpleBlockAssetProvider implements DataProvider {
    private final PackOutput.PathProvider blockStatePathProvider;
    private final PackOutput.PathProvider blockModelPathProvider;
    private final PackOutput.PathProvider itemClientPathProvider;
    private final PackOutput.PathProvider itemModelPathProvider;

    public ModSimpleBlockAssetProvider(PackOutput output) {
        this.blockStatePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.blockModelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
        this.itemClientPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        this.itemModelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        saveCubeAllBlock(futures, cachedOutput, "platinum_ore");
        saveCubeAllBlock(futures, cachedOutput, "deepslate_platinum_ore");
        saveCubeAllBlock(futures, cachedOutput, "raw_platinum_block");
        saveCubeAllBlock(futures, cachedOutput, "platinum_block");
        saveCubeAllBlock(futures, cachedOutput, "aluminium_ore");
        saveCubeAllBlock(futures, cachedOutput, "deepslate_aluminium_ore");
        saveCubeAllBlock(futures, cachedOutput, "raw_aluminium_block");
        saveCubeAllBlock(futures, cachedOutput, "aluminium_block");
        saveCubeAllBlock(futures, cachedOutput, "sulfur_ore");
        saveCubeAllBlock(futures, cachedOutput, "deepslate_sulfur_ore");
        saveCubeAllBlock(futures, cachedOutput, "salt_block");
        saveCubeAllBlock(futures, cachedOutput, "treated_planks");
        saveCubeAllBlock(futures, cachedOutput, "gas_tank");

        saveHorizontalBlockState(futures, cachedOutput, "advanced_furnace");
        saveHorizontalBlockState(futures, cachedOutput, "advanced_mixing_vat");
        saveHorizontalBlockState(futures, cachedOutput, "catalytic_reformer");
        saveHorizontalBlockState(futures, cachedOutput, "centrifuge");
        saveHorizontalBlockState(futures, cachedOutput, "distiller");
        saveHorizontalBlockState(futures, cachedOutput, "drying_rack");
        saveHorizontalBlockState(futures, cachedOutput, "electrolyzer");
        saveHorizontalBlockState(futures, cachedOutput, "fluid_filterer");
        saveHorizontalBlockState(futures, cachedOutput, "gasifier");
        saveHorizontalBlockState(futures, cachedOutput, "gas_tank");
        saveHorizontalBlockState(futures, cachedOutput, "gas_pump");
        saveHorizontalBlockState(futures, cachedOutput, "growth_chamber");
        saveHorizontalBlockState(futures, cachedOutput, "sieve");
        saveHorizontalBlockState(futures, cachedOutput, "steam_cracker");
        saveActiveHorizontalBlockState(futures, cachedOutput, "chemical_reactor", "active");
        saveActiveHorizontalBlockState(futures, cachedOutput, "biochemical_reactor", "lit");

        saveOrientableBlock(futures, cachedOutput, "advanced_mixing_vat", "minecraft:block/orientable",
                textures("front", "advanced_mixing_vat_front", "side", "advanced_mixing_vat_side", "top", "advanced_mixing_vat_top", "bottom", "advanced_mixing_vat_bottom", "particle", "advanced_mixing_vat_top"));
        saveOrientableBlock(futures, cachedOutput, "catalytic_reformer", "minecraft:block/orientable_with_bottom",
                textures("top", "chemical_reactor_top", "front", "chemical_reactor_front", "side", "chemical_reactor_side", "bottom", "chemical_reactor_bottom"));
        saveOrientableBlock(futures, cachedOutput, "centrifuge", "minecraft:block/orientable_with_bottom",
                textures("top", "centrifuge_top", "front", "centrifuge_front", "side", "centrifuge_side", "bottom", "centrifuge_bottom"));
        saveOrientableBlock(futures, cachedOutput, "chemical_reactor", "minecraft:block/orientable",
                textures("top", "chemical_reactor_top", "front", "chemical_reactor_front", "side", "chemical_reactor_side", "bottom", "chemical_reactor_bottom"));
        saveOrientableBlock(futures, cachedOutput, "chemical_reactor_on", "minecraft:block/orientable",
                textures("top", "chemical_reactor_top", "front", "chemical_reactor_front_on", "side", "chemical_reactor_side", "bottom", "chemical_reactor_bottom"));
        saveOrientableBlock(futures, cachedOutput, "distiller", "minecraft:block/orientable_with_bottom",
                textures("top", "distiller_top", "front", "distiller_front", "side", "distiller_side", "bottom", "distiller_bottom"));
        saveOrientableBlock(futures, cachedOutput, "electrolyzer", "minecraft:block/orientable_with_bottom",
                textures("top", "electrolyzer_top", "front", "electrolyzer_front", "side", "electrolyzer_side", "bottom", "electrolyzer_bottom"));
        saveOrientableBlock(futures, cachedOutput, "fluid_filterer", "minecraft:block/orientable_with_bottom",
                textures("top", "fluid_filterer_top", "bottom", "fluid_filterer_bottom", "side", "fluid_filterer_side", "front", "fluid_filterer_front"));
        saveOrientableBlock(futures, cachedOutput, "steam_cracker", "minecraft:block/orientable_with_bottom",
                textures("top", "chemical_reactor_top", "front", "chemical_reactor_front", "side", "chemical_reactor_side", "bottom", "chemical_reactor_bottom"));
        saveAdvancedFurnaceModel(futures, cachedOutput);
        saveCubeMachineModel(futures, cachedOutput, "biochemical_reactor", "biochemical_reactor_front", "biochemical_reactor_side", "biochemical_reactor_top", "biochemical_reactor_bottom");
        saveCubeMachineModel(futures, cachedOutput, "biochemical_reactor_on", "biochemical_reactor_front_on", "biochemical_reactor_side", "biochemical_reactor_top", "biochemical_reactor_bottom");
        saveCubeMachineModel(futures, cachedOutput, "gas_pump", "gas_pump_front", "gas_pump_side", "gas_pump_top", "gas_pump_bottom");
        saveCubeMachineModel(futures, cachedOutput, "growth_chamber", "growth_chamber_front", "growth_chamber_side", "growth_chamber_top", "growth_chamber_bottom");

        saveBlockItemViaBlockModel(futures, cachedOutput, "advanced_furnace");
        saveBlockItemViaBlockModel(futures, cachedOutput, "advanced_mixing_vat");
        saveBlockItemViaBlockModel(futures, cachedOutput, "biochemical_reactor");
        saveBlockItemViaBlockModel(futures, cachedOutput, "catalytic_reformer");
        saveBlockItemViaBlockModel(futures, cachedOutput, "centrifuge");
        saveBlockItemViaBlockModel(futures, cachedOutput, "chemical_reactor");
        saveBlockItemViaBlockModel(futures, cachedOutput, "clay_vat");
        saveBlockItemViaBlockModel(futures, cachedOutput, "distiller");
        saveBlockItemViaBlockModel(futures, cachedOutput, "drying_rack");
        saveBlockItemViaBlockModel(futures, cachedOutput, "electrolyzer");
        saveBlockItemViaBlockModel(futures, cachedOutput, "evaporation_tray");
        saveBlockItemViaBlockModel(futures, cachedOutput, "fluid_filterer");
        saveBlockItemViaBlockModel(futures, cachedOutput, "fluid_pump");
        saveBlockItemViaBlockModel(futures, cachedOutput, "gas_pump");
        saveBlockItemViaBlockModel(futures, cachedOutput, "gas_tank");
        saveBlockItemViaBlockModel(futures, cachedOutput, "gasifier");
        saveBlockItemViaBlockModel(futures, cachedOutput, "grinding_bowl");
        saveBlockItemViaBlockModel(futures, cachedOutput, "growth_chamber");
        saveBlockItemViaBlockModel(futures, cachedOutput, "magic_mushroom");
        saveBlockItemViaBlockModel(futures, cachedOutput, "mixing_vat");
        saveBlockItemViaBlockModel(futures, cachedOutput, "psychedelic_grass");
        saveBlockItemViaBlockModel(futures, cachedOutput, "psychedelic_mycelium");
        saveBlockItemViaBlockModel(futures, cachedOutput, "salt_block");
        saveBlockItemViaBlockModel(futures, cachedOutput, "sieve");
        saveBlockItemViaBlockModel(futures, cachedOutput, "stomp_crafter");
        saveBlockItemViaItemModel(futures, cachedOutput, "stomp_plate");
        saveBlockItemViaBlockModel(futures, cachedOutput, "sulfur_ore");
        saveBlockItemViaItemModel(futures, cachedOutput, "treated_planks");

        saveCrop(futures, cachedOutput, "cannabis_crop", "cannabis_crop_stage");
        saveCrossCrop(futures, cachedOutput, "coca_crop", "coca_stage");
        saveCrop(futures, cachedOutput, "malt_crop", "malt_stage");
        saveCrop(futures, cachedOutput, "rye_crop", "rye_stage");
        saveCrop(futures, cachedOutput, "tobacco_crop", "tobacco_stage");

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void saveCubeAllBlock(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        String blockModel = MyDrugs.MODID + ":block/" + name;

        JsonObject blockStateRoot = new JsonObject();
        JsonObject variants = new JsonObject();
        JsonObject defaultVariant = new JsonObject();
        defaultVariant.addProperty("model", blockModel);
        variants.add("", defaultVariant);
        blockStateRoot.add("variants", variants);
        Path blockStatePath = this.blockStatePathProvider.json(id);
        futures.add(DataProvider.saveStable(cachedOutput, blockStateRoot, blockStatePath));

        JsonObject modelRoot = new JsonObject();
        JsonObject textures = new JsonObject();
        modelRoot.addProperty("parent", "minecraft:block/cube_all");
        textures.addProperty("all", blockModel);
        textures.addProperty("particle", blockModel);
        modelRoot.add("textures", textures);
        Path modelPath = this.blockModelPathProvider.json(id);
        futures.add(DataProvider.saveStable(cachedOutput, modelRoot, modelPath));

        JsonObject clientItemRoot = new JsonObject();
        JsonObject clientItemModel = new JsonObject();
        clientItemModel.addProperty("type", "minecraft:model");
        clientItemModel.addProperty("model", blockModel);
        clientItemRoot.add("model", clientItemModel);
        Path clientItemPath = this.itemClientPathProvider.json(id);
        futures.add(DataProvider.saveStable(cachedOutput, clientItemRoot, clientItemPath));
    }

    private void saveHorizontalBlockState(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        variants.add("facing=north", modelVariant(name, 0));
        variants.add("facing=south", modelVariant(name, 180));
        variants.add("facing=west", modelVariant(name, 270));
        variants.add("facing=east", modelVariant(name, 90));
        root.add("variants", variants);
        futures.add(DataProvider.saveStable(cachedOutput, root, this.blockStatePathProvider.json(id)));
    }

    private void saveActiveHorizontalBlockState(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, String property) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        variants.add("facing=north," + property + "=false", modelVariant(name, 0));
        variants.add("facing=south," + property + "=false", modelVariant(name, 180));
        variants.add("facing=west," + property + "=false", modelVariant(name, 270));
        variants.add("facing=east," + property + "=false", modelVariant(name, 90));
        variants.add("facing=north," + property + "=true", modelVariant(name + "_on", 0));
        variants.add("facing=south," + property + "=true", modelVariant(name + "_on", 180));
        variants.add("facing=west," + property + "=true", modelVariant(name + "_on", 270));
        variants.add("facing=east," + property + "=true", modelVariant(name + "_on", 90));
        root.add("variants", variants);
        futures.add(DataProvider.saveStable(cachedOutput, root, this.blockStatePathProvider.json(id)));
    }

    private JsonObject modelVariant(String name, int y) {
        JsonObject variant = new JsonObject();
        variant.addProperty("model", MyDrugs.MODID + ":block/" + name);
        variant.addProperty("y", y);
        return variant;
    }

    private void saveOrientableBlock(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, String parent, Map<String, String> textureNames) {
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();
        root.addProperty("parent", parent);
        textureNames.forEach((key, texture) -> textures.addProperty(key, MyDrugs.MODID + ":block/" + texture));
        root.add("textures", textures);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        futures.add(DataProvider.saveStable(cachedOutput, root, this.blockModelPathProvider.json(id)));
    }

    private void saveAdvancedFurnaceModel(List<CompletableFuture<?>> futures, CachedOutput cachedOutput) {
        saveOrientableBlock(futures, cachedOutput, "advanced_furnace", "minecraft:block/cube_bottom_top",
                textures("top", "advanced_furnace_top", "bottom", "advanced_furnace_bottom", "side", "advanced_furnace_side"));
    }

    private void saveCubeMachineModel(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, String front, String side, String top, String bottom) {
        saveOrientableBlock(futures, cachedOutput, name, "minecraft:block/cube",
                textures("down", bottom, "up", top, "north", front, "south", side, "west", side, "east", side, "particle", side));
    }

    private void saveBlockTextureModel(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name) {
        saveBlockTextureModel(futures, cachedOutput, name, textures("particle", name, "all", name));
    }

    private void saveBlockTextureModel(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, Map<String, String> textureNames) {
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();
        root.addProperty("parent", "minecraft:block/block");
        textureNames.forEach((key, texture) -> textures.addProperty(key, MyDrugs.MODID + ":block/" + texture));
        root.add("textures", textures);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        futures.add(DataProvider.saveStable(cachedOutput, root, this.blockModelPathProvider.json(id)));
    }

    private void saveBlockItemViaBlockModel(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        saveClientItemModel(futures, cachedOutput, id, MyDrugs.MODID + ":item/" + name);

        JsonObject itemModelRoot = new JsonObject();
        itemModelRoot.addProperty("parent", MyDrugs.MODID + ":block/" + name);
        if (!"advanced_mixing_vat".equals(name)) {
            futures.add(DataProvider.saveStable(cachedOutput, itemModelRoot, this.itemModelPathProvider.json(id)));
        }
    }

    private void saveBlockItemViaItemModel(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        saveClientItemModel(futures, cachedOutput, id, MyDrugs.MODID + ":item/" + name);

        JsonObject itemModelRoot = new JsonObject();
        itemModelRoot.addProperty("parent", MyDrugs.MODID + ":block/" + name);
        futures.add(DataProvider.saveStable(cachedOutput, itemModelRoot, this.itemModelPathProvider.json(id)));
    }

    private void saveClientItemModel(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, ResourceLocation id, String model) {
        JsonObject clientItemRoot = new JsonObject();
        JsonObject clientItemModel = new JsonObject();
        clientItemModel.addProperty("type", "minecraft:model");
        clientItemModel.addProperty("model", model);
        clientItemRoot.add("model", clientItemModel);
        futures.add(DataProvider.saveStable(cachedOutput, clientItemRoot, this.itemClientPathProvider.json(id)));
    }

    private void saveCrop(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String blockStateName, String modelPrefix) {
        ResourceLocation blockStateId = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, blockStateName);
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        for (int age = 0; age <= 7; age++) {
            variants.add("age=" + age, modelVariant(modelPrefix + age, 0));
            saveCropModel(futures, cachedOutput, modelPrefix + age);
        }
        root.add("variants", variants);
        futures.add(DataProvider.saveStable(cachedOutput, root, this.blockStatePathProvider.json(blockStateId)));
    }

    private void saveCropModel(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();
        root.addProperty("parent", "minecraft:block/crop");
        root.addProperty("render_type", "minecraft:cutout");
        textures.addProperty("crop", MyDrugs.MODID + ":block/" + name);
        root.add("textures", textures);
        futures.add(DataProvider.saveStable(cachedOutput, root, this.blockModelPathProvider.json(id)));
    }

    private void saveCrossCrop(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String blockStateName, String modelPrefix) {
        ResourceLocation blockStateId = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, blockStateName);
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        for (int age = 0; age <= 7; age++) {
            variants.add("age=" + age, modelVariant(modelPrefix + age, 0));
            saveCrossCropModel(futures, cachedOutput, modelPrefix + age);
        }
        root.add("variants", variants);
        futures.add(DataProvider.saveStable(cachedOutput, root, this.blockStatePathProvider.json(blockStateId)));
    }

    private void saveCrossCropModel(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();
        root.addProperty("parent", "minecraft:block/cross");
        root.addProperty("render_type", "minecraft:cutout");
        textures.addProperty("cross", MyDrugs.MODID + ":block/" + name);
        textures.addProperty("particle", MyDrugs.MODID + ":block/" + name);
        root.add("textures", textures);
        futures.add(DataProvider.saveStable(cachedOutput, root, this.blockModelPathProvider.json(id)));
    }

    private Map<String, String> textures(String... keysAndNames) {
        Map<String, String> textures = new LinkedHashMap<>();
        for (int i = 0; i < keysAndNames.length; i += 2) {
            textures.put(keysAndNames[i], keysAndNames[i + 1]);
        }
        return textures;
    }

    @Override
    public String getName() {
        return "MyDrugs Simple Block Assets";
    }
}
