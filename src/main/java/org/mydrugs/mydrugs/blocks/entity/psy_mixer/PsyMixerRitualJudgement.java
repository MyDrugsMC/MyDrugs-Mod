package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

public enum PsyMixerRitualJudgement {
    NONE(0, "screen.mydrugs.psy_mixer.judgement.none", "message.mydrugs.psy_mixer.timing.none", 0, 0),
    MISS(1, "screen.mydrugs.psy_mixer.judgement.miss", "message.mydrugs.psy_mixer.timing.miss", 0, 0),
    NEAR(2, "screen.mydrugs.psy_mixer.judgement.near", "message.mydrugs.psy_mixer.timing.near", 1, 0),
    GOOD(3, "screen.mydrugs.psy_mixer.judgement.good", "message.mydrugs.psy_mixer.timing.good", 5, 1),
    GREAT(4, "screen.mydrugs.psy_mixer.judgement.great", "message.mydrugs.psy_mixer.timing.great", 8, 2),
    PERFECT(5, "screen.mydrugs.psy_mixer.judgement.perfect", "message.mydrugs.psy_mixer.timing.perfect", 12, 3);

    private static final PsyMixerRitualJudgement[] BY_ID = values();

    private final int id;
    private final String screenKey;
    private final String messageKey;
    private final int progressBonus;
    private final int qualityPoints;

    PsyMixerRitualJudgement(
            int id,
            String screenKey,
            String messageKey,
            int progressBonus,
            int qualityPoints
    ) {
        this.id = id;
        this.screenKey = screenKey;
        this.messageKey = messageKey;
        this.progressBonus = progressBonus;
        this.qualityPoints = qualityPoints;
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

    public int qualityPoints() {
        return qualityPoints;
    }

    public boolean isHit() {
        return this == GOOD || this == GREAT || this == PERFECT;
    }

    public boolean isGoodEnough() {
        return this == GOOD || this == GREAT || this == PERFECT;
    }

    public boolean isMistake() {
        return this == MISS;
    }

    public static PsyMixerRitualJudgement byId(int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : NONE;
    }
}
