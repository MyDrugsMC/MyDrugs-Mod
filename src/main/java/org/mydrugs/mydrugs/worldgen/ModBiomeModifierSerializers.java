package org.mydrugs.mydrugs.worldgen;

import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.function.Supplier;

public final class ModBiomeModifierSerializers {
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MyDrugs.MODID);

    public static final Supplier<MapCodec<ConfigurableAddFeaturesBiomeModifier>> CONFIGURABLE_ADD_FEATURES =
            BIOME_MODIFIER_SERIALIZERS.register(
                    "configurable_add_features",
                    () -> ConfigurableAddFeaturesBiomeModifier.CODEC
            );

    private ModBiomeModifierSerializers() {
    }

    public static void register(IEventBus modBus) {
        BIOME_MODIFIER_SERIALIZERS.register(modBus);
    }
}
