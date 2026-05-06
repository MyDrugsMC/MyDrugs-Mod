package org.mydrugs.mydrugs.client;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.network.PsyBlueprintPreviewPayload;

public final class PsyBlueprintPreviewPayloadHandler {
    private PsyBlueprintPreviewPayloadHandler() {
    }

    public static void handle(PsyBlueprintPreviewPayload payload, IPayloadContext context) {
        PsyBlueprintPreviewClientState.apply(payload);
    }
}
