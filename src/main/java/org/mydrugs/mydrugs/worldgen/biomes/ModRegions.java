package org.mydrugs.mydrugs.worldgen.biomes;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.worldgen.WorldgenConfig;
import terrablender.api.ParameterUtils;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

public class ModRegions extends Region {
    public ModRegions(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
                          Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {

        this.addModifiedVanillaOverworldBiomes(mapper, builder -> {
            if (Config.WORLDGEN.replaceMushroomFields.get()) {
                builder.replaceBiome(Biomes.MUSHROOM_FIELDS, ModBiomes.PSYCHEDELIC_MUSHROOM_VALLEY);
            }
        });

        if (Config.WORLDGEN.addPsychedelicBiomeSeparately.get()) {
            addSeparatePsychedelicBiome(mapper);
        }
    }

    private void addSeparatePsychedelicBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        switch (WorldgenConfig.psychedelicClimateBands()) {
            case "warm_wet" -> addBiome(
                    mapper,
                    ParameterUtils.Temperature.WARM,
                    ParameterUtils.Humidity.HUMID,
                    ParameterUtils.Continentalness.NEAR_INLAND,
                    ParameterUtils.Erosion.EROSION_2,
                    ParameterUtils.Weirdness.MID_SLICE_NORMAL_ASCENDING,
                    ParameterUtils.Depth.SURFACE,
                    0.0F,
                    ModBiomes.PSYCHEDELIC_MUSHROOM_VALLEY
            );
            case "broad_wet" -> {
                addBiome(
                        mapper,
                        ParameterUtils.Temperature.span(ParameterUtils.Temperature.NEUTRAL, ParameterUtils.Temperature.HOT),
                        ParameterUtils.Humidity.span(ParameterUtils.Humidity.WET, ParameterUtils.Humidity.HUMID),
                        ParameterUtils.Continentalness.span(ParameterUtils.Continentalness.NEAR_INLAND, ParameterUtils.Continentalness.MID_INLAND),
                        ParameterUtils.Erosion.span(ParameterUtils.Erosion.EROSION_1, ParameterUtils.Erosion.EROSION_4),
                        ParameterUtils.Weirdness.span(ParameterUtils.Weirdness.MID_SLICE_NORMAL_ASCENDING, ParameterUtils.Weirdness.HIGH_SLICE_NORMAL_ASCENDING),
                        ParameterUtils.Depth.SURFACE.parameter(),
                        0.0F,
                        ModBiomes.PSYCHEDELIC_MUSHROOM_VALLEY
                );
            }
            default -> addBiomeSimilar(mapper, Biomes.MUSHROOM_FIELDS, ModBiomes.PSYCHEDELIC_MUSHROOM_VALLEY);
        }
    }
}
