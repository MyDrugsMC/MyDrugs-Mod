package org.mydrugs.mydrugs.effects.addiction.client.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.effects.addiction.client.AddictionClientState;
import org.mydrugs.mydrugs.effects.addiction.client.sound.HeadphonesMusicController;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.effects.addiction.network.HeadphonesStatePayload;

public final class ClientPayloadHandler {
    private ClientPayloadHandler() {}

    public static void handleSnapshot(AddictionClientSnapshotPayload payload, IPayloadContext context) {
        AddictionClientState.apply(payload);
    }

    public static void handleHeadphonesState(HeadphonesStatePayload payload, IPayloadContext context) {
        HeadphonesMusicController.apply(payload.enabled(), payload.trackNonce());
    }
}