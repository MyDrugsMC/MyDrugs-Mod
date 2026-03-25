package org.mydrugs.mydrugs.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ModDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(SpaceFoodModelProvider::new);
        event.createProvider(SpaceFoodOverlayTextureProvider::new);
        event.createProvider(ModFluidClientItemProvider::new);
        event.createProvider(ModSimpleClientItemProvider::new);
        event.createProvider(ModLangProvider::new);
        event.createProvider(ModLootTableProvider::new);
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(ModFluidBlockStateProvider::new);

        PackOutput output = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        event.getGenerator().addProvider(
                true,
                new ModFluidTagProvider(output, lookupProvider)
        );
    }

    private ModDataGenerators() {}
}