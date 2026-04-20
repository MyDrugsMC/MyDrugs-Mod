package org.mydrugs.mydrugs.effects.addiction.network;

import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.mydrugs.mydrugs.effects.addiction.client.network.ClientPayloadHandler;

public class AddictionPayloads {
    private AddictionPayloads() {
    }

    public static void registerCommon(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(AddictionClientSnapshotPayload.TYPE, AddictionClientSnapshotPayload.STREAM_CODEC);
        registrar.playToClient(HeadphonesStatePayload.TYPE, HeadphonesStatePayload.STREAM_CODEC);
    }

    public static void registerClient(RegisterClientPayloadHandlersEvent event) {
        event.register(AddictionClientSnapshotPayload.TYPE, ClientPayloadHandler::handleSnapshot);
        event.register(HeadphonesStatePayload.TYPE, ClientPayloadHandler::handleHeadphonesState);
    }
}
