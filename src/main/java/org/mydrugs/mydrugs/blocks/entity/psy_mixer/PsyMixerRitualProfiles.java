package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import org.mydrugs.mydrugs.core.drug.DrugId;

import java.util.List;

public final class PsyMixerRitualProfiles {
    public enum Motif {
        KINETIC("kinetic"),
        BREATH("breath"),
        CIRCULAR("circular"),
        DENSE_CIRCULAR("dense_circular"),
        UNSTABLE("unstable"),
        VOLATILE("volatile"),
        PERCEPTUAL("perceptual"),
        OVERCLOCKED("overclocked"),
        MYCELIAL("mycelial");

        private final String id;

        Motif(String id) {
            this.id = id;
        }

        public String translationKey() {
            return "ritual.mydrugs.profile." + id;
        }
    }

    public record Profile(Motif motif, List<PsyMixerRitualAction> weightedActions) {
        public Profile {
            weightedActions = List.copyOf(weightedActions);
        }
    }

    private PsyMixerRitualProfiles() {
    }

    public static Profile forDrug(DrugId drug) {
        return switch (drug) {
            case COFFEE, COCAINE -> profile(Motif.KINETIC,
                    PsyMixerRitualAction.TIMING_RING, PsyMixerRitualAction.TIMING_RING,
                    PsyMixerRitualAction.JUMP, PsyMixerRitualAction.RIGHT_CLICK_AIR, PsyMixerRitualAction.SNEAK);
            case TOBACCO -> profile(Motif.BREATH,
                    PsyMixerRitualAction.STAND_STILL, PsyMixerRitualAction.SNEAK,
                    PsyMixerRitualAction.LOOK_AT_CORE, PsyMixerRitualAction.TIMING_RING, PsyMixerRitualAction.HOLD_ITEM);
            case WEED -> profile(Motif.CIRCULAR,
                    PsyMixerRitualAction.WALK_RING, PsyMixerRitualAction.WALK_RING,
                    PsyMixerRitualAction.STAND_STILL, PsyMixerRitualAction.LOOK_AT_CORE, PsyMixerRitualAction.RIGHT_CLICK_AIR);
            case HASH -> profile(Motif.DENSE_CIRCULAR,
                    PsyMixerRitualAction.WALK_RING, PsyMixerRitualAction.LOOK_AT_CORE,
                    PsyMixerRitualAction.TIMING_RING, PsyMixerRitualAction.STAND_STILL);
            case ALCOHOL -> profile(Motif.UNSTABLE,
                    PsyMixerRitualAction.WALK_RING, PsyMixerRitualAction.JUMP,
                    PsyMixerRitualAction.SNEAK, PsyMixerRitualAction.TIMING_RING);
            case CRACK -> profile(Motif.VOLATILE,
                    PsyMixerRitualAction.TIMING_RING, PsyMixerRitualAction.TIMING_RING,
                    PsyMixerRitualAction.RIGHT_CLICK_AIR, PsyMixerRitualAction.JUMP, PsyMixerRitualAction.LOOK_AT_CORE);
            case LSD -> profile(Motif.PERCEPTUAL,
                    PsyMixerRitualAction.LOOK_AT_CORE, PsyMixerRitualAction.TIMING_RING,
                    PsyMixerRitualAction.REOPEN_GUI, PsyMixerRitualAction.RIGHT_CLICK_AIR);
            case METH -> profile(Motif.OVERCLOCKED,
                    PsyMixerRitualAction.TIMING_RING, PsyMixerRitualAction.TIMING_RING,
                    PsyMixerRitualAction.JUMP, PsyMixerRitualAction.RIGHT_CLICK_AIR, PsyMixerRitualAction.LOOK_AT_CORE);
            case MUSHROOMS -> profile(Motif.MYCELIAL,
                    PsyMixerRitualAction.WALK_RING, PsyMixerRitualAction.WALK_RING,
                    PsyMixerRitualAction.STAND_STILL, PsyMixerRitualAction.LOOK_AT_CORE, PsyMixerRitualAction.SNEAK);
            default -> profile(Motif.KINETIC, PsyMixerRitualAction.defaultRandomPool().toArray(PsyMixerRitualAction[]::new));
        };
    }

    public static boolean isLegacyGenericPool(List<PsyMixerRitualAction> actions) {
        return actions.size() == PsyMixerRitualAction.defaultRandomPool().size()
                && actions.containsAll(PsyMixerRitualAction.defaultRandomPool());
    }

    private static Profile profile(Motif motif, PsyMixerRitualAction... actions) {
        return new Profile(motif, List.of(actions));
    }
}
