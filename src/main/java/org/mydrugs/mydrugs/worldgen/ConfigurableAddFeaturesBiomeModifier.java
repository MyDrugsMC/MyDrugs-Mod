package org.mydrugs.mydrugs.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public record ConfigurableAddFeaturesBiomeModifier(
        HolderSet<Biome> biomes,
        HolderSet<PlacedFeature> features,
        GenerationStep.Decoration step,
        String configKey
) implements BiomeModifier {
    public static final MapCodec<ConfigurableAddFeaturesBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Biome.LIST_CODEC.fieldOf("biomes").forGetter(ConfigurableAddFeaturesBiomeModifier::biomes),
                            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(ConfigurableAddFeaturesBiomeModifier::features),
                            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(ConfigurableAddFeaturesBiomeModifier::step),
                            Codec.STRING.fieldOf("config_key").forGetter(ConfigurableAddFeaturesBiomeModifier::configKey)
                    )
                    .apply(builder, ConfigurableAddFeaturesBiomeModifier::new)
    );

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || !WorldgenConfig.featureEnabled(configKey) || !biomes.contains(biome)) {
            return;
        }

        BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
        features.forEach(holder -> generationSettings.addFeature(step, holder));
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return ModBiomeModifierSerializers.CONFIGURABLE_ADD_FEATURES.get();
    }
}
