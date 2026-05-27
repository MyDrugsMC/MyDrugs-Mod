package org.mydrugs.mydrugs.core.drug.integration;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

import java.util.Locale;
import java.util.List;
import java.util.Optional;

/**
 * A permanent, stable trait earned by surviving, recovering from and integrating a curated drug.
 *
 * Integrated traits are NOT mutation stats: they are immune to fragility decay and grant a clean,
 * addiction-free echo (~35-50%) of the drug's production gift. One trait per curated drug.
 */
public enum IntegratedTrait {
    CLEAR_FOCUS("clear_focus", DrugId.COFFEE,
            new EffectEcho(EffectType.MANUAL_WORK_SPEED, 0.20F),
            new EffectEcho(EffectType.FOCUS, 0.10F)),
    STEADY_HANDS("steady_hands", DrugId.TOBACCO,
            new EffectEcho(EffectType.PRECISION, 0.16F),
            new EffectEcho(EffectType.MINING_SPEED, 0.08F),
            new EffectEcho(EffectType.TREMOR_REDUCTION, 0.12F)),
    EVEN_KEEL("even_keel", DrugId.WEED,
            new EffectEcho(EffectType.STRESS_RESISTANCE, 0.22F),
            new EffectEcho(EffectType.BAD_TRIP_RESISTANCE, 0.08F)),
    FINE_MOTOR("fine_motor", DrugId.HASH,
            new EffectEcho(EffectType.PRECISION, 0.18F),
            new EffectEcho(EffectType.RITUAL_STABILITY, 0.12F)),
    HARDENED("hardened", DrugId.ALCOHOL,
            new EffectEcho(EffectType.DAMAGE_RESISTANCE, 0.14F),
            new EffectEcho(EffectType.STRESS_RESISTANCE, 0.08F)),
    QUICKSTEP("quickstep", DrugId.COCAINE,
            new EffectEcho(EffectType.MOVEMENT_SPEED, 0.12F),
            new EffectEcho(EffectType.MANUAL_WORK_SPEED, 0.12F)),
    RICHER_SEAMS("richer_seams", DrugId.LSD,
            new EffectEcho(EffectType.ORE_FORTUNE, 0.12F),
            new EffectEcho(EffectType.ORE_AURA, 0.15F),
            new EffectEcho(EffectType.RITUAL_FOCUS, 0.10F)),
    OVERDRIVE_MEMORY("overdrive_memory", DrugId.METH,
            new EffectEcho(EffectType.MINING_SPEED, 0.20F),
            new EffectEcho(EffectType.MANUAL_WORK_SPEED, 0.18F)),
    STRUCTURAL_SENSE("structural_sense", DrugId.MUSHROOMS,
            new EffectEcho(EffectType.MULTIBLOCK_VISION, 1.0F),
            new EffectEcho(EffectType.LOW_LIGHT_VISION, 0.35F));

    private final String id;
    private final DrugId source;
    private final List<EffectEcho> effects;

    IntegratedTrait(String id, DrugId source, EffectEcho... effects) {
        this.id = id;
        this.source = source;
        this.effects = List.of(effects);
    }

    public String serializedName() {
        return id;
    }

    public DrugId source() {
        return source;
    }

    public EffectType effect() {
        return effects.get(0).effect();
    }

    public float magnitude() {
        return effects.get(0).magnitude();
    }

    public List<EffectEcho> effects() {
        return effects;
    }

    public String rewardKey() {
        return "integration.mydrugs.trait." + id + ".reward";
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

    public record EffectEcho(EffectType effect, float magnitude) {
    }
}
