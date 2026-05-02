package org.mydrugs.mydrugs.datagen;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ModAdvancementProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public ModAdvancementProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "advancement");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        new MyDrugsAdvancementGenerator().generate((id, advancement) -> {
            Path path = pathProvider.json(id);
            futures.add(DataProvider.saveStable(cachedOutput, advancement, path));
        });
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "MyDrugs Advancements";
    }

    @FunctionalInterface
    public interface Output {
        void accept(ResourceLocation id, JsonObject advancement);
    }
}
