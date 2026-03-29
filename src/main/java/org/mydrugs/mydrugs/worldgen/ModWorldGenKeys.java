package org.mydrugs.mydrugs.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.mydrugs.mydrugs.MyDrugs;

public final class ModWorldGenKeys {
    public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_MAGIC_MUSHROOM =
            configured("huge_magic_mushroom");

    private ModWorldGenKeys() {
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String name) {
        return ResourceKey.create(
                Registries.CONFIGURED_FEATURE,
                ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name)
        );
    }
}