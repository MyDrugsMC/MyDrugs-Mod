package org.mydrugs.mydrugs.datagen;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModSimpleClientItemProvider implements DataProvider {
    private final PackOutput.PathProvider itemClientPathProvider;
    private final PackOutput.PathProvider itemModelPathProvider;

    public ModSimpleClientItemProvider(PackOutput output) {
        this.itemClientPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        this.itemModelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(saveFlatItem(cachedOutput, "mixing_spatula", "mydrugs:item/mixing_spatula"));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> saveFlatItem(CachedOutput cachedOutput, String itemName, String texturePath) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, itemName);

        JsonObject clientItemRoot = new JsonObject();
        JsonObject clientItemModel = new JsonObject();
        clientItemModel.addProperty("type", "minecraft:model");
        clientItemModel.addProperty("model", MyDrugs.MODID + ":item/" + itemName);
        clientItemRoot.add("model", clientItemModel);

        Path clientItemPath = this.itemClientPathProvider.json(id);
        CompletableFuture<?> clientItemFuture = DataProvider.saveStable(cachedOutput, clientItemRoot, clientItemPath);

        JsonObject itemModelRoot = new JsonObject();
        JsonObject textures = new JsonObject();
        itemModelRoot.addProperty("parent", "minecraft:item/generated");
        textures.addProperty("layer0", texturePath);
        itemModelRoot.add("textures", textures);

        Path itemModelPath = this.itemModelPathProvider.json(id);
        CompletableFuture<?> itemModelFuture = DataProvider.saveStable(cachedOutput, itemModelRoot, itemModelPath);

        return CompletableFuture.allOf(clientItemFuture, itemModelFuture);
    }

    @Override
    public String getName() {
        return "MyDrugs Simple Client Items";
    }
}