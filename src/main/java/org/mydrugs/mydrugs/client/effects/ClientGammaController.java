package org.mydrugs.mydrugs.client.effects;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.client.shaders.ShaderManager;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

public final class ClientGammaController {
    private static float displayedStrength;

    private ClientGammaController() {
    }

    public static void tick(Minecraft mc) {
        if (mc == null) {
            return;
        }

        float target = 0.0F;
        if (Config.CLIENT.enableDrugShaders.get()) {
            target = Mth.clamp(AddictionClientState.getEffectIntensity(EffectType.GAMMA_BOOST), 0.0F, 1.0F);
        }

        displayedStrength = Mth.lerp(target > displayedStrength ? 0.12F : 0.18F, displayedStrength, target);
        if (displayedStrength < 0.002F && target < 0.002F) {
            displayedStrength = 0.0F;
        }
        ShaderManager.INSTANCE.setContinuous(EffectType.GAMMA_BOOST, displayedStrength);
    }
}
