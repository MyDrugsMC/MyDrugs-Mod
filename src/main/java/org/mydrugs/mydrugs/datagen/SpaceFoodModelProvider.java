package org.mydrugs.mydrugs.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.items.ModItems;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SpaceFoodModelProvider implements DataProvider {
    private static final int BASE_TINT = 0xFFFFFF;
    private static final int OVERLAY_TINT = 0x66CC66;

    private final PackOutput.PathProvider itemClientPath;
    private final PackOutput.PathProvider itemModelPath;

    public SpaceFoodModelProvider(PackOutput output) {
        this.itemClientPath = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        this.itemModelPath = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
    }

    private static JsonObject makeModelJson(ResourceLocation baseFoodId) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", baseFoodId.getNamespace() + ":item/" + baseFoodId.getPath());
        textures.addProperty("layer1", MyDrugs.MODID + ":item/space_overlays/space_" + baseFoodId.getPath());

        root.add("textures", textures);
        return root;
    }

    private static JsonObject makeClientItemJson(String spacePath) {
        JsonObject root = new JsonObject();

        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", MyDrugs.MODID + ":item/" + spacePath);

        JsonArray tints = new JsonArray();
        tints.add(makeConstantTint(BASE_TINT));
        tints.add(makeConstantTint(OVERLAY_TINT));
        model.add("tints", tints);

        root.add("model", model);

        JsonObject properties = new JsonObject();
        properties.addProperty("oversized_in_gui", false);
        root.add("properties", properties);

        return root;
    }

    private static JsonObject makeConstantTint(int color) {
        JsonObject tint = new JsonObject();
        tint.addProperty("type", "minecraft:constant");
        tint.addProperty("value", color);
        return tint;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        ModItems.SPACE_FOODS_BY_BASE_ID.forEach((baseId, holder) -> {
            String spacePath = "space_" + baseId.getPath();
            ResourceLocation spaceId = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, spacePath);

            JsonObject modelJson = makeModelJson(baseId);
            JsonObject clientItemJson = makeClientItemJson(spacePath);

            Path modelPath = this.itemModelPath.json(spaceId);
            Path clientItemPath = this.itemClientPath.json(spaceId);

            futures.add(DataProvider.saveStable(cache, modelJson, modelPath));
            futures.add(DataProvider.saveStable(cache, clientItemJson, clientItemPath));
        });

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Space food item models and client items";
    }
}