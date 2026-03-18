package org.mydrugs.mydrugs.worldgen.biomes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.mydrugs.mydrugs.MyDrugs;

public class ModBiomes {
    public static final ResourceKey<Biome> PSYCHEDELIC_VALLEY = key("psychedelic_valley");
    public static final ResourceKey<Biome> PSYCHEDELIC_MUSHROOM_VALLEY = key("psychedelic_mushroom_valley");

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name));
    }

    private ModBiomes() {}
}
