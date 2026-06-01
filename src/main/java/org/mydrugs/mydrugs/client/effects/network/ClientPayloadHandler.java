package org.mydrugs.mydrugs.client.effects.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.client.shaders.ShaderManager;
import org.mydrugs.mydrugs.client.sounds.ClientSoundsHandler;
import org.mydrugs.mydrugs.core.drug.effect.EffectCategory;
import org.mydrugs.mydrugs.client.effects.AddictionClientState;
import org.mydrugs.mydrugs.client.effects.AddictionDebugScreen;
import org.mydrugs.mydrugs.client.effects.hud.AddictionHudRenderer;
import org.mydrugs.mydrugs.client.diary.PersonalDiaryScreen;
import org.mydrugs.mydrugs.addiction.network.PersonalDiarySnapshotPayload;
import org.mydrugs.mydrugs.client.effects.sound.HeadphonesMusicController;
import org.mydrugs.mydrugs.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.addiction.network.AddictionDebugOpenPayload;
import org.mydrugs.mydrugs.addiction.network.BadTripPayload;
import org.mydrugs.mydrugs.addiction.network.BadTripScreamerPayload;
import org.mydrugs.mydrugs.addiction.network.DoseSyncPayload;
import org.mydrugs.mydrugs.addiction.network.DrugEffectCuePayload;
import org.mydrugs.mydrugs.addiction.network.HeadphonesStatePayload;
import org.mydrugs.mydrugs.addiction.network.DrugEffectSyncPayload;
import org.mydrugs.mydrugs.addiction.network.VomitOverlayPayload;
import org.mydrugs.mydrugs.mutation.MutationClientState;
import org.mydrugs.mydrugs.mutation.MutationStat;
import org.mydrugs.mydrugs.mutation.network.MutationSyncPayload;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationClientState;
import org.mydrugs.mydrugs.core.drug.integration.network.IntegrationSyncPayload;
import net.minecraft.client.Minecraft;
import java.util.EnumMap;
import java.util.Map;
import org.mydrugs.mydrugs.client.effects.render.BadTripScreamerOverlay;
import org.mydrugs.mydrugs.client.effects.render.VomitOverlayClientState;
import org.mydrugs.mydrugs.sounds.ModSounds;

public final class ClientPayloadHandler {
    private ClientPayloadHandler() {
    }

    public static void handleSnapshot(AddictionClientSnapshotPayload payload, IPayloadContext context) {
        AddictionClientState.apply(payload);
    }

    public static void handleHeadphonesState(HeadphonesStatePayload payload, IPayloadContext context) {
        HeadphonesMusicController.apply(payload);
    }

    public static void handleDoseSync(DoseSyncPayload payload, IPayloadContext context) {
        AddictionClientState.applyDoseSync(payload);
        ShaderManager.INSTANCE.updateDoses(payload);
    }

    public static void handleBadTrip(BadTripPayload payload, IPayloadContext context) {
        AddictionClientState.applyBadTrip(payload);
    }

    public static void handleDrugEffectSync(DrugEffectSyncPayload payload, IPayloadContext context) {
        AddictionClientState.applyDrugEffectSync(payload);
        ShaderManager.INSTANCE.reconcileDirectEffects(payload.effects());
        ClientSoundsHandler.reconcileEffects(payload.effects());
    }

    public static void handleDrugEffectCue(DrugEffectCuePayload payload, IPayloadContext context) {
        AddictionHudRenderer.enqueuePulse(payload.effectType(), payload.kind(), payload.intensity());
    }

    public static void handleVomitOverlay(VomitOverlayPayload payload, IPayloadContext context) {
        VomitOverlayClientState.trigger(payload.intensity());
    }

    public static void handleBadTripScreamer(BadTripScreamerPayload payload, IPayloadContext context) {
        BadTripScreamerOverlay.trigger(payload.durationTicks(), payload.intensity());
    }

    public static void handleAddictionDebugOpen(AddictionDebugOpenPayload payload, IPayloadContext context) {
        Minecraft.getInstance().setScreen(new AddictionDebugScreen(payload));
    }

    public static void handleMutationSync(MutationSyncPayload payload, IPayloadContext context) {
        Map<MutationStat, Float> values = new EnumMap<>(MutationStat.class);
        for (MutationSyncPayload.Entry entry : payload.entries()) {
            MutationStat stat = MutationStat.bySerializedNameOrNull(entry.statId());
            if (stat != null) {
                values.put(stat, entry.value());
            }
        }
        MutationClientState.apply(values);
    }

    public static void handleIntegrationSync(IntegrationSyncPayload payload, IPayloadContext context) {
        IntegrationClientState.apply(payload.traitIds());
    }

    public static void handlePersonalDiarySnapshot(PersonalDiarySnapshotPayload payload, IPayloadContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof PersonalDiaryScreen open) {
            open.applySnapshot(payload);
        } else {
            mc.setScreen(new PersonalDiaryScreen(payload));
        }
    }
}
