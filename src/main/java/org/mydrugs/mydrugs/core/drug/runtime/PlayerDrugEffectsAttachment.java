package org.mydrugs.mydrugs.core.drug.runtime;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistent per-player snapshot of acute drug effects.
 *
 * <p>This attachment is the source of truth across logout and server restart. The live
 * {@link DrugEffectRuntimeManager} static maps are only an online-player cache; they are written
 * into this attachment on logout / server stop and read back on login. Effect identity is stored
 * by stable {@code EffectType} serialized name, never by enum ordinal, so reordering the enum
 * cannot corrupt save data.
 *
 * <p>Not {@code copyOnDeath}: death intentionally drops acute effects (see lifecycle policy).
 */
public final class PlayerDrugEffectsAttachment implements ValueIOSerializable {

    /** A single persisted effect. Mirrors the serializable state of {@link ActiveDrugEffect}. */
    public record StoredEffect(String serializedName, float baseIntensity, int activeTicks, int ageTicks,
                               int baselineDurationTicks, int fadeTicksRemaining, int fadeDurationTicks,
                               int onsetTicks, int peakTicks, int comedownTicks, float routeIntensityMultiplier,
                               float routeDurationMultiplier, float routeDoseMultiplier, float routeRiskMultiplier,
                               float riskPressure, int overlappingDoses) {
    }

    private final List<StoredEffect> effects = new ArrayList<>();

    public List<StoredEffect> effects() {
        return List.copyOf(effects);
    }

    public boolean isEmpty() {
        return effects.isEmpty();
    }

    public void clear() {
        effects.clear();
    }

    public void add(StoredEffect effect) {
        effects.add(effect);
    }

    public void replaceAll(List<StoredEffect> replacement) {
        effects.clear();
        effects.addAll(replacement);
    }

    @Override
    public void serialize(ValueOutput output) {
        ValueOutput.ValueOutputList list = output.childrenList("effects");
        for (StoredEffect effect : effects) {
            ValueOutput child = list.addChild();
            child.putString("id", effect.serializedName());
            child.putFloat("intensity", effect.baseIntensity());
            child.putInt("active_ticks", effect.activeTicks());
            child.putInt("age_ticks", effect.ageTicks());
            child.putInt("baseline_duration", effect.baselineDurationTicks());
            child.putInt("fade_ticks", effect.fadeTicksRemaining());
            child.putInt("fade_duration", effect.fadeDurationTicks());
            child.putInt("onset_ticks", effect.onsetTicks());
            child.putInt("peak_ticks", effect.peakTicks());
            child.putInt("comedown_ticks", effect.comedownTicks());
            child.putFloat("route_intensity_multiplier", effect.routeIntensityMultiplier());
            child.putFloat("route_duration_multiplier", effect.routeDurationMultiplier());
            child.putFloat("route_dose_multiplier", effect.routeDoseMultiplier());
            child.putFloat("route_risk_multiplier", effect.routeRiskMultiplier());
            child.putFloat("risk_pressure", effect.riskPressure());
            child.putInt("overlapping_doses", effect.overlappingDoses());
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        effects.clear();
        for (ValueInput child : input.childrenListOrEmpty("effects")) {
            String id = child.getStringOr("id", "");
            if (id.isBlank()) {
                continue;
            }
            effects.add(new StoredEffect(
                    id,
                    child.getFloatOr("intensity", 0.0F),
                    child.getIntOr("active_ticks", 0),
                    child.getIntOr("age_ticks", 0),
                    child.getIntOr("baseline_duration", child.getIntOr("active_ticks", 0)),
                    child.getIntOr("fade_ticks", 0),
                    child.getIntOr("fade_duration", 0),
                    child.getIntOr("onset_ticks", 0),
                    child.getIntOr("peak_ticks", 0),
                    child.getIntOr("comedown_ticks", 0),
                    child.getFloatOr("route_intensity_multiplier", 1.0F),
                    child.getFloatOr("route_duration_multiplier", 1.0F),
                    child.getFloatOr("route_dose_multiplier", 1.0F),
                    child.getFloatOr("route_risk_multiplier", 1.0F),
                    child.getFloatOr("risk_pressure", child.getFloatOr("intensity", 0.0F)),
                    child.getIntOr("overlapping_doses", 1)
            ));
        }
    }
}
