package org.mydrugs.mydrugs.dimension.inner.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.function.Supplier;

public final class ModInnerWorldgen {
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, MyDrugs.MODID);

    public static final Supplier<MapCodec<InnerChunkGenerator>> INNER_CONTINENT =
            CHUNK_GENERATORS.register("inner_continent", () -> InnerChunkGenerator.CODEC);

    private ModInnerWorldgen() {
    }

    public static void register(IEventBus modBus) {
        CHUNK_GENERATORS.register(modBus);
    }
}
