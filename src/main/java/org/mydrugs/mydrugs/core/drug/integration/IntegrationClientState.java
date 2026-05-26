package org.mydrugs.mydrugs.core.drug.integration;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Client-side mirror of the player's unlocked integrated traits. */
public final class IntegrationClientState {
    private static final Set<IntegratedTrait> TRAITS = EnumSet.noneOf(IntegratedTrait.class);

    private IntegrationClientState() {
    }

    public static void clear() {
        TRAITS.clear();
    }

    public static void apply(List<String> traitIds) {
        TRAITS.clear();
        if (traitIds == null) {
            return;
        }
        for (String id : traitIds) {
            IntegratedTrait trait = IntegratedTrait.bySerializedNameOrNull(id);
            if (trait != null) {
                TRAITS.add(trait);
            }
        }
    }

    public static boolean has(IntegratedTrait trait) {
        return trait != null && TRAITS.contains(trait);
    }

    public static Set<IntegratedTrait> all() {
        return Collections.unmodifiableSet(TRAITS);
    }
}
