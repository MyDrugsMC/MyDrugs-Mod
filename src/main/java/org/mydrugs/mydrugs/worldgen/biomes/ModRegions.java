package org.mydrugs.mydrugs.worldgen.biomes;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.ParameterUtils;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.VanillaParameterOverlayBuilder;

import java.util.function.Consumer;

public class ModRegions extends Region {
    public ModRegions(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
                          Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {

        VanillaParameterOverlayBuilder valley = new VanillaParameterOverlayBuilder();

        new ParameterUtils.ParameterPointListBuilder()
                .temperature(ParameterUtils.Temperature.NEUTRAL, ParameterUtils.Temperature.WARM)
                .humidity(ParameterUtils.Humidity.NEUTRAL, ParameterUtils.Humidity.HUMID)
                .continentalness(ParameterUtils.Continentalness.INLAND)
                .erosion(ParameterUtils.Erosion.EROSION_4, ParameterUtils.Erosion.EROSION_6)
                .depth(ParameterUtils.Depth.SURFACE, ParameterUtils.Depth.FLOOR)
                .weirdness(ParameterUtils.Weirdness.MID_SLICE_NORMAL_ASCENDING, ParameterUtils.Weirdness.MID_SLICE_NORMAL_DESCENDING)
                .build()
                .forEach(point -> valley.add(point, ModBiomes.PSYCHEDELIC_VALLEY));

        valley.build().forEach(mapper);

        this.addModifiedVanillaOverworldBiomes(mapper, builder -> {
            builder.replaceBiome(Biomes.MUSHROOM_FIELDS, ModBiomes.PSYCHEDELIC_MUSHROOM_VALLEY);
        });
    }
}