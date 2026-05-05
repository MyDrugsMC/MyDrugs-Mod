package org.mydrugs.mydrugs.effects.addiction.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

public final class ClientGammaController {
    private static Double originalGamma;
    private static double displayedGamma;

    private ClientGammaController() {
    }

    public static void tick(Minecraft mc) {
        if (mc == null || mc.options == null) {
            return;
        }

        double intensity = Mth.clamp(AddictionClientState.getEffectIntensity(EffectType.GAMMA_BOOST), 0.0F, 1.0F);
        double currentGamma = mc.options.gamma().get();

        if (intensity > 0.001D) {
            if (originalGamma == null) {
                originalGamma = currentGamma;
                displayedGamma = currentGamma;
            }
            double target = Mth.clamp(originalGamma + intensity * 6.0D, 0.0D, 15.0D);
            displayedGamma = Mth.lerp(0.12D, displayedGamma, target);
            mc.options.gamma().set(displayedGamma);
            return;
        }

        if (originalGamma != null) {
            displayedGamma = Mth.lerp(0.18D, displayedGamma, originalGamma);
            mc.options.gamma().set(displayedGamma);
            if (Math.abs(displayedGamma - originalGamma) < 0.01D) {
                mc.options.gamma().set(originalGamma);
                originalGamma = null;
            }
        }
    }
}
