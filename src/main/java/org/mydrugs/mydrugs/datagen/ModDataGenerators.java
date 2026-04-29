package org.mydrugs.mydrugs.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.worldgen.ModBiomeModifiers;
import org.mydrugs.mydrugs.worldgen.ModConfiguredFeatures;
import org.mydrugs.mydrugs.worldgen.ModPlacedFeatures;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ModDataGenerators {

    private ModDataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(SpaceFoodModelProvider::new);
        event.createProvider(SpaceFoodOverlayTextureProvider::new);
        event.createProvider(ModFluidClientItemProvider::new);
        event.createProvider(ModSimpleClientItemProvider::new);
        event.createProvider(ModSimpleBlockAssetProvider::new);
        event.createProvider(ModLootTableProvider::new);
        event.createProvider(ModVanillaRecipeJsonProvider::new);
        event.createProvider(ModVanillaRecipeSnapshotProvider::new);
        event.createProvider(ModFluidBlockStateProvider::new);
        event.createProvider(ModBlockTagsProvider::new);

        PackOutput output = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        event.getGenerator().addProvider(
                true,
                new ModFluidTagProvider(output, lookupProvider)
        );

        RegistrySetBuilder builder = new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap);

        event.createDatapackRegistryObjects(builder);
    }
}
