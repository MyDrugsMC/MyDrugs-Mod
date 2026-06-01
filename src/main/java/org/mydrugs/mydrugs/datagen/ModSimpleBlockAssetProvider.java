package org.mydrugs.mydrugs.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.dimension.InnerBlockIds;

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
    private boolean emptyBlockModelSaved;

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
        saveCubeAllBlock(futures, cachedOutput, "phosphate_ore");
        saveCubeAllBlock(futures, cachedOutput, "deepslate_phosphate_ore");
        saveCubeAllBlock(futures, cachedOutput, "raw_aluminium_block");
        saveCubeAllBlock(futures, cachedOutput, "aluminium_block");
        saveCubeAllBlock(futures, cachedOutput, "sulfur_ore");
        saveCubeAllBlock(futures, cachedOutput, "deepslate_sulfur_ore");
        saveCubeAllBlock(futures, cachedOutput, "salt_block");
        saveCubeAllBlock(futures, cachedOutput, "treated_planks");
        saveGasTankBlock(futures, cachedOutput);
        saveCubeAllBlockWithTexture(futures, cachedOutput, "mechanical_frame", MyDrugs.MODID + ":block/mechanical_frame");
        saveCubeAllBlockWithTexture(futures, cachedOutput, "recovery_anchor", MyDrugs.MODID + ":block/recovery_anchor");
        saveCubeAllBlockWithTexture(futures, cachedOutput, "therapist_desk", MyDrugs.MODID + ":block/therapist_desk");
        saveCubeAllBlockWithTexture(futures, cachedOutput, "disc_scriber", MyDrugs.MODID + ":block/disc_scriber");
        saveCubeAllBlockWithTexture(futures, cachedOutput, "recovery_jukebox", MyDrugs.MODID + ":block/recovery_jukebox");
        // Inner-dimension nodes & symbolic plants now use their OWN textures (mydrugs:block/<id>)
        // instead of borrowing vanilla / other-plant textures. Missing PNGs are reported by
        // InnerBlockTextureReportTest so they can be drawn. Driven from the shared id lists so the
        // datagen and the test never drift.
        for (String node : InnerBlockIds.NODE_BLOCKS) {
            saveCubeAllBlock(futures, cachedOutput, node);
        }
        for (String plant : InnerBlockIds.SYMBOLIC_PLANTS) {
            saveCrossPlantBlock(futures, cachedOutput, plant, MyDrugs.MODID + ":block/" + plant);
        }

        saveHorizontalBlockState(futures, cachedOutput, "advanced_furnace");
        saveHorizontalBlockState(futures, cachedOutput, "sieve");
        saveHorizontalBlockState(futures, cachedOutput, "fluid_filterer");
        saveHorizontalBlockState(futures, cachedOutput, "stomp_crafter");
        saveHorizontalBlockState(futures, cachedOutput, "distiller");
        saveHorizontalBlockState(futures, cachedOutput, "advanced_mixing_vat");
        saveHorizontalBlockState(futures, cachedOutput, "aromatic_extractor");
        saveHorizontalBlockState(futures, cachedOutput, "autoclave");
        saveHorizontalBlockState(futures, cachedOutput, "bacterial_incubator");
        saveDoubleHalfHorizontalBlockState(futures, cachedOutput, "btx_fractionation_tower");
        saveHorizontalBlockState(futures, cachedOutput, "catalytic_reformer");
        saveHorizontalBlockState(futures, cachedOutput, "centrifuge");
        saveHorizontalBlockState(futures, cachedOutput, "psychotrope_distillery");
        saveHorizontalBlockState(futures, cachedOutput, "distillate_engine");
        saveHorizontalBlockState(futures, cachedOutput, "psychotrope_resonator");
        saveHorizontalBlockState(futures, cachedOutput, "drying_rack");
        saveHorizontalBlockState(futures, cachedOutput, "reduction_still");
        saveHorizontalBlockState(futures, cachedOutput, "electrolyzer");
        saveHorizontalBlockState(futures, cachedOutput, "gasifier");
        saveHorizontalBlockState(futures, cachedOutput, "gas_tank");
        saveHorizontalBlockState(futures, cachedOutput, "gas_pump");
        saveHorizontalBlockState(futures, cachedOutput, "gene_extractor");
        saveHorizontalBlockState(futures, cachedOutput, "hemogenic_infuser");
        saveHorizontalBlockState(futures, cachedOutput, "growth_chamber");
        saveHorizontalBlockState(futures, cachedOutput, "crispr_cas9_combinator");
        saveHorizontalBlockState(futures, cachedOutput, "sieve");
        saveHorizontalBlockState(futures, cachedOutput, "steam_cracker");
        saveActiveHorizontalBlockState(futures, cachedOutput, "chemical_reactor", "active");
        saveActiveHorizontalBlockState(futures, cachedOutput, "biochemical_reactor", "lit");

        saveOrientableBlock(futures, cachedOutput, "advanced_mixing_vat", "minecraft:block/orientable",
                textures("front", "advanced_mixing_vat_front", "side", "advanced_mixing_vat_side", "top", "advanced_mixing_vat_top", "bottom", "advanced_mixing_vat_bottom", "particle", "advanced_mixing_vat_top"));
        saveOrientableBlock(futures, cachedOutput, "catalytic_reformer", "minecraft:block/orientable_with_bottom",
                textures("top", "catalytic_reformer_top", "front", "catalytic_reformer_front", "side", "catalytic_reformer_side", "bottom", "catalytic_reformer_bottom"));
        saveOrientableBlock(futures, cachedOutput, "chemical_reactor", "minecraft:block/orientable",
                textures("top", "chemical_reactor_top", "front", "chemical_reactor_front", "side", "chemical_reactor_side", "bottom", "chemical_reactor_bottom"));
        saveOrientableBlock(futures, cachedOutput, "chemical_reactor_on", "minecraft:block/orientable",
                textures("top", "chemical_reactor_top", "front", "chemical_reactor_front_on", "side", "chemical_reactor_side", "bottom", "chemical_reactor_bottom"));
        saveOrientableBlock(futures, cachedOutput, "psychotrope_distillery", "minecraft:block/orientable_with_bottom",
                textures("top", "psychotrope_distillery_top", "front", "psychotrope_distillery_front", "side", "psychotrope_distillery_side", "bottom", "psychotrope_distillery_bottom"));
        saveOrientableBlock(futures, cachedOutput, "distillate_engine", "minecraft:block/orientable_with_bottom",
                textures("top", "distillate_engine_top", "front", "distillate_engine_front", "side", "distillate_engine_side", "bottom", "distillate_engine_bottom"));
        saveOrientableBlock(futures, cachedOutput, "psychotrope_resonator", "minecraft:block/cube_all",
                textures("all", "psychotrope_resonator", "particle", "psychotrope_resonator"));
        saveOrientableBlock(futures, cachedOutput, "electrolyzer", "minecraft:block/orientable_with_bottom",
                textures("top", "electrolyzer_top", "front", "electrolyzer_front", "side", "electrolyzer_side", "bottom", "electrolyzer_bottom"));
        saveOrientableBlock(futures, cachedOutput, "steam_cracker", "minecraft:block/orientable_with_bottom",
                textures("top", "steam_cracker_top", "front", "steam_cracker_front", "side", "steam_cracker_side", "bottom", "steam_cracker_bottom"));
        saveCubeMachineModel(futures, cachedOutput, "biochemical_reactor", "biochemical_reactor_front", "biochemical_reactor_side", "biochemical_reactor_top", "biochemical_reactor_bottom");
        saveCubeMachineModel(futures, cachedOutput, "biochemical_reactor_on", "biochemical_reactor_front_on", "biochemical_reactor_side", "biochemical_reactor_top", "biochemical_reactor_bottom");
        saveCubeMachineModel(futures, cachedOutput, "autoclave", "autoclave_front", "autoclave_side", "autoclave_top", "autoclave_bottom");
        saveCubeMachineModel(futures, cachedOutput, "bacterial_incubator", "bacterial_incubator_front", "bacterial_incubator_side", "bacterial_incubator_top", "bacterial_incubator_bottom");
        saveCubeMachineModel(futures, cachedOutput, "gene_extractor", "gene_extractor_front", "gene_extractor_side", "gene_extractor_top", "gene_extractor_bottom");
        saveCubeMachineModel(futures, cachedOutput, "hemogenic_infuser", "hemogenic_infuser_front", "hemogenic_infuser_side", "hemogenic_infuser_top", "hemogenic_infuser_bottom");
        saveCubeMachineModel(futures, cachedOutput, "crispr_cas9_combinator", "crispr_cas9_combinator_front", "crispr_cas9_combinator_side", "crispr_cas9_combinator_top", "crispr_cas9_combinator_bottom");
        saveCubeMachineModel(futures, cachedOutput, "reduction_still", "reduction_still_front", "reduction_still_side", "reduction_still_top", "reduction_still_bottom");
        saveSimpleBlockState(futures, cachedOutput, "psy_anvil");
        saveVomitSplash(futures, cachedOutput);

        saveBlockItemViaBlockModel(futures, cachedOutput, "advanced_mixing_vat");
        saveBlockItemViaBlockModel(futures, cachedOutput, "aromatic_extractor");
        saveBlockItemViaBlockModel(futures, cachedOutput, "autoclave");
        saveBlockItemViaBlockModel(futures, cachedOutput, "bacterial_incubator");
        saveBlockItemViaBlockModel(futures, cachedOutput, "biochemical_reactor");
        saveBlockItemViaBlockModel(futures, cachedOutput, "catalytic_reformer");
        saveBlockItemViaBlockModel(futures, cachedOutput, "centrifuge");
        saveBlockItemViaBlockModel(futures, cachedOutput, "chemical_reactor");
        saveBlockItemViaBlockModel(futures, cachedOutput, "clay_vat");
        saveBlockItemViaBlockModel(futures, cachedOutput, "psychotrope_distillery");
        saveBlockItemViaBlockModel(futures, cachedOutput, "distillate_engine");
        saveBlockItemViaBlockModel(futures, cachedOutput, "psychotrope_resonator");
        saveBlockItemViaBlockModel(futures, cachedOutput, "drying_rack");
        saveBlockItemViaBlockModel(futures, cachedOutput, "reduction_still");
        saveBlockItemViaBlockModel(futures, cachedOutput, "electrolyzer");
        saveBlockItemViaBlockModel(futures, cachedOutput, "evaporation_tray");
        saveBlockItemViaBlockModel(futures, cachedOutput, "fluid_pump");
        saveBlockItemViaBlockModel(futures, cachedOutput, "gas_pump");
        saveBlockItemViaBlockModel(futures, cachedOutput, "gas_tank");
        saveBlockItemViaBlockModel(futures, cachedOutput, "gasifier");
        saveBlockItemViaBlockModel(futures, cachedOutput, "gene_extractor");
        saveBlockItemViaBlockModel(futures, cachedOutput, "grinding_bowl");
        saveBlockItemViaBlockModel(futures, cachedOutput, "growth_chamber");
        saveBlockItemViaBlockModel(futures, cachedOutput, "hemogenic_infuser");
        saveBlockItemViaBlockModel(futures, cachedOutput, "crispr_cas9_combinator");
        saveBlockItemViaBlockModel(futures, cachedOutput, "magic_mushroom");
        saveBlockItemViaBlockModel(futures, cachedOutput, "mechanical_frame");
        saveBlockItemViaBlockModel(futures, cachedOutput, "mixing_vat");
        saveBlockItemViaBlockModel(futures, cachedOutput, "psychedelic_grass");
        saveBlockItemViaBlockModel(futures, cachedOutput, "psychedelic_mycelium");
        saveBlockItemViaBlockModel(futures, cachedOutput, "psy_anvil");
        saveBlockItemViaBlockModel(futures, cachedOutput, "recovery_anchor");
        saveBlockItemViaBlockModel(futures, cachedOutput, "disc_scriber");
        saveBlockItemViaBlockModel(futures, cachedOutput, "recovery_jukebox");
        saveBlockItemViaBlockModel(futures, cachedOutput, "salt_block");
        saveBlockItemViaBlockModel(futures, cachedOutput, "stomp_plate_block");
        saveBlockItemViaItemModel(futures, cachedOutput, "stomp_plate");
        saveBlockItemViaBlockModel(futures, cachedOutput, "sulfur_ore");
        saveBlockItemViaBlockModel(futures, cachedOutput, "therapist_desk");
        saveBlockItemViaItemModel(futures, cachedOutput, "treated_planks");

        saveTallCrop(futures, cachedOutput, "cannabis_crop", "cannabis_stage", 4);
        saveCrossCrop(futures, cachedOutput, "aloe_vera_crop", "aloe_vera_stage");
        saveAge3CrossBlock(futures, cachedOutput, "aloe_vera_bush", "aloe_vera_bush_stage");
        saveCrossCrop(futures, cachedOutput, "coca_crop", "coca_stage");
        saveTallCrop(futures, cachedOutput, "coffee_crop", "coffee_stage", 6);
        saveCrossCrop(futures, cachedOutput, "lavender_crop", "lavender_stage");
        saveCrossCrop(futures, cachedOutput, "valerian_crop", "valerian_stage");
        saveCrossCrop(futures, cachedOutput, "ephedra_crop", "ephedra_stage");
        saveTallCrop(futures, cachedOutput, "malt_crop", "malt_stage", 4);
        saveCrossCrop(futures, cachedOutput, "opium_poppy_crop", "opium_poppy_stage");
        saveTallCrop(futures, cachedOutput, "rye_crop", "rye_stage", 4);
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

    private void saveCubeAllBlockWithTexture(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, String texture) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        String blockModel = MyDrugs.MODID + ":block/" + name;

        JsonObject blockStateRoot = new JsonObject();
        JsonObject variants = new JsonObject();
        JsonObject defaultVariant = new JsonObject();
        defaultVariant.addProperty("model", blockModel);
        variants.add("", defaultVariant);
        blockStateRoot.add("variants", variants);
        futures.add(DataProvider.saveStable(cachedOutput, blockStateRoot, this.blockStatePathProvider.json(id)));

        JsonObject modelRoot = new JsonObject();
        JsonObject textures = new JsonObject();
        modelRoot.addProperty("parent", "minecraft:block/cube_all");
        textures.addProperty("all", texture);
        textures.addProperty("particle", texture);
        modelRoot.add("textures", textures);
        futures.add(DataProvider.saveStable(cachedOutput, modelRoot, this.blockModelPathProvider.json(id)));

        JsonObject itemModelRoot = new JsonObject();
        itemModelRoot.addProperty("parent", blockModel);
        futures.add(DataProvider.saveStable(cachedOutput, itemModelRoot, this.itemModelPathProvider.json(id)));

        saveClientItemModel(futures, cachedOutput, id, MyDrugs.MODID + ":item/" + name);
    }

    private void saveGasTankBlock(List<CompletableFuture<?>> futures, CachedOutput cachedOutput) {
        String name = "gas_tank";
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        String blockModel = MyDrugs.MODID + ":block/" + name;

        JsonObject modelRoot = new JsonObject();
        JsonObject textures = new JsonObject();
        modelRoot.addProperty("parent", "minecraft:block/cube_all");
        modelRoot.addProperty("render_type", "minecraft:translucent");
        textures.addProperty("all", blockModel);
        textures.addProperty("particle", blockModel);
        modelRoot.add("textures", textures);
        futures.add(DataProvider.saveStable(cachedOutput, modelRoot, this.blockModelPathProvider.json(id)));

        JsonObject clientItemRoot = new JsonObject();
        JsonObject clientItemModel = new JsonObject();
        clientItemModel.addProperty("type", "minecraft:model");
        clientItemModel.addProperty("model", blockModel);
        clientItemRoot.add("model", clientItemModel);
        futures.add(DataProvider.saveStable(cachedOutput, clientItemRoot, this.itemClientPathProvider.json(id)));
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

    /**
     * Blockstate for a horizontally-facing 2-block-tall block (DoubleBlockHalf). The single
     * block model (named {@code name}) carries the FULL 2-block-tall geometry (elements extending
     * up to Y=32). The lower half references that model; the upper half renders nothing via the
     * shared {@code mydrugs:block/empty} model, so the tall geometry is drawn once instead of
     * being stacked twice.
     */
    private void saveDoubleHalfHorizontalBlockState(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        for (int yRot : new int[] {0, 90, 180, 270}) {
            String facing = switch (yRot) {
                case 90 -> "east";
                case 180 -> "south";
                case 270 -> "west";
                default -> "north";
            };
            variants.add("facing=" + facing + ",half=lower", modelVariant(name, yRot));
            variants.add("facing=" + facing + ",half=upper", modelVariant("empty", yRot));
        }
        root.add("variants", variants);
        futures.add(DataProvider.saveStable(cachedOutput, root, this.blockStatePathProvider.json(id)));
    }

    private void saveSimpleBlockState(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        variants.add("", modelVariant(name, 0));
        root.add("variants", variants);
        futures.add(DataProvider.saveStable(cachedOutput, root, this.blockStatePathProvider.json(id)));
    }

    private void saveVomitSplash(List<CompletableFuture<?>> futures, CachedOutput cachedOutput) {
        String name = "vomit_splash";
        saveSimpleBlockState(futures, cachedOutput, name);

        JsonObject modelRoot = new JsonObject();
        JsonObject textures = new JsonObject();
        modelRoot.addProperty("parent", "minecraft:block/carpet");
        textures.addProperty("wool", MyDrugs.MODID + ":block/" + name);
        modelRoot.add("textures", textures);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        futures.add(DataProvider.saveStable(cachedOutput, modelRoot, this.blockModelPathProvider.json(id)));
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

    private void saveCrossPlantBlock(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, String texture) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        JsonObject blockStateRoot = new JsonObject();
        JsonObject variants = new JsonObject();
        variants.add("", modelVariant(name, 0));
        blockStateRoot.add("variants", variants);
        futures.add(DataProvider.saveStable(cachedOutput, blockStateRoot, this.blockStatePathProvider.json(id)));

        JsonObject blockModelRoot = new JsonObject();
        JsonObject blockTextures = new JsonObject();
        blockModelRoot.addProperty("parent", "minecraft:block/cross");
        blockModelRoot.addProperty("render_type", "minecraft:cutout");
        blockTextures.addProperty("cross", texture);
        blockTextures.addProperty("particle", texture);
        blockModelRoot.add("textures", blockTextures);
        futures.add(DataProvider.saveStable(cachedOutput, blockModelRoot, this.blockModelPathProvider.json(id)));

        JsonObject itemModelRoot = new JsonObject();
        JsonObject itemTextures = new JsonObject();
        itemModelRoot.addProperty("parent", "minecraft:item/generated");
        itemTextures.addProperty("layer0", texture);
        itemModelRoot.add("textures", itemTextures);
        futures.add(DataProvider.saveStable(cachedOutput, itemModelRoot, this.itemModelPathProvider.json(id)));
        saveClientItemModel(futures, cachedOutput, id, MyDrugs.MODID + ":item/" + name);
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

    private void saveTallCrop(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String blockStateName, String texturePrefix, int tallStartAge) {
        ResourceLocation blockStateId = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, blockStateName);
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();

        for (int age = 0; age <= 7; age++) {
            variants.add("age=" + age + ",half=lower", modelVariant(texturePrefix + age + "_lower", 0));
            saveTallCropModel(futures, cachedOutput, texturePrefix + age + "_lower", texturePrefix + age, false);

            String upperModel = age >= tallStartAge ? texturePrefix + age + "_upper" : "empty";
            variants.add("age=" + age + ",half=upper", modelVariant(upperModel, 0));
            if (age >= tallStartAge) {
                saveTallCropModel(futures, cachedOutput, upperModel, texturePrefix + age, true);
            }
        }

        saveEmptyBlockModel(futures, cachedOutput);
        root.add("variants", variants);
        futures.add(DataProvider.saveStable(cachedOutput, root, this.blockStatePathProvider.json(blockStateId)));
    }

    private void saveTallCropModel(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String modelName, String textureName, boolean upper) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, modelName);
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();
        root.addProperty("render_type", "minecraft:cutout");
        textures.addProperty("crop", MyDrugs.MODID + ":block/" + textureName);
        textures.addProperty("particle", MyDrugs.MODID + ":block/" + textureName);
        root.add("textures", textures);

        JsonArray elements = new JsonArray();
        int minV = upper ? 0 : 8;
        int maxV = upper ? 8 : 16;
        elements.add(tallCropPlane(
                vector(0, 0, 8),
                vector(16, 16, 8),
                "north",
                "south",
                minV,
                maxV
        ));
        elements.add(tallCropPlane(
                vector(8, 0, 0),
                vector(8, 16, 16),
                "west",
                "east",
                minV,
                maxV
        ));
        root.add("elements", elements);

        futures.add(DataProvider.saveStable(cachedOutput, root, this.blockModelPathProvider.json(id)));
    }

    private JsonObject tallCropPlane(JsonArray from, JsonArray to, String firstFace, String secondFace, int minV, int maxV) {
        JsonObject element = new JsonObject();
        JsonObject faces = new JsonObject();
        element.add("from", from);
        element.add("to", to);
        faces.add(firstFace, cropFace(minV, maxV));
        faces.add(secondFace, cropFace(minV, maxV));
        element.add("faces", faces);
        return element;
    }

    private JsonObject cropFace(int minV, int maxV) {
        JsonObject face = new JsonObject();
        face.add("uv", vector(0, minV, 16, maxV));
        face.addProperty("texture", "#crop");
        return face;
    }

    private JsonArray vector(int... values) {
        JsonArray vector = new JsonArray();
        for (int value : values) {
            vector.add(value);
        }
        return vector;
    }

    private void saveEmptyBlockModel(List<CompletableFuture<?>> futures, CachedOutput cachedOutput) {
        if (this.emptyBlockModelSaved) {
            return;
        }

        this.emptyBlockModelSaved = true;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "empty");
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/block");
        root.add("elements", new JsonArray());
        futures.add(DataProvider.saveStable(cachedOutput, root, this.blockModelPathProvider.json(id)));
    }

    private void saveCrossCrop(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String blockStateName, String modelPrefix) {
        saveCrossCrop(futures, cachedOutput, blockStateName, modelPrefix, null);
    }

    private void saveCrossCrop(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String blockStateName, String modelPrefix, String textureOverride) {
        ResourceLocation blockStateId = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, blockStateName);
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        for (int age = 0; age <= 7; age++) {
            variants.add("age=" + age, modelVariant(modelPrefix + age, 0));
            saveCrossCropModel(futures, cachedOutput, modelPrefix + age, textureOverride);
        }
        root.add("variants", variants);
        futures.add(DataProvider.saveStable(cachedOutput, root, this.blockStatePathProvider.json(blockStateId)));
    }

    private void saveAge3CrossBlock(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String blockStateName, String modelPrefix) {
        ResourceLocation blockStateId = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, blockStateName);
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        for (int age = 0; age <= 3; age++) {
            variants.add("age=" + age, modelVariant(modelPrefix + age, 0));
            saveCrossCropModel(futures, cachedOutput, modelPrefix + age);
        }
        root.add("variants", variants);
        futures.add(DataProvider.saveStable(cachedOutput, root, this.blockStatePathProvider.json(blockStateId)));
    }

    private void saveCrossCropModel(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name) {
        saveCrossCropModel(futures, cachedOutput, name, null);
    }

    private void saveCrossCropModel(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, String textureOverride) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();
        root.addProperty("parent", "minecraft:block/cross");
        root.addProperty("render_type", "minecraft:cutout");
        String texture = textureOverride == null ? MyDrugs.MODID + ":block/" + name : textureOverride;
        textures.addProperty("cross", texture);
        textures.addProperty("particle", texture);
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
