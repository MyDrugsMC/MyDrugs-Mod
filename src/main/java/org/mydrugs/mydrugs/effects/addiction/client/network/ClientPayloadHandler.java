package org.mydrugs.mydrugs.effects.addiction.client.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.client.shaders.ShaderManager;
import org.mydrugs.mydrugs.client.sounds.ClientSoundsHandler;
import org.mydrugs.mydrugs.core.drug.effect.EffectCategory;
import org.mydrugs.mydrugs.effects.addiction.client.AddictionClientState;
import org.mydrugs.mydrugs.effects.addiction.client.AddictionDebugScreen;
import org.mydrugs.mydrugs.effects.addiction.client.sound.HeadphonesMusicController;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionDebugOpenPayload;
import org.mydrugs.mydrugs.effects.addiction.network.DoseSyncPayload;
import org.mydrugs.mydrugs.effects.addiction.network.HeadphonesStatePayload;
import org.mydrugs.mydrugs.effects.addiction.network.DrugEffectSyncPayload;
import org.mydrugs.mydrugs.effects.addiction.network.VomitOverlayPayload;
import net.minecraft.client.Minecraft;
import org.mydrugs.mydrugs.effects.addiction.client.render.VomitOverlayClientState;
import org.mydrugs.mydrugs.sounds.ModSounds;

public final class ClientPayloadHandler {
    private ClientPayloadHandler() {
    }

    public static void handleSnapshot(AddictionClientSnapshotPayload payload, IPayloadContext context) {
        AddictionClientState.apply(payload);
    }

    public static void handleHeadphonesState(HeadphonesStatePayload payload, IPayloadContext context) {
        HeadphonesMusicController.apply(payload.enabled(), payload.trackNonce());
    }

    public static void handleDoseSync(DoseSyncPayload payload, IPayloadContext context) {
        AddictionClientState.applyDoseSync(payload);
        ShaderManager.INSTANCE.updateDoses(payload);
    }

    public static void handleDrugEffectSync(DrugEffectSyncPayload payload, IPayloadContext context) {
        AddictionClientState.applyDrugEffectSync(payload);

        for (DrugEffectSyncPayload.Entry entry : payload.effects()) {
            if (entry.type() == null || entry.remainingTicks() <= 0 || entry.intensity() <= 0.0F) {
                continue;
            }

            EffectCategory category = entry.type().getCategory();
            if (category == EffectCategory.SHADER && Config.CLIENT.enableDrugShaders.get()) {
                int duration = Config.CLIENT.reducedMotionMode.get()
                        ? Math.max(1, entry.remainingTicks() / 2)
                        : entry.remainingTicks();
                ShaderManager.INSTANCE.addDirect(duration, entry.type(), entry.intensity());
            } else if ((category == EffectCategory.SOUND || category == EffectCategory.SOUND_EFFECT) && Config.CLIENT.enableDrugSounds.get()) {
                ClientSoundsHandler.setToStart(ModSounds.fromEffectType(entry.type()), entry.remainingTicks());
            }
        }
    }

    public static void handleVomitOverlay(VomitOverlayPayload payload, IPayloadContext context) {
        VomitOverlayClientState.trigger(payload.intensity());
    }

    public static void handleAddictionDebugOpen(AddictionDebugOpenPayload payload, IPayloadContext context) {
        Minecraft.getInstance().setScreen(new AddictionDebugScreen(payload));
    }
}
