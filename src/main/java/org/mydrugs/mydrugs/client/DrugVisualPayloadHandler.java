package org.mydrugs.mydrugs.client;

import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.sounds.ModSounds;
import org.mydrugs.mydrugs.client.shaders.ShaderManager;
import org.mydrugs.mydrugs.client.sounds.ClientSoundsHandler;
import org.mydrugs.mydrugs.core.drug.effect.EffectCategory;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.payloads.DrugVisualPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class DrugVisualPayloadHandler {
    private DrugVisualPayloadHandler() {
    }

    public static void handle(DrugVisualPayload payload, IPayloadContext context) {
        EffectType type = payload.effectType();
        if (type == null || payload.duration() <= 0) {
            return;
        }

        if (type.getCategory() == EffectCategory.SHADER) {
            if (!Config.CLIENT.enableDrugShaders.get()) {
                return;
            }
            int duration = Config.CLIENT.reducedMotionMode.get()
                    ? Math.max(1, payload.duration() / 2)
                    : payload.duration();
            ShaderManager.INSTANCE.add(duration, type);
            return;
        }

        if (type.getCategory() == EffectCategory.SOUND_EFFECT && Config.CLIENT.enableDrugSounds.get()) {
            ClientSoundsHandler.setToStart(ModSounds.fromEffectType(type), payload.duration());
        }
    }
}
