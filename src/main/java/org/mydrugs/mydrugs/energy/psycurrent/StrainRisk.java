package org.mydrugs.mydrugs.energy.psycurrent;

import net.minecraft.network.chat.Component;
import org.mydrugs.mydrugs.energy.PsyCurrentConstants;

/**
 * Named buckets for engine strain. Shared by the engine GUI label, the fuel forecast tooltip, and
 * the area preview / pulse visuals — anything that wants to communicate "how close is the engine
 * to overload" in plain language.
 */
public enum StrainRisk {
    STABLE("stable", 0xFF5FB37A, 0xFF77D58F),
    TENSE("tense", 0xFFE8A84A, 0xFFFFD899),
    DANGEROUS("dangerous", 0xFFFF8A4D, 0xFFFFB07A),
    CRITICAL("critical", 0xFFFF5D4D, 0xFFFF8B7C),
    OVERLOADED("overloaded", 0xFFB23A33, 0xFFE56055);

    private final String key;
    private final int colorPrimary;
    private final int colorSecondary;

    StrainRisk(String key, int colorPrimary, int colorSecondary) {
        this.key = key;
        this.colorPrimary = colorPrimary;
        this.colorSecondary = colorSecondary;
    }

    public String key() {
        return this.key;
    }

    public int colorPrimary() {
        return this.colorPrimary;
    }

    public int colorSecondary() {
        return this.colorSecondary;
    }

    public Component label() {
        return Component.translatable("screen.mydrugs.distillate_engine.strain_state." + this.key);
    }

    public static StrainRisk forStrain(int strain) {
        int clamped = Math.max(0, strain);
        if (clamped >= PsyCurrentConstants.ENGINE_MAX_STRAIN) {
            return OVERLOADED;
        }
        if (clamped >= 90) {
            return CRITICAL;
        }
        if (clamped >= 70) {
            return DANGEROUS;
        }
        if (clamped >= 40) {
            return TENSE;
        }
        return STABLE;
    }

    /**
     * Risk a player will see if they ignite a fuel right now — strain on start plus the projected
     * strain accumulation over the whole burn. Clamped to {@link PsyCurrentConstants#ENGINE_MAX_STRAIN}.
     */
    public static StrainRisk forecast(int currentStrain, DistillateFuel fuel, boolean hasCurrentRegulator) {
        if (fuel == null) {
            return forStrain(currentStrain);
        }
        int seconds = Math.max(1, fuel.durationTicks() / 20);
        int strainPerSecond = fuel.strainPerSecond();
        if (strainPerSecond > 0 && hasCurrentRegulator) {
            strainPerSecond = Math.max(0, strainPerSecond - PsyCurrentConstants.ENGINE_CURRENT_REGULATOR_STRAIN_REDUCTION);
        }
        long projected = (long) currentStrain + fuel.strainOnStart() + (long) strainPerSecond * seconds;
        int clamped = (int) Math.max(0, Math.min(PsyCurrentConstants.ENGINE_MAX_STRAIN, projected));
        return forStrain(clamped);
    }
}
