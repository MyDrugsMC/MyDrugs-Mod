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
    public record StoredEffect(String serializedName, float baseIntensity, int activeTicks,
                               int fadeTicksRemaining, int fadeDurationTicks) {
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
            child.putInt("fade_ticks", effect.fadeTicksRemaining());
            child.putInt("fade_duration", effect.fadeDurationTicks());
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
                    child.getIntOr("fade_ticks", 0),
                    child.getIntOr("fade_duration", 0)
            ));
        }
    }
}
