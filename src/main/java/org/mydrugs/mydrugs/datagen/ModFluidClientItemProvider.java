package org.mydrugs.mydrugs.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.fluids.FluidEntry;
import org.mydrugs.mydrugs.fluids.ModFluids;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModFluidClientItemProvider implements DataProvider {
    private final PackOutput.PathProvider itemPathProvider;

    public ModFluidClientItemProvider(PackOutput output) {
        this.itemPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (FluidEntry entry : ModFluids.ALL.values()) {
            futures.add(saveBucketClientItem(cachedOutput, entry));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> saveBucketClientItem(CachedOutput cachedOutput, FluidEntry entry) {
        JsonObject root = new JsonObject();
        JsonObject model = new JsonObject();
        JsonArray models = new JsonArray();

        // Fluid layer first
        JsonObject fluidLayer = new JsonObject();
        fluidLayer.addProperty("type", "minecraft:model");
        fluidLayer.addProperty("model", MyDrugs.MODID + ":item/bucket_fluid_mask");

        JsonArray tints = new JsonArray();
        JsonObject tint = new JsonObject();
        tint.addProperty("type", "minecraft:constant");
        tint.addProperty("value", entry.tint() & 0xFFFFFF);
        tints.add(tint);
        fluidLayer.add("tints", tints);

        // Bucket shell second
        JsonObject shellLayer = new JsonObject();
        shellLayer.addProperty("type", "minecraft:model");
        shellLayer.addProperty("model", "minecraft:item/bucket");

        model.addProperty("type", "minecraft:composite");
        models.add(fluidLayer);
        models.add(shellLayer);
        model.add("models", models);

        root.add("model", model);

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, entry.name() + "_bucket");
        Path path = this.itemPathProvider.json(id);

        return DataProvider.saveStable(cachedOutput, root, path);
    }

    @Override
    public String getName() {
        return "MyDrugs Fluid Bucket Client Items";
    }
}