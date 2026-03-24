package org.mydrugs.mydrugs.datagen;

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

public class ModFluidBlockStateProvider implements DataProvider {
    private final PackOutput.PathProvider blockstatePathProvider;

    public ModFluidBlockStateProvider(PackOutput output) {
        this.blockstatePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (FluidEntry entry : ModFluids.ALL.values()) {
            futures.add(saveFluidBlockstate(cachedOutput, entry));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> saveFluidBlockstate(CachedOutput cachedOutput, FluidEntry entry) {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();

        for (int level = 0; level <= 15; level++) {
            JsonObject model = new JsonObject();
            model.addProperty("model", "minecraft:block/water");
            variants.add("level=" + level, model);
        }

        root.add("variants", variants);

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, entry.name());
        Path path = this.blockstatePathProvider.json(id);

        return DataProvider.saveStable(cachedOutput, root, path);
    }

    @Override
    public String getName() {
        return "MyDrugs Fluid Blockstates";
    }
}