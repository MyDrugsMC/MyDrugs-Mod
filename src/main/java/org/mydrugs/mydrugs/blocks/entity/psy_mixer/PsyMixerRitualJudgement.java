package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

public enum PsyMixerRitualJudgement {
    NONE(0, "screen.mydrugs.psy_mixer.judgement.none", "message.mydrugs.psy_mixer.timing.none", 0, 0.0F, 0.0F),
    MISS(1, "screen.mydrugs.psy_mixer.judgement.miss", "message.mydrugs.psy_mixer.timing.miss", 0, 0.050F, -0.180F),
    NEAR(2, "screen.mydrugs.psy_mixer.judgement.near", "message.mydrugs.psy_mixer.timing.near", 1, 0.012F, -0.050F),
    GOOD(3, "screen.mydrugs.psy_mixer.judgement.good", "message.mydrugs.psy_mixer.timing.good", 5, -0.014F, 0.100F),
    GREAT(4, "screen.mydrugs.psy_mixer.judgement.great", "message.mydrugs.psy_mixer.timing.great", 8, -0.024F, 0.160F),
    PERFECT(5, "screen.mydrugs.psy_mixer.judgement.perfect", "message.mydrugs.psy_mixer.timing.perfect", 12, -0.040F, 0.240F);

    private static final PsyMixerRitualJudgement[] BY_ID = values();

    private final int id;
    private final String screenKey;
    private final String messageKey;
    private final int progressBonus;
    private final float instabilityDelta;
    private final float resonanceDelta;

    PsyMixerRitualJudgement(
            int id,
            String screenKey,
            String messageKey,
            int progressBonus,
            float instabilityDelta,
            float resonanceDelta
    ) {
        this.id = id;
        this.screenKey = screenKey;
        this.messageKey = messageKey;
        this.progressBonus = progressBonus;
        this.instabilityDelta = instabilityDelta;
        this.resonanceDelta = resonanceDelta;
    }

    public int id() {
        return id;
    }

    public String screenKey() {
        return screenKey;
    }

    public String messageKey() {
        return messageKey;
    }

    public int progressBonus() {
        return progressBonus;
    }

    public float instabilityDelta() {
        return instabilityDelta;
    }

    public float resonanceDelta() {
        return resonanceDelta;
    }

    public boolean isHit() {
        return this == GOOD || this == GREAT || this == PERFECT;
    }

    public boolean isMistake() {
        return this == MISS;
    }

    public static PsyMixerRitualJudgement byId(int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : NONE;
    }
}
