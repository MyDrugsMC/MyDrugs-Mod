package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import org.mydrugs.mydrugs.blocks.PsyMixerMultiblock;

public enum PsyMixerRitualFocus {
    CATALYST(0, PsyMixerMultiblock.SLOT_CATALYST, "screen.mydrugs.psy_mixer.catalyst", 0.0F),
    STABILIZER(1, PsyMixerMultiblock.SLOT_STABILIZER, "screen.mydrugs.psy_mixer.stabilizer", 0.25F),
    VESSEL(2, PsyMixerMultiblock.SLOT_VESSEL, "screen.mydrugs.psy_mixer.vessel", 0.5F),
    MATERIAL(3, PsyMixerMultiblock.SLOT_MATERIAL, "screen.mydrugs.psy_mixer.material", 0.75F);

    private static final PsyMixerRitualFocus[] BY_ID = values();

    private final int id;
    private final int slot;
    private final String labelKey;
    private final float targetPhase;

    PsyMixerRitualFocus(int id, int slot, String labelKey, float targetPhase) {
        this.id = id;
        this.slot = slot;
        this.labelKey = labelKey;
        this.targetPhase = targetPhase;
    }

    public int id() {
        return id;
    }

    public int slot() {
        return slot;
    }

    public String labelKey() {
        return labelKey;
    }

    public float targetPhase() {
        return targetPhase;
    }

    public static PsyMixerRitualFocus byId(int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : CATALYST;
    }

    public static int count() {
        return BY_ID.length;
    }
}
