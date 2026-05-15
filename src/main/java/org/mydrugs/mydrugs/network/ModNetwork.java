package org.mydrugs.mydrugs.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionDebugActionPayload;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionDebugOpenPayload;
import org.mydrugs.mydrugs.effects.addiction.network.BadTripPayload;
import org.mydrugs.mydrugs.effects.addiction.network.BadTripScreamerPayload;
import org.mydrugs.mydrugs.effects.addiction.network.DoseSyncPayload;
import org.mydrugs.mydrugs.effects.addiction.network.DrugEffectSyncPayload;
import org.mydrugs.mydrugs.effects.addiction.network.HeadphonesStatePayload;
import org.mydrugs.mydrugs.effects.addiction.network.PersonalDiarySnapshotPayload;
import org.mydrugs.mydrugs.effects.addiction.network.SubmitPersonalDiaryEntryPayload;
import org.mydrugs.mydrugs.effects.addiction.network.VomitOverlayPayload;
import org.mydrugs.mydrugs.effects.payloads.DrugVisualPayload;
import org.mydrugs.mydrugs.mutation.network.MutationSyncPayload;

/**
 * Single source of truth for {@link RegisterPayloadHandlersEvent}.
 *
 * Replaces the previous trio: network.ModPayloads, effects.EffectsNetworkHandler,
 * and effects.addiction.events.RegisterPayloadEvents. Client-side handler lambdas
 * for {@code playToClient} payloads are registered in
 * {@link org.mydrugs.mydrugs.client.network.ClientPayloadHandlers}.
 *
 * Naming convention: keep {@code playToServer} entries close to their domain block
 * (manual-machine, ritual, machine-transfer, ...) so reviewers can audit each
 * server-bound packet at one glance.
 */
@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ModNetwork {
    private ModNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar(MyDrugs.NETWORK_VERSION);

        registerManualMachinePayloads(r);
        registerMachineTransferPayloads(r);
        registerRitualPayloads(r);
        registerBiomeFinderPayloads(r);
        registerStimulantPayloads(r);
        registerDrugFormulaPayloads(r);
        registerVisualPayloads(r);
        registerAddictionPayloads(r);
        registerDiaryPayloads(r);
        registerMutationPayloads(r);
    }

    // --- manual machines: shake / drag impulses, server clamps and validates ---
    private static void registerManualMachinePayloads(PayloadRegistrar r) {
        r.playToServer(SieveShakePayload.TYPE, SieveShakePayload.STREAM_CODEC, SieveShakePayload::handleOnServer);
        r.playToServer(RollerDragPayload.TYPE, RollerDragPayload.STREAM_CODEC, RollerDragPayload::handleOnServer);
        r.playToServer(CoffeePulperDragPayload.TYPE, CoffeePulperDragPayload.STREAM_CODEC, CoffeePulperDragPayload::handleOnServer);
    }

    // --- machine transfer config GUI: open/cycle/refresh snapshot ---
    private static void registerMachineTransferPayloads(PayloadRegistrar r) {
        r.playToServer(OpenMachineTransferConfigPayload.TYPE, OpenMachineTransferConfigPayload.STREAM_CODEC, OpenMachineTransferConfigPayload::handleOnServer);
        r.playToServer(RequestMachineTransferOverlayPayload.TYPE, RequestMachineTransferOverlayPayload.STREAM_CODEC, RequestMachineTransferOverlayPayload::handleOnServer);
        r.playToServer(CycleMachineTransferSidePayload.TYPE, CycleMachineTransferSidePayload.STREAM_CODEC, CycleMachineTransferSidePayload::handleOnServer);
        r.playToClient(MachineTransferConfigSnapshotPayload.TYPE, MachineTransferConfigSnapshotPayload.STREAM_CODEC);
    }

    // --- psy mixer ritual ---
    private static void registerRitualPayloads(PayloadRegistrar r) {
        r.playToServer(PsyMixerStartRitualPayload.TYPE, PsyMixerStartRitualPayload.STREAM_CODEC, PsyMixerStartRitualPayload::handleOnServer);
        r.playToServer(PsyMixerRitualInputPayload.TYPE, PsyMixerRitualInputPayload.STREAM_CODEC, PsyMixerRitualInputPayload::handleOnServer);
    }

    private static void registerBiomeFinderPayloads(PayloadRegistrar r) {
        r.playToClient(BiomeFinderOpenScreenPayload.TYPE, BiomeFinderOpenScreenPayload.STREAM_CODEC);
        r.playToServer(BiomeFinderSelectPayload.TYPE, BiomeFinderSelectPayload.STREAM_CODEC, BiomeFinderSelectPayload::handleOnServer);
    }

    private static void registerStimulantPayloads(PayloadRegistrar r) {
        r.playToServer(StimulantDashPayload.TYPE, StimulantDashPayload.STREAM_CODEC, StimulantDashPayload::handleOnServer);
    }

    private static void registerDrugFormulaPayloads(PayloadRegistrar r) {
        r.playToClient(OpenDrugFormulaNamingPayload.TYPE, OpenDrugFormulaNamingPayload.STREAM_CODEC);
        r.playToServer(SubmitDrugFormulaNamePayload.TYPE, SubmitDrugFormulaNamePayload.STREAM_CODEC, SubmitDrugFormulaNamePayload::handleOnServer);
    }

    private static void registerVisualPayloads(PayloadRegistrar r) {
        r.playToClient(DrugVisualPayload.TYPE, DrugVisualPayload.STREAM_CODEC);
        r.playToClient(PsyBlueprintPreviewPayload.TYPE, PsyBlueprintPreviewPayload.STREAM_CODEC);
    }

    // --- addiction state snapshots and admin actions ---
    private static void registerAddictionPayloads(PayloadRegistrar r) {
        r.playToClient(AddictionClientSnapshotPayload.TYPE, AddictionClientSnapshotPayload.STREAM_CODEC);
        r.playToClient(HeadphonesStatePayload.TYPE, HeadphonesStatePayload.STREAM_CODEC);
        r.playToClient(DoseSyncPayload.TYPE, DoseSyncPayload.STREAM_CODEC);
        r.playToClient(DrugEffectSyncPayload.TYPE, DrugEffectSyncPayload.STREAM_CODEC);
        r.playToClient(VomitOverlayPayload.TYPE, VomitOverlayPayload.STREAM_CODEC);
        r.playToClient(BadTripPayload.TYPE, BadTripPayload.STREAM_CODEC);
        r.playToClient(BadTripScreamerPayload.TYPE, BadTripScreamerPayload.STREAM_CODEC);
        r.playToClient(AddictionDebugOpenPayload.TYPE, AddictionDebugOpenPayload.STREAM_CODEC);
        r.playToServer(AddictionDebugActionPayload.TYPE, AddictionDebugActionPayload.STREAM_CODEC, AddictionDebugActionPayload::handleOnServer);
    }

    private static void registerDiaryPayloads(PayloadRegistrar r) {
        r.playToClient(PersonalDiarySnapshotPayload.TYPE, PersonalDiarySnapshotPayload.STREAM_CODEC);
        r.playToServer(SubmitPersonalDiaryEntryPayload.TYPE, SubmitPersonalDiaryEntryPayload.STREAM_CODEC, SubmitPersonalDiaryEntryPayload::handleOnServer);
    }

    private static void registerMutationPayloads(PayloadRegistrar r) {
        r.playToClient(MutationSyncPayload.TYPE, MutationSyncPayload.STREAM_CODEC);
    }
}
