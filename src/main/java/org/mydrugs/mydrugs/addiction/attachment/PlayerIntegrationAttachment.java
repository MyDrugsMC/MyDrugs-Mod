package org.mydrugs.mydrugs.addiction.attachment;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.mydrugs.mydrugs.core.drug.integration.IntegratedTrait;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * The separate, stable store of a player's unlocked {@link IntegratedTrait}s.
 *
 * Distinct from {@code PlayerMutationsAttachment}: integrated traits are permanent and immune to
 * fragility decay. Persists across death (registered {@code copyOnDeath}).
 */
public final class PlayerIntegrationAttachment implements ValueIOSerializable {
    private final EnumSet<IntegratedTrait> unlocked = EnumSet.noneOf(IntegratedTrait.class);

    public boolean unlock(IntegratedTrait trait) {
        return trait != null && unlocked.add(trait);
    }

    public boolean has(IntegratedTrait trait) {
        return trait != null && unlocked.contains(trait);
    }

    public int unlockedCount() {
        return unlocked.size();
    }

    public boolean isEmpty() {
        return unlocked.isEmpty();
    }

    public Set<IntegratedTrait> all() {
        return Collections.unmodifiableSet(unlocked);
    }

    @Override
    public void serialize(ValueOutput output) {
        ValueOutput.ValueOutputList list = output.childrenList("traits");
        for (IntegratedTrait trait : unlocked) {
            list.addChild().putString("trait_id", trait.serializedName());
        }
        if (list.isEmpty()) {
            output.discard("traits");
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        unlocked.clear();
        for (ValueInput child : input.childrenListOrEmpty("traits")) {
            IntegratedTrait trait = IntegratedTrait.bySerializedNameOrNull(child.getStringOr("trait_id", ""));
            if (trait != null) {
                unlocked.add(trait);
            }
        }
    }
}
