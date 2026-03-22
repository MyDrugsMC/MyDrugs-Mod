package org.mydrugs.mydrugs.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.mydrugs.mydrugs.MyDrugs;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ModDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(SpaceFoodModelProvider::new);
        event.createProvider(SpaceFoodOverlayTextureProvider::new);
    }

    private ModDataGenerators() {}
}