package org.mydrugs.mydrugs.core.drug.integration;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

import java.util.Locale;
import java.util.Optional;

/**
 * A permanent, stable trait earned by surviving, recovering from and integrating a curated drug.
 *
 * Integrated traits are NOT mutation stats: they are immune to fragility decay and grant a clean,
 * addiction-free echo (~35-50%) of the drug's production gift. One trait per curated drug.
 */
public enum IntegratedTrait {
    CLEAR_FOCUS("clear_focus", DrugId.COFFEE, EffectType.MANUAL_WORK_SPEED, 0.10F),
    STEADY_HANDS("steady_hands", DrugId.TOBACCO, EffectType.MINING_SPEED, 0.10F),
    EVEN_KEEL("even_keel", DrugId.WEED, EffectType.STRESS_RESISTANCE, 0.15F),
    FINE_MOTOR("fine_motor", DrugId.HASH, EffectType.PRECISION, 0.12F),
    HARDENED("hardened", DrugId.ALCOHOL, EffectType.DAMAGE_RESISTANCE, 0.08F),
    QUICKSTEP("quickstep", DrugId.COCAINE, EffectType.MOVEMENT_SPEED, 0.06F),
    RICHER_SEAMS("richer_seams", DrugId.LSD, EffectType.ORE_FORTUNE, 0.10F),
    OVERDRIVE_MEMORY("overdrive_memory", DrugId.METH, EffectType.MINING_SPEED, 0.12F),
    STRUCTURAL_SENSE("structural_sense", DrugId.MUSHROOMS, EffectType.MULTIBLOCK_VISION, 1.0F);

    private final String id;
    private final DrugId source;
    private final EffectType effect;
    private final float magnitude;

    IntegratedTrait(String id, DrugId source, EffectType effect, float magnitude) {
        this.id = id;
        this.source = source;
        this.effect = effect;
        this.magnitude = magnitude;
    }

    public String serializedName() {
        return id;
    }

    public DrugId source() {
        return source;
    }

    public EffectType effect() {
        return effect;
    }

    public float magnitude() {
        return magnitude;
    }

    public String translationKey() {
        return "integration.mydrugs.trait." + id;
    }

    public static Optional<IntegratedTrait> bySerializedName(String name) {
        return Optional.ofNullable(bySerializedNameOrNull(name));
    }

    public static @Nullable IntegratedTrait bySerializedNameOrNull(String name) {
        if (name == null) {
            return null;
        }

        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (IntegratedTrait trait : values()) {
            if (trait.id.equals(normalized)) {
                return trait;
            }
        }
        return null;
    }

    public static @Nullable IntegratedTrait bySource(DrugId drugId) {
        if (drugId == null) {
            return null;
        }
        for (IntegratedTrait trait : values()) {
            if (trait.source == drugId) {
                return trait;
            }
        }
        return null;
    }
}
