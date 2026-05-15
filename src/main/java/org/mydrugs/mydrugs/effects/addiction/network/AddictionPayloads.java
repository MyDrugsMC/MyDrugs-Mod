package org.mydrugs.mydrugs.effects.addiction.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.mutation.network.MutationSyncPayload;

public class AddictionPayloads {
    private AddictionPayloads() {
    }

    public static void registerCommon(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MyDrugs.NETWORK_VERSION);
        registrar.playToClient(MutationSyncPayload.TYPE, MutationSyncPayload.STREAM_CODEC);
        registrar.playToClient(AddictionClientSnapshotPayload.TYPE, AddictionClientSnapshotPayload.STREAM_CODEC);
        registrar.playToClient(HeadphonesStatePayload.TYPE, HeadphonesStatePayload.STREAM_CODEC);
        registrar.playToClient(DoseSyncPayload.TYPE, DoseSyncPayload.STREAM_CODEC);
        registrar.playToClient(DrugEffectSyncPayload.TYPE, DrugEffectSyncPayload.STREAM_CODEC);
        registrar.playToClient(VomitOverlayPayload.TYPE, VomitOverlayPayload.STREAM_CODEC);
        registrar.playToClient(BadTripPayload.TYPE, BadTripPayload.STREAM_CODEC);
        registrar.playToClient(BadTripScreamerPayload.TYPE, BadTripScreamerPayload.STREAM_CODEC);
        registrar.playToClient(AddictionDebugOpenPayload.TYPE, AddictionDebugOpenPayload.STREAM_CODEC);
        registrar.playToClient(PersonalDiarySnapshotPayload.TYPE, PersonalDiarySnapshotPayload.STREAM_CODEC);
        registrar.playToServer(
                AddictionDebugActionPayload.TYPE,
                AddictionDebugActionPayload.STREAM_CODEC,
                AddictionDebugActionPayload::handleOnServer
        );
        registrar.playToServer(
                SubmitPersonalDiaryEntryPayload.TYPE,
                SubmitPersonalDiaryEntryPayload.STREAM_CODEC,
                SubmitPersonalDiaryEntryPayload::handleOnServer
        );
    }
}
