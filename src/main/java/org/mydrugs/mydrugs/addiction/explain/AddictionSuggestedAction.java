package org.mydrugs.mydrugs.addiction.explain;

public enum AddictionSuggestedAction {
    NONE(0),
    USE_ANTIDOTE(1),
    STOP_DOSING(2),
    EAT(3),
    HEAL(4),
    GET_SAFE(5),
    STAY_SAFE(6),
    WRITE_DIARY(7),
    USE_HEADPHONES(8),
    DRINK_TEA(9),
    SLEEP_LATER(10),
    WAIT(11),
    GROUND(12);

    private final int networkId;

    AddictionSuggestedAction(int networkId) {
        this.networkId = networkId;
    }

    public int networkId() {
        return networkId;
    }

    public static AddictionSuggestedAction byNetworkId(int networkId) {
        for (AddictionSuggestedAction action : values()) {
            if (action.networkId == networkId) {
                return action;
            }
        }
        return NONE;
    }
}
