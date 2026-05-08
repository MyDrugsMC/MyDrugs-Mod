package org.mydrugs.mydrugs.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.mydrugs.mydrugs.MyDrugs;

public final class ModWorldGenKeys {
    public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_MAGIC_MUSHROOM =
            configured("huge_magic_mushroom");

    public static final ResourceKey<ConfiguredFeature<?, ?>> SALT_DISK =
            configured("salt_disk");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SULFUR_ORE =
            configured("sulfur_ore");

    public static final ResourceKey<PlacedFeature> SALT_DISK_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, id("salt_disk_placed"));
    public static final ResourceKey<PlacedFeature> SULFUR_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, id("sulfur_ore_placed"));

    public static final ResourceKey<BiomeModifier> ADD_SALT_TO_OCEANS =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("add_salt_to_oceans"));
    public static final ResourceKey<BiomeModifier> ADD_SULFUR_ORE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("add_sulfur_ore"));

    public static final ResourceKey<ConfiguredFeature<?, ?>> PLATINUM_ORE =
            configured("platinum_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ALUMINIUM_ORE =
            configured("aluminium_ore");

    public static final ResourceKey<ConfiguredFeature<?, ?>> PETROLEUM_LAKE =
            configured("petroleum_lake");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ALOE_VERA_PATCH =
            configured("aloe_vera_patch");

    public static final ResourceKey<PlacedFeature> PLATINUM_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, id("platinum_ore_placed"));
    public static final ResourceKey<PlacedFeature> ALUMINIUM_ORE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, id("aluminium_ore_placed"));

    public static final ResourceKey<PlacedFeature> PETROLEUM_LAKE_SURFACE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, id("petroleum_lake_surface_placed"));
    public static final ResourceKey<PlacedFeature> ALOE_VERA_PATCH_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, id("aloe_vera_patch_placed"));

    public static final ResourceKey<BiomeModifier> ADD_PLATINUM_ORE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("add_platinum_ore"));
    public static final ResourceKey<BiomeModifier> ADD_ALUMINIUM_ORE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("add_aluminium_ore"));

    public static final ResourceKey<BiomeModifier> ADD_PETROLEUM_LAKES_TO_DESERTS =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("add_petroleum_lakes_to_deserts"));
    public static final ResourceKey<BiomeModifier> ADD_ALOE_VERA_TO_OVERWORLD =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, id("add_aloe_vera_to_overworld"));

    private ModWorldGenKeys() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, path);
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String name) {
        return ResourceKey.create(
                Registries.CONFIGURED_FEATURE,
                id(name)
        );
    }
}
