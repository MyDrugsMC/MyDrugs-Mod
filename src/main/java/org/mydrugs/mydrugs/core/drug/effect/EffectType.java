package org.mydrugs.mydrugs.core.drug.effect;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public enum EffectType {
    CUSTOM_NAUSEA(EffectCategory.CAMERA, EffectPolarity.HARMFUL, "Nausea", "my stomach turns"),
    BLUR(EffectCategory.VISUAL, EffectPolarity.HARMFUL, "Blur", "my sight loses focus"),
    GAMMA_BOOST(EffectCategory.SHADER, EffectPolarity.BENEFICIAL, "Better vision", "dark places are easier to read"),
    LOW_LIGHT_VISION(EffectCategory.VISUAL, EffectPolarity.BENEFICIAL, "Low-light vision", "dark places are easier to read"),
    BRIGHTNESS_BOOST(EffectCategory.VISUAL, EffectPolarity.BENEFICIAL, "Brightness boost", "the world looks clearer"),
    MOVEMENT_SPEED(EffectCategory.MOVEMENT, EffectPolarity.BENEFICIAL, "Movement speed", "I move faster"),
    MOVEMENT_SLOWDOWN(EffectCategory.MOVEMENT, EffectPolarity.HARMFUL, "Movement slowdown", "my body feels heavy"),
    MINING_SPEED(EffectCategory.MINING, EffectPolarity.BENEFICIAL, "Mining speed", "blocks break faster"),
    DAMAGE_RESISTANCE(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Damage resistance", "incoming damage hurts less"),
    HP_DECREASE(EffectCategory.STAT_MODIFIER, EffectPolarity.HARMFUL, "Body fragility", "max health is lower"),
    ATTACK_DAMAGE(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Attack damage", "hits land harder"),
    ATTACK_SPEED(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Attack speed", "I swing faster"),
    MANUAL_WORK_SPEED(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Manual work speed", "tasks feel quicker"),
    PRECISION(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Precision", "my hands feel steadier and mining improves"),
    TREMOR_REDUCTION(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Tremor reduction", "my hands shake less"),
    RITUAL_FOCUS(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Ritual focus", "rituals are easier to follow"),
    RITUAL_STABILITY(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Ritual stability", "rituals feel steadier"),
    MOB_DETECTION_REDUCTION(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Mob detection reduction", "danger notices me less"),
    ADRENALINE_SURGE(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Adrenaline surge", "I get a burst of energy"),
    CAMERA_SWAY(EffectCategory.CAMERA, EffectPolarity.HARMFUL, "Camera sway", "my view will not settle"),
    TREMOR(EffectCategory.CAMERA, EffectPolarity.HARMFUL, "Tremor", "my hands shake"),
    STUMBLE(EffectCategory.INPUT, EffectPolarity.HARMFUL, "Stumble", "my footing feels unreliable"),
    INPUT_FAIL(EffectCategory.INPUT, EffectPolarity.HARMFUL, "Input failure", "my body does not obey cleanly"),
    VOMIT(EffectCategory.SERVER_ACTION, EffectPolarity.HARMFUL, "Nausea", "my stomach is unstable"),
    CONFUSION(EffectCategory.CAMERA, EffectPolarity.HARMFUL, "Confusion", "my thoughts feel crossed"),
    FOCUS(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Focus", "my mind locks onto the task"),

    // Deprecated compatibility names. They are routed through the custom runtime, never vanilla potion effects.
    @Deprecated NAUSEA(EffectCategory.CAMERA, EffectPolarity.HARMFUL),
    @Deprecated SLOWNESS(EffectCategory.MOVEMENT, EffectPolarity.HARMFUL),

    CHROMATIC_DREAM(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Chromatic dream", "colors refuse to stay normal"),
    ACID_WARP(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Acid warp", "space bends around me"),
    VOID_PULSE(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Void pulse", "the world pulses at the edges"),
    FOG(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Fog", "the world feels covered and distant"),
    IRIDESCENT_HAZE(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Iridescent haze", "everything is too bright and strange"),
    LUCID_DREAM(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Lucid dream", "reality feels thin"),
    MELT_REALITY(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Melt reality", "the world feels like it is melting"),
    VELVET_ECHO(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Velvet echo", "sensations keep echoing"),
    EVENT_HORIZON(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Event horizon", "space feels like it is pulling me in"),
    NEON_CELLS(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Neon cells", "vision breaks into patterns"),
    OPAL_WAVE(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Opal wave", "waves move through my sight"),
    QUANTUM_FLOWER(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Quantum flower", "patterns bloom everywhere"),
    COSMIC_TUNNEL(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Cosmic tunnel", "the world tunnels inward"),
    FRACTAL_WARP(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Fractal warp", "reality repeats itself"),
    LIQUID_CHROMA(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Liquid chroma", "colors flow too much"),
    MELTING_REALITY(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Melting reality", "solid things stop feeling solid"),
    AURORA_RIBBONS(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Aurora ribbons", "light keeps dragging across my eyes"),
    SPECTRAL_POSTER(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Spectral poster", "the world looks unreal"),
    HEARTBEAT(EffectCategory.SOUND, EffectPolarity.HARMFUL, "Heartbeat", "my pulse is too loud"),
    DRUNK_VISION(EffectCategory.SHADER, EffectPolarity.HARMFUL, "Drunk vision", "my sight is unstable"),

    // Optional ritual bonus / drug mix effects (PR 2)
    STRESS_RELIEF(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Stress relief", "the pressure is draining away"),
    STRESS_RESISTANCE(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Stress resistance", "stress has less grip on me"),
    BAD_TRIP_RESISTANCE(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Bad trip resistance", "panic has less room to grow"),
    FALL_CONTROL(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Fall control", "falling feels less dangerous"),
    DASH_POWER(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Dash power", "I can push forward harder"),
    BURST_WINDOW(EffectCategory.STAT_MODIFIER, EffectPolarity.BENEFICIAL, "Burst window", "quick reactions are stronger"),
    ORE_AURA(EffectCategory.VISUAL, EffectPolarity.BENEFICIAL, "Ore aura", "valuable blocks stand out"),
    ORE_FORTUNE(EffectCategory.MINING, EffectPolarity.BENEFICIAL, "Ore fortune", "ores seem to fracture along richer seams"),
    MULTIBLOCK_VISION(EffectCategory.VISUAL, EffectPolarity.BENEFICIAL, "Multiblock vision", "structures are easier to understand");

    private final EffectCategory category;
    private final EffectPolarity polarity;
    private final String diaryLabel;
    private final String diaryExplanation;

    EffectType(EffectCategory category, EffectPolarity polarity) {
        this(category, polarity, "", "");
    }

    EffectType(EffectCategory category, EffectPolarity polarity, String diaryLabel, String diaryExplanation) {
        this.category = category;
        this.polarity = polarity;
        this.diaryLabel = diaryLabel;
        this.diaryExplanation = diaryExplanation;
    }

    public EffectCategory getCategory() {
        return category;
    }

    public EffectPolarity polarity() {
        return polarity;
    }

    public boolean isBeneficial() {
        return polarity == EffectPolarity.BENEFICIAL;
    }

    public boolean isHarmful() {
        return polarity == EffectPolarity.HARMFUL;
    }

    public boolean hasDiaryReason() {
        return !diaryLabel.isBlank() && !diaryExplanation.isBlank();
    }

    public String diaryLabel() {
        return diaryLabel;
    }

    public String diaryExplanation() {
        return diaryExplanation;
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<EffectType> bySerializedName(String name) {
        return Optional.ofNullable(bySerializedNameOrNull(name));
    }

    public static @Nullable EffectType bySerializedNameOrNull(String name) {
        if (name == null) {
            return null;
        }

        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (EffectType type : values()) {
            if (type.serializedName().equals(normalized)) {
                return type;
            }
        }
        return null;
    }
}
