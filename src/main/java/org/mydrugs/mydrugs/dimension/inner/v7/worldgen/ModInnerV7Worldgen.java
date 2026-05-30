package org.mydrugs.mydrugs.dimension.inner.v7.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.function.Supplier;

public final class ModInnerV7Worldgen {
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, MyDrugs.MODID);

    public static final Supplier<MapCodec<InnerV7ChunkGenerator>> INNER_V7 =
            CHUNK_GENERATORS.register("inner_v7", () -> InnerV7ChunkGenerator.CODEC);

    private ModInnerV7Worldgen() {
    }

    public static void register(IEventBus modBus) {
        CHUNK_GENERATORS.register(modBus);
    }
}
