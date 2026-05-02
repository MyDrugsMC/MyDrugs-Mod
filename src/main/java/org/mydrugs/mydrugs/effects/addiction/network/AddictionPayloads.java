package org.mydrugs.mydrugs.effects.addiction.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.mydrugs.mydrugs.MyDrugs;

public class AddictionPayloads {
    private AddictionPayloads() {
    }

    public static void registerCommon(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MyDrugs.NETWORK_VERSION);
        registrar.playToClient(AddictionClientSnapshotPayload.TYPE, AddictionClientSnapshotPayload.STREAM_CODEC);
        registrar.playToClient(HeadphonesStatePayload.TYPE, HeadphonesStatePayload.STREAM_CODEC);
        registrar.playToClient(DoseSyncPayload.TYPE, DoseSyncPayload.STREAM_CODEC);
        registrar.playToClient(AddictionDebugOpenPayload.TYPE, AddictionDebugOpenPayload.STREAM_CODEC);
        registrar.playToServer(
                AddictionDebugActionPayload.TYPE,
                AddictionDebugActionPayload.STREAM_CODEC,
                AddictionDebugActionPayload::handleOnServer
        );
    }
}
