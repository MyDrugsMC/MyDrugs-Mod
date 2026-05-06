package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.menu.client.VanillaBiomeFinderScreen;
import org.mydrugs.mydrugs.network.BiomeFinderOpenScreenPayload;

public final class BiomeFinderClientPayloadHandler {
    private BiomeFinderClientPayloadHandler() {
    }

    public static void handleOpenScreen(BiomeFinderOpenScreenPayload payload, IPayloadContext context) {
        Minecraft.getInstance().setScreen(new VanillaBiomeFinderScreen(
                payload.hand(),
                payload.selectedBiome(),
                payload.availableBiomes()
        ));
    }
}
