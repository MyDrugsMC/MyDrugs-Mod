package org.mydrugs.mydrugs.effects.addiction.client.render;

import net.minecraft.client.Minecraft;
import org.mydrugs.mydrugs.effects.addiction.client.AddictionClientState;
import org.mydrugs.mydrugs.effects.addiction.config.SymptomFlags;

public final class DissociationCameraEffect {
    private DissociationCameraEffect() {
    }

    public static float yawOffset(float partialTicks) {
        if (!AddictionClientState.has(SymptomFlags.DISSOCIATION)) return 0.0F;
        return (float) Math.sin((Minecraft.getInstance().level.getGameTime() + partialTicks) * 0.10F) * AddictionClientState.globalSeverity * 0.6F;
    }

    public static float pitchOffset(float partialTicks) {
        if (!AddictionClientState.has(SymptomFlags.DISSOCIATION)) return 0.0F;
        return (float) Math.cos((Minecraft.getInstance().level.getGameTime() + partialTicks) * 0.08F) * AddictionClientState.globalSeverity * 0.4F;
    }
}