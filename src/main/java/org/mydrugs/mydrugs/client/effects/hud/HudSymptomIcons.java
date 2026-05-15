package org.mydrugs.mydrugs.client.effects.hud;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.client.effects.AddictionClientState;
import org.mydrugs.mydrugs.addiction.config.SymptomFlags;
import org.mydrugs.mydrugs.core.drug.dose.DoseState;

/**
 * Canonical list of symptom/effect icons shown on the left HUD column.
 * Shared between {@link AddictionHudRenderer} and the diary screen so both stay in sync.
 *
 * Each icon resolves its current 0..1 intensity from the live {@link AddictionClientState}.
 */
public final class HudSymptomIcons {
    public static final float MIN_VISIBLE = 0.015F;

    public static final HudSymptomIcon[] LIST = {
            new HudSymptomIcon("insomnia", "Insomnia", HudSymptomIcons::insomniaIntensity),
            new HudSymptomIcon("hallucination", "Hallucinations", () -> flagIntensity(SymptomFlags.HALLUCINATION)),
            new HudSymptomIcon("vision", "Vision distortions", () -> flagIntensity(SymptomFlags.VISION)),
            new HudSymptomIcon("confusion", "Confusion",
                    () -> Math.max(flagIntensity(SymptomFlags.CONFUSION), effectIntensity(EffectType.CONFUSION))),
            new HudSymptomIcon("stress", "Stress",
                    () -> Math.max(flagIntensity(SymptomFlags.STRESS), AddictionClientState.stressLevel)),
            new HudSymptomIcon("dissociation", "Dissociation", () -> flagIntensity(SymptomFlags.DISSOCIATION)),
            new HudSymptomIcon("fatigue", "Fatigue", () -> flagIntensity(SymptomFlags.FATIGUE)),
            new HudSymptomIcon("intrusive_thoughts", "Intrusive thoughts", () -> flagIntensity(SymptomFlags.INTRUSIVE_THOUGHTS)),
            new HudSymptomIcon("fragility", "Body fragility", () -> flagIntensity(SymptomFlags.FRAGILITY)),
            new HudSymptomIcon("blur", "Blurred vision", () -> effectIntensity(EffectType.BLUR)),
            new HudSymptomIcon("vomit", "Nausea",
                    () -> Math.max(effectIntensity(EffectType.VOMIT), effectIntensity(EffectType.CUSTOM_NAUSEA))),
            new HudSymptomIcon("tremor", "Tremors", () -> effectIntensity(EffectType.TREMOR)),
            new HudSymptomIcon("stumble", "Stumble", () -> effectIntensity(EffectType.STUMBLE)),
            new HudSymptomIcon("input_fail", "Lost reflexes", () -> effectIntensity(EffectType.INPUT_FAIL)),
            new HudSymptomIcon("camera_sway", "Camera sway", () -> effectIntensity(EffectType.CAMERA_SWAY)),
            new HudSymptomIcon("heartbeat", "Heartbeat",
                    () -> Math.max(effectIntensity(EffectType.HEARTBEAT), heartbeatIntensity())),
            new HudSymptomIcon("overdose", "Overdose danger", HudSymptomIcons::overdoseIntensity),
            new HudSymptomIcon("dose", "Dose load", HudSymptomIcons::doseIntensity)
    };

    private HudSymptomIcons() {
    }

    public static float flagIntensity(int flag) {
        if (AddictionClientState.badTripActive && (
                flag == SymptomFlags.HALLUCINATION
                        || flag == SymptomFlags.VISION
                        || flag == SymptomFlags.CONFUSION
                        || flag == SymptomFlags.INTRUSIVE_THOUGHTS
                        || flag == SymptomFlags.DISSOCIATION
                        || flag == SymptomFlags.STRESS)) {
            return Mth.clamp(Math.max(0.35F, AddictionClientState.badTripSeverity), 0.0F, 1.0F);
        }
        return AddictionClientState.has(flag) ? Mth.clamp(AddictionClientState.globalSeverity, 0.25F, 1.0F) : 0.0F;
    }

    public static float effectIntensity(EffectType type) {
        return Mth.clamp(AddictionClientState.getEffectIntensity(type), 0.0F, 1.0F);
    }

    public static float insomniaIntensity() {
        if (AddictionClientState.hasInsomnia()) {
            return Mth.clamp(AddictionClientState.globalSeverity, 0.25F, 1.0F);
        }
        return 0.0F;
    }

    public static float heartbeatIntensity() {
        if (AddictionClientState.badTripActive) {
            return Mth.clamp(Math.max(0.35F, AddictionClientState.badTripSeverity), 0.0F, 1.0F);
        }
        return AddictionClientState.has(SymptomFlags.STRESS) ? Mth.clamp(AddictionClientState.stressLevel, 0.3F, 1.0F) : 0.0F;
    }

    public static float overdoseIntensity() {
        return AddictionClientState.hasOverdoseTimer() ? 1.0F : 0.0F;
    }

    public static float doseIntensity() {
        DoseState state = AddictionClientState.getDominantDoseState();
        return switch (state) {
            case NORMAL -> 0.0F;
            case HIGH, DRUNK -> 0.45F;
            case VERY_HIGH, VERY_DRUNK -> 0.75F;
            case OVERDOSE, ETHYLIC_COMA -> 1.0F;
        };
    }

    public record HudSymptomIcon(String iconName, String label, IntensityProvider provider) {
        public ResourceLocation texture() {
            return ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "textures/gui/symptoms/" + iconName + ".png");
        }

        public float intensity() {
            return provider.get();
        }
    }

    public interface IntensityProvider {
        float get();
    }
}
