package org.mydrugs.mydrugs.client.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.client.BiomeFinderClientPayloadHandler;
import org.mydrugs.mydrugs.client.DrugFormulaNamingPayloadHandler;
import org.mydrugs.mydrugs.client.DrugVisualPayloadHandler;
import org.mydrugs.mydrugs.client.PsyBlueprintPreviewPayloadHandler;
import org.mydrugs.mydrugs.client.effects.network.ClientPayloadHandler;
import org.mydrugs.mydrugs.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.addiction.network.AddictionDebugOpenPayload;
import org.mydrugs.mydrugs.addiction.network.BadTripPayload;
import org.mydrugs.mydrugs.addiction.network.BadTripScreamerPayload;
import org.mydrugs.mydrugs.addiction.network.DoseSyncPayload;
import org.mydrugs.mydrugs.addiction.network.DrugEffectSyncPayload;
import org.mydrugs.mydrugs.addiction.network.HeadphonesStatePayload;
import org.mydrugs.mydrugs.addiction.network.PersonalDiarySnapshotPayload;
import org.mydrugs.mydrugs.addiction.network.StartMemoryCapturePayload;
import org.mydrugs.mydrugs.addiction.network.VomitOverlayPayload;
import org.mydrugs.mydrugs.client.diary.MemoryCaptureClient;
import org.mydrugs.mydrugs.client.psy_mixer.PsyMixerRitualClientState;
import org.mydrugs.mydrugs.network.DrugVisualPayload;
import org.mydrugs.mydrugs.mutation.network.MutationSyncPayload;
import org.mydrugs.mydrugs.network.BiomeFinderOpenScreenPayload;
import org.mydrugs.mydrugs.network.MachineTransferConfigSnapshotPayload;
import org.mydrugs.mydrugs.network.OpenDrugFormulaNamingPayload;
import org.mydrugs.mydrugs.network.PsyBlueprintPreviewPayload;
import org.mydrugs.mydrugs.network.PsyMixerRitualSyncPayload;
import org.mydrugs.mydrugs.pipe.client.MachineTransferClientPayloadHandler;

/**
 * Single source of truth for {@link RegisterClientPayloadHandlersEvent}.
 *
 * Counterpart to {@link org.mydrugs.mydrugs.network.ModNetwork}: that class
 * registers payload TYPE/STREAM_CODEC for every direction; this class registers
 * the client-side handler lambdas for {@code playToClient} payloads.
 */
@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class ClientPayloadHandlers {
    private ClientPayloadHandlers() {
    }

    @SubscribeEvent
    public static void register(RegisterClientPayloadHandlersEvent event) {
        // Machine transfer config UI snapshot from server.
        event.register(MachineTransferConfigSnapshotPayload.TYPE, MachineTransferClientPayloadHandler::handleSnapshot);

        // Visual / shader / preview payloads.
        event.register(DrugVisualPayload.TYPE, DrugVisualPayloadHandler::handle);
        event.register(PsyBlueprintPreviewPayload.TYPE, PsyBlueprintPreviewPayloadHandler::handle);
        event.register(BiomeFinderOpenScreenPayload.TYPE, BiomeFinderClientPayloadHandler::handleOpenScreen);
        event.register(OpenDrugFormulaNamingPayload.TYPE, DrugFormulaNamingPayloadHandler::handle);
        event.register(PsyMixerRitualSyncPayload.TYPE, PsyMixerRitualClientState::handle);

        // Addiction snapshots, dose/effect sync, headphones, vomit, bad-trip.
        event.register(AddictionClientSnapshotPayload.TYPE, ClientPayloadHandler::handleSnapshot);
        event.register(HeadphonesStatePayload.TYPE, ClientPayloadHandler::handleHeadphonesState);
        event.register(DoseSyncPayload.TYPE, ClientPayloadHandler::handleDoseSync);
        event.register(DrugEffectSyncPayload.TYPE, ClientPayloadHandler::handleDrugEffectSync);
        event.register(VomitOverlayPayload.TYPE, ClientPayloadHandler::handleVomitOverlay);
        event.register(BadTripPayload.TYPE, ClientPayloadHandler::handleBadTrip);
        event.register(BadTripScreamerPayload.TYPE, ClientPayloadHandler::handleBadTripScreamer);
        event.register(AddictionDebugOpenPayload.TYPE, ClientPayloadHandler::handleAddictionDebugOpen);
        event.register(PersonalDiarySnapshotPayload.TYPE, ClientPayloadHandler::handlePersonalDiarySnapshot);
        event.register(StartMemoryCapturePayload.TYPE, (payload, context) -> MemoryCaptureClient.start(payload));

        // Mutation stat sync from server.
        event.register(MutationSyncPayload.TYPE, ClientPayloadHandler::handleMutationSync);
    }
}
